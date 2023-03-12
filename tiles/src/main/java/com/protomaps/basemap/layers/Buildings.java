package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.List;

public class Buildings implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "buildings";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() && (sf.hasTag("building"))) {
      Double height = parseDoubleOrNull(sf.getString("height"));
      features.polygon(this.name())
        .setAttrWithMinzoom("name", sf.getString("name"), 13)
        .setAttrWithMinzoom("building:part", sf.getString("building:part"), 13)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 13)
        .setAttrWithMinzoom("height", height, 13)
        .setZoomRange(10, 15);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    if (zoom >= 14)
      return items;
    return FeatureMerge.mergeNearbyPolygons(items, 1, 1, 0.5, 0.5);
  }
}
