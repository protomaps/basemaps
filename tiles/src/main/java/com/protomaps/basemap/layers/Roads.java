package com.protomaps.basemap.layers;

import static com.protomaps.basemap.postprocess.LinkSimplify.linkSimplify;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.locales.CartographicLocale;
import com.protomaps.basemap.locales.US;
import com.protomaps.basemap.names.OsmNames;
import java.util.*;

public class Roads implements ForwardingProfile.LayerPostProcessor {

  private static final String LAYER_NAME = "roads";

  private static class ProcessHighways {
    private static final List<String> EXCLUDED_HIGHWAY_VALUES = List.of(
      "proposed",
      "abandoned",
      "razed",
      "demolished",
      "removed",
      "construction",
      "elevator"
    );

    private record TagConfig(String tag, String value, String kind, Integer minZoom, Integer minZoomShieldText, Integer minZoomNames) {}

    private static final List<TagConfig> TAG_CONFIGS = List.of(
      new TagConfig("highway", "motorway", "highway", 3, 7, 11),
      new TagConfig("highway", "motorway_link", "highway", 3, 12, 11),
      new TagConfig("highway", "trunk", "major_road", 6, 8, 12),
      new TagConfig("highway", "trunk_link", "major_road", 7, 12, 12),
      new TagConfig("highway", "primary", "major_road", 7, 10, 12),
      new TagConfig("highway", "primary_link", "major_road", 7, 13, 12),
      new TagConfig("highway", "secondary", "major_road", 9, 11, 12),
      new TagConfig("highway", "secondary_link", "major_road", 9, 13, 14),
      new TagConfig("highway", "tertiary", "major_road", 9, 12, 13),
      new TagConfig("highway", "tertiary_link", "major_road", 9, 13, 14),
      new TagConfig("highway", "residential", "minor_road", 12, 12, 14),
      new TagConfig("highway", "service", "minor_road", 13, 12, 14),
      new TagConfig("highway", "unclassified", "minor_road", 12, 12, 14),
      new TagConfig("highway", "road", "minor_road", 12, 12, 14),
      new TagConfig("highway", "raceway", "minor_road", 12, 12, 14),
      new TagConfig("highway", "pedestrian", "path", 12, 12, 14),
      new TagConfig("highway", "track", "path", 12, 12, 14),
      new TagConfig("highway", "path", "path", 13, 12, 14),
      new TagConfig("highway", "cycleway", "path", 13, 12, 14),
      new TagConfig("highway", "bridleway", "path", 13, 12, 14),
      new TagConfig("highway", "footway", "path", 13, 12, 14),
      new TagConfig("highway", "steps", "path", 13, 12, 14),
      new TagConfig("highway", "corridor", "path", 14, 12, 14)
    );

    public static void process(SourceFeature sf, FeatureCollector features, CountryCoder countryCoder, CartographicLocale locale) {
      String highway = sf.getString("highway", "");

      if (highway.equals("") || EXCLUDED_HIGHWAY_VALUES.contains(highway)) {
        return;
      }
    
      String kind = "other";
      String kindDetail = highway;
      int minZoom = 14;
      int minZoomShieldText = 14;
      int minZoomNames = 14;

      for (var tagConfig : TAG_CONFIGS) {
        if (sf.hasTag(tagConfig.tag, tagConfig.value)) {
          kind = tagConfig.kind;
          minZoom = tagConfig.minZoom;
          minZoomShieldText = tagConfig.minZoomShieldText;
          minZoomNames = tagConfig.minZoomNames;
          break;
        }
      }

      if (kind.equals("minor_road") && highway.equals("service") && sf.hasTag("service")) {
        minZoom = 14;
      }
      if (kind.equals("path") && sf.hasTag("footway", "sidewalk", "crossing")) {
        minZoom = 14;
        kindDetail = sf.getString("footway", "");
      }
      if (kind.equals("other")) {
        kindDetail = sf.getString("service", "");
      }

      Shield shield = locale.getShield(sf);
      Integer shieldTextLength = shield.text() == null ? null : shield.text().length();

      var feat = features.line(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", kind)
        .setAttr("kind_detail", kindDetail)
        .inheritAttrFromSource("service")
        // To power better client label collisions
        .setAttr("min_zoom", minZoom + 1)
        .setAttrWithMinzoom("ref", shield.text(), minZoomShieldText)
        .setAttrWithMinzoom("shield_text_length", shieldTextLength, minZoomShieldText)
        .setAttrWithMinzoom("network", shield.network(), minZoomShieldText)
        .setAttrWithMinzoom("oneway", sf.getString("oneway"), 14)
        // `highway` is a temporary attribute that gets removed in the post-process step
        .setAttr("highway", highway)
        .setAttr("sort_rank", 400)
        .setSortKey(minZoom)
        .setMinPixelSize(0)
        .setPixelTolerance(0)
        .setMinZoom(minZoom);

      try {
        var code = countryCoder.getCountryCode(sf.latLonGeometry());
      } catch (Exception e) {
        // do logic based on country code
      }

      if (kindDetail.endsWith("_link")) {
        feat.setAttr("is_link", true);
      }

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
      if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
        feat.setAttrWithMinzoom("is_bridge", true, 12);
      } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
        feat.setAttrWithMinzoom("is_tunnel", true, 12);
      }

