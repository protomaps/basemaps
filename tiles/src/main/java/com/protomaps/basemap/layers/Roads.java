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
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.Matcher;
import com.protomaps.basemap.locales.CartographicLocale;
import com.protomaps.basemap.names.OsmNames;
import java.util.*;

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
  private static final String HIGHWAY = "protomaps-basemaps:highway";
  private static final String UNDEFINED = "protomaps-basemaps:undefined";

  private static final MultiExpression.Index<Map<String, Object>> indexHighways = MultiExpression.of(List.of(
    rule(
      with(),
      use("kindDetail", fromTag("highway"))
    ),
    rule(
      with("service"),
      use("kindDetail", fromTag("service"))
    ),
    rule(
      with("highway", "motorway"),
      use("kind", "highway"),
      use("minZoom", 3),
      use("minZoomShieldText", 7),
      use("minZoomNames", 11)
    ),
    rule(
      with("highway", "motorway_link"),
      use("kind", "highway"),
      use("minZoom", 3),
      use("minZoomShieldText", 12),
      use("minZoomNames", 11)
    ),
    rule(
      with("highway", "trunk"),
      use("kind", "major_road"),
      use("minZoom", 6),
      use("minZoomShieldText", 8),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "trunk_link"),
      use("kind", "major_road"),
      use("minZoom", 6),
      use("minZoomShieldText", 12),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "primary"),
      use("kind", "major_road"),
      use("minZoom", 7),
      use("minZoomShieldText", 10),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "primary_link"),
      use("kind", "major_road"),
      use("minZoom", 7),
      use("minZoomNames", 13)
    ),
    rule(
      with("highway", "secondary"),
      use("kind", "major_road"),
      use("minZoom", 9),
      use("minZoomShieldText", 11),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "secondary_link"),
      use("kind", "major_road"),
      use("minZoom", 9),
      use("minZoomShieldText", 13),
      use("minZoomNames", 14)
    ),
    rule(
      with("highway", "tertiary"),
      use("kind", "major_road"),
      use("minZoom", 9),
      use("minZoomShieldText", 12),
      use("minZoomNames", 13)
    ),
    rule(
      with("highway", "tertiary_link"),
      use("kind", "major_road"),
      use("minZoom", 9),
      use("minZoomShieldText", 13),
      use("minZoomNames", 14)
    ),
    rule(
      with("""
          highway
          residential
          unclassified
          road
          raceway
        """),
      use("kind", "minor_road"),
      use("minZoom", 12),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14)
    ),
    rule(
      with("highway", "service"),
      use("kind", "minor_road"),
      use("kindDetail", "service"),
      use("minZoom", 13),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14)
    ),
    rule(
      with("highway", "service"),
      with("service"),
      use("kind", "minor_road"),
      use("kindDetail", "service"),
      use("minZoom", 14),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14),
      use("service", fromTag("service"))
    ),
    rule(
      with("""
          highway
          pedestrian
          track
          corridor
        """),
      use("kind", "path"),
      use("minZoom", 12),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14)
    ),
    rule(
      with("""
          highway
          path
          cycleway
          bridleway
          footway
          steps
        """),
      use("kind", "path"),
      use("minZoom", 13),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14)
    ),
    rule(
      with("highway", "footway"),
      with("""
          footway
          sidewalk
          crossing
        """),
      use("kind", "path"),
      use("kindDetail", fromTag("footway")),
      use("minZoom", 14),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14)
    ),
    rule(
      with("highway", "corridor"),
      use("kind", "path"),
      use("kindDetail", fromTag("footway")),
      use("minZoom", 14),
      use("minZoomShieldText", 12),
      use("minZoomNames", 14)
    ),
    rule(
      with("_country", "US"),
      with("""
          highway
          motorway
          motorway_link
          trunk
          trunk_link
        """),
      use("minZoom", 7)
    ),
    rule(
      with("_country", "US"),
      with("_r_network_US:US"),
      use("minZoom", 6)
    ),
    rule(
      with("_country", "US"),
      with("_r_network_US:I"),
      use("minZoom", 3)
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

  private static final MultiExpression.Index<Map<String, Object>> overtureKindsIndex =
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

  private static final MultiExpression.Index<Map<String, Object>> highwayZoomsIndex = MultiExpression.ofOrdered(List.of(

    rule(use(MINZOOM, 99)),

    rule(with(KIND, "highway"), use(MINZOOM, 3)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "trunk", "trunk_link"), use(MINZOOM, 6)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "primary", "primary_link"), use(MINZOOM, 7)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "secondary", "secondary_link"), use(MINZOOM, 9)),
    rule(with(KIND, "major_road"), with(HIGHWAY, "tertiary", "tertiary_link"), use(MINZOOM, 9)),
    rule(with(KIND, "minor_road"), use(MINZOOM, 12)),
    rule(with(KIND, "minor_road"), with(KIND_DETAIL, "service"), use(MINZOOM, 13)),
    rule(with(KIND, "path"), use(MINZOOM, 12)),

    rule(with(KIND, "path"), with(KIND_DETAIL, "path", "cycleway", "bridleway", "footway", "steps"), use(MINZOOM, 13)),
    rule(with(KIND, "path"), with(KIND_DETAIL, "sidewalk", "crossing"), use(MINZOOM, 14))

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
      code.ifPresent(s -> sf.setTag("_country", s));
      locale = CountryCoder.getLocale(code);
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = indexHighways.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, "kind", "other");
    String kindDetail = getString(sf, matches, "kindDetail", "");
    int minZoom = getInteger(sf, matches, "minZoom", 14);
    int minZoomShieldText = getInteger(sf, matches, "minZoomShieldText", 14);
    int minZoomNames = getInteger(sf, matches, "minZoomNames", 14);

    if (sf.hasTag("access", "private", "no")) {
      minZoom = Math.max(minZoom, 15);
    }

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
      // `highway` is a temporary attribute that gets removed in the post-process step
      .setAttr("highway", highway)
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

    if (sf.hasTag("railway", "abandoned", "razed", "demolished", "removed", "construction", "platform", "proposed")) {
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

  public void processOverture(SourceFeature sf, FeatureCollector features) {
    // Filter by type field - Overture transportation theme
    if (!"transportation".equals(sf.getString("theme"))) {
      return;
    }

    if (!"segment".equals(sf.getString("type"))) {
      return;
    }

    if (!"road".equals(sf.getString("subtype"))) {
      return;
    }

    var kindMatches = overtureKindsIndex.getMatches(sf);
    if (kindMatches.isEmpty()) {
      return;
    }

    String name = sf.getString("names.primary");
    String kind = getString(sf, kindMatches, KIND, UNDEFINED);
    String kindDetail = getString(sf, kindMatches, KIND_DETAIL, UNDEFINED);
    String highway = getString(sf, kindMatches, HIGHWAY, UNDEFINED);
    Integer minZoom;

    // Quickly eliminate any features with non-matching tags
    if (kind.equals(UNDEFINED))
      return;

    // Calculate minZoom using zooms indexes
    var sf2 = new Matcher.SourceFeatureWithComputedTags(
      sf,
      Map.of(KIND, kind, KIND_DETAIL, kindDetail, HIGHWAY, highway)
    );
    var zoomMatches = highwayZoomsIndex.getMatches(sf2);
    if (zoomMatches.isEmpty())
      return;

    // Initial minZoom
    minZoom = getInteger(sf2, zoomMatches, MINZOOM, 99);

    features.line(this.name())
      .setId(FeatureId.create(sf))
      .setAttr("kind", kind)
      .setAttr("kind_detail", kindDetail)
      .setAttr("name", name)
      // To power better client label collisions
      .setAttr("min_zoom", minZoom + 1)
      // `highway` is a temporary attribute that gets removed in the post-process step
      .setAttr("highway", kind)
      .setAttr("sort_rank", 400)
      .setMinPixelSize(0)
      .setPixelTolerance(0)
      .setZoomRange(Math.min(minZoom, 15), 15);
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    // limit the application of LinkSimplify to where cloverleafs are unlikely to be at tile edges.
    if (zoom < 12) {
      items = linkSimplify(items, "highway", "motorway", "motorway_link");
      items = linkSimplify(items, "highway", "trunk", "trunk_link");
      items = linkSimplify(items, "highway", "primary", "primary_link");
      items = linkSimplify(items, "highway", "secondary", "secondary_link");
    }

    for (var item : items) {
      item.tags().remove("highway");
    }

    items = FeatureMerge.mergeLineStrings(items,
      0.5, // after merging, remove lines that are still less than 0.5px long
      0.1, // simplify output linestrings using a 0.1px tolerance
      4 // remove any detail more than 4px outside the tile boundary
    );

    return items;
  }
}
