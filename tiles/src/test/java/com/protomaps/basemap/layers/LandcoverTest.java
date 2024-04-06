package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LandcoverTest extends LayerTest {

  @ParameterizedTest
  @CsvSource(value = {
    "urban,urban_area",
    "crop,farmland",
    "grass,grassland",
    "trees,forest",
    "snow,glacier",
    "shrub,scrub",
    "barren,barren"
  })
  void simple(String daylightClassString, String expectedString) {
    assertFeatures(7,
      List.of(Map.of("pmap:kind", expectedString)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("class", daylightClassString)),
        "landcover",
        null,
        0
      )));
  }
}
