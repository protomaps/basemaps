package com.protomaps.basemap.names;

import java.lang.Character.UnicodeScript;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScriptSegmenter {

  private ScriptSegmenter() {}

  public static UnicodeScript getCharScript(char ch) {
    UnicodeScript script = Character.UnicodeScript.of(ch);
    if (script == UnicodeScript.COMMON || script == UnicodeScript.INHERITED || script == UnicodeScript.UNKNOWN) {
      return null;
    }
    return script;
  }

  public static String cleanEndsAndZWSP(String input) {
    String result = input.strip();
    if (result.endsWith("/") ||
      result.endsWith("-") ||
      result.endsWith(";") ||
      result.endsWith("(") ||
      result.endsWith(",")) {
      result = result.substring(0, result.length() - 1);
      result = result.strip();
    }
    if (result.endsWith(")") && !result.contains("(")) {
      result = result.substring(0, result.length() - 1);
      result = result.strip();
    }
    result = result.replace("\u200B", ""); // remove ZWSP
    return result;
  }

  public static boolean hasRepeatedScript(List<String> segments) {

    Set<String> scripts = new HashSet<>();

    for (String segment : segments) {
      String script = Script.getScript(segment);
      if (scripts.contains(script)) {
        return true;
      }
      scripts.add(script);
    }

    return false;
  }

  public static boolean hasLengthOneSegment(List<String> segments) {
    for (String segment : segments) {
      if (segment.length() == 1) {
        return true;
      }
    }
    return false;
  }

  public static boolean shouldSegment(String line) {

    Set<UnicodeScript> scriptsInLine = new HashSet<>();

    if (line.endsWith(" I") ||
      line.endsWith("-I") ||
      line.endsWith(" V") ||
      line.endsWith("-V")) {
      line = line.substring(0, line.length() - 2);
    }
    if (line.endsWith(" II") ||
      line.endsWith("-II") ||
      line.endsWith(" IV") ||
      line.endsWith("-IV") ||
      line.endsWith(" VI") ||
      line.endsWith("-VI")) {
      line = line.substring(0, line.length() - 3);
    }
    if (line.endsWith(" III") ||
      line.endsWith("-III")) {
      line = line.substring(0, line.length() - 4);
    }
    for (char ch : line.toCharArray()) {
      UnicodeScript script = getCharScript(ch);
      if (script != null) {
        scriptsInLine.add(script);
      }
    }

    if (scriptsInLine.size() == 2 && scriptsInLine.contains(UnicodeScript.HAN)) {
      if (scriptsInLine.contains(UnicodeScript.HIRAGANA)) {
        return false;
      }
      if (scriptsInLine.contains(UnicodeScript.KATAKANA)) {
        return false;
      }
    }

    if (scriptsInLine.size() == 3 &&
      scriptsInLine.contains(UnicodeScript.HAN) &&
      scriptsInLine.contains(UnicodeScript.HIRAGANA) &&
      scriptsInLine.contains(UnicodeScript.KATAKANA)) {
      return false;
    }

    return scriptsInLine.size() > 1;
  }

  public static List<String> segmentByScript(String input) {
    List<String> segments = new ArrayList<>();
    if (input == null || input.isEmpty()) {
      return segments;
    }

    if (!shouldSegment(input)) {
      segments.add(input);
      return segments;
    }

    StringBuilder currentSegment = new StringBuilder();
    Character.UnicodeScript currentScript = getCharScript(input.charAt(0));

    for (char ch : input.toCharArray()) {
      Character.UnicodeScript script = getCharScript(ch);

      if (currentScript == null) {
        // handles the start of the string if the first character is not
        // a defined script (UNKNOWN, INHERITED, COMMON)
        currentScript = script;
        currentSegment.append(ch);
      } else {
        if (script == currentScript || script == null) {
          currentSegment.append(ch);
        } else {
          if (!currentSegment.isEmpty()) {
            String segment = cleanEndsAndZWSP(currentSegment.toString());
            if (!segment.isEmpty()) {
              segments.add(segment);
            }
          }
          currentSegment.setLength(0); // Clear the current segment
          currentSegment.append(ch);
          currentScript = script;
        }
      }
    }

    if (!currentSegment.isEmpty()) {
      String segment = cleanEndsAndZWSP(currentSegment.toString());
      if (!segment.isEmpty()) {
        segments.add(segment);
      }
    }

    if (hasRepeatedScript(segments) || hasLengthOneSegment(segments)) {
      segments = new ArrayList<>();
      segments.add(input);
    }

    return segments;
  }
}
