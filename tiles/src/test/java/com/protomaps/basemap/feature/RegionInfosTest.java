package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RegionInfosTest {

  @Test
  void testLookup() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("name", "Yukon"), "testsource", null, 0);
    var info = RegionInfos.getByName(sf);
    assertEquals(2.5, info.minZoom());
    assertEquals(6.5, info.maxZoom());
  }

  @Test
  void testAlternateNameTag() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("name:en", "Yukon", "name", "NotYukon"), "testsource",
      null, 0);
    var info = RegionInfos.getByName(sf);
    assertEquals(2.5, info.minZoom());
    assertEquals(6.5, info.maxZoom());
  }

  @Test
  void testNotFoundRegion() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("name", "Null Island"), "testsource", null, 0);
    var info = RegionInfos.getByName(sf);
    assertEquals(8.0, info.minZoom());
    assertEquals(11.0, info.maxZoom());
  }
}
