package com.protomaps.basemap.names;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OsmNamesTest {
  @Test
  void checkNames() {
    assertTrue(OsmNames.isAllowed("name:en"));
    assertTrue(OsmNames.isAllowed("name:nl"));
    assertFalse(OsmNames.isAllowed("name:zh"));
    assertTrue(OsmNames.isAllowed("name:zh-Hans"));
    assertTrue(OsmNames.isAllowed("name:zh-Hant"));
    assertFalse(OsmNames.isAllowed("name:dk"));
  }
}
