package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.postprocess.LinkSimplify.linkSimplify;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.Matcher;
import com.protomaps.basemap.geometry.Linear;
import com.protomaps.basemap.locales.CartographicLocale;
import com.protomaps.basemap.names.OsmNames;
import java.util.*;
import org.locationtech.jts.geom.LineString;

@SuppressWarnings("java:S1192")
public class Roads implements ForwardingProfile.LayerPostProcessor, ForwardingProfile.OsmRelationPreprocessor {

  private final CountryCoder countryCoder;

  public Roads(CountryCoder countryCoder) {
    this.countryCoder = countryCoder;
  }

  public static final String LAYER_NAME = "roads";

  // Internal tags used to reference calculated values between matchers
  private static final String KIND = "protomaps-basemaps:kind";
  private static final String KIND_DETAIL = "protomaps-basemaps:kindDetail";
  private static final String MINZOOM = "protomaps-basemaps:minZoom";
  private static final String MINZOOM_SHIELD = "protomaps-basemaps:minZoomShield";
  private static final String MINZOOM_NAME = "protomaps-basemaps:minZoomName";
  private static final String HIGHWAY = "protomaps-basemaps:highway";
  private static final String COUNTRY = "protomaps-basemaps:country";
  private static final String UNDEFINED = "protomaps-basemaps:undefined";

  private static final MultiExpression.Index<Map<String, Object>> osmKindsIndex = MultiExpression.of(List.of(
    rule(
      use(KIND_DETAIL, fromTag("highway")),
      use(HIGHWAY, fromTag("highway"))
    ),
    rule(with("service"), use(KIND_DETAIL, fromTag("service"))),

    rule(with("highway", "motorway"), use(KIND, "highway")),
    rule(with("highway", "motorway_link"), use(KIND, "highway")),

    rule(with("highway", "trunk"), use(KIND, "major_road")),
    rule(with("highway", "trunk_link"), use(KIND, "major_road")),
    rule(with("highway", "primary"), use(KIND, "major_road")),
    rule(with("highway", "primary_link"), use(KIND, "major_road")),
    rule(with("highway", "secondary"), use(KIND, "major_road")),
    rule(with("highway", "secondary_link"), use(KIND, "major_road")),
    rule(with("highway", "tertiary"), use(KIND, "major_road")),
    rule(with("highway", "tertiary_link"), use(KIND, "major_road")),

    rule(with("highway", "residential", "unclassified", "road", "raceway"), use(KIND, "minor_road")),
    rule(with("highway", "service"), use(KIND, "minor_road"), use(KIND_DETAIL, "service")),
    rule(
      with("highway", "service"),
      with("service"),
      use(KIND, "minor_road"),
      use(KIND_DETAIL, "service"),
      use("service", fromTag("service"))
    ),
    rule(with("highway", "pedestrian", "track", "corridor"), use(KIND, "path")),
    rule(
      with("highway", "path", "cycleway", "bridleway", "footway", "steps"),
      use(KIND, "path")
    ),
    rule(
      with("highway", "footway"),
      with("footway", "sidewalk", "crossing"),
      use(KIND, "path"),
      use(KIND_DETAIL, fromTag("footway"))
    ),
    rule(
      with("highway", "corridor"),
      use(KIND, "path"),
      use(KIND_DETAIL, "corridor") // fromTag("footway") fails tests
    )
  )).index();

