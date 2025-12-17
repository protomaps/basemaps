package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LanduseTest extends LayerTest {

  private FeatureCollector processWith(String... arguments) {
    Map<String, Object> tags = new HashMap<>();
    List<String> argumentList = List.of(arguments);
    if (argumentList.size() % 2 == 0) {
      for (int i = 0; i < argumentList.size(); i += 2) {
        tags.put(argumentList.get(i), argumentList.get(i + 1));
      }
    }
    return process(SimpleFeature.create(
      newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
      tags,
      "osm",
      null,
      0
    ));
  }

  @Test
  void testFromTagAeroway() {
    assertFeatures(15,
      List.of(Map.of("kind", "aerodrome")),
      processWith("aeroway", "aerodrome")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "runway")),
      processWith("aeroway", "runway")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "runway")),
      processWith("area:aeroway", "runway")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "taxiway")),
      processWith("area:aeroway", "taxiway")
    );
  }

  @Test
  void testAerodromeOverIndustrial() {
    assertFeatures(15,
      List.of(Map.of("kind", "aerodrome")),
      processWith("aeroway", "aerodrome", "landuse", "industrial")
    );
  }


  @Test
  void testFromTagAmenity() {
    assertFeatures(15,
      List.of(Map.of("kind", "hospital")),
      processWith("amenity", "hospital")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "school")),
      processWith("amenity", "school")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "kindergarten")),
      processWith("amenity", "kindergarten")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "university")),
      processWith("amenity", "university")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "college")),
      processWith("amenity", "college")
    );
  }

  @Test
  void testFromTagLanduse() {
    assertFeatures(15,
      List.of(Map.of("kind", "recreation_ground")),
      processWith("landuse", "recreation_ground")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "industrial")),
      processWith("landuse", "industrial")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "railway")),
      processWith("landuse", "railway")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "cemetery")),
      processWith("landuse", "cemetery")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "commercial")),
      processWith("landuse", "commercial")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "grass")),
      processWith("landuse", "grass")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "farmland")),
      processWith("landuse", "farmland")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "residential")),
      processWith("landuse", "residential")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "military")),
      processWith("landuse", "military")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "village_green")),
      processWith("landuse", "village_green")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "allotments")),
      processWith("landuse", "allotments")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("landuse", "forest")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "meadow")),
      processWith("landuse", "meadow")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "grass")),
      processWith("landuse", "grass")
    );
  }

  @Test
  void testFromTagLeisure() {
    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("leisure", "park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "garden")),
      processWith("leisure", "garden")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "golf_course")),
      processWith("leisure", "golf_course")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "dog_park")),
      processWith("leisure", "dog_park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "playground")),
      processWith("leisure", "playground")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "pitch")),
      processWith("leisure", "pitch")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "nature_reserve")),
      processWith("leisure", "nature_reserve")
    );
  }

  @Test
  void testFromTagManMade() {
    assertFeatures(15,
      List.of(Map.of("kind", "pier")),
      processWith("man_made", "pier")
    );
  }

  @Test
  void testFromTagNatural() {
    assertFeatures(15,
      List.of(Map.of("kind", "beach")),
      processWith("natural", "beach")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "wood")),
      processWith("natural", "wood")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "glacier")),
      processWith("natural", "glacier")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "grassland")),
      processWith("natural", "grassland")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "dam")),
      processWith("waterway", "dam")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "scrub")),
      processWith("natural", "scrub")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "sand")),
      processWith("natural", "sand")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "wetland")),
      processWith("natural", "wetland")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "bare_rock")),
      processWith("natural", "bare_rock")
    );
  }

  @Test
  void testFromTagHighway() {
    assertFeatures(15,
      List.of(Map.of("kind", "pedestrian")),
      processWith("area", "yes",
        "highway", "pedestrian")
    );
  }

  @Test
  void testFromTagRailway() {
    assertFeatures(15,
      List.of(Map.of("kind", "platform")),
      processWith("railway", "platform")
    );
  }

  @Test
  void testFromTagTourism() {
    assertFeatures(15,
      List.of(Map.of("kind", "zoo")),
      processWith("tourism", "zoo")
    );
  }

  @Test
  void testFromTagMilitary() {
    assertFeatures(15,
      List.of(Map.of("kind", "naval_base")),
      processWith("landuse", "military",
        "military", "naval_base")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "airfield")),
      processWith("landuse", "military",
        "military", "airfield")
    );
  }

  @Test
  void testRemappingToIndustrial() {
    assertFeatures(15,
      List.of(Map.of("kind", "industrial")),
      processWith("landuse", "brownfield")
    );
  }

  @Test
  void testRemappingToFarmland() {
    assertFeatures(15,
      List.of(Map.of("kind", "farmland")),
      processWith("landuse", "orchard")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "farmland")),
      processWith("landuse", "farmyard")
    );
  }

  @Test
  void testRemappingToPedestrian() {
    assertFeatures(15,
      List.of(Map.of("kind", "pedestrian")),
      processWith("man_made", "bridge")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "pedestrian")),
      processWith("area", "yes",
        "highway", "footway")
    );
  }

  @Test
  void testRemappingToOther() {
    assertFeatures(15,
      List.of(Map.of("kind", "other")),
      processWith("place", "neighbourhood")
    );
  }

  @Test
  void testRemappingToPark() {
    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "operator", "United States Forest Service",
        "protect_class", "2")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "operator", "United States Forest Service",
        "operator:en", "Parks Canada")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "operator", "United States Forest Service",
        "designation", "national_park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "operator", "United States Forest Service",
        "protection_title", "National Park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "protection_title", "Conservation Area",
        "protect_class", "2")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "protection_title", "Conservation Area",
        "operator:en", "Parks Canada")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "protection_title", "Conservation Area",
        "designation", "national_park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "park")),
      processWith("boundary", "national_park",
        "protection_title", "Conservation Area",
        "operator", "United States National Park Service")
    );
  }

  @Test
  void testRemappingToForest() {
    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "United States Forest Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "US Forest Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "U.S. Forest Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "USDA Forest Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "United States Department of Agriculture")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "US National Forest Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "United State Forest Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "forest")),
      processWith("boundary", "protected_area",
        "protect_class", "6",
        "operator", "U.S. National Forest Service")
    );
  }

  @Test
  void testNationalPark() {
    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "protect_class", "2")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "protect_class", "3")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "operator", "United States National Park Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "operator", "National Park Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "operator", "U.S. National Park Service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "operator", "US National Park service")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "operator:en", "Parks Canada")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "designation", "national_park")
    );

    assertFeatures(15,
      List.of(Map.of("kind", "national_park")),
      processWith("boundary", "national_park",
        "protection_title", "National Park")
    );
  }
}
