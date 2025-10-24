package com.protomaps.basemap.locales;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NLTest {

  CartographicLocale locale = new NL();

  @ParameterizedTest
  @CsvSource({
    "S100,S100,NL:S-road",
  })
  void shieldNl(String refTag, String expectedText, String expectedNetwork) {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", refTag)));
    var shield = locale.getShield(feature);
    assertEquals(expectedText, shield.text());
    assertEquals(expectedNetwork, shield.network());
  }
}
