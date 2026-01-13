package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
import com.protomaps.basemap.Basemap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LandcoverTest extends LayerTest {

  @ParameterizedTest
  @CsvSource(value = {
    "urban,urban_area,6",
    "crop,farmland,2",
    "grass,grassland,4",
    "trees,forest,5",
    "snow,glacier,1",
    "shrub,scrub,3",
    "barren,barren,0"
  })
  void simple(String daylightClassString, String expectedString, Integer expectedSortKey) {
    assertFeatures(7,
      List.of(Map.of("kind", expectedString, "_sortkey", expectedSortKey, "_id", 1L + expectedSortKey)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("class", daylightClassString)),
        "landcover",
        null,
        0
      )));
  }

  @Test
  void testNe() {
    assertFeatures(15,
      List.of(),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of()),
        "ne",
        "ne_10m_glaciated_areas",
        1
      ))
    );

    assertFeatures(15,
      List.of(Map.of("kind", "glacier",
        "_minzoom", 0,
        "_maxzoom", 7)),
      process(SimpleFeature.create(
        newPolygon(0, -75, 0, -76, 1, -76, 0, -75),
        new HashMap<>(Map.of()),
        "ne",
        "ne_10m_glaciated_areas",
        1
      ))
    );
  }

  @Test
  void testOvertureGrass() {
    assertFeatures(7,
      List.of(Map.of("kind", "grassland")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "grass")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testOvertureBarren() {
    assertFeatures(7,
      List.of(Map.of("kind", "barren")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "barren")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testOvertureUrban() {
    assertFeatures(7,
      List.of(Map.of("kind", "urban_area")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "urban")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testOvertureCrop() {
    assertFeatures(7,
      List.of(Map.of("kind", "farmland")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "crop")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testOvertureSnow() {
    assertFeatures(7,
      List.of(Map.of("kind", "glacier")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "snow")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testOvertureShrub() {
    assertFeatures(7,
      List.of(Map.of("kind", "scrub")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "shrub")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }

  @Test
  void testOvertureForest() {
    assertFeatures(7,
      List.of(Map.of("kind", "forest")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("type", "land_cover", "subtype", "forest")),
        Basemap.SRC_OVERTURE,
        null,
        0
      )));
  }
}
