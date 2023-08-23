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
}
