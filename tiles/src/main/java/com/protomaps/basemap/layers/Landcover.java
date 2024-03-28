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
    if (kind.equals("urban"))
      kind = "urban_area";
    if (kind.equals("crop"))
      kind = "farmland";
    if (kind.equals("grass"))
      kind = "grassland";
    if (kind.equals("trees"))
      kind = "forest";
    if (kind.equals("snow"))
      kind = "glacier";
    if (kind.equals("shrub"))
      kind = "scrub";
    // barren is passed through

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
