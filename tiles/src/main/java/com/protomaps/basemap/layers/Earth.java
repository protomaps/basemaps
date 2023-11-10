package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.List;

public class Earth implements ForwardingProfile.FeaturePostProcessor {
  @Override
  public String name() {
    return "earth";
  }

  public void processPreparedOsm(SourceFeature ignoredSf, FeatureCollector features) {
    features.polygon(this.name())
      .setAttr("pmap:kind", "earth")
      .setZoomRange(6, 15).setBufferPixels(8);
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    if (sourceLayer.equals("ne_50m_land")) {
      features.polygon(this.name()).setZoomRange(0, 4).setBufferPixels(8).setAttr("pmap:kind", "earth");
    } else if (sourceLayer.equals("ne_10m_land")) {
      features.polygon(this.name()).setZoomRange(5, 5).setBufferPixels(8).setAttr("pmap:kind", "earth");
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}
