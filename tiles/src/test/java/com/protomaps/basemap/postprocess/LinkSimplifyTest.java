package com.protomaps.basemap.postprocess;

import static com.onthegomap.planetiler.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LinkSimplifyTest {

  @Test
  void testNoop() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0)),
      Map.of("highway", "tag1_link")
    ));

    var result = LinkSimplify.linkSimplify(items, "highway", "tag1", "tag1_link");
    assertEquals(0, result.size());
  }

  @Test
  void testOtherTags() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0)),
      Map.of("highway", "tag1_link")
    ));

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0)),
      Map.of("railway", "rail")
    ));

    var result = LinkSimplify.linkSimplify(items, "highway", "tag1", "tag1_link");
    assertEquals(1, result.size());
  }

  @Test
  void testLinkConnectsMainAtAnyPoint() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<VectorTile.Feature>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0, 20, 0)),

      Map.of("highway", "tag1")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(10, 0, 10, 10)),
      Map.of("highway", "tag1_link")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 10, 10, 10, 20, 10)),
      Map.of("highway", "tag1")
    ));

    var result = LinkSimplify.linkSimplify(items, "highway", "tag1", "tag1_link");
    assertEquals(3, result.size());
  }

  @Test
  void testLinkIsOfframp() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<VectorTile.Feature>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0, 20, 0)),

      Map.of("highway", "tag1")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(10, 0, 10, 10)),
      Map.of("highway", "tag1_link")
    ));

    var result = LinkSimplify.linkSimplify(items, "highway", "tag1", "tag1_link");
    assertEquals(1, result.size());
  }

  @Test
  void testLinkConnectsLinksAtEdges() throws GeometryException {
    List<VectorTile.Feature> items = new ArrayList<VectorTile.Feature>();

    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(0, 0, 10, 0)),
      Map.of("highway", "tag1")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(10, 0, 20, 0)),
      Map.of("highway", "tag1_link")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(20, 0, 30, 0)),
      Map.of("highway", "tag1_link")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(30, 0, 40, 0)),
      Map.of("highway", "tag1_link")
    ));
    items.add(new VectorTile.Feature("mylayer", 1,
      VectorTile.encodeGeometry(newLineString(40, 0, 50, 0)),
      Map.of("highway", "tag1")
    ));

    var result = LinkSimplify.linkSimplify(items, "highway", "tag1", "tag1_link");
    assertEquals(5, result.size());
  }


}
