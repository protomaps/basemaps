package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class Transit implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "transit";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && (sf.hasTag("railway") ||
      sf.hasTag("aerialway", "cable_car") ||
      sf.hasTag("route", "ferry") ||
      sf.hasTag("aeroway", "runway", "taxiway")) &&
      (!sf.hasTag("railway", "abandoned", "construction", "platform", "proposed"))) {
      var feature = features.line(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("railway", sf.getString("railway"))
        .setZoomRange(12, 15);

      OsmNames.setOsmNames(feature, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
