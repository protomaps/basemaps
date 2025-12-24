package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.*;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class PoisTest extends LayerTest {

  // Static polygon instances testing area thresholds in Pois.java
  private static final Geometry AREA_2K_SQ_KM = newPolygon(0, 0, 0, 0.4, 0.4, 0.4, 0.4, 0, 0, 0);
  private static final Geometry AREA_1K_SQ_KM = newPolygon(0, 0, 0, 0.3, 0.3, 0.3, 0.3, 0, 0, 0);
  private static final Geometry AREA_8_SQ_KM = newPolygon(0, 0, 0, 0.025, 0.025, 0.025, 0.025, 0, 0, 0);
  private static final Geometry AREA_5_SQ_KM = newPolygon(0, 0, 0, 0.020, 0.020, 0.020, 0.020, 0, 0, 0);
  private static final Geometry AREA_3_SQ_KM = newPolygon(0, 0, 0, 0.015, 0.015, 0.015, 0.015, 0, 0, 0);
  private static final Geometry AREA_12K_SQ_M = newPolygon(0, 0, 0, 0.001, 0.001, 0.001, 0.001, 0, 0, 0);
  private static final Geometry AREA_127_SQ_M = newPolygon(0, 0, 0, 0.0001, 0.0001, 0.0001, 0.0001, 0, 0, 0);

  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("kind", "school")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "school")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void busStop() {
    assertFeatures(15,
      List.of(Map.of("kind", "bus_stop", "min_zoom", 18)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("highway", "bus_stop")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void allotments() {
    // this test shows two list elements because we're running against the whole profile, which means we're getting
    // results form multiple layer classes. This may mean the test breaks when other layer classes are changed.
    assertFeatures(15,
      List.of(Map.of("kind", "allotments"),
        Map.of("kind", "allotments", "min_zoom", 16, "name", "Kleingartenverein Kartoffel")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new HashMap<>(Map.of("landuse", "allotments", "name", "Kleingartenverein Kartoffel")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void villageGreen() {
    assertFeatures(15,
      List.of(Map.of("kind", "village_green"),
        Map.of("kind", "village_green", "min_zoom", 8, "name", "Stadtpark Eiche")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new HashMap<>(Map.of("landuse", "village_green", "name", "Stadtpark Eiche")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void playground() {
    assertFeatures(15,
      List.of(Map.of("kind", "playground"),
        Map.of("kind", "playground", "min_zoom", 18, "name", "Spielwiese")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new HashMap<>(Map.of("leisure", "playground", "name", "Spielwiese")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void withQrank() {
    assertFeatures(11,
      List.of(
        Map.of("kind", "aerodrome", "name", "SFO", "min_zoom", 11)),
      process(SimpleFeature.create(
        newPoint(0, 0),
        new HashMap<>(Map.of("aeroway", "aerodrome", "name", "SFO", "wikidata", "Q8888")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void withQrankPoly() {
    assertFeatures(11,
      List.of(Map.of("kind", "aerodrome"),
        Map.of("kind", "aerodrome", "name", "SFO", "min_zoom", 11)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new HashMap<>(Map.of("aeroway", "aerodrome", "name", "SFO", "wikidata", "Q8888")),
        "osm",
        null,
        0
      )));
  }

  // ========== Representative MinZoom tests ==========

  @Test
  void zoom_14_aerodrome_default() {
    assertFeatures(14,
      List.of(Map.of("kind", "aerodrome", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("aeroway", "aerodrome")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_12_aerodrome_withIATA() {
    assertFeatures(12,
      List.of(Map.of("kind", "aerodrome", "min_zoom", 12)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("aeroway", "aerodrome", "iata", "LAX")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_13_hospital() {
    assertFeatures(13,
      List.of(Map.of("kind", "hospital", "min_zoom", 13)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "hospital")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_library() {
    assertFeatures(14,
      List.of(Map.of("kind", "library", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "library")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_university() {
    assertFeatures(15,
      List.of(Map.of("kind", "university", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "university")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_cafe() {
    assertFeatures(15,
      List.of(Map.of("kind", "cafe", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "cafe")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_peak() {
    assertFeatures(14,
      List.of(Map.of("kind", "peak", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("natural", "peak")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_golfCourse() {
    assertFeatures(14,
      List.of(Map.of("kind", "golf_course", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "golf_course")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_supermarket() {
    assertFeatures(15,
      List.of(Map.of("kind", "supermarket", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("shop", "supermarket")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_attraction() {
    assertFeatures(15,
      List.of(Map.of("kind", "attraction", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "attraction")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_parkPoint() {
    assertFeatures(15,
      List.of(Map.of("kind", "park", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "park")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_cemetery() {
    assertFeatures(15,
      List.of(Map.of("kind", "cemetery", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "cemetery")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_stadium() {
    assertFeatures(14,
      List.of(Map.of("kind", "stadium", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "stadium")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_marina() {
    assertFeatures(14,
      List.of(Map.of("kind", "marina", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "marina")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_campSite() {
    assertFeatures(15,
      List.of(Map.of("kind", "camp_site", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "camp_site")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_hotel() {
    assertFeatures(15,
      List.of(Map.of("kind", "hotel", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "hotel")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_postOffice() {
    assertFeatures(14,
      List.of(Map.of("kind", "post_office", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "post_office")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_townhall() {
    assertFeatures(14,
      List.of(Map.of("kind", "townhall", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "townhall")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_college() {
    assertFeatures(15,
      List.of(Map.of("kind", "college", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "college")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_grocery_unnamed() {
    assertFeatures(15,
      List.of(Map.of("kind", "grocery", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("shop", "grocery")),
        "osm", null, 0
      )));
  }

  // ========== Special cases and overrides ==========

  @Test
  void zoom_7_nationalPark_default() {
    assertFeatures(7,
      List.of(Map.of("kind", "national_park"),
        Map.of("kind", "national_park", "min_zoom", 7, "name", "Yosemite")),
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "national_park",
          "name", "Yosemite",
          "protect_class", "2"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_8_nationalForest_usfsOperator() {
    assertFeatures(8,
      List.of(Map.of("kind", "park"), // landuse layer
        Map.of("kind", "park", "min_zoom", 8, "name", "Angeles National Forest")), // pois layer shows as park with area-based zoom
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "national_park",
          "name", "Angeles National Forest",
          "operator", "United States Forest Service"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_navalBase() {
    assertFeatures(15,
      List.of(Map.of("kind", "naval_base", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "military", "military", "naval_base")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_airfield() {
    assertFeatures(15,
      List.of(Map.of("kind", "airfield", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "military", "military", "airfield")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_kindDetail_cuisine() {
    assertFeatures(15,
      List.of(Map.of("kind", "cafe", "kind_detail", "italian", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "cafe", "cuisine", "italian")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_16_kindDetail_religion() {
    assertFeatures(15,
      List.of(Map.of("kind", "place_of_worship", "kind_detail", "christian", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "place_of_worship", "religion", "christian")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_kindDetail_sport() {
    assertFeatures(15,
      List.of(Map.of("kind", "stadium", "kind_detail", "soccer", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "stadium", "sport", "soccer")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_aerodromeDetail() {
    assertFeatures(15,
      List.of(Map.of("kind", "aerodrome", "kind_detail", "international", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("aeroway", "aerodrome", "aerodrome", "international")),
        "osm", null, 0
      )));
  }

  // ========== Area-based zoom overrides ==========

  @Test
  void zoom_7_nationalPark_largeArea() {
    assertFeatures(7,
      List.of(Map.of("kind", "national_park"),
        Map.of("kind", "national_park", "min_zoom", 7, "name", "Huge Park")),
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "national_park",
          "name", "Huge Park",
          "protect_class", "2"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_13_aerodrome_largePolygon() {
    assertFeatures(13,
      List.of(Map.of("kind", "aerodrome"),
        Map.of("kind", "aerodrome", "min_zoom", 13, "name", "Big Airport")),
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "aeroway", "aerodrome",
          "name", "Big Airport"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_11_aerodrome_polygonWithIATA() {
    assertFeatures(11,
      List.of(Map.of("kind", "aerodrome"),
        Map.of("kind", "aerodrome", "min_zoom", 11, "name", "JFK", "iata", "JFK")),
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "aeroway", "aerodrome",
          "name", "JFK",
          "iata", "JFK"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_13_aerodrome_smallPolygonNoIATA() {
    assertFeatures(13,
      List.of(Map.of("kind", "aerodrome"),
        Map.of("kind", "aerodrome", "min_zoom", 13, "name", "Regional Airport")),
      process(SimpleFeature.create(
        AREA_5_SQ_KM,
        new HashMap<>(Map.of(
          "aeroway", "aerodrome",
          "name", "Regional Airport"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_8_university_largeArea() {
    assertFeatures(8,
      List.of(Map.of("kind", "university"),
        Map.of("kind", "university", "min_zoom", 8, "name", "Large State University")),
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "amenity", "university",
          "name", "Large State University"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_university_academyOfArt() {
    assertFeatures(15,
      List.of(Map.of("kind", "university"),
        Map.of("kind", "university", "min_zoom", 15, "name", "Academy of Art University")),
      process(SimpleFeature.create(
        AREA_2K_SQ_KM,
        new HashMap<>(Map.of(
          "amenity", "university",
          "name", "Academy of Art University"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_8_park_largeArea() {
    assertFeatures(8,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 8, "name", "Giant City Park")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "leisure", "park",
          "name", "Giant City Park"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_12_park_mediumArea() {
    assertFeatures(12,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 12, "name", "Medium Park")),
      process(SimpleFeature.create(
        AREA_3_SQ_KM,
        new HashMap<>(Map.of(
          "leisure", "park",
          "name", "Medium Park"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_13_cemetery_largeArea() {
    assertFeatures(13,
      List.of(Map.of("kind", "cemetery"),
        Map.of("kind", "cemetery", "min_zoom", 13, "name", "Large Cemetery")),
      process(SimpleFeature.create(
        AREA_8_SQ_KM,
        new HashMap<>(Map.of(
          "landuse", "cemetery",
          "name", "Large Cemetery"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_13_school_largeArea() {
    assertFeatures(13,
      List.of(Map.of("kind", "school"),
        Map.of("kind", "school", "min_zoom", 13, "name", "Large School")),
      process(SimpleFeature.create(
        AREA_8_SQ_KM,
        new HashMap<>(Map.of(
          "amenity", "school",
          "name", "Large School"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_13_natureReserve_wilderness() {
    assertFeatures(13,
      List.of(Map.of("kind", "nature_reserve"),
        Map.of("kind", "nature_reserve", "min_zoom", 13, "name", "Some Wilderness")),
      process(SimpleFeature.create(
        AREA_3_SQ_KM,
        new HashMap<>(Map.of(
          "leisure", "nature_reserve",
          "name", "Some Wilderness"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_8_forest_largeArea() {
    assertFeatures(8,
      List.of(Map.of("kind", "forest"),
        Map.of("kind", "forest", "min_zoom", 8, "name", "Big Forest")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "landuse", "forest",
          "name", "Big Forest"
        )),
        "osm", null, 0
      )));
  }

  // ========== Height-based zoom boost ==========

  @Test
  void zoom_12_tallBuilding_100mHeight() {
    assertFeatures(12,
      List.of(Map.of("kind", "office", "min_zoom", 12, "name", "Skyscraper", "elevation", "100")),
      process(SimpleFeature.create(
        AREA_12K_SQ_M,
        new HashMap<>(Map.of(
          "amenity", "office",
          "name", "Skyscraper",
          "height", "100",
          "ele", "100"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_14_tallHotel_clamped() {
    assertFeatures(14,
      List.of(Map.of("kind", "hotel", "min_zoom", 14, "name", "Tall Hotel")),
      process(SimpleFeature.create(
        AREA_12K_SQ_M,
        new HashMap<>(Map.of(
          "tourism", "hotel",
          "name", "Tall Hotel",
          "height", "50"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_15_tallStorageRental() {
    assertFeatures(15,
      List.of(Map.of("kind", "storage_rental", "min_zoom", 15, "name", "Self Storage")),
      process(SimpleFeature.create(
        AREA_12K_SQ_M,
        new HashMap<>(Map.of(
          "amenity", "storage_rental",
          "name", "Self Storage",
          "height", "100"
        )),
        "osm", null, 0
      )));
  }

  // ========== Long name penalty ==========

  @Test
  void zoom_10_longName_46chars() {
    assertFeatures(10,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 10, "name", "This Is A Very Long Name For A Park That Goes On Forever")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "leisure", "park",
          "name", "This Is A Very Long Name For A Park That Goes On Forever"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_8_mediumLongName_31chars() {
    assertFeatures(8,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 8, "name", "Medium Length Park Name Here")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "leisure", "park",
          "name", "Medium Length Park Name Here"
        )),
        "osm", null, 0
      )));
  }

  // ========== Unnamed amenities late zoom ==========

  @Test
  void zoom_17_unnamedBench() {
    assertFeatures(15,
      List.of(Map.of("kind", "bench", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "bench")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_unnamedATM() {
    assertFeatures(15,
      List.of(Map.of("kind", "atm", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "atm")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_unnamedPlayground() {
    assertFeatures(15,
      List.of(Map.of("kind", "playground", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "playground")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_unnamedViewpoint() {
    assertFeatures(15,
      List.of(Map.of("kind", "viewpoint", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "viewpoint")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_18_namedPlaygroundPolygon() {
    assertFeatures(15,
      List.of(Map.of("kind", "playground"),
        Map.of("kind", "playground", "min_zoom", 18, "name", "Fun Playground")),
      process(SimpleFeature.create(
        AREA_127_SQ_M,
        new HashMap<>(Map.of(
          "leisure", "playground",
          "name", "Fun Playground"
        )),
        "osm", null, 0
      )));
  }

  // ========== Other specific shop/tourism overrides ==========

  @Test
  void zoom_17_unnamedCarRepair() {
    assertFeatures(15,
      List.of(Map.of("kind", "car_repair", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("shop", "car_repair")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_memorial() {
    assertFeatures(15,
      List.of(Map.of("kind", "memorial", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("historic", "memorial")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_pitch() {
    assertFeatures(15,
      List.of(Map.of("kind", "pitch", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "pitch")),
        "osm", null, 0
      )));
  }

  @Test
  void zoom_17_bedAndBreakfast() {
    assertFeatures(15,
      List.of(Map.of("kind", "bed_and_breakfast", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "bed_and_breakfast")),
        "osm", null, 0
      )));
  }

  // ========== Tests for kind assignments ==========

  @Test
  void kind_military_noSpecificType() {
    assertFeatures(15,
      List.of(Map.of("kind", "military", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "military")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_forest_fromLanduseForest() {
    assertFeatures(15,
      List.of(Map.of("kind", "forest"),
        Map.of("kind", "forest", "min_zoom", 8, "name", "Test Forest")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "landuse", "forest",
          "name", "Test Forest"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_forest_fromBoundaryProtectedArea() {
    assertFeatures(15,
      List.of(Map.of("kind", "forest"),
        Map.of("kind", "forest", "min_zoom", 8, "name", "Protected Forest")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "protected_area",
          "protect_class", "6",
          "operator", "United States Forest Service",
          "name", "Protected Forest"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_park_fromNationalParkUSFS() {
    assertFeatures(15,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 8, "name", "National Forest Area")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "national_park",
          "operator", "United States Forest Service",
          "name", "National Forest Area"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_park_fromNationalParkProtectionTitle() {
    assertFeatures(15,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 8, "name", "Test National Forest")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "national_park",
          "protect_class", "6",
          "protection_title", "National Forest",
          "name", "Test National Forest"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_forest_fromLanduseForestProtectClass6() {
    assertFeatures(15,
      List.of(Map.of("kind", "forest"),
        Map.of("kind", "forest", "min_zoom", 8, "name", "Protected Forest Area")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "landuse", "forest",
          "protect_class", "6",
          "name", "Protected Forest Area"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_forest_fromLanduseForestUSFS() {
    assertFeatures(15,
      List.of(Map.of("kind", "forest"),
        Map.of("kind", "forest", "min_zoom", 8, "name", "USFS Forest")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "landuse", "forest",
          "operator", "US Forest Service",
          "name", "USFS Forest"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_park_fromNationalParkBoundaryConservation() {
    assertFeatures(15,
      List.of(Map.of("kind", "park"),
        Map.of("kind", "park", "min_zoom", 8, "name", "Conservation Area")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "national_park",
          "protection_title", "Conservation Area",
          "name", "Conservation Area"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_zoo_kindFromLeisure() {
    assertFeatures(15,
      List.of(Map.of("kind", "zoo", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "zoo")),
        "osm", null, 0
      )));
  }

  // ========== Generic kind assignments from "other" section ==========

  @Test
  void kind_pharmacy_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "pharmacy", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "pharmacy")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_waterSlide_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "water_slide", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("attraction", "water_slide")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_brewery_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "brewery", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("craft", "brewery")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_quarry_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "quarry", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "quarry")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_garden_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "garden", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "garden")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_beach_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "beach", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("natural", "beach")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_station_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "station", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("railway", "station")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_bakery_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "bakery", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("shop", "bakery")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_museum_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "museum", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "museum")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_castle_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "castle", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("historic", "castle")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_protectedArea_yesValue_skipToOther() {
    assertFeatures(15,
      List.of(Map.of("kind", "protected_area", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("historic", "yes", "boundary", "protected_area")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_protectedArea_generic() {
    assertFeatures(15,
      List.of(Map.of("kind", "protected_area", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("boundary", "protected_area")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_protectedArea_fromBoundary() {
    assertFeatures(15,
      List.of(Map.of("kind", "protected_area", "min_zoom", 8, "name", "Protected Area")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "boundary", "protected_area",
          "name", "Protected Area"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_natureReserve_fromLeisure() {
    assertFeatures(15,
      List.of(Map.of("kind", "nature_reserve"),
        Map.of("kind", "nature_reserve", "min_zoom", 8, "name", "Nature Reserve")),
      process(SimpleFeature.create(
        AREA_1K_SQ_KM,
        new HashMap<>(Map.of(
          "leisure", "nature_reserve",
          "name", "Nature Reserve"
        )),
        "osm", null, 0
      )));
  }

  @Test
  void kind_recreationGround_fromLanduse() {
    assertFeatures(15,
      List.of(Map.of("kind", "recreation_ground", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "recreation_ground")),
        "osm", null, 0
      )));
  }

  @Test
  void kind_winterSports_fromLanduse() {
    assertFeatures(15,
      List.of(Map.of("kind", "winter_sports", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "winter_sports")),
        "osm", null, 0
      )));
  }
}
