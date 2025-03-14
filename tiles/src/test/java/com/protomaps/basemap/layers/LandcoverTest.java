package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
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
}
