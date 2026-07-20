package com.protomaps.basemap.locales;

import java.util.List;

/*
 * Logic specific to the 50 US states.
 * <p>
 * Prioritizes route networks for shield ordering: Interstate over US highway over any
 * state / county / local network.
 */
public class US extends CartographicLocale {

  // Ordered by shield priority; the first matching prefix wins. "US:" is a catch-all for every
  // state, county and local network (US:CO, US:CA, US:CO:Denver, ...) that sorts below the
  // national Interstate and US-highway networks.
  private static final List<String> NETWORK_PRIORITY = List.of(
    "US:I",
    "US:US",
    "US:"
  );

  // Carriageway suffixes distinguish which roadway carries a route (e.g. the local and express
  // lanes of a collector-express freeway). They share the base route's shield, so collapse them.
  private static final List<String> CARRIAGEWAY_SUFFIXES = List.of(":Local", ":Express");

  @Override
  public String normalizeNetwork(String network) {
    if (network != null) {
      for (String suffix : CARRIAGEWAY_SUFFIXES) {
        if (network.endsWith(suffix)) {
          return network.substring(0, network.length() - suffix.length());
        }
      }
    }
    return network;
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
