package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class PhysicalLine implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "physical_line";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && (sf.hasTag("waterway") ||
      sf.hasTag("natural", "strait", "cliff")) && (!sf.hasTag("waterway", "riverbank", "reservoir"))) {
      int min_zoom = 12;
      String kind = "other";
      if (sf.hasTag("waterway")) {
        kind = sf.getString("waterway");
        if (sf.hasTag("waterway", "river")) {
          min_zoom = 9;
        }
      } else if (sf.hasTag("natural")) {
        kind = sf.getString("natural");
      }

      var feat = features.line(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // Core OSM tags for different kinds of places
        // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
        //                      If an explicate value is needed it should bea kind, or included in kind_detail
        .setAttr("waterway", sf.getString("waterway"))
        .setAttr("natural", sf.getString("natural"))
        // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
        //.setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        //.setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 12)
        .setZoomRange(min_zoom, 15);

      // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
      if (sf.hasTag("intermittent", "yes")) {
        feat.setAttr("intermittent", true);
      }

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
      // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
      if (sf.hasTag("bridge") && ! sf.hasTag("bridge", "no")) {
        feat.setAttrWithMinzoom("pmap:level", 1, 12);
      } else if (sf.hasTag("tunnel") && ! sf.hasTag("tunnel", "no")) {
        feat.setAttrWithMinzoom("pmap:level", -1, 12);
      } else {
        feat.setAttrWithMinzoom("pmap:level", 0, 12);
      }

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
