package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.*;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PoisTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("pmap:kind", "school")),
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
      List.of(Map.of("pmap:kind", "bus_stop", "pmap:min_zoom", 18)),
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
      List.of(Map.of("pmap:kind", "allotments"),
        Map.of("pmap:kind", "allotments", "pmap:min_zoom", 16, "name", "Kleingartenverein Kartoffel")),
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
      List.of(Map.of("pmap:kind", "village_green"),
        Map.of("pmap:kind", "village_green", "pmap:min_zoom", 8, "name", "Stadtpark Eiche")),
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
      List.of(Map.of("pmap:kind", "playground"),
        Map.of("pmap:kind", "playground", "pmap:min_zoom", 18, "name", "Spielwiese")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new HashMap<>(Map.of("leisure", "playground", "name", "Spielwiese")),
        "osm",
        null,
        0
      )));
  }
}
