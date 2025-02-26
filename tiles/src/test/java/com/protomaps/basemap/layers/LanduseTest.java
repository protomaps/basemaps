package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LanduseTest extends LayerTest {

  private FeatureCollector processWith(String... arguments) {
    Map<String, Object> tags = new HashMap<>();
    List<String> argumentList = List.of(arguments);
    if (argumentList.size() % 2 == 0) {
      for (int i = 0; i < argumentList.size(); i += 2) {
        tags.put(argumentList.get(i), argumentList.get(i + 1));
      }
    }
    return process(SimpleFeature.create(
      newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
      tags,
      "osm",
      null,
      0
    ));
  }

  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("kind", "hospital")),
      processWith("amenity", "hospital")
    );
  }

  @Test
  void landuseVillageGreen() {
    assertFeatures(15,
      List.of(Map.of("kind", "village_green")),
      processWith("landuse", "village_green")
    );
  }

  @Test
  void landuseAllotments() {
    assertFeatures(15,
      List.of(Map.of("kind", "allotments")),
      processWith("landuse", "allotments")
    );
  }

  @Test
  void landuseGlacier() {
    assertFeatures(15,
      List.of(Map.of("kind", "glacier")),
      processWith("natural", "glacier")
    );
  }
}
