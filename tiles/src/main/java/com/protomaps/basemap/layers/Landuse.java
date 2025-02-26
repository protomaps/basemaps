package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getString;
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


public class Landuse implements ForwardingProfile.LayerPostProcessor {

  private static final MultiExpression.Index<Map<String, Object>> index = MultiExpression.of(List.of(
    rule(
      with("area:aeroway", "taxiway", "runway"),
      use("kind", "other")
    ),
    rule(
      with("aeroway", "aerodrome", "runway"),
      use("kind", fromTag("aeroway"))
    ),
    rule(
      with("amenity", "hospital", "school", "kindergarten", "university", "college", "library", "post_office",
        "townhall", "cafe"),
      use("kind", fromTag("amenity"))
    ),
    rule(
      with("boundary", "national_park", "protected_area"),
      use("kind", fromTag("boundary"))
    ),
    rule(
      with("landuse", "recreation_ground", "railway", "commercial", "grass", "forest", "meadow", "grass"),
      use("kind", "other")
    ),
    rule(
      with("landuse", "cemetery", "residential", "village_green", "allotments", "military"),
      use("kind", fromTag("landuse"))
    ),
    rule(
      with("landuse", "orchard", "farmland", "farmyard"),
      use("kind", "farmland")
    ),
    rule(
      with("landuse", "industrial", "brownfield"),
      use("kind", "industrial")
    ),
    rule(
      with("landuse", "military"),
      with("military", "naval_base", "airfield"),
      use("kind", fromTag("military"))
    ),
    rule(
      with("leisure", "golf_course", "marina", "park", "stadium", "playground", "garden", "dog_park", "pitch", "nature_reserve"),
      use("kind", fromTag("leisure"))
    ),
    rule(
      with("man_made", "bridge"),
      use("kind", "pedestrian")
    ),
    rule(
      with("man_made", "pier"),
      use("kind", "pier")
    ),
    rule(
      with("natural", "beach", "wood", "glacier", "grass", "scrub", "sand", "wetland", "bare_rock"),
      use("kind", fromTag("natural"))
    ),
    rule(
      with("place", "neighbourhood"),
      use("kind", "other")
    ),
    rule(
      with("railway", "platform"),
      use("kind", "platform")
    ),
    rule(
      with("tourism", "zoo"),
      use("kind", "other")
    ),
    rule(
      with("tourism", "attraction", "camp_site", "hotel"),
      use("kind", fromTag("tourism"))
    ),
    rule(
      with("area", "yes"),
      with("highway", "pedestrian", "footway"),
      use("kind", "pedestrian")
    ),
    rule(
      with("shop", "grocery", "supermarket"),
      use("kind", fromTag("shop"))
    ),
    rule(
      with("boundary", "national_park"),
      with("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      use("kind", "forest")
    ),
    rule(
      with("boundary", "national_park"),
      with("protect_class", "6"),
      with("protection_title", "National Forest"),
      use("kind", "forest")
    ),
    rule(
      with("landuse", "forest"),
      with("protect_class", "6"),
      use("kind", "forest")
    ),
    rule(
      with("landuse", "forest"),
      with("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      use("kind", "forest")
    ),
    rule(
      with("landuse", "forest"),
      use("kind", "forest")
    ),
    rule(
      with("boundary", "national_park"),
      use("kind", "park")
    ),
    rule(
      with("boundary", "protected_area"),
      with("protect_class", "6"),
      with("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      use("kind", "forest")
    ),
    rule(
      without("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
        "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
        "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
        "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
        "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary"),
      with("protect_class", "2", "3"),
      use("kind", "national_park")
    ),
    rule(
      without("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
        "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
        "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
        "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
        "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary"),
      with("operator", "United States National Park Service", "National Park Service",
        "US National Park Service", "U.S. National Park Service", "US National Park service"),
      use("kind", "national_park")
    ),
    rule(
      without("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
        "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
        "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
        "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
        "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary"),
      with("operator:en", "Parks Canada"),
      use("kind", "national_park")
    ),
    rule(
      without("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
        "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
        "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
        "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
        "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary"),
      with("designation", "national_park"),
      use("kind", "national_park")
    ),
    rule(
      without("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
        "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
        "United State Forest Service", "U.S. National Forest Service"),
      without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
        "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
        "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
        "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
        "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary"),
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


      // NOTE: (nvkelso 20230622) landuse labels for polygons are found in the pois layer
      //OsmNames.setOsmNames(poly, sf, 0);
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
