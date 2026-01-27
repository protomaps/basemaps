package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPoint;

import com.onthegomap.planetiler.reader.SimpleFeature;
import com.protomaps.basemap.Basemap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlacesTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("kind", "neighbourhood")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "suburb", "name", "Whoville")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelCountry() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 1, "_maxzoom", 5, "kind", "country")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "country", "wikidata", "Q30", "name", "US")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelCountryNotFound() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 5, "_maxzoom", 8, "kind", "country")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "country", "wikidata", "Q-some-wikidata", "name", "some-name")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelCountryNoMatch() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 5, "_maxzoom", 8, "kind", "country")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "country", "wikidata", "Q999", "name", "XX")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelRegion() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 3, "_maxzoom", 7, "kind", "region")),
      process(SimpleFeature.create(
        newPoint(-119.9583, 37.2221),
        new HashMap<>(Map.of("place", "state", "wikidata", "Q99", "name", "CA")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelRegionNoMatch() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 8, "_maxzoom", 11, "kind", "region")),
      process(SimpleFeature.create(
        newPoint(-119.9583, 37.2221),
        new HashMap<>(Map.of("place", "state", "wikidata", "Q999", "name", "XX")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelPopulatedPlace() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 4, "kind", "locality", "population_rank", 12)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "city", "wikidata", "Q72", "name", "Zürich")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelPopulatedPlaceNoMatch() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 8, "kind", "locality")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "city", "wikidata", "Q999", "name", "XX")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testLocalityPopulationOsm() {
    assertFeatures(13,
      List.of(Map.of("kind", "locality",
        "kind_detail", "locality",
        "population", 1111)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "locality", "name", "Localityville", "population", "1111")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testLocalityNoPopulationOsm() {
    assertFeatures(14,
      List.of(Map.of("kind", "locality",
        "kind_detail", "locality",
        "population", 1000)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "locality", "name", "Localityville")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testHamletOsm() {
    assertFeatures(14,
      List.of(Map.of("kind", "locality",
        "kind_detail", "hamlet",
        "population", 200)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "hamlet", "name", "Hamletville")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testIsolatedDwellingOsm() {
    assertFeatures(14,
      List.of(Map.of("kind", "locality",
        "kind_detail", "isolated_dwelling",
        "population", 100)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "isolated_dwelling", "name", "Isolatedville")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testFarmOsm() {
    assertFeatures(14,
      List.of(Map.of("kind", "locality",
        "kind_detail", "farm",
        "population", 50)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "farm", "name", "Farmville")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testAllotmentsOsm() {
    assertFeatures(14,
      List.of(Map.of("kind", "locality",
        "kind_detail", "allotments",
        "population", 1000)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "allotments", "name", "Allotmentville")),
        "osm",
        null,
        0
      )));
  }
}


class PlacesOvertureTest extends LayerTest {

  @Test
  void testOaklandCity() {
    assertFeatures(12,
      List.of(Map.of(
        "kind", "locality",
        "kind_detail", "city",
        "name", "Oakland",
        "min_zoom", 9,
        "population", 433031,
        "population_rank", 10
      )),
      process(SimpleFeature.create(
        newPoint(-122.2708, 37.8044),
        new HashMap<>(Map.of(
          "id", "9d45ba84-c664-42bd-81e4-3f75b1d179c9",
          "theme", "divisions",
          "type", "division",
          "subtype", "locality",
          "class", "city",
          "names.primary", "Oakland",
          "population", 433031
        )),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testPiedmontTown() {
    assertFeatures(12,
      List.of(Map.of(
        "kind", "locality",
        "kind_detail", "town",
        "name", "Piedmont",
        "min_zoom", 10,
        "population", 0,
        "population_rank", 1
      )),
      process(SimpleFeature.create(
        newPoint(-122.2312, 37.8244),
        new HashMap<>(Map.of(
          "id", "bf3e15f5-1287-48a2-b8c4-2b9061950f74",
          "theme", "divisions",
          "type", "division",
          "subtype", "locality",
          "class", "town",
          "names.primary", "Piedmont"
        )),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testDowntownOaklandMacrohood() {
    assertFeatures(12,
      List.of(Map.of(
        "kind", "macrohood",
        "name", "Downtown Oakland",
        "min_zoom", 11,
        "population", 0,
        "population_rank", 1
      )),
      process(SimpleFeature.create(
        newPoint(-122.2708, 37.8044),
        new HashMap<>(Map.of(
          "id", "81e4b45f-1210-4e79-9ea1-becc6e223778",
          "theme", "divisions",
          "type", "division",
          "subtype", "macrohood",
          "names.primary", "Downtown Oakland"
        )),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testLakesideNeighborhood() {
    assertFeatures(14,
      List.of(Map.of(
        "kind", "neighbourhood",
        "kind_detail", "neighbourhood",
        "name", "Lakeside",
        "min_zoom", 13,
        "population", 0,
        "population_rank", 1
      )),
      process(SimpleFeature.create(
        newPoint(-122.2476, 37.8074),
        new HashMap<>(Map.of(
          "id", "d95da2a7-5c9d-44ce-9d9b-8b1fa7aa93a1",
          "theme", "divisions",
          "type", "division",
          "subtype", "neighborhood",
          "names.primary", "Lakeside"
        )),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }
}
