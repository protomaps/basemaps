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
      List.of(Map.of("_id", 1L, "kind", "earth")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "osm_land",
        null,
        1
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

  @Test
  void island() {
    assertFeatures(6,
      List.of(Map.of("kind", "island", "_minzoom", 6)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("place", "island")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testNe() {
    assertFeatures(15,
      List.of(Map.of("kind", "earth",
        "_id", 1L,
        "_minzoom", 0,
        "_maxzoom", 4)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "ne",
        "ne_50m_land",
        1
      ))
    );
    assertFeatures(15,
      List.of(Map.of("kind", "earth",
        "_minzoom", 5,
        "_maxzoom", 5)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "ne",
        "ne_10m_land",
        1
      ))
    );
  }
}
