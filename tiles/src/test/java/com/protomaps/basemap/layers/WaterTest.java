package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.*;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WaterTest extends LayerTest {

  private FeatureCollector processWithPolygon(String source, String sourceLayer, String... arguments) {
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
      source,
      sourceLayer,
      1
    ));
  }

  @Test
  void preparedOsm() {
    assertFeatures(15,
      List.of(Map.of("_id", 0L, "kind", "ocean")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "osm_water",
        null,
        1
      )));
  }

  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("kind", "swimming_pool")),
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
      List.of(Map.of("kind", "fountain")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("natural", "water", "amenity", "fountain")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void kindRiver() {
    assertFeatures(9,
      List.of(Map.of("kind", "river")),
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

  @Test
  void kindDam() {
    assertFeatures(15,
      List.of(),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "waterway", "dam"
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void oceanLabel() {
    assertFeatures(12,
      List.of(Map.of("kind", "ocean")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "ocean")),
        "osm",
        null,
        0
      )));
  }

  @ParameterizedTest
  @CsvSource({
    "Lake, ne_50m_lakes, 0, 4, lake",
    "Alkaline Lake, ne_50m_lakes, 0, 4, lake",
    "Reservoir, ne_50m_lakes, 0, 4, lake",
    "Lake, ne_10m_lakes, 5, 5, lake",
    "Alkaline Lake, ne_10m_lakes, 5, 5, lake",
    "Reservoir, ne_10m_lakes, 5, 5, lake",
    "Playa, ne_50m_lakes, 0, 4, playa",
    "Playa, ne_10m_lakes, 5, 5, playa",
    
  })
  void testNeLake(String featurecla, String sourceLayer, int minZoom, int maxZoom, String kind) {
    assertFeatures(1,
      List.of(Map.of("kind", kind, 
        "_minzoom", minZoom,
        "_maxzoom", maxZoom
      )),
      processWithPolygon("ne", sourceLayer,
        "featurecla", featurecla,
        "min_zoom", "1"
      )
    );
  }
}
