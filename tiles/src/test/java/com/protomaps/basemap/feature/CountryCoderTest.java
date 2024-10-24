package com.protomaps.basemap.feature;

import static com.onthegomap.planetiler.TestUtils.newPoint;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CountryCoderTest {

  @Test
  void testLookupByPoint() throws IOException {
    CountryCoder c = CountryCoder.fromJsonString(
      "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"country\":\"GB\",\"nameEn\":\"Great Britain\"},\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[0,0],[0,1],[1,1],[0,0]]]]}}]}");
    assertEquals(Optional.of("GB"), c.getCountryCode(newPoint(0.1, 0.1)));
  }

  @Test
  void testNullGeometry() throws IOException {
    CountryCoder c = CountryCoder.fromJsonString(
      "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"country\":\"xx\",\"nameEn\":\"Iberia\"},\"geometry\":null}]}");
    assertEquals(Optional.empty(), c.getCountryCode(newPoint(0.1, 0.1)));
  }

  @Test
  void testNoGeometry() throws IOException {
    CountryCoder c = CountryCoder.fromJsonString(
      "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"country\":\"xx\",\"nameEn\":\"Iberia\"}}]}");
    assertEquals(Optional.empty(), c.getCountryCode(newPoint(0.1, 0.1)));
  }

  @Test
  void testNoCountry() throws IOException {
    CountryCoder c = CountryCoder.fromJsonString(
      "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"nameEn\":\"Iberia\"},\"geometry\":null}]}");
    assertEquals(Optional.empty(), c.getCountryCode(newPoint(0.1, 0.1)));
  }
}
