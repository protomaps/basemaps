package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EarthTest extends LayerTest {

  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("kind", "earth")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "osm_land",
        null,
        0
      )));
  }

  @Test
  void cliff() {
    assertFeatures(15,
      List.of(Map.of("kind", "cliff")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of("natural", "cliff")),
        "osm",
        null,
        0
      )));
  }
}
