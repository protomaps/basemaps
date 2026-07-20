package com.protomaps.basemap.locales;

import java.util.List;

/*
 * Logic specific to the Netherlands.
 * <p>
 * Prioritizes route networks for shield ordering: A-roads (motorways) over N-roads
 * (national / provincial) over S-roads (city distributor rings).
 */
public class NL extends CartographicLocale {

  /**
   * OSM scopes stadsroute networks per municipality: NL:S:Amsterdam, NL:S:Rotterdam, NL:S:Den Haag, NL:S:Nijmegen,
   * NL:S:Zaanstad, NL:S:Parkstad. They share one shield design, so they collapse onto a single network for
   * symbolization.
   */
  private static final String S_ROAD_PREFIX = "NL:S:";

  /**
   * Legacy name for the collapsed stadsroute network. This is not an OSM value — it was synthesized by an older
   * way-ref-based implementation, and the sprite sheets and styles are keyed to it. Renaming it to "NL:S" is deferred
   * to the next breaking release.
   */
  private static final String S_ROAD_NETWORK = "NL:S-road";

  // Ordered by shield priority; the first matching prefix wins.
  private static final List<String> NETWORK_PRIORITY = List.of(
    "NL:A",
    "NL:N",
    S_ROAD_NETWORK
  );

  @Override
  public String normalizeNetwork(String network) {
    if (network != null && network.startsWith(S_ROAD_PREFIX)) {
      return S_ROAD_NETWORK;
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