      OsmNames.setOsmNames(feat, sf, minZoomNames);
    }
  }

  private static class ProcessNonHighways {
    private static final List<String> EXCLUDED_RAILWAY_VALUES = List.of(
      "abandoned", 
      "razed", 
      "demolished", 
      "removed", 
      "construction", 
      "platform", 
      "proposed");

    private record TagConfig(String tag, String value, String kind, String kindDetail, Integer minZoom) {}

    private static final List<TagConfig> TAG_CONFIGS = List.of(
      new TagConfig("railway", "rail", "rail", "rail", 11),
      new TagConfig("railway", "disused", "rail", "disused", 15),
      new TagConfig("railway", "funicular", "rail", "funicular", 14),
      new TagConfig("railway", "light_rail", "rail", "light_rail", 14),
      new TagConfig("railway", "miniature", "rail", "miniature", 14),
      new TagConfig("railway", "monorail", "rail", "monorail", 14),
      new TagConfig("railway", "narrow_gauge", "rail", "narrow_gauge", 14),
      new TagConfig("railway", "preserved", "rail", "preserved", 14),
      new TagConfig("railway", "subway", "rail", "subway", 14),
      new TagConfig("railway", "tram", "rail", "tram", 14),
      new TagConfig("aeroway", "runnway", "aeroway", "aeroway", 9),
      new TagConfig("aeroway", "taxiway", "aeroway", "aeroway", 10),
      new TagConfig("man_made", "pier", "path", "pier", 13),
      new TagConfig("aerialway", "cable_car", "aerialway", "cable_car", 11),
      new TagConfig("route", "ferry", "ferry", "ferry", 11)
    );
  
    public static void process(SourceFeature sf, FeatureCollector features) {
      String railway = sf.getString("railway", "");

      if (EXCLUDED_RAILWAY_VALUES.contains(railway) || sf.hasTag("building")) {
        // for buildings see https://github.com/protomaps/basemaps/issues/249
        return;
      }

      String kind = null;
      String kindDetail = null;
      Integer minZoom = null;

      boolean hasNoValidTag = true;
      for (var tagConfig : TAG_CONFIGS) {
        if (sf.hasTag(tagConfig.tag, tagConfig.value)) {
          kind = tagConfig.kind;
          kindDetail = tagConfig.kindDetail;
          minZoom = tagConfig.minZoom;
          hasNoValidTag = false;
          break;
        }
      }
      if (hasNoValidTag) {
        return;
      }

      if (sf.hasTag("service", "yard", "siding", "crossover")) {
        minZoom = 13;
      }

      var feature = features.line(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", kind)
        .setAttr("kind_detail", kindDetail)
        // Used for client-side label collisions
        .setAttr("min_zoom", minZoom + 1)
        .inheritAttrFromSource("network")
        .inheritAttrFromSource("ref")
        .inheritAttrFromSource("route")
        .inheritAttrFromSource("service")
        .setAttr("sort_rank", 400)
        .setSortKey(minZoom)
        .setMinZoom(minZoom);

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

      // TODO: (nvkelso 20230623) This should be variable, but 12 is better than 0 for line merging
      OsmNames.setOsmNames(feature, sf, 12);
    }
  }
  
  private CountryCoder countryCoder;

  public Roads(CountryCoder countryCoder) {
    this.countryCoder = countryCoder;
  }

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // Hardcoded to US for now
  private CartographicLocale locale = new US();

  public record Shield(String text, String network) {}

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (!sf.canBeLine()) {
      return;
    }
    ProcessHighways.process(sf, features, countryCoder, locale);
    ProcessNonHighways.process(sf, features);
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
