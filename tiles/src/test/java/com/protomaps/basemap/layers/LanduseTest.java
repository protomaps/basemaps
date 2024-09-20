package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LanduseTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("kind", "hospital")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("amenity", "hospital")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void landuseVillageGreen() {
    assertFeatures(15,
      List.of(Map.of("kind", "village_green")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("landuse", "village_green")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void landuseAllotments() {
    assertFeatures(15,
      List.of(Map.of("kind", "allotments")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("landuse", "allotments")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void landuseGlacier() {
    assertFeatures(15,
      List.of(Map.of("kind", "glacier")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("natural", "glacier")),
        "osm",
        null,
        0
      )));
  }
}
