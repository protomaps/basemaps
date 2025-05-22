package com.protomaps.basemap.names;


public class Script {

  private Script() {}

  public static String getScript(String text) {
    if (text == null || text.isEmpty()) {
      return "Generic";
    }

    String overallScript = "";

    for (int i = 0; i < text.length(); ++i) {
      String script = "" + Character.UnicodeScript.of(text.charAt(i));
      if (script.equals("COMMON") || script.equals("UNKNOWN") || script.equals("INHERITED")) {
        continue;
      }
      if (overallScript.equals("")) {
        overallScript = script;
      } else {
        if (!script.equals(overallScript)) {
          for (int j = 0; j < text.length(); ++j) {
            Character.UnicodeScript unicodeScript = Character.UnicodeScript.of(text.charAt(j));
            if (unicodeScript.equals(Character.UnicodeScript.KATAKANA) ||
              unicodeScript.equals(Character.UnicodeScript.HIRAGANA)) {
              return "Mixed-Japanese";
            }
          }
          return "Mixed";
        }
      }
    }

    if (overallScript.isEmpty()) {
      // all characters are in COMMON or UNKNOWN or INHERITED
      return "Generic";
    }

    return overallScript.substring(0, 1).toUpperCase() + overallScript.substring(1).toLowerCase();
  }
}
