package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoadsTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("pmap:kind", "highway", "layer", 1, "pmap:kind_detail", "motorway")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "layer", "1",
          "highway", "motorway"
        )),
        "osm",
        null,
        0
      )));
  }
}
