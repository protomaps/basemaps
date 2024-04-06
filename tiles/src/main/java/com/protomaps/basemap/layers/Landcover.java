package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import java.util.List;
import java.util.Map;

public class Landcover implements ForwardingProfile.FeaturePostProcessor {

  static final Map<String, String> kindMapping = Map.of("urban", "urban_area", "crop", "farmland", "grass", "grassland",
    "trees", "forest", "snow", "glacier", "shrub", "scrub", "barren", "barren");

  static final Map<String, Integer> sortKeyMapping = Map.of("barren",0, "snow", 1, "crop", 2, "shrub", 3, "grass", 4, "forest", 5);

  public void processLandcover(SourceFeature sf, FeatureCollector features) {
    String kind = kindMapping.getOrDefault(sf.getString("class"), "unknown");

    // polygons are disjoint and non-overlapping, but order them in archive in consistent way
    Integer sortKey = sortKeyMapping.getOrDefault(sf.getString("class"), 6);

    features.polygon(this.name())
      .setId(FeatureId.create(sf))
      .setAttr("pmap:kind", kind)
      .setZoomRange(0, 7)
      .setSortKey(sortKey)
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
