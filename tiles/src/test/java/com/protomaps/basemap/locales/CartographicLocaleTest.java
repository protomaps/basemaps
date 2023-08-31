package com.protomaps.basemap.locales;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CartographicLocaleTest {

  CartographicLocale locale = new CartographicLocale();

  @Test
  void shield() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "A")));
    var shield = locale.getShield(feature);
    assertEquals("A", shield.text());
    assertEquals("other", shield.network());
  }

  @Test
  void shieldMultipleRefs() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "A;B")));
    var shield = locale.getShield(feature);
    assertEquals("A", shield.text());
    assertEquals("other", shield.network());
  }

  @Test
  void shieldWhitespace() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "A \t")));
    var shield = locale.getShield(feature);
    assertEquals("A", shield.text());
    assertEquals("other", shield.network());
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
