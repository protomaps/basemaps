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
}
