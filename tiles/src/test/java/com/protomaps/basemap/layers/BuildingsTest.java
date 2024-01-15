package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BuildingsTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("height", 10.0)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of(
          "building", "yes",
          "height", 10
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void parseBuildingHeights() {
    var result = Buildings.parseHeight("10.5", "12", null);
    assertEquals(10.5, result.height());
    result = Buildings.parseHeight("2", null, "1");
    assertEquals(2, result.height());
    assertEquals(1, result.min_height());
  }

  @Test
  void parseBuildingHeightsFromLevels() {
    var result = Buildings.parseHeight(null, "3", null);
    assertEquals(11, result.height());
    result = Buildings.parseHeight(null, "0.5", null);
    assertEquals(5, result.height());
  }
}