  private static final MultiExpression.Index<Map<String, Object>> indexNonHighways = MultiExpression.of(List.of(
    rule(
      with("railway"),
      use("kind", "rail"),
      use("kindDetail", fromTag("railway")),
      use("minZoom", 11)
    ),
    rule(
      with("railway", "service"),
      use("minZoom", 13)
    ),
    rule(
      with("railway", "service"),
      with("service"),
      use("minZoom", 14)
    ),
    rule(
      with("""
          railway
          funicular
          light_rail
          miniature
          monorail
          narrow_gauge
          preserved
          subway
          tram
        """),
      use("minZoom", 14)
    ),
    rule(
      with("railway", "disused"),
      use("minZoom", 15)
    ),
    rule(
      with("railway"),
      with("""
          service
          yard
          siding
          crossover
        """),
      use("minZoom", 13)
    ),
    rule(
      with("aerialway", "cable_car"),
      use("kind", "aerialway"),
      use("kindDetail", "cable_car"),
      use("minZoom", 11)
    ),
    rule(
      with("man_made", "pier"),
      use("kind", "path"),
      use("kindDetail", "pier"),
      use("minZoom", 13)
    ),
    rule(
      with("route", "ferry"),
      use("kind", "ferry"),
      use("minZoom", 11)
    ),
    rule(
      with("aeroway", "taxiway"),
      use("kind", "aeroway"),
      use("kindDetail", "taxiway"),
      use("minZoom", 10)
    ),
    rule(
      with("aeroway", "runway"),
      use("kind", "aeroway"),
      use("kindDetail", "runway"),
      use("minZoom", 9)
    )
  )).index();

  // Overture properties to Protomaps kind mapping

  private static final MultiExpression.Index<Map<String, Object>> overtureRoadKindsIndex =
    MultiExpression.ofOrdered(List.of(

      // Everything is undefined at first
      rule(use(KIND, UNDEFINED), use(KIND_DETAIL, UNDEFINED), use(HIGHWAY, UNDEFINED)),

      // Pull detail from road class by default, also store in HIGHWAY for zoom grading
      rule(
        with("class"),
        use(KIND, fromTag("class")),
        use(KIND_DETAIL, fromTag("class")),
        use(HIGHWAY, fromTag("class"))
      ),

      // Overwrite detail with subclass if it exists
      rule(
        with("class"),
        with("subclass"),
        use(KIND_DETAIL, fromTag("subclass"))
      ),

      // Assign specific HighRoad kinds from class
      rule(with("class", "motorway", "motorway_link"), use(KIND, "highway")),
      rule(
        with("class", "trunk", "trunk_link", "primary", "primary_link", "secondary", "secondary_link", "tertiary",
          "tertiary_link"),
        use(KIND, "major_road")
      ),
      rule(with("class", "residential", "unclassified", "road", "raceway", "service"), use(KIND, "minor_road")),
      rule(
        with("class", "pedestrian", "track", "corridor", "path", "cycleway", "bridleway", "footway", "steps"),
        use(KIND, "path")
      ),

      // Assign kind_detail=service if appropriate
      rule(with("class", "service"), use(KIND_DETAIL, "service"))

    )).index();

  private static final MultiExpression.Index<Map<String, Object>> overtureRailKindsIndex =
    MultiExpression.ofOrdered(List.of(

      // Everything is undefined at first
      rule(use(KIND, UNDEFINED), use(KIND_DETAIL, UNDEFINED), use(HIGHWAY, UNDEFINED)),

      // Move Overture type=segment/subtype=rail class to kind_detail
      rule(with("class"), use(KIND, "rail"), use(KIND_DETAIL, fromTag("class")))

    )).index();

  private static final MultiExpression.Index<Map<String, Object>> overtureWaterKindsIndex =
    MultiExpression.ofOrdered(List.of(

      // All Overture type=segment/subtype=water is going to be kind=ferry for now
      rule(use(KIND, "ferry"), use(KIND_DETAIL, UNDEFINED), use(HIGHWAY, UNDEFINED))

    )).index();

  // Protomaps kind/kind_detail to min_zoom mapping

