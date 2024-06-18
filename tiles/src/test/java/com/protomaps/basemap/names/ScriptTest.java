package com.protomaps.basemap.names;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScriptTest {

  @Test
  void singleScript() {
    assertEquals("LATIN", Script.getScript("Berlin"));
    assertEquals("ARABIC", Script.getScript("دبي"));
    assertEquals("CYRILLIC", Script.getScript("Скопје"));
    assertEquals("HAN", Script.getScript("北京"));
    assertEquals("ETHIOPIC", Script.getScript("አዲስ አበባ"));
    assertEquals("ARMENIAN", Script.getScript("Երևան"));
    assertEquals("HANGUL", Script.getScript("서울"));
    assertEquals("GEORGIAN", Script.getScript("თბილისი"));
    assertEquals("GREEK", Script.getScript("Αθήνα"));
    assertEquals("MONGOLIAN", Script.getScript("ᠤᠯᠠᠭᠠᠨᠪᠠᠭᠠᠲᠤᠷ"));
    assertEquals("DEVANAGARI", Script.getScript("काठमाडौँ"));
    assertEquals("HEBREW", Script.getScript("תל אביב"));
    assertEquals("KANNADA", Script.getScript("ಬೆಂಗಳೂರು"));
    assertEquals("BENGALI", Script.getScript("ঢাকা"));
    assertEquals("MYANMAR", Script.getScript("ရန်ကုန်"));
    assertEquals("KHMER", Script.getScript("ភ្នំពេញ"));
    assertEquals("LAO", Script.getScript("ປາກເຊ"));
    assertEquals("MALAYALAM", Script.getScript("കൊല്ലം"));
    assertEquals("GURMUKHI", Script.getScript("ਜਲੰਧਰ"));
    assertEquals("SINHALA", Script.getScript("කොළඹ"));
    assertEquals("TAMIL", Script.getScript("கொழும்பு"));
    assertEquals("TELUGU", Script.getScript("హైదరాబాద్"));
    assertEquals("THAI", Script.getScript("กรุงเทพมหานคร"));
  }

  @Test
  void nullInput() {
    assertEquals("GENERIC", Script.getScript(null));
  }

  @Test
  void emptyString() {
    assertEquals("GENERIC", Script.getScript(""));
  }

  @Test
  void whitespace() {
    assertEquals("GENERIC", Script.getScript(" "));
  }

  @Test
  void numbers() {
    assertEquals("GENERIC", Script.getScript("123"));
  }

  @Test
  void punctuation() {
    assertEquals("GENERIC", Script.getScript("!?-.,"));
  }

  @Test
  void greekNumber() {
    assertEquals("GREEK", Script.getScript("Αθήνα 123"));
  }

  @Test
  void latinPunctuation() {
    assertEquals("LATIN", Script.getScript("Berlin!"));
  }

  @Test
  void nonAsciiLatin() {
    assertEquals("LATIN", Script.getScript("Zürich"));
  }

  @Test
  void latinGreek() {
    assertEquals("MIXED", Script.getScript("Berlin Αθήνα"));
  }

  @Test
  void devanagariTamil() {
    assertEquals("MIXED", Script.getScript("काठमाडौँ கொழும்பு"));
  }
}
