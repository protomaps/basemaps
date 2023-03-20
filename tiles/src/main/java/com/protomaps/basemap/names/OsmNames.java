package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;

public class OsmNames {
  public static FeatureCollector.Feature setOsmNames(FeatureCollector.Feature feature, SourceFeature source,
    int minzoom) {
    feature.setAttrWithMinzoom("name", source.getTag("name"), minzoom);
    return feature;
  }
}
