package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;

import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BoundariesTest extends LayerTest {
  @Test
  void testUntaggedWay() {
    var infos = profile.preprocessOsmRelation(
      new OsmElement.Relation(1, Map.of("type", "boundary", "boundary", "administrative", "admin_level", "2"),
        List.of(new OsmElement.Relation.Member(OsmElement.Type.WAY, 123, ""))));

    var way = SimpleFeature.createFakeOsmFeature(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of(
      )),
      "osm",
      null,
      123,
      infos.stream().map(r -> new OsmReader.RelationMember<>("", r)).toList()
    );

    var collector = featureCollectorFactory.get(way);
    profile.processFeature(way, collector);

    assertFeatures(12,
      List.of(Map.of("pmap:kind", "country")),
      collector);
  }

  @Test
  void testDisputedNe() {
    var way = SimpleFeature.create(newLineString(0, 0, 1, 1), new HashMap<>(Map.of(
      "featurecla", "Disputed (please verify)", "min_zoom", 0)), "ne", "ne_10m_admin_0_boundary_lines_land", 123);

    var collector = featureCollectorFactory.get(way);
    profile.processFeature(way, collector);

    assertFeatures(12,
      List.of(Map.of("disputed", true)),
      collector);
  }

  @Test
  void testDisputedOsm() {
    var infos = profile.preprocessOsmRelation(
      new OsmElement.Relation(1, Map.of("type", "boundary", "boundary", "disputed", "admin_level", "2"),
        List.of(new OsmElement.Relation.Member(OsmElement.Type.WAY, 123, ""))));

    var way = SimpleFeature.createFakeOsmFeature(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of(
      )),
      "osm",
      null,
      123,
      infos.stream().map(r -> new OsmReader.RelationMember<>("", r)).toList()
    );

    var collector = featureCollectorFactory.get(way);
    profile.processFeature(way, collector);

    assertFeatures(12,
      List.of(Map.of("disputed", true)),
      collector);
  }
}
