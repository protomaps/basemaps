package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.List;

public class Water implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "water";
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    features.polygon(this.name())
      .setZoomRange(6, 15).setBufferPixels(8);
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    if (sourceLayer.equals("ne_110m_ocean") || sourceLayer.equals("ne_110m_lakes")) {
      features.polygon(this.name()).setZoomRange(0, 1);
    } else if (sourceLayer.equals("ne_50m_ocean") || sourceLayer.equals("ne_50m_lakes")) {
      features.polygon(this.name()).setZoomRange(2, 4);
    } else if (sourceLayer.equals("ne_10m_ocean") || sourceLayer.equals("ne_10m_lakes")) {
      features.polygon(this.name()).setZoomRange(5, 5);
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() && (sf.hasTag("water") || sf.hasTag("waterway") || sf.hasTag("natural", "water") ||
      sf.hasTag("landuse", "reservoir", "swimming_pool"))) {
      features.polygon(this.name())
        .setAttr("name", sf.getString("name"))
        .setAttr("water", sf.getString("water"))
        .setZoomRange(6, 15)
        .setBufferPixels(8);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}