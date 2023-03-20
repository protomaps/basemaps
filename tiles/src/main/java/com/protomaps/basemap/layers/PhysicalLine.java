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
      var feat = features.line(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("waterway", sf.getString("waterway"))
        .setAttr("natural", sf.getString("natural"))
        .setZoomRange(12, 15);

      String kind = "other";
      if (sf.hasTag("waterway")) {
        kind = "waterway";
      } else if (sf.hasTag("natural")) {
        kind = "natural";
      }

      feat.setAttr("pmap:kind", kind);

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
