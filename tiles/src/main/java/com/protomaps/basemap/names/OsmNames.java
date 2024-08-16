package com.protomaps.basemap.names;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.text.FontRegistry;
import com.protomaps.basemap.text.TextEngine;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class OsmNames {

  private OsmNames() {}

  private static final String[] ALLOWED_LANGS = new String[]{
    "ar", // Arabic
    "cs", // Czech
    "bg", // Bulgarian
    "da", // Danish
    "de", // German
    "el", // Greek
    "en", // English
    "es", // Spanish
    "et", // Estonian
    "fa", // Persian
    "fi", // Finnish
    "fr", // French
    "ga", // Irish
    "he", // Hebrew
    "hi", // Hindi
    "hr", // Croatian
    "hu", // Hungarian
    "id", // Indonesian
    "it", // Italian
    "ja", // Japanese
    "ko", // Korean
    "lt", // Lithuanian
    "lv", // Latvian
    "ne", // Nepali
    "nl", // Dutch
    "no", // Norwegian
    "mr", // Marathi
    "mt", // Maltese
    "pl", // Polish
    "pt", // Portuguese
    "ro", // Romanian
    "ru", // Russian
    "sk", // Slovak
    "sl", // Slovenian
    "sv", // Swedish
    "tr", // Turkish
    "uk", // Ukrainian
    "ur", // Urdu
    "vi", // Vietnamese
    "zh", // Chinese (General)
    "zh-Hans", // Chinese (Simplified)
    "zh-Hant" // Chinese (Traditional)
  };

  private static final Set<String> ALLOWED_LANG_SET =
    new HashSet<>(Stream.of(ALLOWED_LANGS).map(s -> "name:" + s).toList());

  public static boolean isAllowed(String osmKey) {
    return ALLOWED_LANG_SET.contains(osmKey);
  }

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

      if (isAllowed(key)) {
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
