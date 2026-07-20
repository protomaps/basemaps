package com.protomaps.basemap.locales;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class USTest {

  CartographicLocale locale = new US();

  @Test
  void networkPriority() {
    // Interstate outranks US highway outranks any state / county / local network.
    assertTrue(locale.networkRank("US:I") < locale.networkRank("US:US"));
    assertTrue(locale.networkRank("US:US") < locale.networkRank("US:CO"));
    // State, county and local networks all share the catch-all rank.
    assertEquals(locale.networkRank("US:CO"), locale.networkRank("US:CO:Denver"));
    // Unknown networks rank below every prioritized one.
    assertTrue(locale.networkRank("US:CO") < locale.networkRank("e-road"));
  }

  @Test
  void normalizeCarriagewayNetworks() {
    // Local / express carriageways collapse onto the base Interstate network; other networks
    // (including deeper hierarchies) are left untouched.
    assertEquals("US:I", locale.normalizeNetwork("US:I:Local"));
    assertEquals("US:I", locale.normalizeNetwork("US:I:Express"));
    assertEquals("US:US", locale.normalizeNetwork("US:US:Express"));
    assertEquals("US:I", locale.normalizeNetwork("US:I"));
    assertEquals("US:CO:Denver", locale.normalizeNetwork("US:CO:Denver"));
    assertNull(locale.normalizeNetwork(null));
  }

  @Test
  void orderShields() {
    // Concurrent I 70 / US 6 / CO 91 relations sort by network priority regardless of input order.
    var shields = locale.orderShields(List.of(
      new CartographicLocale.Shield("91", "US:CO"),
      new CartographicLocale.Shield("6", "US:US"),
      new CartographicLocale.Shield("70", "US:I")));
    assertEquals(List.of(
      new CartographicLocale.Shield("70", "US:I"),
      new CartographicLocale.Shield("6", "US:US"),
      new CartographicLocale.Shield("91", "US:CO")), shields);
  }

  @Test
  void fallbackShieldIsGeneric() {
    // Without route relations there is no network to guess, so the way ref falls back to "other".
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of("ref", "US 1")));
    var shield = locale.getShield(feature);
    assertEquals("US1", shield.text());
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
