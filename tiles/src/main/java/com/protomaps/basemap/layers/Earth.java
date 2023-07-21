package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.SpatialFilter.withinBounds;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.List;
import org.locationtech.jts.geom.Envelope;

public class Earth implements ForwardingProfile.FeaturePostProcessor {

  private Envelope bounds;

  public Earth(Envelope bounds) {
    this.bounds = bounds;
  }

  @Override
  public String name() {
    return "earth";
  }

  public void processPreparedOsm(SourceFeature sf, FeatureCollector features) {
    if (withinBounds(this.bounds, sf)) {
      features.polygon(this.name())
        .setAttr("pmap:kind", "earth")
        .setZoomRange(6, 15).setBufferPixels(8);
    }
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    if (sourceLayer.equals("ne_110m_land")) {
      features.polygon(this.name()).setZoomRange(0, 1).setBufferPixels(8).setAttr("pmap:kind", "earth");
    } else if (sourceLayer.equals("ne_50m_land")) {
      features.polygon(this.name()).setZoomRange(2, 4).setBufferPixels(8).setAttr("pmap:kind", "earth");
    } else if (sourceLayer.equals("ne_10m_land")) {
      features.polygon(this.name()).setZoomRange(5, 5).setBufferPixels(8).setAttr("pmap:kind", "earth");
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}
