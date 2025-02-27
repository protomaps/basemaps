package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoadsTest extends LayerTest {
  private FeatureCollector processWith(String... arguments) {
    Map<String, Object> tags = new HashMap<>();
    List<String> argumentList = List.of(arguments);
    if (argumentList.size() % 2 == 0) {
      for (int i = 0; i < argumentList.size(); i += 2) {
        tags.put(argumentList.get(i), argumentList.get(i + 1));
      }
    }
    return process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      tags,
      "osm",
      null,
      0
    ));
  }

  @Test
  void simple() {
    assertFeatures(12,
      List.of(Map.of("kind", "highway",
        "kind_detail", "motorway",
        "ref", "1",
        "network", "US:US",
        "shield_text_length", 1
      )),
      processWith("layer", "1",
        "highway", "motorway",
        "ref", "US 1"
      )
    );
  }

  @Test
  void relation1() {
    // highway=motorway is part of a US Interstate relation and is located in the US -> minzoom should be 3
    var relationResult = profile.preprocessOsmRelation(new OsmElement.Relation(1, Map.of(
      "type", "route",
      "route", "road",
      "network", "US:I"
    ), List.of(
      new OsmElement.Relation.Member(OsmElement.Type.WAY, 2, "role")
    )));

    FeatureCollector features = process(SimpleFeature.createFakeOsmFeature(
      newLineString(-104.97235, 39.73867, -105.260503, 40.010771), // Denver - Boulder
      new HashMap<>(Map.of("highway", "motorway")),
      "osm",
      null,
      2,
      relationResult.stream().map(info -> new OsmReader.RelationMember<>("role", info)).toList()
    ));

    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 3
      )),
      features
    );
  }

  @Test
  void relation2() {
    // highway=motorway is part of US State network and is located in the US -> minzoom should be 6
    var relationResult = profile.preprocessOsmRelation(new OsmElement.Relation(1, Map.of(
      "type", "route",
      "route", "road",
      "network", "US:US"
    ), List.of(
      new OsmElement.Relation.Member(OsmElement.Type.WAY, 2, "role")
    )));

    FeatureCollector features = process(SimpleFeature.createFakeOsmFeature(
      newLineString(-104.97235, 39.73867, -105.260503, 40.010771), // Denver - Boulder
      new HashMap<>(Map.of("highway", "motorway")),
      "osm",
      null,
      2,
      relationResult.stream().map(info -> new OsmReader.RelationMember<>("role", info)).toList()
    ));

    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 6
      )),
      features
    );
  }

  @Test
  void relation3() {
    // highway=motorway is not part of US Interstate/State network and is located in the US -> minzoom should be 7
    var relationResult = profile.preprocessOsmRelation(new OsmElement.Relation(1, Map.of(
      "type", "route",
      "route", "road",
      "network", "some:network"
    ), List.of(
      new OsmElement.Relation.Member(OsmElement.Type.WAY, 2, "role")
    )));

    FeatureCollector features = process(SimpleFeature.createFakeOsmFeature(
      newLineString(-104.97235, 39.73867, -105.260503, 40.010771), // Denver - Boulder
      new HashMap<>(Map.of("highway", "motorway")),
      "osm",
      null,
      2,
      relationResult.stream().map(info -> new OsmReader.RelationMember<>("role", info)).toList()
    ));

    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 7
      )),
      features
    );
  }

  @Test
  void relation4() {
    // highway=motorway is part of US State network and is located ouside of the US -> minzoom should be 3
    var relationResult = profile.preprocessOsmRelation(new OsmElement.Relation(1, Map.of(
      "type", "route",
      "route", "road",
      "network", "US:US"
    ), List.of(
      new OsmElement.Relation.Member(OsmElement.Type.WAY, 2, "role")
    )));

    FeatureCollector features = process(SimpleFeature.createFakeOsmFeature(
      newLineString(2.424, 48.832, 8.52332, 47.36919), // Paris - Zurich
      new HashMap<>(Map.of("highway", "motorway")),
      "osm",
      null,
      2,
      relationResult.stream().map(info -> new OsmReader.RelationMember<>("role", info)).toList()
    ));

    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 3
      )),
      features
    );
  }

}
