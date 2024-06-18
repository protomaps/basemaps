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
      List.of(Map.of("pmap:kind", "neighbourhood")),
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
      List.of(Map.of("_minzoom", 2, "_maxzoom", 4, "pmap:kind", "country")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "country", "wikidata", "Q1", "name", "US")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelCountryNoMatch() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 5, "_maxzoom", 8, "pmap:kind", "country")),
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
      List.of(Map.of("_minzoom", 4, "_maxzoom", 7, "pmap:kind", "region")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "state", "wikidata", "Q2", "name", "CA")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelRegionNoMatch() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 8, "_maxzoom", 11, "pmap:kind", "region")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "state", "wikidata", "Q999", "name", "XX")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelPopulatedPlace() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 8, "pmap:kind", "locality", "pmap:population_rank", 2)),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "city", "wikidata", "Q3", "name", "SF")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelPopulatedPlaceNoMatch() {
    assertFeatures(12,
      List.of(Map.of("_minzoom", 8, "pmap:kind", "locality")),
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
      List.of(Map.of("pmap:kind", "locality",
        "pmap:kind_detail", "locality",
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
      List.of(Map.of("pmap:kind", "locality",
        "pmap:kind_detail", "locality",
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
      List.of(Map.of("pmap:kind", "locality",
        "pmap:kind_detail", "hamlet",
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
      List.of(Map.of("pmap:kind", "locality",
        "pmap:kind_detail", "isolated_dwelling",
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
      List.of(Map.of("pmap:kind", "locality",
        "pmap:kind_detail", "farm",
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
      List.of(Map.of("pmap:kind", "locality",
        "pmap:kind_detail", "allotments",
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
