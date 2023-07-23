package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.Map;

public class OsmNames {
  public static FeatureCollector.Feature setOsmNames(FeatureCollector.Feature feature, SourceFeature sf,
    int minZoom) {
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      var key = tag.getKey();
      if (key.equals("name") || key.startsWith("name:")) {
        feature.setAttrWithMinzoom(key, sf.getTag(key), minZoom);
      }
    }

    return feature;
  }
}
