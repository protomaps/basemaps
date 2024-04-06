package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import java.util.List;

public class Landcover implements ForwardingProfile.FeaturePostProcessor {

  public void processLandcover(SourceFeature sf, FeatureCollector features) {
    String kind = sf.getString("class");
    switch (kind) {
      case "urban":
        kind = "urban_area";
        break;
      case "crop":
        kind = "farmland";
        break;
      case "grass":
        kind = "grassland";
        break;
      case "trees":
        kind = "forest";
        break;
      case "snow":
        kind = "glacier";
        break;
      case "shrub":
        kind = "scrub";
      default:
        break;
    }

    features.polygon(this.name())
      .setId(FeatureId.create(sf))
      .setAttr("pmap:kind", kind)
      .setZoomRange(0, 7)
      .setMinPixelSize(0.0);
  }

  @Override
  public String name() {
    return "landcover";
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return items;
  }
}
