package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Map;

public class OsmNames {

  private OsmNames() {}

  public static FeatureCollector.Feature setOsmNames(FeatureCollector.Feature feature, SourceFeature sf,
    int minZoom) {
    for (Map.Entry<String, Object> tag : sf.tags().entrySet()) {
      var key = tag.getKey();
      // Full names of places (default and translations)
      if (key.equals("name") || key.startsWith("name:")) {
        feature.setAttrWithMinzoom(key, sf.getTag(key), minZoom);
      }

      if (key.equals("name")) {
        var script = Script.getScript(sf.getTag(key).toString());
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
