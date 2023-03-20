package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class PhysicalPoint implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "physical_point";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() && (sf.hasTag("place", "sea", "ocean") || sf.hasTag("natural", "peak"))) {

      // TODO: rank based on ele

      int minzoom = 12;
      if (sf.hasTag("natural", "peak")) {
        minzoom = 13;
      }
      if (sf.hasTag("place", "sea")) {
        minzoom = 3;
      }

      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("place", sf.getString("place"))
        .setAttr("natural", sf.getString("natural"))
        .setAttr("ele", sf.getString("ele"))
        .setZoomRange(minzoom, 15);

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
