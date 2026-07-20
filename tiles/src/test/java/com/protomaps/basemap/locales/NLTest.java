package com.protomaps.basemap.locales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NLTest {

  CartographicLocale locale = new NL();

  @ParameterizedTest
  @ValueSource(strings = {
    "NL:S:Amsterdam",
    "NL:S:Rotterdam",
    "NL:S:Den Haag",
    "NL:S:Nijmegen",
    "NL:S:Zaanstad",
    "NL:S:Parkstad"
  })
  void municipalStadsrouteNetworksCollapse(String network) {
    // Every municipality's stadsroute network symbolizes with the same shield.
    assertEquals("NL:S-road", locale.normalizeNetwork(network));
  }

  @Test
  void unrelatedNetworksArePassedThrough() {
    assertEquals("NL:A", locale.normalizeNetwork("NL:A"));
    assertEquals("NL:N", locale.normalizeNetwork("NL:N"));
    // The A10 ring is not a stadsroute network and is left alone.
    assertEquals("NL:ring:Amsterdam", locale.normalizeNetwork("NL:ring:Amsterdam"));
  }

  @Test
  void networkPriority() {
    // A-roads outrank N-roads outrank S-road city rings.
    assertTrue(locale.networkRank("NL:A") < locale.networkRank("NL:N"));
    assertTrue(locale.networkRank("NL:N") < locale.networkRank("NL:S-road"));
    // Unknown networks rank below every prioritized one.
    assertTrue(locale.networkRank("NL:S-road") < locale.networkRank("e-road"));
  }

  @Test
  void orderShields() {
    var shields = locale.orderShields(List.of(
      new CartographicLocale.Shield("S100", "NL:S-road"),
      new CartographicLocale.Shield("2", "NL:A")));
    assertEquals(List.of(
      new CartographicLocale.Shield("2", "NL:A"),
      new CartographicLocale.Shield("S100", "NL:S-road")), shields);
  }
}
