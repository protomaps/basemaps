package com.protomaps.basemap.locales;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  @Test
  void orderShieldsDedupesAndStripsAndTiebreaksOnText() {
    // Duplicate (from directional relations) is removed; whitespace stripped; with no locale
    // priority the base class tiebreaks deterministically on shield text.
    var shields = locale.orderShields(List.of(
      new CartographicLocale.Shield("B 5", "de:BAB"),
      new CartographicLocale.Shield("A 3", "de:BAB"),
      new CartographicLocale.Shield("A3", "de:BAB")));
    assertEquals(List.of(
      new CartographicLocale.Shield("A3", "de:BAB"),
      new CartographicLocale.Shield("B5", "de:BAB")), shields);
  }

  @Test
  void orderShieldsDropsNullText() {
    // A relation contributing a network but no ref yields no renderable shield.
    var shields = locale.orderShields(List.of(
      new CartographicLocale.Shield(null, "de:BAB"),
      new CartographicLocale.Shield("A3", "de:BAB")));
    assertEquals(List.of(new CartographicLocale.Shield("A3", "de:BAB")), shields);
  }

  @Test
  void orderShieldsCapsAtMaxShields() {
    var input = new ArrayList<CartographicLocale.Shield>();
    for (int i = 0; i < CartographicLocale.MAX_SHIELDS + 3; i++) {
      input.add(new CartographicLocale.Shield("A" + i, "network" + i));
    }
    assertEquals(CartographicLocale.MAX_SHIELDS, locale.orderShields(input).size());
  }
}
