package com.protomaps.basemap.names;

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

    String firstLetterCapitalized =
      overallScript.substring(0, 1).toUpperCase() + overallScript.substring(1).toLowerCase();

    return firstLetterCapitalized;
  }
}
