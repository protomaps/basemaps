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
import com.protomaps.basemap.feature.OsmTags;
import com.protomaps.basemap.locales.CartographicLocale;
import com.protomaps.basemap.locales.US;
import com.protomaps.basemap.names.OsmNames;
import java.util.*;

public class Roads implements ForwardingProfile.LayerPostProcessor, ForwardingProfile.OsmRelationPreprocessor {

  private CountryCoder countryCoder;

  public Roads(CountryCoder countryCoder) {
    this.countryCoder = countryCoder;
  }

  public static final String LAYER_NAME = "roads";

  private static final class UseKeys {
    public static final String KIND = "kind";
    public static final String KIND_DETAIL = "kind_detail";
    public static final String MIN_ZOOM = "min_zoom";
    public static final String MIN_ZOOM_SHIELD_TEXT = "min_zoom_shield_text";
    public static final String MIN_ZOOM_NAMES = "min_zoom_names";
    public static final String SERVICE = "service";
  }

  private static final MultiExpression.Index<Map<String, Object>> indexHighways = MultiExpression.of(List.of(
    rule(
      with(),
      use(UseKeys.KIND_DETAIL, fromTag(OsmTags.HIGHWAY))
    ),
    rule(
      with(OsmTags.SERVICE),
      use(UseKeys.KIND_DETAIL, fromTag(OsmTags.SERVICE))
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.MOTORWAY),
      use(UseKeys.KIND, "highway"),
      use(UseKeys.MIN_ZOOM, 3),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 7),
      use(UseKeys.MIN_ZOOM_NAMES, 11)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.MOTORWAY_LINK),
      use(UseKeys.KIND, "highway"),
      use(UseKeys.MIN_ZOOM, 3),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 11)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.TRUNK),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 6),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 8),
      use(UseKeys.MIN_ZOOM_NAMES, 12)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.TRUNK_LINK),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 6),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 12)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.PRIMARY),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 7),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 10),
      use(UseKeys.MIN_ZOOM_NAMES, 12)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.PRIMARY_LINK),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 7),
      use(UseKeys.MIN_ZOOM_NAMES, 13)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.SECONDARY),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 9),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 11),
      use(UseKeys.MIN_ZOOM_NAMES, 12)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.SECONDARY_LINK),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 9),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 13),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.TERTIARY),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 9),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 13)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.TERTIARY_LINK),
      use(UseKeys.KIND, "major_road"),
      use(UseKeys.MIN_ZOOM, 9),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 13),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(
        OsmTags.HIGHWAY,
        OsmTags.HighwayValues.RESIDENTIAL,
        OsmTags.HighwayValues.UNCLASSIFIED,
        OsmTags.HighwayValues.ROAD,
        OsmTags.HighwayValues.RACEWAY
      ),
      use(UseKeys.KIND, "minor_road"),
      use(UseKeys.MIN_ZOOM, 12),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.SERVICE),
      use(UseKeys.KIND, "minor_road"),
      use(UseKeys.KIND_DETAIL, "service"),
      use(UseKeys.MIN_ZOOM, 13),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.SERVICE),
      with(OsmTags.SERVICE),
      use(UseKeys.KIND, "minor_road"),
      use(UseKeys.KIND_DETAIL, "service"),
      use(UseKeys.MIN_ZOOM, 14),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14),
      use(UseKeys.SERVICE, fromTag(OsmTags.SERVICE))
    ),
    rule(
      with(
        OsmTags.HIGHWAY,
        OsmTags.HighwayValues.PEDESTRIAN,
        OsmTags.HighwayValues.TRACK,
        OsmTags.HighwayValues.CORRIDOR
      ),
      use(UseKeys.KIND, "path"),
      use(UseKeys.MIN_ZOOM, 12),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(
        OsmTags.HIGHWAY,
        OsmTags.HighwayValues.PATH,
        OsmTags.HighwayValues.CYCLEWAY,
        OsmTags.HighwayValues.BRIDLEWAY,
        OsmTags.HighwayValues.FOOTWAY,
        OsmTags.HighwayValues.STEPS
      ),
      use(UseKeys.KIND, "path"),
      use(UseKeys.MIN_ZOOM, 13),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.FOOTWAY),
      with(
        OsmTags.FOOTWAY,
        OsmTags.FootwayValues.SIDEWALK,
        OsmTags.FootwayValues.CROSSING
      ),
      use(UseKeys.KIND, "path"),
      use(UseKeys.KIND_DETAIL, fromTag(OsmTags.FOOTWAY)),
      use(UseKeys.MIN_ZOOM, 14),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with(OsmTags.HIGHWAY, OsmTags.HighwayValues.CORRIDOR),
      use(UseKeys.KIND, "path"),
      use(UseKeys.KIND_DETAIL, fromTag(OsmTags.FOOTWAY)),
      use(UseKeys.MIN_ZOOM, 14),
      use(UseKeys.MIN_ZOOM_SHIELD_TEXT, 12),
      use(UseKeys.MIN_ZOOM_NAMES, 14)
    ),
    rule(
      with("_country", "US"),
      with(
        OsmTags.HIGHWAY,
        OsmTags.HighwayValues.MOTORWAY,
        OsmTags.HighwayValues.MOTORWAY_LINK,
        OsmTags.HighwayValues.TRUNK,
        OsmTags.HighwayValues.TRUNK_LINK
      ),
      use(UseKeys.MIN_ZOOM, 7)
    ),
    rule(
      with("_country", "US"),
      with("_r_network_US:US"),
      use(UseKeys.MIN_ZOOM, 6)
    ),
    rule(
      with("_country", "US"),
      with("_r_network_US:I"),
      use(UseKeys.MIN_ZOOM, 3)
    )
  )).index();

  private static final MultiExpression.Index<Map<String, Object>> indexNonHighways = MultiExpression.of(List.of(
    rule(
      with(OsmTags.RAILWAY),
      use(UseKeys.KIND, "rail"),
      use(UseKeys.KIND_DETAIL, fromTag("railway")),
      use(UseKeys.MIN_ZOOM, 11)
    ),
    rule(
      with(OsmTags.RAILWAY, OsmTags.RailwayValues.SERVICE),
      use(UseKeys.MIN_ZOOM, 13)
    ),
    rule(
      with(OsmTags.RAILWAY, OsmTags.RailwayValues.SERVICE),
      with(OsmTags.SERVICE),
      use(UseKeys.MIN_ZOOM, 14)
    ),
    rule(
      with(
        OsmTags.RAILWAY,
        OsmTags.RailwayValues.FUNICULAR,
        OsmTags.RailwayValues.LIGHT_RAIL,
        OsmTags.RailwayValues.MINIATURE,
        OsmTags.RailwayValues.MONORAIL,
        OsmTags.RailwayValues.NARROW_GAUGE,
        OsmTags.RailwayValues.PRESERVED,
        OsmTags.RailwayValues.SUBWAY,
        OsmTags.RailwayValues.TRAM
      ),
      use(UseKeys.MIN_ZOOM, 14)
    ),
    rule(
      with(OsmTags.RAILWAY, OsmTags.RailwayValues.DISUSED),
      use(UseKeys.MIN_ZOOM, 15)
    ),
    rule(
      with(OsmTags.RAILWAY),
      with(
        OsmTags.SERVICE,
        OsmTags.ServiceValues.YARD,
        OsmTags.ServiceValues.SIDING,
        OsmTags.ServiceValues.CROSSOVER
      ),
      use(UseKeys.MIN_ZOOM, 13)
    ),
    rule(
      with(OsmTags.AERIALWAY, OsmTags.AerialwayValues.CABLE_CAR),
      use(UseKeys.KIND, "aerialway"),
      use(UseKeys.KIND_DETAIL, "cable_car"),
      use(UseKeys.MIN_ZOOM, 11)
    ),
    rule(
      with(OsmTags.MAN_MADE, OsmTags.ManMadeValues.PIER),
      use(UseKeys.KIND, "path"),
      use(UseKeys.KIND_DETAIL, "pier"),
      use(UseKeys.MIN_ZOOM, 13)
    ),
    rule(
      with(OsmTags.ROUTE, OsmTags.RouteValues.FERRY),
      use(UseKeys.KIND, "ferry"),
      use(UseKeys.MIN_ZOOM, 11)
    ),
    rule(
      with(OsmTags.AEROWAY, OsmTags.AerowayValues.TAXIWAY),
      use(UseKeys.KIND, "aeroway"),
      use(UseKeys.KIND_DETAIL, "taxiway"),
      use(UseKeys.MIN_ZOOM, 10)
    ),
    rule(
      with(OsmTags.AEROWAY, OsmTags.AerowayValues.RUNWAY),
      use(UseKeys.KIND, "aeroway"),
      use(UseKeys.KIND_DETAIL, "runway"),
      use(UseKeys.MIN_ZOOM, 9)
    )
  )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // Hardcoded to US for now
  private CartographicLocale locale = new US();

  public record Shield(String text, String network) {}

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

    if (!sf.hasTag(OsmTags.HIGHWAY)) {
      return;
    }

    if (sf.hasTag(OsmTags.HIGHWAY,
      OsmTags.HighwayValues.PROPOSED,
      OsmTags.HighwayValues.ABANDONED,
      OsmTags.HighwayValues.RAZED,
      OsmTags.HighwayValues.DEMOLISHED,
      OsmTags.HighwayValues.REMOVED,
      OsmTags.HighwayValues.CONSTRUCTION,
      OsmTags.HighwayValues.ELEVATOR
    )) {
      return;
    }

    String highway = sf.getString(OsmTags.HIGHWAY);

    Shield shield = locale.getShield(sf);
    Integer shieldTextLength = shield.text() == null ? null : shield.text().length();

    for (var routeInfo : sf.relationInfo(RouteRelationInfo.class)) {
      RouteRelationInfo relation = routeInfo.relation();
      if (relation.network != null) {
        sf.setTag("_r_network_" + relation.network, "yes");
      }
    }

    try {
      Optional<String> code = countryCoder.getCountryCode(sf.latLonGeometry());
      if (code.isPresent()) {
        sf.setTag("_country", code.get());
      }
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = indexHighways.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, UseKeys.KIND, "other");
    String kindDetail = getString(sf, matches, UseKeys.KIND_DETAIL, "");
    int minZoom = getInteger(sf, matches, UseKeys.MIN_ZOOM, 14);
    int minZoomShieldText = getInteger(sf, matches, UseKeys.MIN_ZOOM_SHIELD_TEXT, 14);
    int minZoomNames = getInteger(sf, matches, UseKeys.MIN_ZOOM_NAMES, 14);

    var feat = features.line("roads")
      .setId(FeatureId.create(sf))
      .setAttr("kind", kind)
      // To power better client label collisions
      .setAttr("min_zoom", minZoom + 1)
      .setAttrWithMinzoom("ref", shield.text(), minZoomShieldText)
      .setAttrWithMinzoom("shield_text_length", shieldTextLength, minZoomShieldText)
      .setAttrWithMinzoom("network", shield.network(), minZoomShieldText)
      .setAttrWithMinzoom("oneway", sf.getString("oneway"), 14)
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
    if (kind.equals("other") && sf.hasTag(OsmTags.SERVICE)) {
      feat.inheritAttrFromSource(OsmTags.SERVICE);
    }

    if (sf.hasTag(OsmTags.HIGHWAY,
      OsmTags.HighwayValues.MOTORWAY_LINK,
      OsmTags.HighwayValues.TRUNK_LINK,
      OsmTags.HighwayValues.PRIMARY_LINK,
      OsmTags.HighwayValues.SECONDARY_LINK,
      OsmTags.HighwayValues.TERTIARY_LINK
    )) {
      feat.setAttr("is_link", true);
    }

    // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
    // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
    if (sf.hasTag(OsmTags.BRIDGE) && !sf.hasTag(OsmTags.BRIDGE, OsmTags.BridgeValues.NO)) {
      feat.setAttrWithMinzoom("is_bridge", true, 12);
    } else if (sf.hasTag(OsmTags.TUNNEL) && !sf.hasTag(OsmTags.TUNNEL, OsmTags.TunnelValues.NO)) {
      feat.setAttrWithMinzoom("is_tunnel", true, 12);
    }

    // Server sort features so client label collisions are pre-sorted
    feat.setSortKey(minZoom);

    OsmNames.setOsmNames(feat, sf, minZoomNames);
  }

  private void processOsmNonHighways(SourceFeature sf, FeatureCollector features) {

    if (sf.hasTag(OsmTags.BUILDING)) {
      // see https://github.com/protomaps/basemaps/issues/249
      return;
    }

    if (sf.hasTag(
      OsmTags.RAILWAY,
      OsmTags.RailwayValues.ABANDONED,
      OsmTags.RailwayValues.RAZED,
      OsmTags.RailwayValues.DEMOLISHED,
      OsmTags.RailwayValues.REMOVED,
      OsmTags.RailwayValues.CONSTRUCTION,
      OsmTags.RailwayValues.PLATFORM,
      OsmTags.RailwayValues.PROPOSED
    )) {
      return;
    }

    var matches = indexNonHighways.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    int minZoom = getInteger(sf, matches, UseKeys.MIN_ZOOM, 11);
    String kind = getString(sf, matches, UseKeys.KIND, "other");
    String kindDetail = getString(sf, matches, UseKeys.KIND_DETAIL, "");


    var feature = features.line(this.name())
      .setId(FeatureId.create(sf))
      .setAttr(UseKeys.KIND, kind)
      // Used for client-side label collisions
      .setAttr("min_zoom", minZoom + 1)
      .setAttr("network", sf.getString("network"))
      .setAttr("ref", sf.getString("ref"))
      .setAttr("route", sf.getString("route"))
      .setAttr("service", sf.getString(OsmTags.SERVICE))
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
    if (sf.hasTag(OsmTags.BRIDGE) && !sf.hasTag(OsmTags.BRIDGE, OsmTags.BridgeValues.NO)) {
      feature.setAttrWithMinzoom("is_bridge", true, 12);
    } else if (sf.hasTag(OsmTags.TUNNEL) && !sf.hasTag(OsmTags.TUNNEL, OsmTags.TunnelValues.NO)) {
      feature.setAttrWithMinzoom("is_tunnel", true, 12);
    }

    // Too many small pier lines otherwise
    if (kindDetail.equals("pier")) {
      feature.setMinPixelSize(2);
    }

    // Server sort features so client label collisions are pre-sorted
    feature.setSortKey(minZoom);

    // TODO: (nvkelso 20230623) This should be variable, but 12 is better than 0 for line merging
    OsmNames.setOsmNames(feature, sf, 12);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (!sf.canBeLine()) {
      return;
    }

    processOsmHighways(sf, features);
    processOsmNonHighways(sf, features);

  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    // limit the application of LinkSimplify to where cloverleafs are unlikely to be at tile edges.
    // TODO: selectively apply each class depending on zoom level.
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
