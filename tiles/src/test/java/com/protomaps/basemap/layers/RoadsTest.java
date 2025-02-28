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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;


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

  @ParameterizedTest
  @CsvSource({
    "motorway, highway, motorway, 3, 7",
    "motorway_link, highway, motorway_link, 3, 7",
    "trunk, major_road, trunk, 6, 7",
    "trunk_link, major_road, trunk_link, 7, 7",
    "primary, major_road, primary, 7, 7",
    "primary_link, major_road, primary_link, 7, 7",
    "secondary, major_road, secondary, 9, 9",
    "secondary_link, major_road, secondary_link, 9, 9",
    "tertiary, major_road, tertiary, 9, 9",
    "tertiary_link, major_road, tertiary_link, 9, 9",
    "residential, minor_road, residential, 12, 12",
    "service, minor_road, service, 13, 13",
    "residential, minor_road, residential, 12, 12",
    "unclassified, minor_road, unclassified, 12, 12",
    "road, minor_road, road, 12, 12",
    "raceway, minor_road, raceway, 12, 12",
    "pedestrian, path, pedestrian, 12, 12",
    "track, path, track, 12, 12",
    "path, path, path, 13, 13",
    "cycleway, path, cycleway, 13, 13",
    "bridleway, path, bridleway, 13, 13",
    "footway, path, footway, 13, 13",
    "steps, path, steps, 13, 13",
    "corridor, path, corridor, 14, 14",
  })
  void testHighways(String highway, String kind, String kindDetail, int genericMinZoom, int usMinZoom) {

    // generic
    assertFeatures(12,
      List.of(Map.of("kind", kind,
        "kind_detail", kindDetail,
        "_minzoom", genericMinZoom
      )),
      processWith("highway", highway)
    );

    // US
    assertFeatures(12,
      List.of(Map.of("kind", kind,
        "kind_detail", kindDetail,
        "_minzoom", usMinZoom
      )),
      processWithRelationAndCoords("",
        -104.97235, 39.73867, -105.260503, 40.010771,
        "highway", highway
      )
    );

    // US with relation US:US
    assertFeatures(12,
      List.of(Map.of("kind", kind,
        "kind_detail", kindDetail,
        "_minzoom", 6
      )),
      processWithRelationAndCoords("US:US",
        -104.97235, 39.73867, -105.260503, 40.010771,
        "highway", highway
      )
    );

    // US with relation US:I
    assertFeatures(12,
      List.of(Map.of("kind", kind,
        "kind_detail", kindDetail,
        "_minzoom", 3
      )),
      processWithRelationAndCoords("US:I",
        -104.97235, 39.73867, -105.260503, 40.010771,
        "highway", highway
      )
    );
  }
}
