package com.protomaps.basemap.text;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.GlyphMetrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.protomaps.basemap.names.Script;


public class TextEngine {

  private static Integer[][] deltas = new Integer[][]{
    {0, 0},
    {-1, 0},
    {0, 1},
    {0, -1},
    {1, 0},
    {-2, 0},
    {-1, 1},
    {-1, -1},
    {0, 2},
    {0, -2},
    {1, 1},
    {1, -1},
    {2, 0},
    {-3, 0},
    {-2, 1},
    {-2, -1},
    {-1, 2},
    {-1, -2},
    {0, 3},
    {0, -3},
    {1, 2},
    {1, -2},
    {2, 1},
    {2, -1},
    {3, 0},
    {-4, 0},
    {-3, 1},
    {-3, -1},
    {-2, 2},
    {-2, -2},
    {-1, 3},
    {-1, -3},
    {0, 4},
    {0, -4},
    {1, 3},
    {1, -3},
    {2, 2},
    {2, -2},
    {3, 1},
    {3, -1},
    {4, 0},
  };

  private static int codepointFromGlyph(HashMap<String, Integer> encoding, int index, int xOffset, int yOffset, int xAdvance, int yAdvance) {

    for (int i = 0; i < deltas.length; ++i) {
      int deltaXOffset = deltas[i][0];
      int deltaXAdvance = deltas[i][1];
      String glyphKey = FontRegistry.getGlyphKey(index, xOffset + deltaXOffset, yOffset, xAdvance + deltaXAdvance, yAdvance);
      if (encoding.get(glyphKey) != null) {
        return encoding.get(glyphKey);
      }
    }
    // TODO: handle error
    System.out.println("Could not find a matching glyph for index = " + index +
      ", xOffset = " + xOffset +
      ", yOffset = " + yOffset +
      ", xAdvance = " + xAdvance +
      ", yAdvance = " + yAdvance + ". Aborting...");
    System.exit(1);
    return 0; // did not find any matching codepoint
  }

  public static String encode(String text, Font font, HashMap<String, Integer> encoding) {
    String result = "";

    FontRenderContext frc = new FontRenderContext(null, true, true);
    char[] charArray = text.toCharArray();
    GlyphVector glyphVector = font.layoutGlyphVector(frc, charArray, 0, charArray.length, 0);

    float sumXAdvances = 0;

    for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {
      GlyphMetrics glyphMetrics = glyphVector.getGlyphMetrics(i);
      int glyphCode = glyphVector.getGlyphCode(i);

      double xAdvance = glyphMetrics.getAdvanceX();
      double xPosition = glyphVector.getGlyphPosition(i).getX();
      double xOffset = xPosition - sumXAdvances;

      int xAdvanceML = (int) Math.floor(1000.0 * xAdvance / 64.0);
      int xOffsetML = (int) Math.floor(1000.0 * xOffset / 64.0);

      int yAdvanceML = 0;
      int yOffsetML = 0;

      int codepoint = codepointFromGlyph(encoding, glyphCode, xOffsetML, yOffsetML, xAdvanceML, yAdvanceML);

      sumXAdvances += xAdvance;

      result += new StringBuilder().appendCodePoint(codepoint).toString();
    }
    return result;
  }

  public static List<String> segment(String text, List<String> scripts) {
    List<String> segments = new ArrayList<>();

    if (text == null || text.isEmpty()) {
      return segments;
    }

    String inner = "";
    for (String script : scripts) {
      inner += "\\p{In" + script + "}";
    }
    String regex = "[" + inner + "]+|[^" + inner + "]+";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      segments.add(matcher.group());
    }

    return segments;
  }

  public static String encodeRegisteredScripts(String text, FontRegistry fontRegistry) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    List<String> segments = segment(text, fontRegistry.getScripts());
    String encodedText = "";
    for (String segment : segments) {
      String script = Script.getScript(segment);
      if (fontRegistry.getScripts().contains(script)) {
        encodedText += TextEngine.encode(segment, fontRegistry.getFont(script), fontRegistry.getEncoding(script));
      }
      else {
        encodedText += segment;
      }
    }
    return encodedText;
  }

  public static void main(String[] args) {
    String pgfEncodingRepoHash = "e9c03fb";
    FontRegistry fontRegistry = new FontRegistry(pgfEncodingRepoHash);

    String name = "NotoSansDevanagari-Regular";
    String version = "1";
    String script = "Devanagari";

    fontRegistry.loadFontBundle(name, version, script);

    String text = "काठमाडौँ";
    Font font = fontRegistry.getFont(script);
    HashMap<String, Integer> encoding = fontRegistry.getEncoding(script);

    String textEncoded = TextEngine.encode(text, font, encoding);
    System.out.println(textEncoded);

    for (int i = 0; i < textEncoded.length(); i++) {
        char character = textEncoded.charAt(i);
        int charCode = (int) character;
        System.out.println("Character: " + character + ", ASCII: " + charCode);
    }

    text = "काठमाडौँ ఇది काठमाडौँ తెలుగు hallo";
    List<String> scripts = new ArrayList<>(fontRegistry.getScripts());

    List<String> segments = segment(text, scripts);

    for (String segment : segments) {
        System.out.println(segment);
        System.out.println(Script.getScript(segment));
    }
  }
}
