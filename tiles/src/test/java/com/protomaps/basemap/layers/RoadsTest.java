package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPoint;

import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoadsTest extends LayerTest {
  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("kind", "highway", "kind_detail", "motorway", "ref", "1", "network", "US:US",
        "shield_text_length", 1)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "layer", "1",
          "highway", "motorway",
          "ref", "US 1"
        )),
        "osm",
        null,
        0
      )));
  }

  // @Test
  // void relation() {
  //   var interstateRelation = new OsmElement.Relation(1);
  //   interstateRelation.setTag("route", "road");
  //   interstateRelation.setTag("type", "route");
  //   interstateRelation.setTag("networks", "US:I");

  //   assertFeatures(3,
  //     List.of(Map.of("kind", "highway")),
  //     process(SimpleFeature.createFakeOsmFeature(
  //       newLineString(0, 10, 0, 20),
  //       new HashMap<>(Map.of("highway", "motorway")),
  //       "osm",
  //       null,
  //       0,
  //       profile.preprocessOsmRelation(interstateRelation).stream().map(r -> new OsmReader.RelationMember<>("", r)).toList()
  //     ))
  //   );
  // }
}
