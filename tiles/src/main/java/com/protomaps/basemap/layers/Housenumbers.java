package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;

import java.util.List;

public class Housenumbers  implements ForwardingProfile.LayerPostProcesser {
  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.hasTag("addr:housenumber")) {
      if (sf.isPoint()) {
        features.point(this.name())
          .setId(FeatureId.create(sf))
          .setAttr("housenumber", sf.getString("addr:housenumber"))
          .setZoomRange(10, 17);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return items;
  }

  @Override
  public String name() {
    return "housenumbers";
  }

}
