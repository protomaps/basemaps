package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CountryInfosTest {

  @Test
  void testLookup() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-1:alpha2", "MX"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(5.9, info.minZoom());
    assertEquals(10.2, info.maxZoom());
  }

  @Test
  void testAlternateNameTag() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-1", "MX"), "testsource",
      null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(5.9, info.minZoom());
    assertEquals(10.2, info.maxZoom());
  }

  @Test
  void testNotFoundCountry() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-1:alpha2", "XX"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(8.0, info.minZoom());
    assertEquals(11.0, info.maxZoom());
  }
}
