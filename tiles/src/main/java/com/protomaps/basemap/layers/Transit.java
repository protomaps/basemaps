package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class Transit implements ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "transit";
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    // todo: exclude railway stations, levels
    if (sf.canBeLine() && (sf.hasTag("railway") ||
      sf.hasTag("aerialway", "cable_car") ||
      sf.hasTag("man_made", "pier") ||
      sf.hasTag("route", "ferry") ||
      sf.hasTag("aeroway", "runway", "taxiway")) &&
      (!sf.hasTag("building") /* see https://github.com/protomaps/basemaps/issues/249 */) &&
      (!sf.hasTag("railway", "abandoned", "razed", "demolished", "removed", "construction", "platform", "proposed"))) {

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
      } else if (sf.hasTag("ferry")) {
        kind = "ferry";
        kindDetail = sf.getString("ferry");
      } else if (sf.hasTag("man_made", "pier")) {
        kind = "pier";
      } else if (sf.hasTag("aerialway")) {
        kind = "aerialway";
        kindDetail = sf.getString("aerialway");
      }

      var feature = features.line(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // Used for client-side label collisions
        .setAttr("pmap:min_zoom", minZoom + 1)
        // Core OSM tags for different kinds of places
        .setAttr("layer", Parse.parseIntOrNull(sf.getString("layer")))
        .setAttr("network", sf.getString("network"))
        .setAttr("ref", sf.getString("ref"))
        .setAttr("route", sf.getString("route"))
        .setAttr("service", sf.getString("service"))
        .setZoomRange(minZoom, 15);

      // Core Tilezen schema properties
      if (!kindDetail.isEmpty()) {
        feature.setAttr("pmap:kind_detail", kindDetail);
      }

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
      // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
      if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
        feature.setAttr("pmap:level", 1);
      } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
        feature.setAttr("pmap:level", -1);
      } else {
        feature.setAttr("pmap:level", 0);
      }

      // Too many small pier lines otherwise
      if (kind.equals("pier")) {
        feature.setMinPixelSize(2);
      }

      // Server sort features so client label collisions are pre-sorted
      feature.setSortKey(minZoom);

      // TODO: (nvkelso 20230623) This should be variable, but 12 is better than 0 for line merging
      OsmNames.setOsmNames(feature, sf, 12);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
