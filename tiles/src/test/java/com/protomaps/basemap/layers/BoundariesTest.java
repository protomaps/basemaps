package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPolygon;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
      List.of(Map.of("kind", "country")),
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

  @ParameterizedTest
  @CsvSource(value = {
    "disputed,yes",
    "disputed_by,a",
    "claimed_by,a",
    "boundary,claim",
    "boundary,disputed"
  })
  void testDisputedOsmWay(String key, String value) {
    var infos = profile.preprocessOsmRelation(
      new OsmElement.Relation(1, Map.of("type", "boundary", "boundary", "administrative", "admin_level", "2"),
        List.of(new OsmElement.Relation.Member(OsmElement.Type.WAY, 123, ""))));

    var way = SimpleFeature.createFakeOsmFeature(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of(key, value)),
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

  private static final String NE_ADMIN_1_LINES = "ne_10m_admin_1_states_provinces_lines";

  private FeatureCollector processNe(String sourceLayer, Map<String, Object> tags) {
    return process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(tags),
      "ne",
      sourceLayer,
      1
    ));
  }

  /** Attributes every Natural Earth boundary line is expected to carry. */
  private static Map<String, Object> neLine(String kind, int kindDetail, boolean disputed) {
    return Map.of(
      "_layer", "boundaries",
      "_type", "line",
      "kind", kind,
      "kind_detail", kindDetail,
      "sort_rank", disputed ? 288 : 289,
      "disputed", disputed ? true : "<null>"
    );
  }

  // Every featurecla value the Natural Earth switch knows about, so that a mistyped or reordered
  // case arm is caught rather than silently falling through to the "drop the feature" default.
  @ParameterizedTest
  @CsvSource(value = {
    "Disputed (please verify),country,2,true",
    "Indefinite (please verify),country,2,true",
    "Indeterminant frontier,country,2,true",
    "Line of control (please verify),country,2,true",
    "International boundary (verify),country,2,false",
    "Lease limit,lease_limit,3,false",
    "Overlay limit,overlay_limit,3,false",
    "Unrecognized,unrecognized_country,2,false",
    "Map unit boundary,map_unit,3,false",
    "Breakaway,unrecognized_country,3,false",
    "Claim boundary,unrecognized_country,3,false",
    "Elusive frontier,unrecognized_country,3,false",
    "Reference line,unrecognized_country,3,false",
    "Admin-1 region boundary,macroregion,3,false",
    "Admin-1 boundary,region,4,false",
    "Admin-1 statistical boundary,region,4,false",
    "Admin-1 statistical meta bounds,region,4,false",
    "1st Order Admin Lines,region,4,false",
    "Unrecognized Admin-1 region boundary,unrecognized_macroregion,4,false",
    "Unrecognized Admin-1 boundary,unrecognized_region,4,false",
    "Unrecognized Admin-1 statistical boundary,unrecognized_region,4,false",
    "Unrecognized Admin-1 statistical meta bounds,unrecognized_region,4,false"
  })
  void testNeFeatureClass(String featureClass, String kind, int kindDetail, boolean disputed) {
    assertFeatures(5,
      List.of(neLine(kind, kindDetail, disputed)),
      processNe(NE_ADMIN_1_LINES, Map.of("FEATURECLA", featureClass, "MIN_ZOOM", 2.0)));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "ne_10m_admin_0_boundary_lines_land",
    "ne_10m_admin_0_boundary_lines_map_units",
    "ne_10m_admin_0_boundary_lines_disputed_areas",
    NE_ADMIN_1_LINES
  })
  void testNeSourceLayers(String sourceLayer) {
    assertFeatures(5,
      List.of(neLine("country", 2, false)),
      processNe(sourceLayer, Map.of("FEATURECLA", "International boundary (verify)", "MIN_ZOOM", 2.0)));
  }

  // Natural Earth column casing is not consistent: the sqlite package lower cases every column, the GeoPackage
  // package preserves the source shapefile spelling, and some GeoPackage tables mix both in one table.
  // MIN_ZOOM also arrives as a REAL from the GeoPackage but as text from sqlite.
  @ParameterizedTest
  @CsvSource(value = {
    "FEATURECLA,MIN_ZOOM,BRK_A3",
    "featurecla,min_zoom,brk_a3",
    "FEATURECLA,min_zoom,BRK_A3",
    "featurecla,MIN_ZOOM,brk_a3"
  })
  void testNeColumnCasing(String featureClassKey, String minZoomKey, String brkA3Key) {
    assertFeatures(5,
      List.of(Map.of("kind", "region", "kind_detail", 4, "brk_a3", "USA", "_minzoom", 1, "_maxzoom", 5)),
      processNe(NE_ADMIN_1_LINES,
        Map.of(featureClassKey, "Admin-1 boundary", minZoomKey, 2.0, brkA3Key, "USA")));
  }

  // min_zoom drives the low end of the zoom range, and the Natural Earth boundaries always stop at z5
  // where the OpenStreetMap ones take over. Note that min_zoom above 6 is left uncovered here: it produces
  // an inverted zoom range, which trips an assertion inside planetiler's setZoomRange.
  @ParameterizedTest
  @CsvSource(value = {
    "1.0,0",
    "2.0,1",
    "4.5,3",
    "6.0,5"
  })
  void testNeZoomRange(String minZoom, int expectedMinZoom) {
    assertFeatures(5,
      List.of(Map.of("_minzoom", expectedMinZoom, "_maxzoom", 5)),
      processNe(NE_ADMIN_1_LINES, Map.of("FEATURECLA", "Admin-1 boundary", "MIN_ZOOM", minZoom)));
  }

  @Test
  void testNeMissingBrkA3() {
    assertFeatures(5,
      List.of(Map.of("kind", "region", "brk_a3", "<null>")),
      processNe(NE_ADMIN_1_LINES, Map.of("FEATURECLA", "Admin-1 boundary", "MIN_ZOOM", 2.0)));
  }

  // Regression test for the NullPointerException from switching the "ne" source to the GeoPackage:
  // FEATURECLA was present but featurecla was not, which blew up on a null switch expression.
  @Test
  void testNeMissingFeatureClass() {
    assertFeatures(5,
      List.of(),
      processNe(NE_ADMIN_1_LINES, Map.of("MIN_ZOOM", 2.0)));
  }

  @Test
  void testNeUnknownFeatureClass() {
    assertFeatures(5,
      List.of(),
      processNe(NE_ADMIN_1_LINES, Map.of("FEATURECLA", "Some new NE value", "MIN_ZOOM", 2.0)));
  }

  @Test
  void testNeMissingMinZoom() {
    assertFeatures(5,
      List.of(),
      processNe(NE_ADMIN_1_LINES, Map.of("FEATURECLA", "Admin-1 boundary")));
  }

  // Natural Earth has many more boundary tables than the four this layer reads.
  @Test
  void testNeUnhandledSourceLayer() {
    assertFeatures(5,
      List.of(),
      processNe("ne_10m_admin_0_boundary_lines_maritime_indicator",
        Map.of("FEATURECLA", "Admin-1 boundary", "MIN_ZOOM", 2.0)));
  }

  @Test
  void testNePolygonIsIgnored() {
    assertFeatures(5,
      List.of(),
      process(SimpleFeature.create(
        newPolygon(0, 0, 0, 1, 1, 1, 0, 0),
        new HashMap<>(Map.of("FEATURECLA", "Admin-1 boundary", "MIN_ZOOM", 2.0)),
        "ne",
        NE_ADMIN_1_LINES,
        1
      )));
  }
}
