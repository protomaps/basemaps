package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.text.FontRegistry;
import com.protomaps.basemap.text.TextEngine;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Map;

public class OsmNames {

  private OsmNames() {}

  public static FeatureCollector.Feature setOsmNames(FeatureCollector.Feature feature, SourceFeature sf,
    int minZoom, FontRegistry fontRegistry) {
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      var key = tag.getKey();
      String value = sf.getTag(key).toString();
      var script = Script.getScript(value);

      // Full names of places (default and translations)
      if (key.equals("name") || key.startsWith("name:")) {
        if (fontRegistry != null && fontRegistry.getScripts().contains(script)) {
          value = TextEngine.encodeRegisteredScripts(value, fontRegistry);
        }
        feature.setAttrWithMinzoom(key, value, minZoom);
      }

      if (key.equals("name")) {
        if (!script.equals("Latin") && !script.equals("Generic")) {
          feature.setAttrWithMinzoom("pmap:script", script, minZoom);
        }
      }
    }
    return feature;
  }

  public static FeatureCollector.Feature setOsmRefs(FeatureCollector.Feature feature, SourceFeature sf,
    int minZoom) {
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      var key = tag.getKey();
      // Short codes (CA not Calif.)
      // (nvkelso 20230801) 58% of state/province nodes have ref values
      if (key.equals("ref") || key.startsWith("ref:")) {
        // Really, they should be short (CA not US-CA)
        if (sf.getString(key).length() < 5) {
          // Really, they should be strings not numbers (CA not 6)
          ParsePosition pos = new ParsePosition(0);
          NumberFormat.getInstance().parse(sf.getString(key), pos);
          if (sf.getString(key).length() != pos.getIndex()) {
            feature.setAttrWithMinzoom(key, sf.getTag(key), minZoom);
          }
        }
      }
    }
    return feature;
  }
}
