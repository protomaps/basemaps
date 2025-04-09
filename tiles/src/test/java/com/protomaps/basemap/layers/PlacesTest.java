package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPoint;

import com.onthegomap.planetiler.reader.SimpleFeature;
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
