package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
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
      features.line(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("name", sf.getString("name"))
        .setAttr("waterway", sf.getString("waterway"))
        .setAttr("natural", sf.getString("natural"))
        .setZoomRange(12, 15);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
