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
    int minZoom) {
    FontRegistry fontRegistry = FontRegistry.getInstance();
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      var key = tag.getKey();
      String value = sf.getTag(key).toString();
      var script = Script.getScript(value);

      if (key.equals("name")) {
        feature.setAttrWithMinzoom("name", value, minZoom);

        if (!script.equals("Latin") && !script.equals("Generic")) {
          feature.setAttrWithMinzoom("pmap:script", script, minZoom);
        }

        String encodedValue = TextEngine.encodeRegisteredScripts(value);
        feature.setAttrWithMinzoom("pmap:pgf:name", encodedValue, minZoom);
      }

      if (key.startsWith("name:")) {
        feature.setAttrWithMinzoom(key, value, minZoom);

        if (fontRegistry.getScripts().contains(script)) {
          String encodedValue = TextEngine.encodeRegisteredScripts(value);
          feature.setAttrWithMinzoom("pmap:pgf:" + key, encodedValue, minZoom);
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
