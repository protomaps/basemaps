package com.protomaps.basemap.postprocess;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HighRoadPlusTest {
  @Test
  void testNoop() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0)),
      Map.of("layer", 1)
    ));

    var result = HighRoadPlus.duplicate(items);
    assertEquals(1, result.size());
  }

  @Test
  void testBridge() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0)),
      Map.of("layer", 2)
    ));

    var result = HighRoadPlus.duplicate(items);
    assertEquals(2, result.size());
  }
}

