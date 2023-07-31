package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CountryInfosTest {

  @Test
  void testLookupUSbyISO() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "US", "ISO3166-1:alpha2", "US", "name", "United States", "wikidata", "Q30"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(0.7, info.minZoom());
    assertEquals(4.7, info.maxZoom());
  }

  @Test
  void testLookupUSbyWikidata() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "US", "ISO3166-1:alpha2", "US", "name", "United States", "wikidata", "Q30"), "testsource", null, 0);
    var info = CountryInfos.getByWikidata(sf);
    assertEquals(0.7, info.minZoom());
    assertEquals(4.7, info.maxZoom());
  }

  @Test
  void testLookupMXbyISO() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "MX", "ISO3166-1:alpha2", "MX", "name", "México", "wikidata", "Q96"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(1.0, info.minZoom());
    assertEquals(5.7, info.maxZoom());
  }

  @Test
  void testLookupMXbyWikidata() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "MX", "ISO3166-1:alpha2", "MX", "name", "México", "wikidata", "Q96"), "testsource", null, 0);
    var info = CountryInfos.getByWikidata(sf);
    assertEquals(1.0, info.minZoom());
    assertEquals(5.7, info.maxZoom());
  }

  @Test
  void testAlternateIsoTag() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1", "MX", "ISO3166-1", "MX", "name", "México", "wikidata", "Q96"), "testsource",
      null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(1.0, info.minZoom());
    assertEquals(5.7, info.maxZoom());
  }

  @Test
  void testNotFoundCountryIso() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1", "XX", "ISO3166-1:alpha2", "XX", "name", "Null Island trap street"), "testsource", null, 0);
    var info = CountryInfos.getByISO(sf);
    assertEquals(7.0, info.minZoom());
    assertEquals(10.0, info.maxZoom());
  }
}
