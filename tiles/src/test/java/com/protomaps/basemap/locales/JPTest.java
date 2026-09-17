package com.protomaps.basemap.locales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JPTest {

  CartographicLocale locale = new JP();

  @ParameterizedTest
  @ValueSource(strings = {
    "JP:prefectural:tokyo",
    "JP:prefectural:nagano",
    "JP:prefectural:kagawa",
    "JP:prefectural:tottori",
    "JP:prefectural:saga",
    "JP:prefectural:gunma",
    "JP:prefectural:Fukui",
    "JP:prefectural:tokyo;JP:prefectural:kanagawa",
    "JP:perfectural"
  })
  void prefecturalNetworksCollapse(String network) {
    // Prefecture-scoped networks symbolize with the same prefectural shield.
    assertEquals("JP:prefectural", locale.normalizeNetwork(network));
  }

  @Test
  void nationalExpresswayCollapsesToNumberedExpressway() {
    assertEquals("JP:E", locale.normalizeNetwork("JP:national:expressway"));
  }

  @Test
  void unrelatedNetworksArePassedThrough() {
    assertEquals("JP:E", locale.normalizeNetwork("JP:E"));
    assertEquals("JP:national", locale.normalizeNetwork("JP:national"));
    assertEquals("JP:prefectural", locale.normalizeNetwork("JP:prefectural"));
  }

  @Test
  void networkPriority() {
    // Expressways outrank national highways outrank prefectural roads.
    assertTrue(locale.networkRank("JP:E") < locale.networkRank("JP:national"));
    assertTrue(locale.networkRank("JP:national") < locale.networkRank("JP:prefectural"));
    // Unknown networks rank below every prioritized one.
    assertTrue(locale.networkRank("JP:prefectural") < locale.networkRank("e-road"));
  }

  @Test
  void orderShields() {
    var shields = locale.orderShields(List.of(
      new CartographicLocale.Shield("1", "JP:prefectural"),
      new CartographicLocale.Shield("1", "JP:national"),
      new CartographicLocale.Shield("E1", "JP:E")));
    assertEquals(List.of(
      new CartographicLocale.Shield("E1", "JP:E"),
      new CartographicLocale.Shield("1", "JP:national"),
      new CartographicLocale.Shield("1", "JP:prefectural")), shields);
  }
}
