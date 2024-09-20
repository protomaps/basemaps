package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TransitTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("kind", "rail", "kind_detail", "service")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "railway", "service"
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void isBuilding() {
    assertFeatures(12,
      List.of(Map.of("kind", "building")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 1, 1, 0, 1, 0, 0),
        new HashMap<>(Map.of(
          "railway", "signal_box",
          "building", "yes"
        )),
        "osm",
        null,
        0
      )));
  }
}
