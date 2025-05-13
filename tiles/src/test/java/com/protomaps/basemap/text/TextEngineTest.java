package com.protomaps.basemap.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Font;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TextEngineTest {
  @Test
  void testEncode() {
    FontRegistry fontRegistry = FontRegistry.getInstance();
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "pgf-encoding-fixture.zip");
    String zipFilePath = cwd.resolveSibling(pathFromRoot).toString();
    fontRegistry.setZipFilePath(zipFilePath);

    String name = "NotoSansDevanagari-Regular";
    String version = "1";
    String script = "Devanagari";

    fontRegistry.loadFontBundle(name, version, script);

    String text = "काठमाडौँ";
    Font font = fontRegistry.getFont(script);
    Map<String, Integer> encoding = fontRegistry.getEncoding(script);

    String textEncoded = TextEngine.encode(text, font, encoding);

    assertEquals(63736, textEncoded.codePointAt(0));
    assertEquals(63743, textEncoded.codePointAt(1));
    assertEquals(63644, textEncoded.codePointAt(2));
    assertEquals(63739, textEncoded.codePointAt(3));
    assertEquals(63743, textEncoded.codePointAt(4));
    assertEquals(63700, textEncoded.codePointAt(5));
    assertEquals(63393, textEncoded.codePointAt(6));

  }

  @Test
  void testSegment() {
    FontRegistry fontRegistry = FontRegistry.getInstance();
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "pgf-encoding-fixture.zip");
    String zipFilePath = cwd.resolveSibling(pathFromRoot).toString();
    fontRegistry.setZipFilePath(zipFilePath);

    String name = "NotoSansDevanagari-Regular";
    String version = "1";
    String script = "Devanagari";

    fontRegistry.loadFontBundle(name, version, script);

    List<String> scripts = fontRegistry.getScripts();

    String text = "काठमाडौँ 123";
    List<String> segments = TextEngine.segment(text, scripts);

    assertEquals(2, segments.size());
    assertEquals("काठमाडौँ", segments.get(0));
    assertEquals(" 123", segments.get(1));

    text = "काठमाडौँ काठमाडौँ";
    segments = TextEngine.segment(text, scripts);

    assertEquals(3, segments.size());
    assertEquals("काठमाडौँ", segments.get(0));
    assertEquals(" ", segments.get(1));
    assertEquals("काठमाडौँ", segments.get(2));

    text = "काठमाडौँ తెలుగు Hello! काठमाडौँ";
    segments = TextEngine.segment(text, scripts);

    assertEquals(3, segments.size());
    assertEquals("काठमाडौँ", segments.get(0));
    assertEquals(" తెలుగు Hello! ", segments.get(1));
    assertEquals("काठमाडौँ", segments.get(2));

    text = "తెలుగు Hello!";
    segments = TextEngine.segment(text, scripts);

    assertEquals(1, segments.size());
    assertEquals("తెలుగు Hello!", segments.get(0));

  }
}
