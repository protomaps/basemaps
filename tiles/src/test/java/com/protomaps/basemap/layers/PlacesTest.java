package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPoint;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlacesTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("pmap:kind", "neighbourhood")),
      process(SimpleFeature.create(
        newPoint(1, 1),
        new HashMap<>(Map.of("place", "suburb", "name", "Whoville")),
        "osm",
        null,
        0
      )));
  }

  @Test
  void testMinMaxLabelCountry() {

  }

  @Test
  void testMinMaxLabelCountryNoMatch() {

  }

  @Test
  void testMinMaxLabelRegion() {

  }

  @Test
  void testMinMaxLabelRegionNoMatch() {

  }

  @Test
  void testMinMaxLabelPopulatedPlace() {

  }

  @Test
  void testMinMaxLabelPopulatedPlaceNoMatch() {

  }

  @Test
  void testPopulatedPlaceSpatialJoin() {

  }
}
