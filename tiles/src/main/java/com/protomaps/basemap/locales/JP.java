package com.protomaps.basemap.locales;

import java.util.List;

/*
 * Logic specific to Japan.
 * <p>
 * Prioritizes route networks for shield ordering: numbered expressways (JP:E) over
 * national highways (JP:national) over prefectural roads (JP:prefectural).
 */
public class JP extends CartographicLocale {

  /**
   * OSM sometimes scopes prefectural networks per prefecture (JP:prefectural:nagano, ...) and occasionally misspells the
   * tag as JP:perfectural. They share one shield design, so they collapse onto a single network for symbolization.
   */
  private static final String PREFECTURAL_PREFIX = "JP:prefectural";

  private static final String PREFECTURAL_TYPO = "JP:perfectural";

  private static final String PREFECTURAL_NETWORK = "JP:prefectural";

  /** National-highway expressway variants share the numbered expressway shield. */
  private static final String NATIONAL_EXPRESSWAY = "JP:national:expressway";

  private static final String EXPRESSWAY_NETWORK = "JP:E";

  // Ordered by shield priority the first matching prefix wins.
  private static final List<String> NETWORK_PRIORITY = List.of(
    EXPRESSWAY_NETWORK,
    "JP:national",
    PREFECTURAL_NETWORK
  );

  @Override
  public String normalizeNetwork(String network) {
    if (network != null) {
      if (network.startsWith(PREFECTURAL_PREFIX) || network.startsWith(PREFECTURAL_TYPO)) {
        return PREFECTURAL_NETWORK;
      }
      if (network.equals(NATIONAL_EXPRESSWAY)) {
        return EXPRESSWAY_NETWORK;
      }
    }
    return super.normalizeNetwork(network);
  }

  @Override
  public int networkRank(String network) {
    if (network != null) {
      for (int i = 0; i < NETWORK_PRIORITY.size(); i++) {
        if (network.startsWith(NETWORK_PRIORITY.get(i))) {
          return i;
        }
      }
    }
    return super.networkRank(network);
  }
}
