package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WaterTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("pmap:kind", "swimming_pool")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("leisure", "swimming_pool")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void kindFountain() {
    assertFeatures(15,
      List.of(Map.of("pmap:kind", "fountain")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("natural", "water", "amenity", "fountain")),
        "osm",
        null,
        0
      )));
  }
}
