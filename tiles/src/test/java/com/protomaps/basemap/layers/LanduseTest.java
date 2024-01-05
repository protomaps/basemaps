package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newPolygon;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;

class LanduseTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("pmap:kind", "hospital")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("amenity", "hospital")),
        "osm",
        null,
        0
      )));
  }

  void assertKind(String expected, Map<String, String> keys) {
    assertFeatures(15,
      List.of(Map.of("pmap:kind", expected)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(keys),
        "osm",
        null,
        0
      )));
  }

  void assertNone(Map<String, String> keys) {
    assertEquals(0,
      StreamSupport.stream(process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(keys),
        "osm",
        null,
        0
      )).spliterator(), false).toList().size());
  }


  @Test
  void kinds() {
    assertKind("aerodrome", Map.of("aeroway", "aerodrome"));
    assertKind("runway", Map.of("aeroway", "runway"));
    assertKind("taxiway", Map.of("area:aeroway", "taxiway"));
    assertKind("runway", Map.of("area:aeroway", "runway"));
    assertKind("university", Map.of("amenity", "university"));
    assertKind("college", Map.of("amenity", "college"));
    //    assertKind("townhall", Map.of("amenity", "townhall"));
    assertKind("pedestrian", Map.of("highway", "pedestrian", "area", "yes"));
    assertNone(Map.of("highway", "pedestrian"));
    assertKind("pedestrian", Map.of("highway", "footway", "area", "yes"));
    assertNone(Map.of("highway", "footway"));
    assertKind("cemetery", Map.of("landuse", "cemetery"));
    assertKind("farmland", Map.of("landuse", "orchard"));
    assertKind("farmland", Map.of("landuse", "farmland"));
    assertKind("farmland", Map.of("landuse", "farmyard"));
    assertKind("residential", Map.of("landuse", "residential"));
    assertKind("industrial", Map.of("landuse", "industrial"));
    assertKind("industrial", Map.of("landuse", "brownfield"));
    assertKind("military", Map.of("landuse", "military"));
    //    assertKind("naval_base", Map.of("military", "naval_base"));
    //    assertKind("airfield", Map.of("military", "airfield"));
    assertKind("golf_course", Map.of("leisure", "golf_course"));
    //    assertKind("marina", Map.of("leisure", "marina"));
    assertKind("park", Map.of("leisure", "park"));
    //    assertKind("stadium", Map.of("leisure", "stadium"));
    //    assertKind("pedestrian", Map.of("man_made", "bridge"));
    assertKind("pier", Map.of("man_made", "pier"));
    //    assertKind("grocery", Map.of("shop", "grocery"));
    //    assertKind("supermarket", Map.of("shop", "supermarket"));
    assertKind("attraction", Map.of("tourism", "attraction"));
    assertKind("camp_site", Map.of("tourism", "camp_site"));
    assertKind("hotel", Map.of("tourism", "hotel"));
  }
}
