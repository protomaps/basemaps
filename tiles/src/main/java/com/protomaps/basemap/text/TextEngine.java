package com.protomaps.basemap.text;

import com.protomaps.basemap.names.Script;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextEngine {

  private TextEngine() {}

  private static final Logger LOGGER = LoggerFactory.getLogger(TextEngine.class);

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

  private static int codepointFromGlyph(Map<String, Integer> encoding, int index, int xOffset, int yOffset,
    int xAdvance, int yAdvance) {

    for (int i = 0; i < deltas.length; ++i) {
      int deltaXOffset = deltas[i][0];
      int deltaXAdvance = deltas[i][1];
      String glyphKey =
        FontRegistry.getGlyphKey(index, xOffset + deltaXOffset, yOffset, xAdvance + deltaXAdvance, yAdvance);
      if (encoding.get(glyphKey) != null) {
        return encoding.get(glyphKey);
      }
    }
    LOGGER.error(
      "Could not find a matching glyph for index = {}, xOffset = {}, yOffset = {}, xAdvance = {}, yAdvance = {}. Inserting an exclamation mark.",
      index, xOffset, yOffset, xAdvance, yAdvance);

    // Unicode decimal 33 == "!"
    // Excalmation mark means did not find a matching codepoint
    return 33;
  }

  public static String encode(String text, Font font, Map<String, Integer> encoding) {
    StringBuilder resultBld = new StringBuilder();

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

      resultBld.append(new StringBuilder().appendCodePoint(codepoint).toString());
    }
    return resultBld.toString();
  }

  public static List<String> segment(String text, List<String> scripts) {
    List<String> segments = new ArrayList<>();

    if (text == null || text.isEmpty()) {
      return segments;
    }

    if (scripts.isEmpty()) {
      return new ArrayList<>(List.of(text));
    }

    StringBuilder innerBld = new StringBuilder();
    for (String script : scripts) {
      innerBld.append("\\p{In" + script + "}");
    }
    String regex = "[" + innerBld.toString() + "]+|[^" + innerBld.toString() + "]+";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      segments.add(matcher.group());
    }

    return segments;
  }

  public static String encodeRegisteredScripts(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    FontRegistry fontRegistry = FontRegistry.getInstance();
    List<String> segments = segment(text, fontRegistry.getScripts());
    StringBuilder encodedTextBld = new StringBuilder();
    for (String segment : segments) {
      String script = Script.getScript(segment);
      if (fontRegistry.getScripts().contains(script)) {
        encodedTextBld
          .append(TextEngine.encode(segment, fontRegistry.getFont(script), fontRegistry.getEncoding(script)));
      } else {
        encodedTextBld.append(segment);
      }
    }
    return encodedTextBld.toString();
  }
}
