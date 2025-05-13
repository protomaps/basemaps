package com.protomaps.basemap.names;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ScriptSegmenterTest {
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

  @Test
  void segmentByScript() {
    // Mixed Japanese
    assertEquals(1, ScriptSegmenter.segmentByScript("つつじケ丘五丁目").size());
    assertEquals("つつじケ丘五丁目", ScriptSegmenter.segmentByScript("つつじケ丘五丁目").get(0));

    // Typo Greek in Latin
    assertEquals(1, ScriptSegmenter.segmentByScript("Tazaγart").size());
    assertEquals("Tazaγart", ScriptSegmenter.segmentByScript("Tazaγart").get(0));

    // Latin in Cyrillic
    assertEquals(1, ScriptSegmenter.segmentByScript("Æлбортыхъæу").size());
    assertEquals("Æлбортыхъæу", ScriptSegmenter.segmentByScript("Æлбортыхъæу").get(0));

    // Latin Han repeated script
    assertEquals(1, ScriptSegmenter.segmentByScript("第2D區 Area 2D").size());
    assertEquals("第2D區 Area 2D", ScriptSegmenter.segmentByScript("第2D區 Area 2D").get(0));

    // Arabic Latin
    assertEquals(2, ScriptSegmenter.segmentByScript("Quartier 7 / حارة 7").size());
    assertEquals("Quartier 7", ScriptSegmenter.segmentByScript("Quartier 7 / حارة 7").get(0));
    assertEquals("حارة 7", ScriptSegmenter.segmentByScript("Quartier 7 / حارة 7").get(1));

    // Latin Hangul
    assertEquals(2, ScriptSegmenter.segmentByScript("긴계단 (Gingyedan)").size());
    assertEquals("긴계단", ScriptSegmenter.segmentByScript("긴계단 (Gingyedan)").get(0));
    assertEquals("Gingyedan", ScriptSegmenter.segmentByScript("긴계단 (Gingyedan)").get(1));

    // Latin Tifinagh Arabic
    assertEquals(3, ScriptSegmenter.segmentByScript("Casablanca ⴰⵏⴼⴰ الدار البيضاء").size());
    assertEquals("Casablanca", ScriptSegmenter.segmentByScript("Casablanca ⴰⵏⴼⴰ الدار البيضاء").get(0));
    assertEquals("ⴰⵏⴼⴰ", ScriptSegmenter.segmentByScript("Casablanca ⴰⵏⴼⴰ الدار البيضاء").get(1));
    assertEquals("الدار البيضاء", ScriptSegmenter.segmentByScript("Casablanca ⴰⵏⴼⴰ الدار البيضاء").get(2));

  }
}
