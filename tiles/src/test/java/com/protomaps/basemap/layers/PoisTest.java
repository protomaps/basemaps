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
  private static final Geometry AREA_3_SQ_KM = newPolygon(0, 0, 0, 0.015, 0.015, 0.015, 0.015, 0, 0, 0);
  private static final Geometry AREA_5_SQ_KM = newPolygon(0, 0, 0, 0.020, 0.020, 0.020, 0.020, 0, 0, 0);
  private static final Geometry AREA_8_SQ_KM = newPolygon(0, 0, 0, 0.025, 0.025, 0.025, 0.025, 0, 0, 0);
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
  void aerodrome_defaultMinZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "aerodrome", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("aeroway", "aerodrome")),
        "osm", null, 0
      )));
  }

  @Test
  void aerodrome_withIATA_minZoom12() {
    assertFeatures(12,
      List.of(Map.of("kind", "aerodrome", "min_zoom", 12)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("aeroway", "aerodrome", "iata", "LAX")),
        "osm", null, 0
      )));
  }

  @Test
  void hospital_minZoom13() {
    assertFeatures(13,
      List.of(Map.of("kind", "hospital", "min_zoom", 13)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "hospital")),
        "osm", null, 0
      )));
  }

  @Test
  void library_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "library", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "library")),
        "osm", null, 0
      )));
  }

  @Test
  void university_minZoom15() {
    assertFeatures(15,
      List.of(Map.of("kind", "university", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "university")),
        "osm", null, 0
      )));
  }

  @Test
  void cafe_minZoom16() {
    assertFeatures(15,
      List.of(Map.of("kind", "cafe", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "cafe")),
        "osm", null, 0
      )));
  }

  @Test
  void peak_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "peak", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("natural", "peak")),
        "osm", null, 0
      )));
  }

  @Test
  void golfCourse_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "golf_course", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "golf_course")),
        "osm", null, 0
      )));
  }

  @Test
  void supermarket_minZoom15() {
    assertFeatures(15,
      List.of(Map.of("kind", "supermarket", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("shop", "supermarket")),
        "osm", null, 0
      )));
  }

  @Test
  void attraction_minZoom16() {
    assertFeatures(15,
      List.of(Map.of("kind", "attraction", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "attraction")),
        "osm", null, 0
      )));
  }

  @Test
  void parkPoint_minZoom15() {
    assertFeatures(15,
      List.of(Map.of("kind", "park", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "park")),
        "osm", null, 0
      )));
  }

  @Test
  void cemetery_minZoom15() {
    assertFeatures(15,
      List.of(Map.of("kind", "cemetery", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "cemetery")),
        "osm", null, 0
      )));
  }

  @Test
  void stadium_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "stadium", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "stadium")),
        "osm", null, 0
      )));
  }

  @Test
  void marina_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "marina", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "marina")),
        "osm", null, 0
      )));
  }

  @Test
  void campSite_minZoom16() {
    assertFeatures(15,
      List.of(Map.of("kind", "camp_site", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "camp_site")),
        "osm", null, 0
      )));
  }

  @Test
  void hotel_minZoom16() {
    assertFeatures(15,
      List.of(Map.of("kind", "hotel", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "hotel")),
        "osm", null, 0
      )));
  }

  @Test
  void postOffice_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "post_office", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "post_office")),
        "osm", null, 0
      )));
  }

  @Test
  void townhall_minZoom14() {
    assertFeatures(14,
      List.of(Map.of("kind", "townhall", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "townhall")),
        "osm", null, 0
      )));
  }

  @Test
  void college_minZoom15() {
    assertFeatures(15,
      List.of(Map.of("kind", "college", "min_zoom", 15)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "college")),
        "osm", null, 0
      )));
  }

  @Test
  void grocery_unnamedMinZoom17() {
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
  void nationalPark_defaultMinZoom7() {
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
  void nationalForest_usfsOperator_minZoom8() {
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
  void navalBase_minZoom16() {
    assertFeatures(15,
      List.of(Map.of("kind", "naval_base", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "military", "military", "naval_base")),
        "osm", null, 0
      )));
  }

  @Test
  void airfield_minZoom16() {
    assertFeatures(15,
      List.of(Map.of("kind", "airfield", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("landuse", "military", "military", "airfield")),
        "osm", null, 0
      )));
  }

  @Test
  void kindDetail_cuisine() {
    assertFeatures(15,
      List.of(Map.of("kind", "cafe", "kind_detail", "italian", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "cafe", "cuisine", "italian")),
        "osm", null, 0
      )));
  }

  @Test
  void kindDetail_religion() {
    assertFeatures(15,
      List.of(Map.of("kind", "place_of_worship", "kind_detail", "christian", "min_zoom", 16)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "place_of_worship", "religion", "christian")),
        "osm", null, 0
      )));
  }

  @Test
  void kindDetail_sport() {
    assertFeatures(15,
      List.of(Map.of("kind", "stadium", "kind_detail", "soccer", "min_zoom", 14)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "stadium", "sport", "soccer")),
        "osm", null, 0
      )));
  }

  @Test
  void aerodromeDetail() {
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
  void nationalPark_largeArea_minZoom7() {
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
  void aerodrome_largePolygon_minZoom13() {
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
  void aerodrome_polygonWithIATA_minZoom11() {
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
  void aerodrome_smallPolygonNoIATA_minZoom13() {
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
  void university_largeArea_minZoom8() {
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
  void university_academyOfArt_minZoom15() {
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
  void park_largeArea_minZoom8() {
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
  void park_mediumArea_minZoom12() {
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
  void cemetery_largeArea_minZoom13() {
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
  void school_largeArea_minZoom13() {
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
  void natureReserve_wilderness_minZoom13() {
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
  void forest_largeArea_minZoom8() {
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
  void tallBuilding_100mHeight_minZoom12() {
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
  void tallHotel_clampedMinZoom14() {
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
  void tallStorageRental_minZoom15() {
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
  void longName_46chars_minZoom10() {
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
  void mediumLongName_31chars_minZoom8() {
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
  void unnamedBench_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "bench", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "bench")),
        "osm", null, 0
      )));
  }

  @Test
  void unnamedATM_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "atm", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("amenity", "atm")),
        "osm", null, 0
      )));
  }

  @Test
  void unnamedPlayground_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "playground", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "playground")),
        "osm", null, 0
      )));
  }

  @Test
  void unnamedViewpoint_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "viewpoint", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "viewpoint")),
        "osm", null, 0
      )));
  }

  @Test
  void namedPlaygroundPolygon_minZoom18() {
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
  void unnamedCarRepair_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "car_repair", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("shop", "car_repair")),
        "osm", null, 0
      )));
  }

  @Test
  void memorial_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "memorial", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("historic", "memorial")),
        "osm", null, 0
      )));
  }

  @Test
  void pitch_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "pitch", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("leisure", "pitch")),
        "osm", null, 0
      )));
  }

  @Test
  void bedAndBreakfast_minZoom17() {
    assertFeatures(15,
      List.of(Map.of("kind", "bed_and_breakfast", "min_zoom", 17)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("tourism", "bed_and_breakfast")),
        "osm", null, 0
      )));
  }
}
