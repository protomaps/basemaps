package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.expression.Expression.*;
import static com.onthegomap.planetiler.expression.MultiExpression.entry;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Landuse implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  public static Stream<MultiExpression.Entry<String>> valueEntries(String field, String... values) {
    return Stream.of(values).map(v -> entry(v, matchAny(field, v)));
  }

  public static MultiExpression.Index<String> compose(Stream<MultiExpression.Entry<String>>... values) {
    return MultiExpression
      .of(Stream.of(values).reduce(Stream::concat).orElseGet(Stream::empty).collect(Collectors.toList())).index();
  }

  // TODO craft and historic
  public static MultiExpression.Index<String> LANDUSE_KIND = compose(
    valueEntries("aeroway", "aerodrome", "runway"),
    valueEntries("area:aeroway", "taxiway", "runway"),
    valueEntries("amenity", "university", "college", "hospital", "library", "school"), // townhall? post_office?
    Stream.of(entry("pedestrian", and(
      matchAny("area", "yes"),
      matchAny("highway", "pedestrian", "footway")
    ))),
    valueEntries("landuse", "cemetery"),
    Stream.of(entry("farmland", matchAny("landuse", "orchard", "farmland", "farmyard"))),
    valueEntries("landuse", "residential"),
    Stream.of(entry("industrial", matchAny("landuse", "industrial", "brownfield"))),
    valueEntries("landuse", "military"),
    valueEntries("military", "naval_base", "airfield"),
    valueEntries("leisure", "golf_course", "park", "stadium", "garden", "dog_park", "playground", "pitch",
      "nature_reserve"),
    Stream.of(entry("pedestrian", matchAny("man_made", "bridge"))),
    valueEntries("man_made", "pier", "bridge"),
    valueEntries("natural", "beach"),
    valueEntries("shop", "grocery", "supermarket"), // not in tilezen?
    valueEntries("tourism", "attraction", "camp_site", "hotel", "zoo"),
    valueEntries("railway", "platform"),
    // Boundary is most generic, so place last else we loose out
    // on nature_reserve detail versus all the protected_area
    valueEntries("boundary", "national_park", "protected_area"),
    valueEntries("landuse", "recreation_ground", "railway", "commercial", "grass")
  );

  static MatchAny US_OPERATOR =
    matchAny("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
      "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
      "United State Forest Service", "U.S. National Forest Service");

  static MatchAny PROTECTION_TITLE =
    matchAny("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
      "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
      "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
      "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
      "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary");

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (!sf.canBePolygon())
      return;
    List<String> matches = LANDUSE_KIND.getMatches(sf);
    if (matches.size() == 0)
      return;
    String kind = matches.get(0);

    features.polygon(this.name())
      .setId(FeatureId.create(sf))
      .setAttr("pmap:kind", kind)
      // NOTE: (nvkelso 20230622) Consider zoom 5 instead...
      //       But to match Protomaps v2 we do earlier
      .setZoomRange(2, 15)
      .setMinPixelSize(2.0);
  }

  @Override
  public String name() {
    return "landuse";
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    if (zoom == 15)
      return items;
    int minArea = 400 / (4096 * 4096) * (256 * 256);
    if (zoom == 6)
      minArea = 600 / (4096 * 4096) * (256 * 256);
    else if (zoom <= 5)
      minArea = 800 / (4096 * 4096) * (256 * 256);
    items = Area.filterArea(items, minArea);

    // We only care about park boundaries inside groups of adjacent parks at higher zooms when they are labeled
    // so at lower zooms we merge them to reduce file size
    if (zoom <= 6) {
      return FeatureMerge.mergeNearbyPolygons(items, 3.125, 3.125, 0.5, 0.5);
    }
    return items;
  }
}
