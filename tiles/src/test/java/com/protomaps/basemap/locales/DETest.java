package com.protomaps.basemap.locales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class DETest {

  CartographicLocale locale = new DE();

  @Test
  void unrelatedNetworksArePassedThrough() {
    assertEquals("BAB", locale.normalizeNetwork("BAB"));
    assertEquals("DE:national", locale.normalizeNetwork("DE:national"));
    assertEquals("Landesstraßen NRW", locale.normalizeNetwork("Landesstraßen NRW"));
    assertEquals("e-road", locale.normalizeNetwork("e-road"));
  }

  @Test
  void networkPriority() {
    // Autobahn outranks Bundesstraße, Landesstraßen share the default rank with other unknowns.
    assertTrue(locale.networkRank("BAB") < locale.networkRank("DE:national"));
    assertTrue(locale.networkRank("DE:national") < locale.networkRank("Landesstraßen NRW"));
    assertEquals(CartographicLocale.DEFAULT_RANK, locale.networkRank("Landesstraßen NRW"));
    assertEquals(CartographicLocale.DEFAULT_RANK, locale.networkRank("e-road"));
  }

  @Test
  void orderShields() {
    var shields = locale.orderShields(List.of(
      new CartographicLocale.Shield("L12", "Landesstraßen NRW"),
      new CartographicLocale.Shield("B1", "DE:national"),
      new CartographicLocale.Shield("A1", "BAB")));
    assertEquals(List.of(
      new CartographicLocale.Shield("A1", "BAB"),
      new CartographicLocale.Shield("B1", "DE:national"),
      new CartographicLocale.Shield("L12", "Landesstraßen NRW")), shields);
  }
}
