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

  private FeatureCollector processWithRelationAndCoords(String network, double startLon, double startLat, double endLon,
    double endLat, String... arguments) {
    var relationResult = profile.preprocessOsmRelation(new OsmElement.Relation(1, Map.of(
      "type", "route",
      "route", "road",
      "network", network
    ), List.of(
      new OsmElement.Relation.Member(OsmElement.Type.WAY, 2, "role")
    )));

    Map<String, Object> tags = new HashMap<>();
    List<String> argumentList = List.of(arguments);
    if (argumentList.size() % 2 == 0) {
      for (int i = 0; i < argumentList.size(); i += 2) {
        tags.put(argumentList.get(i), argumentList.get(i + 1));
      }
    }

    return process(SimpleFeature.createFakeOsmFeature(
      newLineString(startLon, startLat, endLon, endLat),
      tags,
      "osm",
      null,
      2,
      relationResult.stream().map(info -> new OsmReader.RelationMember<>("role", info)).toList()
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
    // Denver - Boulder
    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 3
      )),
      processWithRelationAndCoords("US:I", -104.97235, 39.73867, -105.260503, 40.010771, "highway", "motorway")
    );
  }

  @Test
  void relation2() {
    // highway=motorway is part of US State network and is located in the US -> minzoom should be 6
    // Denver - Boulder
    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 6
      )),
      processWithRelationAndCoords("US:US", -104.97235, 39.73867, -105.260503, 40.010771, "highway", "motorway")
    );
  }

  @Test
  void relation3() {
    // highway=motorway is not part of US Interstate/State network and is located in the US -> minzoom should be 7
    // Denver - Boulder
    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 7
      )),
      processWithRelationAndCoords("some:network", -104.97235, 39.73867, -105.260503, 40.010771, "highway", "motorway")
    );
  }

  @Test
  void relation4() {
    // highway=motorway is part of US State network and is located ouside of the US -> minzoom should be 3
    // Paris - Zurich
    assertFeatures(0,
      List.of(Map.of(
        "_minzoom", 3
      )),
      processWithRelationAndCoords("US:US", 2.424, 48.832, 8.52332, 47.36919, "highway", "motorway")
    );
  }

  @Test
  void testHighwayExcluded() {
    assertFeatures(12,
      List.of(),
      processWith("highway", "proposed")
    );

    assertFeatures(12,
      List.of(),
      processWith("highway", "abandoned")
    );

    assertFeatures(12,
      List.of(),
      processWith("highway", "razed")
    );

    assertFeatures(12,
      List.of(),
      processWith("highway", "demolished")
    );

    assertFeatures(12,
      List.of(),
      processWith("highway", "removed")
    );

    assertFeatures(12,
      List.of(),
      processWith("highway", "construction")
    );

    assertFeatures(12,
      List.of(),
      processWith("highway", "elevator")
    );
  }

  @Test
  void testHighwayOther() {
    assertFeatures(12,
      List.of(Map.of("kind", "other",
        "kind_detail", "a",
        "_minzoom", 14
      )),
      processWith("highway", "a")
    );

    assertFeatures(12,
      List.of(Map.of("kind", "other",
        "kind_detail", "b",
        "_minzoom", 14
      )),
      processWith("highway", "a",
        "service", "b")
    );
  }

  @Test
  void testHighwayMotorway() {

    // generic highway=motorway
    assertFeatures(12,
      List.of(Map.of("kind", "highway",
        "kind_detail", "motorway",
        "_minzoom", 3
      )),
      processWith("highway", "motorway")
    );


    // US highway=motorway
    assertFeatures(12,
      List.of(Map.of("kind", "highway",
        "kind_detail", "motorway",
        "_minzoom", 7
      )),
      processWithRelationAndCoords("",
        -104.97235, 39.73867, -105.260503, 40.010771,
        "highway", "motorway"
      )
    );

    // US highway=motorway with relation US:US
    assertFeatures(12,
      List.of(Map.of("kind", "highway",
        "kind_detail", "motorway",
        "_minzoom", 6
      )),
      processWithRelationAndCoords("US:US",
        -104.97235, 39.73867, -105.260503, 40.010771,
        "highway", "motorway"
      )
    );

    // US highway=motorway with relation US:I
    assertFeatures(12,
      List.of(Map.of("kind", "highway",
        "kind_detail", "motorway",
        "_minzoom", 3
      )),
      processWithRelationAndCoords("US:I",
        -104.97235, 39.73867, -105.260503, 40.010771,
        "highway", "motorway"
      )
    );
  }
}
