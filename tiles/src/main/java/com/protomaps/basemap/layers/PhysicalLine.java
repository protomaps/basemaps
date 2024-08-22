package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class PhysicalLine implements ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "physical_line";
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && !sf.canBePolygon() && (sf.hasTag("waterway") ||
      sf.hasTag("natural", "cliff")) && (!sf.hasTag("waterway", "riverbank", "reservoir"))) {
      int minZoom = 12;
      String kind = "other";
      if (sf.hasTag("waterway")) {
        kind = sf.getString("waterway");
        if (sf.hasTag("waterway", "river")) {
          minZoom = 9;
        }
      } else if (sf.hasTag("natural")) {
        kind = sf.getString("natural");
      }

      var feat = features.line(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // Used for client-side label collisions
        .setAttr("pmap:min_zoom", minZoom + 1)
        // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
        //.setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        //.setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setZoomRange(minZoom, 15);

      // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
      if (sf.hasTag("intermittent", "yes")) {
        feat.setAttr("intermittent", true);
      }

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficient
      // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
      if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
        feat.setAttr("pmap:level", 1);
      } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
        feat.setAttr("pmap:level", -1);
      } else if (sf.hasTag("layer", "-6", "-5", "-4", "-3", "-2", "-1")) {
        feat.setAttr("pmap:level", -1);
      } else {
        feat.setAttr("pmap:level", 0);
      }

      // Server sort features so client label collisions are pre-sorted
      feat.setSortKey(minZoom);

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
