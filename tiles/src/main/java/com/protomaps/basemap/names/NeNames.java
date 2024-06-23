package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.text.FontRegistry;
import com.protomaps.basemap.text.TextEngine;

import java.util.Map;

public class NeNames {

  private NeNames() {}

  public static FeatureCollector.Feature setNeNames(FeatureCollector.Feature feature, SourceFeature sf,
    int minZoom, FontRegistry fontRegistry) {
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      String key = tag.getKey().toString();
      if (sf.getTag(key) == null) {
        continue;
      }
      String value = sf.getTag(key).toString();
      var script = Script.getScript(value);

      if (key.equals("name") && !script.equals("Latin") && !script.equals("Generic")) {
        feature.setAttrWithMinzoom("pmap:script", script, minZoom);
      }

      if (key.startsWith("name_")) {
        key = key.replace("_", ":");
      }

      if (fontRegistry != null && fontRegistry.getScripts().contains(script)) {
        value = TextEngine.encodeRegisteredScripts(value, fontRegistry);
      }

      feature.setAttrWithMinzoom(key, value, minZoom);
    }

    return feature;
  }
}
