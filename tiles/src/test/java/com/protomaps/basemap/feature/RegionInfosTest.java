package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RegionInfosTest {

  @Test
  void testLookupRegionIso() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-2", "CA-YT", "name", "Yukon", "wikidata", "Q2009"), "testsource", null, 0);
    var info = RegionInfos.getByISO(sf);
    assertEquals(2.5, info.minZoom());
    assertEquals(6.5, info.maxZoom());
  }

  @Test
  void testNotFoundRegionIso() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-2", "XX-XX", "name", "Null Island trap street"), "testsource", null, 0);
    var info = RegionInfos.getByISO(sf);
    assertEquals(8.0, info.minZoom());
    assertEquals(11.0, info.maxZoom());
  }

  @Test
  void testLookupRegionWikidataYukon() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-2", "CA-YT", "name", "Yukon", "wikidata", "Q2009"), "testsource", null, 0);
    var info = RegionInfos.getByISO(sf);
    assertEquals(2.5, info.minZoom());
    assertEquals(6.5, info.maxZoom());
  }

  @Test
  void testLookupRegionWikidataKansas() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-2", "US-KS", "name", "Kansas", "wikidata", "Q1558"), "testsource", null, 0);
    var info = RegionInfos.getByISO(sf);
    assertEquals(2.5, info.minZoom());
    assertEquals(6.5, info.maxZoom());
  }

  @Test
  void testNotFoundRegionWikidata() {
    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("ISO3166-2", "XX-XX", "name", "Null Island trap street"), "testsource", null, 0);
    var info = RegionInfos.getByISO(sf);
    assertEquals(8.0, info.minZoom());
    assertEquals(11.0, info.maxZoom());
  }

}
