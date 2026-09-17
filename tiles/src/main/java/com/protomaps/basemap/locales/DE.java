package com.protomaps.basemap.locales;

import java.util.List;

/*
 * Logic specific to Germany.
 * <p>
 * Prioritizes route networks for shield ordering: Autobahn (BAB) over Bundesstraßen (DE:national).
 */
public class DE extends CartographicLocale {

  // Ordered by shield priority, the first matching prefix wins.
  private static final List<String> NETWORK_PRIORITY = List.of(
    "BAB",
    "DE:national"
  );

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
