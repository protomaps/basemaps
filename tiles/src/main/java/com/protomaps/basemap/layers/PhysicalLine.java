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
      sf.hasTag("natural", "strait", "cliff")) && (!sf.hasTag("waterway", "riverbank", "reservoir")))
    {
      int min_zoom = 12;
      String kind = "other";
      if (sf.hasTag("waterway")) {
        kind = sf.getString("waterway");
        if (sf.hasTag("waterway","river")) {
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
        .setAttr("waterway", sf.getString("waterway"))
        .setAttr("natural", sf.getString("natural"))
        // Add less common attributes only at higher zooms
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 12)
        .setZoomRange(min_zoom, 15);

      if (sf.hasTag("intermittent", "yes")) {
        feat.setAttr("intermittent", true);
      }

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
