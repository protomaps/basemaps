package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.without;

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
import java.util.Map;


@SuppressWarnings("java:S1192")
public class Landuse implements ForwardingProfile.LayerPostProcessor {

  private static final String US_FOREST_OPERATORS = """
      operator
      United States Forest Service
      US Forest Service
      U.S. Forest Service
      USDA Forest Service
      United States Department of Agriculture
      US National Forest Service
      United State Forest Service
      U.S. National Forest Service
    """;

  private static final String PROTECTION_TITLES = """
      protection_title
      Conservation Area
      Conservation Park
      Environmental use
      Forest Reserve
      National Forest
      National Wildlife Refuge
      Nature Refuge
      Nature Reserve
      Protected Site
      Provincial Park
      Public Access Land
      Regional Reserve
      Resources Reserve
      State Forest
      State Game Land
      State Park
      Watershed Recreation Unit
      Wild Forest
      Wilderness Area
      Wilderness Study Area
      Wildlife Management
      Wildlife Management Area
      Wildlife Sanctuary
    """;

  private static final MultiExpression.Index<Map<String, Object>> index = MultiExpression.ofOrdered(List.of(
    rule(
      with("""
          amenity
          hospital
          school
          kindergarten
          university
          college
        """),
      use("kind", fromTag("amenity"))
    ),
    rule(
      with("""
          landuse
          recreation_ground
          industrial
          railway
          cemetery
          commercial
          grass
          farmland
          residential
          military
          village_green
          allotments
          forest
          meadow
          grass
        """),
      use("kind", fromTag("landuse"))
    ),
    rule(
      with("""
          aeroway
          aerodrome
          runway
        """),
      use("kind", fromTag("aeroway"))
    ),
    rule(
      with("""
          leisure
          park
          garden
          golf_course
          dog_park
          playground
          pitch
          nature_reserve
        """),
      use("kind", fromTag("leisure"))
    ),
    rule(
      with("man_made", "pier"),
      use("kind", "pier")
    ),
    rule(
      with("""
          natural
          beach
          wood
          glacier
          grassland
          scrub
          sand
          wetland
          bare_rock
        """),
      use("kind", fromTag("natural"))
    ),
    rule(
      with("""
          highway
          pedestrian
          footway
        """),
      with("area", "yes"),
      use("kind", "pedestrian")
    ),
    rule(
      with("waterway", "dam"),
      use("kind", "dam")
    ),
    rule(
      with("railway", "platform"),
      use("kind", "platform")
    ),
    rule(
      with("tourism", "zoo"),
      use("kind", "zoo")
    ),
    rule(
      with("landuse", "military"),
      with("military", "airfield", "naval_base"),
      use("kind", fromTag("military"))
    ),
    rule(
      with("landuse", "brownfield"),
      use("kind", "industrial")
    ),
    rule(
      with("landuse", "farmyard", "orchard"),
      use("kind", "farmland")
    ),
    rule(
      with("man_made", "bridge"),
      use("kind", "pedestrian")
    ),
    rule(
      with("area:aeroway", "taxiway", "runway"),
      use("kind", fromTag("area:aeroway"))
    ),
    rule(
      with("place", "neighbourhood"),
      use("kind", "other")
    ),
    rule(
      with("boundary", "protected_area"),
      with("protect_class", "6"),
      with(US_FOREST_OPERATORS),
      use("kind", "forest")
    ),
    rule(
      with("boundary", "national_park"),
      use("kind", "park")
    ),
    rule(
      with("boundary", "national_park"),
      without(US_FOREST_OPERATORS),
      without(PROTECTION_TITLES),
      with("protect_class", "2", "3"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without(US_FOREST_OPERATORS),
      without(PROTECTION_TITLES),
      with("""
          operator
          United States National Park Service
          National Park Service
          US National Park Service
          U.S. National Park Service
          US National Park service
        """),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without(US_FOREST_OPERATORS),
      without(PROTECTION_TITLES),
      with("operator:en", "Parks Canada"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without(US_FOREST_OPERATORS),
      without(PROTECTION_TITLES),
      with("designation", "national_park"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without(US_FOREST_OPERATORS),
      without(PROTECTION_TITLES),
      with("protection_title", "National Park"),
      use("kind", "national_park")
    )
  )).index();

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon()) {

      var matches = index.getMatches(sf);
      if (matches.isEmpty()) {
        return;
      }

      String kind = getString(sf, matches, "kind", "other");

      features.polygon(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("kind", kind)
        .setAttr("sort_rank", 189)
        // NOTE: (nvkelso 20230622) Consider zoom 5 instead...
        //       But to match Protomaps v2 we do earlier
        .setZoomRange(2, 15)
        .setMinPixelSize(2.0);

    }
  }

  public static final String LAYER_NAME = "landuse";

  @Override
  public String name() {
    return LAYER_NAME;
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
