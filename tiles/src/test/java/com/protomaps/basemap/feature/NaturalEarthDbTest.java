package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NaturalEarthDbTest {
  @Test
  void testLookupUSbyISO() {
    var db = NaturalEarthDb.fromList(
      List.of(new NaturalEarthDb.NeAdmin0Country("United States", "United States of America", "US", "Q30", 1.7, 5.7)),
      List.of(), List.of());

    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "US",
      "ISO3166-1:alpha2", "US", "name", "United States", "wikidata", "Q30"), "testsource", null, 0);

    var result = db.getAdmin0ByIso(sf);
    assertEquals(1.7, result.minLabel());
    assertEquals(5.7, result.maxLabel());
  }

  @Test
  void testLookupUSbyWikidata() {
    var db = NaturalEarthDb.fromList(
      List.of(new NaturalEarthDb.NeAdmin0Country("United States", "United States of America", "US", "Q30", 1.7, 5.7)),
      List.of(), List.of());

    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT, Map.of("country_code_iso3166_1_alpha_2", "US",
      "ISO3166-1:alpha2", "US", "name", "United States", "wikidata", "Q30"), "testsource", null, 0);

    var result = db.getAdmin0ByWikidata(sf.getString("wikidata"));
    assertEquals(1.7, result.minLabel());
    assertEquals(5.7, result.maxLabel());
  }

  @Test
  void testLookupCountryByAlternateIso() {
    var db = NaturalEarthDb.fromList(
      List.of(new NaturalEarthDb.NeAdmin0Country("Mexico", "México", "MX", "Q96", 2.0, 6.7)),
      List.of(), List.of());

    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT,
      Map.of("country_code_iso3166_1", "MX", "ISO3166-1", "MX", "name", "México", "wikidata", "Q96"), "testsource",
      null, 0);

    var result = db.getAdmin0ByIso(sf);
    assertEquals(2.0, result.minLabel());
    assertEquals(6.7, result.maxLabel());
  }

  @Test
  void testNotFoundCountryIso() {
    var db = NaturalEarthDb.fromList(List.of(), List.of(), List.of());

    var sf = SimpleFeature.create(GeoUtils.EMPTY_POINT,
      Map.of("country_code_iso3166_1", "XX", "ISO3166-1:alpha2", "XX", "name", "Null Island trap street"), "testsource",
      null, 0);

    var result = db.getAdmin0ByIso(sf);
    assertNull(result);
  }

  @Test
  void testLookupRegionIso() {
    var db = NaturalEarthDb.fromList(List.of(), List.of(
      new NaturalEarthDb.NeAdmin1StateProvince("Yukon", "CA-YT", "Q2009", 3.5, 7.5)
    ), List.of());

    var result = db.getAdmin1ByIso("CA-YT");
    assertEquals(3.5, result.minLabel());
    assertEquals(7.5, result.maxLabel());
  }

  @Test
  void testLookupRegionWikidata() {
    var db = NaturalEarthDb.fromList(List.of(), List.of(
      new NaturalEarthDb.NeAdmin1StateProvince("Yukon", "CA-YT", "Q2009", 3.5, 7.5)
    ), List.of());

    var result = db.getAdmin1ByWikidata("Q2009");
    assertEquals(3.5, result.minLabel());
    assertEquals(7.5, result.maxLabel());
  }

  @Test
  void testNotFoundRegionIso() {
    var db = NaturalEarthDb.fromList(List.of(), List.of(), List.of());
    var result = db.getAdmin1ByIso("CA-YT");
    assertNull(result);
  }

  @Test
  void testNotFoundRegionWikidata() {
    var db = NaturalEarthDb.fromList(List.of(), List.of(), List.of());
    var result = db.getAdmin1ByWikidata("Q2009");
    assertNull(result);
  }

  @Test
  void testSQLImportCountries() {

  }

  @Test
  void testSQLImportRegions() {

  }

  @Test
  void testSQLImportPopulatedPlaces() {

  }
}
