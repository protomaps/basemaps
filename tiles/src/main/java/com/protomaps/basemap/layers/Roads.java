package com.protomaps.basemap.layers;


import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.without;
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

  private static final MultiExpression.Index<Map<String, Object>> indexHighways = MultiExpression.of(List.of(
    rule(
      with("highway", "motorway"),
      use("kind", "highway"),
      use("kindDetail", "motorway"),
      use("minZoom", 3),
      use("minZoomShieldText", 7),
      use("minZoomNames", 11)
    ),
    rule(
      with("highway", "motorway_link"),
      use("kind", "highway"),
      use("kindDetail", "motorway_link"),
      use("minZoom", 3),
      use("minZoomShieldText", 12),
      use("minZoomNames", 11)
    ),
    rule(
      with("highway", "trunk"),
      use("kind", "major_road"),
      use("kindDetail", "trunk"),
      use("minZoom", 6),
      use("minZoomShieldText", 8),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "trunk_link"),
      use("kind", "major_road"),
      use("kindDetail", "trunk_link"),
      use("minZoom", 7),
      use("minZoomShieldText", 12),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "primary"),
      use("kind", "major_road"),
      use("kindDetail", "primary"),
      use("minZoom", 7),
      use("minZoomShieldText", 10),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "primary_link"),
      use("kind", "major_road"),
      use("kindDetail", "primary_link"),
      use("minZoom", 7),
      use("minZoomNames", 13)
    ),
    rule(
      with("highway", "secondary"),
      use("kind", "major_road"),
      use("kindDetail", "secondary"),
      use("minZoom", 9),
      use("minZoomShieldText", 11),
      use("minZoomNames", 12)
    ),
    rule(
      with("highway", "secondary_link"),
      use("kind", "major_road"),
      use("kindDetail", "secondary_link"),
      use("minZoom", 9),
      use("minZoomShieldText", 13),
      use("minZoomNames", 14)
    ),
    rule(
      with("highway", "tertiary"),
      use("kind", "major_road"),
      use("kindDetail", "tertiary"),
      use("minZoom", 9),
      use("minZoomShieldText", 12),
      use("minZoomNames", 13)
    ),
    rule(
      with("highway", "tertiary_link"),
      use("kind", "major_road"),
      use("kindDetail", "tertiary_link"),
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
      use("kindDetail", fromTag("highway")),
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
      use("kindDetail", fromTag("highway")),
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
      use("kindDetail", fromTag("highway")),
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
    return null;
  }

  private void processOsmHighways(SourceFeature sf, FeatureCollector features) {

    String highway = sf.getString("highway");
    String service = "";

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

    String kind = getString(sf, matches, "kind", "other");
    String kindDetail = getString(sf, matches, "kindDetail", "");
    int minZoom = getInteger(sf, matches, "minZoom", 14);
    int minZoomShieldText = getInteger(sf, matches, "minZoomShieldText", 14);
    int minZoomNames = getInteger(sf, matches, "minZoomNames", 14);

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
    if (!service.isEmpty()) {
      feat.setAttr("service", service);
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

    int minZoom = 11;

    if (sf.hasTag("aeroway", "runway")) {
      minZoom = 9;
    } else if (sf.hasTag("aeroway", "taxiway")) {
      minZoom = 10;
    } else if (sf.hasTag("service", "yard", "siding", "crossover")) {
      minZoom = 13;
    } else if (sf.hasTag("man_made", "pier")) {
      minZoom = 13;
    }

    String kind = "other";
    String kindDetail = "";
    if (sf.hasTag("aeroway")) {
      kind = "aeroway";
      kindDetail = sf.getString("aeroway");
    } else if (sf.hasTag("railway", "disused", "funicular", "light_rail", "miniature", "monorail", "narrow_gauge",
      "preserved", "subway", "tram")) {
      kind = "rail";
      kindDetail = sf.getString("railway");
      minZoom = 14;

      if (sf.hasTag("railway", "disused")) {
        minZoom = 15;
      }
    } else if (sf.hasTag("railway")) {
      kind = "rail";
      kindDetail = sf.getString("railway");

      if (kindDetail.equals("service")) {
        minZoom = 13;

        // eg a rail yard
        if (sf.hasTag("service")) {
          minZoom = 14;
        }
      }
    } else if (sf.hasTag("route", "ferry")) {
      kind = "ferry";
    } else if (sf.hasTag("man_made", "pier")) {
      kind = "path";
      kindDetail = "pier";
    } else if (sf.hasTag("aerialway")) {
      kind = "aerialway";
      kindDetail = sf.getString("aerialway");
    }

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

    // TODO: (nvkelso 20230623) This should be variable, but 12 is better than 0 for line merging
    OsmNames.setOsmNames(feature, sf, 12);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && sf.hasTag("highway") &&
      !(sf.hasTag("highway", "proposed", "abandoned", "razed", "demolished", "removed", "construction", "elevator"))) {
        processOsmHighways(sf, features);
    } 

    if (sf.canBeLine() && (sf.hasTag("railway") ||
      sf.hasTag("aerialway", "cable_car") ||
      sf.hasTag("man_made", "pier") ||
      sf.hasTag("route", "ferry") ||
      sf.hasTag("aeroway", "runway", "taxiway")) &&
      (!sf.hasTag("building") /* see https://github.com/protomaps/basemaps/issues/249 */) &&
      (!sf.hasTag("railway", "abandoned", "razed", "demolished", "removed", "construction", "platform", "proposed"))) {
      processOsmNonHighways(sf, features);
    }
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
