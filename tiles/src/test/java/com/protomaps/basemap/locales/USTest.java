package com.protomaps.basemap.locales;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class USTest {

  CartographicLocale locale = new US();

  @Test
  void shieldUs() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "US 1")));
    var shield = locale.getShield(feature);
    assertEquals("1", shield.text());
    assertEquals("US:US", shield.network());
  }

  @Test
  void shieldMultiple() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "US 1;US 5")));
    var shield = locale.getShield(feature);
    assertEquals("1", shield.text());
    assertEquals("US:US", shield.network());
  }

  @Test
  void shieldInterstate() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "I 5")));
    var shield = locale.getShield(feature);
    assertEquals("5", shield.text());
    assertEquals("US:US_I", shield.network());
  }

  @Test
  void shieldOther() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "6")));
    var shield = locale.getShield(feature);
    assertEquals("6", shield.text());
    assertEquals("other", shield.network());
  }

  @Test
  void shieldWhitespace() {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "US 1 ")));
    var shield = locale.getShield(feature);
    assertEquals("1", shield.text());
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
