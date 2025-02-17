package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SimpleFeature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BuildingsTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(15,
      List.of(Map.of("height", 10.0)),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of(
          "building", "yes",
          "height", 10
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void parseWellFormedDouble() {
    var result = Buildings.parseWellFormedDouble("10.5");
    assertEquals(10.5, result);
    result = Buildings.parseWellFormedDouble("10");
    assertEquals(10, result);
    result = Buildings.parseWellFormedDouble("0");
    assertEquals(0, result);
    result = Buildings.parseWellFormedDouble("9,10,11,12");
    assertNull(result);
  }

  @Test
  void parseBuildingHeights() {
    var result = Buildings.parseHeight("10.5", "12", null);
    assertEquals(10.5, result.height());
    result = Buildings.parseHeight("2", null, "1");
    assertEquals(2, result.height());
    assertEquals(1, result.min_height());
  }

  @Test
  void parseBuildingHeightsFromLevels() {
    var result = Buildings.parseHeight(null, "3", null);
    assertEquals(11, result.height());
    result = Buildings.parseHeight(null, "0.5", null);
    assertEquals(5, result.height());
  }


  @Test
  void railwayTagIsBuilding() {
    assertFeatures(12,
      List.of(Map.of("kind", "building")),
      process(SimpleFeature.create(
        newPolygon(0, 0, 1, 1, 0, 1, 0, 0),
        new HashMap<>(Map.of(
          "railway", "signal_box",
          "building", "yes"
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void addAddressPointCentroid() {
    assertFeatures(15,
      List.of(Map.of("kind", "address")),
      process(SimpleFeature.create(
        newPoint(0, 0),
        new HashMap<>(Map.of(
          "addr:housenumber", "12",
          "addr:street", "Main Street"
        )),
        "osm",
        null,
        0
      )));
  }

  @Test
  void deduplicateAddress() throws GeometryException {
    List<VectorTile.Feature> total = new ArrayList<>();
    total.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newPoint(0, 0)),
      new HashMap<>(Map.of("addr_housenumber", "1", "addr_street", "main"))
    ));
    total.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newPoint(1, 0)),
      new HashMap<>(Map.of("addr_housenumber", "1", "addr_street", "main"))
    ));
    var b = new Buildings();
    var deduplicated = b.postProcess(15, total);
    assertEquals(1, deduplicated.size());
  }
}
