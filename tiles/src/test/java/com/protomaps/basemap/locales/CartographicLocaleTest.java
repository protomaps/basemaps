package com.protomaps.basemap.locales;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CartographicLocaleTest {

  CartographicLocale locale = new CartographicLocale();

  @ParameterizedTest
  @CsvSource({
    "A,A,other",
    "B;A,B,other",
    "A \t;,A,other"
  })
  void shieldUs(String refTag, String expectedRef, String expectedNetwork) {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", refTag)));
    var shield = locale.getShield(feature);
    assertEquals(expectedRef, shield.text());
    assertEquals(expectedNetwork, shield.network());
  }

  @Test
  void shieldNull() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("network", "irrelevant")));
    var shield = locale.getShield(feature);
    assertNull(shield.text());
    assertNull(shield.network());
  }
}
