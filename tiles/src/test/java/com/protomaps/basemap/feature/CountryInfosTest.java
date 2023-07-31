package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CountryInfosTest {

  @Test
  void testLookupUS() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "US", "ISO3166-1:alpha2", "US", "name", "United States"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(2.5, info.minZoom());
    assertEquals(6.5, info.maxZoom());
  }

  @Test
  void testLookupMX() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "MX", "ISO3166-1:alpha2", "MX", "name", "México"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(5.9, info.minZoom());
    assertEquals(10.2, info.maxZoom());
  }

  @Test
  void testAlternateIsoTag() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1", "MX", "ISO3166-1", "MX", "name", "México"), "testsource",
      null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(5.9, info.minZoom());
    assertEquals(10.2, info.maxZoom());
  }

  @Test
  void testNotFoundCountryIso() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1", "XX", "ISO3166-1:alpha2", "XX", "name", "Null Island trap street"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(8.0, info.minZoom());
    assertEquals(11.0, info.maxZoom());
  }
}
