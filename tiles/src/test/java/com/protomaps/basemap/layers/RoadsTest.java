package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import com.protomaps.basemap.Basemap;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.TestUtils;
import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
        "ref", "US 1"
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

  @ParameterizedTest
  @CsvSource({
    "proposed",
    "abandoned",
    "razed",
    "demolished",
    "removed",
    "construction",
    "elevator"
  })
  void testHighwayExcluded(String highway) {
    assertFeatures(12,
      List.of(),
      processWith("highway", highway)
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
    "trunk_link, major_road, trunk_link, 6, 7",
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

  void testFootways() {
    assertFeatures(12,
      List.of(Map.of("kind_detail", "sidewalk",
        "_minzoom", 14
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "highway", "footway",
        "footway", "sidewalk"
      )
    );

    assertFeatures(12,
      List.of(Map.of("kind_detail", "crossing",
        "_minzoom", 14
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "highway", "footway",
        "footway", "crossing"
      )
    );
  }

  @Test
  void testService() {
    assertFeatures(12,
      List.of(Map.of("service", "b",
        "_minzoom", 14,
        "kind", "other"
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "highway", "a",
        "service", "b"
      )
    );
  }

  @Test
  void testRailway() {
    assertFeatures(12,
      List.of(Map.of("kind", "rail",
        "kind_detail", "a",
        "_minzoom", 11
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", "a"
      )
    );

    assertFeatures(12,
      List.of(Map.of("kind", "rail",
        "kind_detail", "service",
        "_minzoom", 13
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", "service"
      )
    );

    assertFeatures(12,
      List.of(Map.of("kind", "rail",
        "kind_detail", "service",
        "_minzoom", 14
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", "service",
        "service", "a"
      )
    );
  }

  @ParameterizedTest
  @CsvSource({
    "funicular",
    "light_rail",
    "miniature",
    "monorail",
    "narrow_gauge",
    "preserved",
    "subway",
    "tram"
  })
  void testRailwaysSpecial(String railway) {
    assertFeatures(12,
      List.of(Map.of("kind", "rail",
        "kind_detail", railway,
        "_minzoom", 14
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", railway
      )
    );
  }

  @Test
  void testRailwayDisused() {
    assertFeatures(12,
      List.of(Map.of("kind", "rail",
        "kind_detail", "disused",
        "_minzoom", 15
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", "disused"
      )
    );
  }

  @Test
  void testAerialwayCableCar() {
    assertFeatures(12,
      List.of(Map.of("kind", "aerialway",
        "kind_detail", "cable_car",
        "_minzoom", 11
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "aerialway", "cable_car"
      )
    );
  }

  @Test
  void testManMadePier() {
    assertFeatures(12,
      List.of(Map.of("kind", "path",
        "kind_detail", "pier",
        "_minzoom", 13
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "man_made", "pier"
      )
    );
  }

  @Test
  void testRouteFerry() {
    assertFeatures(12,
      List.of(Map.of("kind", "ferry",
        "_minzoom", 11
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "route", "ferry"
      )
    );
  }

  @Test
  void testAerowayTaxiway() {
    assertFeatures(12,
      List.of(Map.of("kind", "aeroway",
        "kind_detail", "taxiway",
        "_minzoom", 10
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "aeroway", "taxiway"
      )
    );
  }

  @Test
  void testAerowayRunway() {
    assertFeatures(12,
      List.of(Map.of("kind", "aeroway",
        "kind_detail", "runway",
        "_minzoom", 9
      )),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "aeroway", "runway"
      )
    );
  }

  @Test
  void testAvoidBuildings() {
    assertFeatures(12,
      List.of(),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "aeroway", "runway",
        "building", "a"
      )
    );
  }

  @ParameterizedTest
  @CsvSource({
    "abandoned",
    "razed",
    "demolished",
    "removed",
    "construction",
    "platform",
    "proposed"
  })
  void testRailwayExcluded(String railway) {
    assertFeatures(12,
      List.of(),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", railway
      )
    );
  }

  @ParameterizedTest
  @CsvSource({
    "yard",
    "siding",
    "crossover"
  })
  void testRailwayService(String service) {
    assertFeatures(13,
      List.of(Map.of("_minzoom", 13)),
      processWithRelationAndCoords("",
        0, 0, 1, 1,
        "railway", "a",
        "service", service
      )
    );
  }

}


class RoadsOvertureTest extends LayerTest {

  @Test
  void kind_highway_fromMotorwayClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "highway", "min_zoom", 4, "oneway", "yes", "name", "Nimitz Freeway")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "99f8b0b1-efde-4649-820a-9ef5498ba58a", // https://www.openstreetmap.org/way/692662557/history/5
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "motorway",
          "names.primary", "Nimitz Freeway",
          "access_restrictions", List.of(
            Map.of("access_type", "denied", "when", Map.of("heading", "backward"))
          )
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_highwayLink_fromMotorwayClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "highway", "min_zoom", 4, "oneway", "yes", "is_link", true)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "ed49cecd-d577-4924-92b6-abaaf92bee6c", // https://www.openstreetmap.org/way/932872494/history/2
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "motorway",
          "subclass", "link",
          "road_flags", List.of(Map.of("values", List.of("is_link"))),
          "access_restrictions", List.of(
            Map.of("access_type", "denied", "when", Map.of("heading", "backward"))
          )
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorRoad_fromTrunkClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 7, "name", "Mission Street")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "1dfe52aa-9432-4d38-8117-4b1e1fa345f0", // https://www.openstreetmap.org/way/143666210/history/16
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "trunk",
          "names.primary", "Mission Street"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorLink_fromTrunkClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 7, "oneway", "yes", "is_link", true)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "3aefac69-2653-41a1-ae19-0d36d6d03491", // https://www.openstreetmap.org/way/198565349/history/11
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "trunk",
          "subclass", "link",
          "road_flags", List.of(Map.of("values", List.of("is_link"))),
          "access_restrictions", List.of(
            Map.of("access_type", "denied", "when", Map.of("heading", "backward"))
          )
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorRoad_fromPrimaryClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 8, "name", "Ashby Avenue")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "7189cf1b-a235-463d-8871-5e6ffe0c8c3d", // https://www.openstreetmap.org/way/202317700/history/16
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "primary",
          "names.primary", "Ashby Avenue"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorLink_fromPrimaryClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 8, "oneway", "yes", "is_link", true)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "2c9442b6-14c2-44e0-975d-d69bd83a0da7", // https://www.openstreetmap.org/way/198565347/history/10
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "primary",
          "subclass", "link",
          "road_flags", List.of(Map.of("values", List.of("is_link"))),
          "access_restrictions", List.of(
            Map.of("access_type", "denied", "when", Map.of("heading", "backward"))
          )
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorRoad_fromSecondaryClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 10, "name", "40th Street")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "6dc28d02-5b57-4091-9db2-30462f4e2273", // https://www.openstreetmap.org/way/36982937/history/14
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "secondary",
          "names.primary", "40th Street"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorLink_fromSecondaryClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 10, "oneway", "yes", "is_link", true)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "bf0ba372-0a6e-417b-a180-e174f4276b9c", // https://www.openstreetmap.org/way/23591806/history/10
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "secondary",
          "subclass", "link",
          "road_flags", List.of(Map.of("values", List.of("is_link"))),
          "access_restrictions", List.of(
            Map.of("access_type", "denied", "when", Map.of("heading", "backward"))
          )
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorRoad_fromTertiaryClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 10, "name", "West Street")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "e3046f27-df36-4aaa-97f5-7c12eee4310d", // https://www.openstreetmap.org/way/6346756/history/24
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "tertiary",
          "names.primary", "West Street"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_majorLink_fromTertiaryClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "major_road", "min_zoom", 10, "oneway", "yes", "is_link", true)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "ad765059-60f9-4eb5-b672-9faf90748f00", // https://www.openstreetmap.org/way/8915068/history/18
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "tertiary",
          "subclass", "link",
          "road_flags", List.of(Map.of("values", List.of("is_link"))),
          "access_restrictions", List.of(
            Map.of("access_type", "denied", "when", Map.of("heading", "backward"))
          )
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_path_fromPedestrianClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "path", "min_zoom", 13, "name", "13th Street")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "c7a5a72b-cb51-40a9-899c-4ecb2d3fa809", // https://www.openstreetmap.org/way/513726605/history/4
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "pedestrian",
          "names.primary", "13th Street"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_minorRoad_fromResidentialClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "minor_road", "min_zoom", 13, "name", "17th Street")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "1314012e-c948-4812-b9bd-cb9cfd9c2b63", // https://www.openstreetmap.org/way/1033706847/history/3
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "residential",
          "names.primary", "17th Street"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_minorRoad_fromServiceClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "minor_road", "kind_detail", "service", "min_zoom", 14, "name", "Derby Street")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "4651ca6a-16e7-4f97-99b5-5dad4228f146", // https://www.openstreetmap.org/way/8917186/history/6
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "service",
          "names.primary", "Derby Street"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_path_fromCyclewayClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "path", "min_zoom", 14, "name", "Ohlone Greenway")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "96154d80-f268-4b2d-99da-7bb411cf1718", // https://www.openstreetmap.org/way/164658210/history/11
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "cycleway",
          "names.primary", "Ohlone Greenway"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_rail_fromStandardGauge() {
    assertFeatures(15,
      List.of(Map.of("kind", "rail", "kind_detail", "standard_gauge", "min_zoom", 12, "name", "UP Niles Subdivision")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "c6fe375e-046f-40d2-a872-0ed5506d13a0", // https://www.openstreetmap.org/way/318755220/history/15
          "theme", "transportation",
          "type", "segment",
          "subtype", "rail",
          "class", "standard_gauge",
          "names.primary", "UP Niles Subdivision"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_ferry_fromWaterway() {
    assertFeatures(15,
      List.of(Map.of("kind", "ferry", "min_zoom", 12, "name", "Oakland Jack London Square - San Francisco Ferry Building")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "7553c04c-b6fb-4ce5-b03b-a8966816c3f9", // https://www.openstreetmap.org/way/662437195/history/16
          "theme", "transportation",
          "type", "segment",
          "subtype", "water",
          "names.primary", "Oakland Jack London Square - San Francisco Ferry Building"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_rail_fromSubwayClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "rail", "kind_detail", "subway", "min_zoom", 15, "name", "A-Line")),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "d445b0b6-82a9-4e23-8944-478099e6f3fd", // https://www.openstreetmap.org/way/50970282/history/18
          "theme", "transportation",
          "type", "segment",
          "subtype", "rail",
          "class", "subway",
          "names.primary", "A-Line"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  @Test
  void kind_sidewalk_fromFootwayClass() {
    assertFeatures(15,
      List.of(Map.of("kind", "path", "kind_detail", "sidewalk", "min_zoom", 15)),
      process(SimpleFeature.create(
        newLineString(0, 0, 1, 1),
        new HashMap<>(Map.of(
          "id", "5cdc43ee-68d2-416f-9b50-4b9058415f51", // https://www.openstreetmap.org/way/1040431008/history/2
          "theme", "transportation",
          "type", "segment",
          "subtype", "road",
          "class", "footway",
          "subclass", "sidewalk"
        )),
        Basemap.SRC_OVERTURE, null, 0
      )));
  }

  // Tests for partial application of properties (bridge, tunnel, oneway, level) requiring line splitting

  @Test
  void split_partialBridge_middleSection() {
    // Test: Single bridge section in the middle of a line
    // Geometry: (0,0) to (144,0) - Input treated as lat/lon, output transformed to world coords
    // Bridge from 0.25 to 0.75
    // Expected: 3 output features with correct geometries and is_bridge attribute
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 144, 0),
      new HashMap<>(Map.of(
        "id", "test-bridge-middle",
        "theme", "transportation",
        "type", "segment",
        "subtype", "road",
        "class", "primary",
        "road_flags", List.of(
          Map.of("values", List.of("is_bridge"), "between", List.of(0.25, 0.75))
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of(
        "kind", "major_road",
        "kind_detail", "primary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5))
      ),
      Map.of(
        "kind", "major_road",
        "kind_detail", "primary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.8, 0.5)),
        "is_bridge", true
      ),
      Map.of(
        "kind", "major_road",
        "kind_detail", "primary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.8, 0.5, 0.9, 0.5))
      )
    ), results);
  }

  @Test
  void split_partialBridge_twoSections() {
    // Test: Two separate bridge sections
    // Geometry: (0,0) to (180,0) - Input treated as lat/lon, output transformed to world coords
    // Bridges from 0.2-0.4 and 0.6-0.8
    // Expected: 5 output features
    // Based on Overture c3b55f85-220c-4d00-8419-be3f2c795729 (footway with 2 bridge sections)
    // OSM ways: 999763975, 999763974, 22989089, 999763972, 999763973
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 180, 0),
      new HashMap<>(Map.of(
        "id", "c3b55f85-220c-4d00-8419-be3f2c795729",
        "theme", "transportation",
        "type", "segment",
        "subtype", "road",
        "class", "footway",
        "road_flags", List.of(
          Map.of("values", List.of("is_bridge"), "between", List.of(0.2, 0.4)),
          Map.of("values", List.of("is_bridge"), "between", List.of(0.6, 0.8))
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of("kind", "path", "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5))),
      Map.of("kind", "path", "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.7, 0.5)), "is_bridge", true),
      Map.of("kind", "path", "_geom", new TestUtils.NormGeometry(newLineString(0.7, 0.5, 0.8, 0.5))),
      Map.of("kind", "path", "_geom", new TestUtils.NormGeometry(newLineString(0.8, 0.5, 0.9, 0.5)), "is_bridge", true),
      Map.of("kind", "path", "_geom", new TestUtils.NormGeometry(newLineString(0.9, 0.5, 1.0, 0.5)))
    ), results);
  }

  @Test
  void split_partialTunnel_subwayRail() {
    // Test: Rail with bridge and tunnel sections
    // Geometry: (0,0) to (144,0) - Input treated as lat/lon, output transformed to world coords
    // Expected: 4 output features
    // Based on Overture d445b0b6-82a9-4e23-8944-478099e6f3fd (railway with tunnel sections)
    // OSM ways: 32103864, 50970282, etc.
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 144, 0),
      new HashMap<>(Map.of(
        "id", "d445b0b6-82a9-4e23-8944-478099e6f3fd",
        "theme", "transportation",
        "type", "segment",
        "subtype", "rail",
        "class", "subway",
        "rail_flags", List.of(
          Map.of("values", List.of("is_bridge"), "between", List.of(0.25, 0.5)),
          Map.of("values", List.of("is_tunnel"), "between", List.of(0.75, 1.0))
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of("kind", "rail", "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5))),
      Map.of("kind", "rail", "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.7, 0.5)), "is_bridge", true),
      Map.of("kind", "rail", "_geom", new TestUtils.NormGeometry(newLineString(0.7, 0.5, 0.8, 0.5))),
      Map.of("kind", "rail", "_geom", new TestUtils.NormGeometry(newLineString(0.8, 0.5, 0.9, 0.5)), "is_tunnel", true)
    ), results);
  }

  @Test
  void split_partialTunnel_fromStart() {
    // Test: Tunnel from start to middle
    // Geometry: (0,0) to (72,0) - Input treated as lat/lon, output transformed to world coords
    // Tunnel from 0.0 to 0.5
    // Expected: 2 output features
    // Based on Overture 6c52a051-7433-470a-aa89-935be681c967 (primary with tunnel)
    // OSM ways: 659613394, 25966237
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 72, 0),
      new HashMap<>(Map.of(
        "id", "6c52a051-7433-470a-aa89-935be681c967",
        "theme", "transportation",
        "type", "segment",
        "subtype", "road",
        "class", "primary",
        "road_flags", List.of(
          Map.of("values", List.of("is_tunnel"), "between", List.of(0.0, 0.5))
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of(
        "kind", "major_road",
        "kind_detail", "primary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5)),
        "is_tunnel", true
      ),
      Map.of(
        "kind", "major_road",
        "kind_detail", "primary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.7, 0.5))
      )
    ), results);
  }

  @Test
  void split_partialLevel_elevatedSection() {
    // Test: Elevated/bridge section with level=1
    // Geometry: (0,0) to (144,0) - Input treated as lat/lon, output transformed to world coords
    // Level 1 from 0.25 to 0.75
    // Expected: 3 output features with different level values
    // Based on Overture 8d70a823-6584-459d-999d-cabf3b9672f6 (motorway with elevated section)
    // OSM ways: 41168616, 931029707, 41168617
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 144, 0),
      new HashMap<>(Map.of(
        "id", "8d70a823-6584-459d-999d-cabf3b9672f6",
        "theme", "transportation",
        "type", "segment",
        "subtype", "road",
        "class", "motorway",
        "level_rules", List.of(
          Map.of("value", 1, "between", List.of(0.25, 0.75))
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of(
        "kind", "highway",
        "kind_detail", "motorway",
        "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5))
      ),
      Map.of(
        "kind", "highway",
        "kind_detail", "motorway",
        "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.8, 0.5)),
        "level", 1
      ),
      Map.of(
        "kind", "highway",
        "kind_detail", "motorway",
        "_geom", new TestUtils.NormGeometry(newLineString(0.8, 0.5, 0.9, 0.5))
      )
    ), results);
  }

  @Test
  void split_partialOneway_secondHalf() {
    // Test: Oneway restriction on second half of line
    // Geometry: (0,0) to (72,0) - Input treated as lat/lon, output transformed to world coords
    // Oneway (access denied backward) from 0.5 to 1.0
    // Expected: 2 output features
    // Based on Overture 10536347-2a89-4f05-9a3d-92d365931bc4 (secondary with partial oneway)
    // OSM ways: 394110740, 59689569
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 72, 0),
      new HashMap<>(Map.of(
        "id", "10536347-2a89-4f05-9a3d-92d365931bc4",
        "theme", "transportation",
        "type", "segment",
        "subtype", "road",
        "class", "secondary",
        "access_restrictions", List.of(
          Map.of(
            "access_type", "denied",
            "when", Map.of("heading", "backward"),
            "between", List.of(0.5, 1.0)
          )
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of(
        "kind", "major_road",
        "kind_detail", "secondary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5))
      ),
      Map.of(
        "kind", "major_road",
        "kind_detail", "secondary",
        "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.7, 0.5)),
        "oneway", "yes"
      )
    ), results);
  }

  @Test
  void split_overlapping_bridgeAndOneway() {
    // Test: Overlapping bridge and oneway restrictions
    // Geometry: (0,0) to (144,0) - Input treated as lat/lon, output transformed to world coords
    // Bridge from 0.25 to 0.75
    // Oneway from 0.5 to 1.0
    // Expected: 4 output features with different combinations
    var results = process(SimpleFeature.create(
      newLineString(0, 0, 144, 0),
      new HashMap<>(Map.of(
        "id", "test-overlap",
        "theme", "transportation",
        "type", "segment",
        "subtype", "road",
        "class", "primary",
        "road_flags", List.of(
          Map.of("values", List.of("is_bridge"), "between", List.of(0.25, 0.75))
        ),
        "access_restrictions", List.of(
          Map.of(
            "access_type", "denied",
            "when", Map.of("heading", "backward"),
            "between", List.of(0.5, 1.0)
          )
        )
      )),
      Basemap.SRC_OVERTURE, null, 0
    ));

    assertFeatures(15, List.of(
      Map.of(
        "kind", "major_road",
        "_geom", new TestUtils.NormGeometry(newLineString(0.5, 0.5, 0.6, 0.5))
      ),
      Map.of(
        "kind", "major_road",
        "_geom", new TestUtils.NormGeometry(newLineString(0.6, 0.5, 0.7, 0.5)),
        "is_bridge", true
      ),
      Map.of(
        "kind", "major_road",
        "_geom", new TestUtils.NormGeometry(newLineString(0.7, 0.5, 0.8, 0.5)),
        "is_bridge", true,
        "oneway", "yes"
      ),
      Map.of(
        "kind", "major_road",
        "_geom", new TestUtils.NormGeometry(newLineString(0.8, 0.5, 0.9, 0.5)),
        "oneway", "yes"
      )
    ), results);
  }
}
