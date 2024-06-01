package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.Map;

import com.protomaps.basemap.names.Script;

public class NeNames {

  private NeNames() {}

  public static FeatureCollector.Feature setNeNames(FeatureCollector.Feature feature, SourceFeature sf,
    int minZoom) {
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      var key = tag.getKey();
      if (key.equals("name")) {
        feature.setAttrWithMinzoom(key, sf.getTag(key), minZoom);

        var script = Script.getScript(sf.getTag(key).toString());
        if (!script.equals("LATIN") && !script.equals("GENERIC")) {
          feature.setAttrWithMinzoom("pmap:script", script, minZoom);
        }
      } else if (key.startsWith("name_")) {
        feature.setAttrWithMinzoom(key.replace("_", ":"), sf.getTag(key), minZoom);
      }
    }

    return feature;
  }
}
