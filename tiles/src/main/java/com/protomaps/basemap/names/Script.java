package com.protomaps.basemap.names;

import java.util.ArrayList;
import java.util.List;

public class Script {

  private Script() {}

  public static String getScript(String text) {
    if (text == null || text.length() == 0) {
      return "Generic";
    }

    String overallScript = "";

    for (int i = 0; i < text.length(); ++i) {
      String script = "" + Character.UnicodeScript.of(text.charAt(i));
      if (script.equals("COMMON")) {
        continue;
      }
      if (script.equals("UNKNOWN")) {
        continue;
      }
      if (script.equals("INHERITED")) {
        continue;
      }
      if (overallScript.equals("")) {
        overallScript = script;
      } else {
        if (script.equals(overallScript)) {
          continue;
        } else {
          return "Mixed";
        }
      }
    }

    if (overallScript.equals("")) {
      // all characters are in COMMON or UNKNOWN or INHERITED
      return "Generic";
    }

    String firstLetterCapitalized = overallScript.substring(0, 1).toUpperCase() + overallScript.substring(1).toLowerCase();

    return firstLetterCapitalized;
  }

  public static List<String> segmentByScript(String text) {
    if (text == null || text.isEmpty()) {
        return new ArrayList<>();
    }

    List<String> segments = new ArrayList<>();
    StringBuilder currentSegment = new StringBuilder();
    String currentScript = getScript(Character.toString(text.charAt(0)));
    currentSegment.append(text.charAt(0));

    for (int i = 1; i < text.length(); i++) {
        char charAtI = text.charAt(i);
        String script = getScript(Character.toString(charAtI));

        if (script.equals(currentScript)) {
            currentSegment.append(charAtI);
        } else {
            segments.add(currentSegment.toString());
            currentScript = script;
            currentSegment = new StringBuilder();
            currentSegment.append(charAtI);
        }
    }

    // Append the last segment.
    segments.add(currentSegment.toString());

    return segments;
  }
}
