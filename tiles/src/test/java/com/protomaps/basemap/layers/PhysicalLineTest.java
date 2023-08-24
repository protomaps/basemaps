package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PhysicalLineTest extends LayerTest {

  @Test
  void simple() {
    assertFeatures(9,
      List.of(Map.of("pmap:kind", "river")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "waterway", "river"
        )),
        "osm",
        null,
        0
      )));
  }
}