  private static final MultiExpression.Index<Map<String, Object>> highwayZoomsIndex = MultiExpression.ofOrdered(List.of(

    // Everything is ~14 at first
    rule(use(MINZOOM, 14), use(MINZOOM_NAME, 14), use(MINZOOM_SHIELD, 12)),

    // Freeways show up earliest
    rule(with(KIND, "highway"), use(MINZOOM, 3), use(MINZOOM_NAME, 11), use(MINZOOM_SHIELD, 7)),
    rule(with(KIND, "highway"), with(HIGHWAY, "motorway_link"), use(MINZOOM_NAME, 11), use(MINZOOM_SHIELD, 12)),

    // Major roads show up early also
    rule(with(KIND, "major_road"), with(HIGHWAY, "trunk"), use(MINZOOM, 6), use(MINZOOM_NAME, 12),
      use(MINZOOM_SHIELD, 8)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "trunk_link"), use(MINZOOM, 6), use(MINZOOM_NAME, 12),
      use(MINZOOM_SHIELD, 12)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "primary"), use(MINZOOM, 7), use(MINZOOM_NAME, 12),
      use(MINZOOM_SHIELD, 10)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "primary_link"), use(MINZOOM, 7), use(MINZOOM_NAME, 13),
      use(MINZOOM_SHIELD, 11)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "secondary"), use(MINZOOM, 9), use(MINZOOM_NAME, 12),
      use(MINZOOM_SHIELD, 11)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "secondary_link"), use(MINZOOM, 9), use(MINZOOM_NAME, 14),
      use(MINZOOM_SHIELD, 13)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "tertiary"), use(MINZOOM, 9), use(MINZOOM_NAME, 13),
      use(MINZOOM_SHIELD, 12)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "tertiary_link"), use(MINZOOM, 9), use(MINZOOM_NAME, 14),
      use(MINZOOM_SHIELD, 13)),

    // Minor roads and paths show up a little early
    rule(with(KIND, "minor_road"), use(MINZOOM, 12)),
    rule(with(KIND, "minor_road"), with(KIND_DETAIL, "service"), use(MINZOOM, 13)),

    rule(with(KIND, "path"), use(MINZOOM, 12)),
    rule(with(KIND, "path"), with(KIND_DETAIL, "path", "cycleway", "bridleway", "footway", "steps"), use(MINZOOM, 13)),
    rule(with(KIND, "path"), with(KIND_DETAIL, "sidewalk", "crossing", "corridor"), use(MINZOOM, 14)),

    // Non-roads
    rule(with(KIND, "ferry"), use(MINZOOM, 11)),
    rule(with(KIND, "rail"), use(MINZOOM, 11)),
    rule(
      with(KIND, "rail"),
      with(KIND_DETAIL, "funicular", "light_rail", "monorail", "narrow_gauge", "subway", "tram", "unknown"),
      use(MINZOOM, 14)
    ),

    // Freeways in the US are special

    rule(
      with(COUNTRY, "US"),
      with("highway", "motorway", "motorway_link", "trunk", "trunk_link"),
      use(MINZOOM, 7)
    ),
    rule(
      with(COUNTRY, "US"),
      with("_r_network_US:US"),
      use(MINZOOM, 6)
    ),
    rule(
      with(COUNTRY, "US"),
      with("_r_network_US:I"),
      use(MINZOOM, 3)
    )

  )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // Hardcoded to US for now
  private CartographicLocale locale = new CartographicLocale();

  private record RouteRelationInfo(
    @Override long id,
    String network
  ) implements OsmRelationInfo {}

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    if (relation.hasTag("type", "route") && relation.hasTag("route", "road")) {
      return List.of(new RouteRelationInfo(
        relation.id(),
        relation.getString("network")
      ));
    }
    return new ArrayList<>();
  }

  private void processOsmHighways(SourceFeature sf, FeatureCollector features) {

    if (!sf.hasTag("highway")) {
      return;
    }

    if (sf.hasTag("highway", "proposed", "abandoned", "razed", "demolished", "removed", "construction", "elevator")) {
      return;
    }

    String highway = sf.getString("highway");

    CartographicLocale.Shield shield = locale.getShield(sf);

    for (var routeInfo : sf.relationInfo(RouteRelationInfo.class)) {
      RouteRelationInfo relation = routeInfo.relation();
      if (relation.network != null) {
        sf.setTag("_r_network_" + relation.network, "yes");
      }
    }

    try {
      var code = countryCoder.getCountryCode(sf.latLonGeometry());
      code.ifPresent(s -> sf.setTag(COUNTRY, s));
      locale = CountryCoder.getLocale(code);
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = osmKindsIndex.getMatches(sf);

    String kind = getString(sf, matches, KIND, "other");
    String kindDetail = getString(sf, matches, KIND_DETAIL, "");
    int minZoom;
    int minZoomShieldText;
    int minZoomNames;

    // Calculate minZoom using zooms indexes
    var sf2 = new Matcher.SourceFeatureWithComputedTags(
      sf,
      Map.of(KIND, kind, KIND_DETAIL, kindDetail, HIGHWAY, highway)
    );
    var zoomMatches = highwayZoomsIndex.getMatches(sf2);

    // Initial minZoom
    minZoom = getInteger(sf2, zoomMatches, MINZOOM, 99);
    minZoomShieldText = getInteger(sf2, zoomMatches, MINZOOM_SHIELD, 99);
    minZoomNames = getInteger(sf2, zoomMatches, MINZOOM_NAME, 99);

    minZoom = sf.hasTag("access", "private", "no") ? Math.max(minZoom, 15) : minZoom;

    var feat = features.line("roads")
      .setId(FeatureId.create(sf))
      .setAttr("kind", kind)
      // To power better client label collisions
      .setAttr("min_zoom", minZoom + 1)
      .setAttrWithMinzoom("ref", sf.getString("ref"), minZoomShieldText)
      .setAttrWithMinzoom("shield_text", shield.text(), minZoomShieldText)
      .setAttrWithMinzoom("network", shield.network(), minZoomShieldText)
      .setAttrWithMinzoom("oneway", sf.getString("oneway"), 14)
      .setAttrWithMinzoom("access", sf.getTag("access"), 15)
      // temporary attribute that gets removed in the post-process step
      .setAttr(HIGHWAY, highway)
      .setAttr("sort_rank", 400)
      .setMinPixelSize(0)
      .setPixelTolerance(0)
      .setMinZoom(minZoom);

    if (!kindDetail.isEmpty()) {
      feat.setAttr("kind_detail", kindDetail);
    } else {
      feat.setAttr("kind_detail", highway);
    }

    // Core OSM tags for different kinds of places
    if (kind.equals("other") && sf.hasTag("service")) {
      feat.inheritAttrFromSource("service");
    }

    if (sf.hasTag("highway", "motorway_link", "trunk_link", "primary_link", "secondary_link",
      "tertiary_link")) {
      feat.setAttr("is_link", true);
    }

    // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
    // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
    if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
      feat.setAttrWithMinzoom("is_bridge", true, 12);
    } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
      feat.setAttrWithMinzoom("is_tunnel", true, 12);
    }

    // Server sort features so client label collisions are pre-sorted
    feat.setSortKey(minZoom);

    OsmNames.setOsmNames(feat, sf, minZoomNames);
  }

  private void processOsmNonHighways(SourceFeature sf, FeatureCollector features) {

    if (sf.hasTag("building")) {
      // see https://github.com/protomaps/basemaps/issues/249
      return;
    }

    if (sf.hasTag("railway", "abandoned", "razed", "demolished", "removed", "construction", "platform", "platform_edge",
      "proposed")) {
      return;
    }

    var matches = indexNonHighways.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    int minZoom = getInteger(sf, matches, "minZoom", 11);
    String kind = getString(sf, matches, "kind", "other");
    String kindDetail = getString(sf, matches, "kindDetail", "");


    var feature = features.line(this.name())
      .setId(FeatureId.create(sf))
      .setAttr("kind", kind)
      // Used for client-side label collisions
      .setAttr("min_zoom", minZoom + 1)
      .setAttr("network", sf.getString("network"))
      .setAttr("ref", sf.getString("ref"))
      .setAttr("route", sf.getString("route"))
      .setAttr("service", sf.getString("service"))
      .setAttr("sort_rank", 400)
      .setMinPixelSize(0)
      .setPixelTolerance(0)
      .setZoomRange(minZoom, 15);

    if (!kindDetail.isEmpty()) {
      feature.setAttr("kind_detail", kindDetail);
    }

    // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
    // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
    // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
    if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
      feature.setAttrWithMinzoom("is_bridge", true, 12);
    } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
      feature.setAttrWithMinzoom("is_tunnel", true, 12);
    }

    // Too many small pier lines otherwise
    if (kindDetail.equals("pier")) {
      feature.setMinPixelSize(2);
    }

    // Server sort features so client label collisions are pre-sorted
    feature.setSortKey(minZoom);

    OsmNames.setOsmNames(feature, sf, 12);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (!sf.canBeLine()) {
      return;
    }

    processOsmHighways(sf, features);
    processOsmNonHighways(sf, features);

  }

  /**
   * Represents properties that can apply to a segment of a road
   */
  private static class OvertureSegmentProperties {
    boolean isBridge;
    boolean isTunnel;
    boolean isOneway;
    boolean isLink;
    Integer level;

    OvertureSegmentProperties() {
      this.isBridge = false;
      this.isTunnel = false;
      this.isOneway = false;
      this.isLink = false;
      this.level = null;
    }
  }

  public void processOverture(SourceFeature sf, FeatureCollector features) {
    // Filter by type field - Overture transportation theme
    if (!"transportation".equals(sf.getString("theme"))) {
      return;
    }

    if (!"segment".equals(sf.getString("type"))) {
      return;
    }

    List<Map<String, Object>> kindMatches;

    String subtype = sf.getString("subtype");
    if ("road".equals(subtype)) {
      kindMatches = overtureRoadKindsIndex.getMatches(sf);
    } else if ("rail".equals(subtype)) {
      kindMatches = overtureRailKindsIndex.getMatches(sf);
    } else if ("water".equals(subtype)) {
      kindMatches = overtureWaterKindsIndex.getMatches(sf);
    } else {
      return;
    }

    String name = sf.getString("names.primary");
    String kind = getString(sf, kindMatches, KIND, UNDEFINED);
    String kindDetail = getString(sf, kindMatches, KIND_DETAIL, UNDEFINED);
    String highway = getString(sf, kindMatches, HIGHWAY, UNDEFINED);
    Integer minZoom;

    // Quickly eliminate any features with non-matching tags
    if (UNDEFINED.equals(kind))
      return;

    // Calculate minZoom using zooms indexes
    var sf2 = new Matcher.SourceFeatureWithComputedTags(
      sf,
      Map.of(KIND, kind, KIND_DETAIL, kindDetail, HIGHWAY, highway)
    );
    var zoomMatches = highwayZoomsIndex.getMatches(sf2);

    // Initial minZoom
    minZoom = getInteger(sf2, zoomMatches, MINZOOM, 99);

    // Collect all split points from all property arrays
    List<Double> splitPoints = new ArrayList<>();
    collectOvertureSplitPoints(sf.getTag("road_flags"), splitPoints);
    collectOvertureSplitPoints(sf.getTag("rail_flags"), splitPoints);
    collectOvertureSplitPoints(sf.getTag("access_restrictions"), splitPoints);
    collectOvertureSplitPoints(sf.getTag("level_rules"), splitPoints);

    // Get the original geometry - use latLonGeometry for consistency with test infrastructure
    try {
      LineString originalLine = (LineString) sf.latLonGeometry();

      // If no split points, process as single feature
      if (splitPoints.isEmpty()) {
        emitOvertureFeature(features, sf, originalLine, kind, kindDetail, name, highway, minZoom,
          extractOvertureSegmentProperties(sf, 0.0, 1.0));
        return;
      }

      // Split the line and emit features for each segment
      List<LineString> splitGeometries = Linear.splitAtFractions(originalLine, splitPoints);
      List<Linear.Segment> segments = Linear.createSegments(splitPoints);

      for (int i = 0; i < segments.size() && i < splitGeometries.size(); i++) {
        Linear.Segment seg = segments.get(i);
        LineString segmentGeom = splitGeometries.get(i);
        OvertureSegmentProperties props = extractOvertureSegmentProperties(sf, seg.start, seg.end);

        emitOvertureFeature(features, sf, segmentGeom, kind, kindDetail, name, highway, minZoom, props);
      }

    } catch (GeometryException e) {
      // Skip features with geometry problems
    }
  }

  /**
   * Emit a road feature with given geometry and properties
   */
  @java.lang.SuppressWarnings("java:S107")
  private void emitOvertureFeature(FeatureCollector features, SourceFeature sf, LineString geometry,
    String kind, String kindDetail, String name, String highway, int minZoom,
    OvertureSegmentProperties props) {

    // Transform geometry from lat/lon to world coordinates for rendering
    LineString worldGeometry = (LineString) GeoUtils.latLonToWorldCoords(geometry);

    var feat = features.geometry(this.name(), worldGeometry)
      .setId(FeatureId.create(sf))
      .setAttr("kind", kind)
      .setAttr("kind_detail", kindDetail)
      .setAttr("name", name)
      .setAttr("min_zoom", minZoom + 1)
      // temporary attribute that gets removed in the post-process step
      .setAttr(HIGHWAY, highway)
      .setAttr("sort_rank", 400)
      .setMinPixelSize(0)
      .setPixelTolerance(0)
      .setZoomRange(Math.min(minZoom, 15), 15);

    if (props.isOneway) {
      feat.setAttrWithMinzoom("oneway", "yes", 14);
    }

    if (props.isLink) {
      feat.setAttr("is_link", true);
    }

    if (props.isBridge) {
      feat.setAttrWithMinzoom("is_bridge", true, 12);
    }

    if (props.isTunnel) {
      feat.setAttrWithMinzoom("is_tunnel", true, 12);
    }

    if (props.level != null) {
      feat.setAttr("level", props.level);
    }
  }

  /**
   * Collect all split points from road_flags, rail_flags, access_restrictions, and level_rules
   */
  @java.lang.SuppressWarnings("java:S135")
  private void collectOvertureSplitPoints(Object segmentsObj, List<Double> splitPoints) {
    if (!(segmentsObj instanceof List)) {
      return;
    }
    @SuppressWarnings("unchecked") List<Object> segmentList = (List<Object>) segmentsObj;
    for (Object segmentObj : segmentList) {
      if (!(segmentObj instanceof Map)) {
        continue;
      }
      @SuppressWarnings("unchecked") Map<String, Object> flag = (Map<String, Object>) segmentObj;
      Object betweenObj = flag.get("between");
      if (!(betweenObj instanceof List)) {
        continue;
      }
      @SuppressWarnings("unchecked") List<?> between = (List<?>) betweenObj;
      if (between.size() >= 2 && between.get(0) instanceof Number && between.get(1) instanceof Number) {
        splitPoints.add(((Number) between.get(0)).doubleValue());
        splitPoints.add(((Number) between.get(1)).doubleValue());
      }
    }
  }

  private void extractOvertureSegmentFlags(OvertureSegmentProperties props, Map<String, Object> flag, double start,
    double end) {
    Object valuesObj = flag.get("values");
    Object betweenObj = flag.get("between");

    // Determine the range this flag applies to
    double rangeStart = 0.0;
    double rangeEnd = 1.0;
    if (betweenObj instanceof List) {
      @SuppressWarnings("unchecked") List<?> between = (List<?>) betweenObj;
      if (between.size() >= 2 && between.get(0) instanceof Number && between.get(1) instanceof Number) {
        rangeStart = ((Number) between.get(0)).doubleValue();
        rangeEnd = ((Number) between.get(1)).doubleValue();
      }
    }

    // Check if this segment overlaps with the flag's range
    if (Linear.overlaps(start, end, rangeStart, rangeEnd) && valuesObj instanceof List) {
      @SuppressWarnings("unchecked") List<String> values = (List<String>) valuesObj;
      if (values.contains("is_bridge")) {
        props.isBridge = true;
      }
      if (values.contains("is_tunnel")) {
        props.isTunnel = true;
      }
      if (values.contains("is_link")) {
        props.isLink = true;
      }
    }
  }

  private void extractOvertureSegmentRestrictions(OvertureSegmentProperties props, Map<String, Object> restriction,
    double start, double end) {
    String accessType = (String) restriction.get("access_type");
    if (!"denied".equals(accessType)) {
      return;
    }

    Object whenObj = restriction.get("when");
    if (whenObj instanceof Map) {
      @SuppressWarnings("unchecked") Map<String, Object> when = (Map<String, Object>) whenObj;
      String heading = (String) when.get("heading");

      if ("backward".equals(heading)) {
        // Determine the range this restriction applies to
        double rangeStart = 0.0;
        double rangeEnd = 1.0;
        Object betweenObj = restriction.get("between");
        if (betweenObj instanceof List) {
          @SuppressWarnings("unchecked") List<?> between = (List<?>) betweenObj;
          if (between.size() >= 2 && between.get(0) instanceof Number && between.get(1) instanceof Number) {
            rangeStart = ((Number) between.get(0)).doubleValue();
            rangeEnd = ((Number) between.get(1)).doubleValue();
          }
        }

        if (Linear.overlaps(start, end, rangeStart, rangeEnd)) {
          props.isOneway = true;
        }
      }
    }
  }

  private void extractOvertureSegmentLevels(OvertureSegmentProperties props, Map<String, Object> rule, double start,
    double end) {
    Object valueObj = rule.get("value");
    if (valueObj instanceof Number number) {
      Integer levelValue = number.intValue();

      // Determine the range this level applies to
      double rangeStart = 0.0;
      double rangeEnd = 1.0;
      Object betweenObj = rule.get("between");
      if (betweenObj instanceof List) {
        @SuppressWarnings("unchecked") List<?> between = (List<?>) betweenObj;
        if (between.size() >= 2 && between.get(0) instanceof Number && between.get(1) instanceof Number) {
          rangeStart = ((Number) between.get(0)).doubleValue();
          rangeEnd = ((Number) between.get(1)).doubleValue();
        }
      }

      if (Linear.overlaps(start, end, rangeStart, rangeEnd)) {
        props.level = levelValue;
      }
    }
  }

  /**
   * Extract properties that apply to a segment defined by [start, end] fractional positions
   */
  private OvertureSegmentProperties extractOvertureSegmentProperties(SourceFeature sf, double start, double end) {
    OvertureSegmentProperties props = new OvertureSegmentProperties();

    for (String segmentsKey : List.of("road_flags", "rail_flags", "access_restrictions", "level_rules")) {
      Object segmentsObj = sf.getTag(segmentsKey);
      if (!(segmentsObj instanceof List)) {
        continue;
      }
      @SuppressWarnings("unchecked") List<Object> segmentList = (List<Object>) segmentsObj;
      for (Object segmentObj : segmentList) {
        if (!(segmentObj instanceof Map)) {
          continue;
        }
        if ("road_flags".equals(segmentsKey) || "rail_flags".equals(segmentsKey)) {
          @SuppressWarnings("unchecked") Map<String, Object> flag = (Map<String, Object>) segmentObj;
          extractOvertureSegmentFlags(props, flag, start, end);

        } else if ("access_restrictions".equals(segmentsKey)) {
          @SuppressWarnings("unchecked") Map<String, Object> restriction = (Map<String, Object>) segmentObj;
          extractOvertureSegmentRestrictions(props, restriction, start, end);

        } else if ("level_rules".equals(segmentsKey)) {
          @SuppressWarnings("unchecked") Map<String, Object> rule = (Map<String, Object>) segmentObj;
          extractOvertureSegmentLevels(props, rule, start, end);
        }
      }
    }

    return props;
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    // limit the application of LinkSimplify to where cloverleafs are unlikely to be at tile edges.
    if (zoom < 12) {
      items = linkSimplify(items, HIGHWAY, "motorway", "motorway_link");
      items = linkSimplify(items, HIGHWAY, "trunk", "trunk_link");
      items = linkSimplify(items, HIGHWAY, "primary", "primary_link");
      items = linkSimplify(items, HIGHWAY, "secondary", "secondary_link");
    }

    for (var item : items) {
      item.tags().remove(HIGHWAY);
    }

    items = FeatureMerge.mergeLineStrings(items,
      0.5, // after merging, remove lines that are still less than 0.5px long
      0.1, // simplify output linestrings using a 0.1px tolerance
      4 // remove any detail more than 4px outside the tile boundary
    );

    return items;
  }
}
