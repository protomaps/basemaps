package com.protomaps.basemap.names;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ScriptSegmenterTest {
  @Test
  void getCharScript() {
    // Null
    assertEquals(null, ScriptSegmenter.getCharScript("1".charAt(0)));
    assertEquals(null, ScriptSegmenter.getCharScript(" ".charAt(0)));
    assertEquals(null, ScriptSegmenter.getCharScript("-".charAt(0)));

    // Latin
    assertEquals(Character.UnicodeScript.LATIN, ScriptSegmenter.getCharScript("A".charAt(0)));
  }

  @Test
  void cleanEndsAndZWSP() {
    // remove trailing spaces
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("  Zürich "));

    // remove slash
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich/"));

    // remove slash with whitespace
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich /"));

    // remove minus
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich-"));

    // remove comma
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich,"));

    // remove semicolon
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich;"));

    // remove opening parenthesis
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich("));

    // remove closing parenthesis
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zürich)"));

    // do not remove matched closing parenthesis
    assertEquals("Zürich (ZH)", ScriptSegmenter.cleanEndsAndZWSP("Zürich (ZH)"));

    // remove ZWSP
    assertEquals("Zürich", ScriptSegmenter.cleanEndsAndZWSP("Zür\u200Bich"));
  }

  @Test
  void hasRepeatedScript() {

    // Latin - Latin
    assertEquals(true, ScriptSegmenter.hasRepeatedScript(Arrays.asList("t", "t")));

    // Generic - Latin - Generic
    assertEquals(true, ScriptSegmenter.hasRepeatedScript(Arrays.asList("_", "A", "_")));

    // Latin - Greek - Latin
    assertEquals(true, ScriptSegmenter.hasRepeatedScript(Arrays.asList("t", "τ", "t")));

    // Latin - Greek
    assertEquals(false, ScriptSegmenter.hasRepeatedScript(Arrays.asList("t", "τ")));
  }

  @Test
  void hasLengthOneSegment() {
    assertEquals(true, ScriptSegmenter.hasLengthOneSegment(Arrays.asList("Zür", "i", "ch")));
    assertEquals(false, ScriptSegmenter.hasLengthOneSegment(Arrays.asList("Zürich")));
  }

  @Test
  void shouldSegment() {
    // Cyrillic with Latin numeral
    assertEquals(false, ScriptSegmenter.shouldSegment("Дугинка I"));
    assertEquals(false, ScriptSegmenter.shouldSegment("Дугинка-II"));
    assertEquals(false, ScriptSegmenter.shouldSegment("Дугинка III"));

    // Han and Hiragana
    assertEquals(false, ScriptSegmenter.shouldSegment("さいたま市"));

    // Han and Katakana
    assertEquals(false, ScriptSegmenter.shouldSegment("金冷シ"));

    // Han and Hiragana and Katakana
    assertEquals(false, ScriptSegmenter.shouldSegment("つつじケ丘五丁目"));

    // Latin and Greek
    assertEquals(true, ScriptSegmenter.shouldSegment("Athens Αθήνα"));
  }
}
