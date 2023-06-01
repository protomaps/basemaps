package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.names.OsmNames;
import com.protomaps.basemap.postprocess.Area;
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
      sf.hasTag("landuse", "reservoir") || sf.hasTag("leisure", "swimming_pool"))) {
      var feature = features.polygon(this.name())
        .setAttr("natural", sf.getString("natural"))
        .setAttr("landuse", sf.getString("landuse"))
        .setAttr("leisure", sf.getString("leisure"))
        .setAttr("water", sf.getString("water"))
        .setAttr("waterway", sf.getString("waterway"))
        // Add less common attributes only at higher zooms
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 12)
        .setZoomRange(6, 15)
        .setMinPixelSize(3.0)
        .setBufferPixels(8);

      if (sf.hasTag("intermittent", "yes")) {
        feature.setAttr("intermittent", 1);
      }

      OsmNames.setOsmNames(feature, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    items = Area.addAreaTag(items);
    if (zoom == 15)
      return items;
    int minArea = 400 / (4096 * 4096) * (256 * 256);
    if (zoom == 6)
      minArea = 600 / (4096 * 4096) * (256 * 256);
    else if (zoom <= 5)
      minArea = 800 / (4096 * 4096) * (256 * 256);
    items = Area.filterArea(items, minArea);
    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}
