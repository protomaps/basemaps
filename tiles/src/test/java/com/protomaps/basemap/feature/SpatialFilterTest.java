package com.protomaps.basemap.feature;

import static com.onthegomap.planetiler.TestUtils.newPoint;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;

class SpatialFilterTest {
  @Test
  void testContains() {
    var sf = SimpleFeature.create(newPoint(10, 10), Map.of("name", "Mexico"), "testsource", null, 0);
    assertTrue(SpatialFilter.withinBounds(new Envelope(0, 20, 0, 20), sf));
    assertFalse(SpatialFilter.withinBounds(new Envelope(20, 30, 20, 30), sf));
    assertTrue(SpatialFilter.withinBounds(null, sf));
  }
}
