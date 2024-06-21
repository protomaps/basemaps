package com.protomaps.basemap.names;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ScriptTest {

  @Test
  void singleScript() {
    assertEquals("Latin", Script.getScript("Berlin"));
    assertEquals("Arabic", Script.getScript("دبي"));
    // Languages using the Cyrillic script include:
    // BELARUSIAN, BULGARIAN, KAZAKH, KYRGYZ, MACEDONIAN, RUSSIAN,
    // SERBIAN, UKRAINIAN
    assertEquals("Cyrillic", Script.getScript("Скопје"));
    // Unicode does not differentiate between Han used to write simplified
    // Chinese, traditional Chinese, Japanese, etc. See Han Unification.
    assertEquals("Han", Script.getScript("北京"));
    assertEquals("Ethiopic", Script.getScript("አዲስ አበባ"));
    assertEquals("Armenian", Script.getScript("Երևան"));
    // Hangul is the Korean alphabet
    assertEquals("Hangul", Script.getScript("서울"));
    assertEquals("Georgian", Script.getScript("თბილისი"));
    assertEquals("Greek", Script.getScript("Αθήνα"));
    assertEquals("Mongolian", Script.getScript("ᠤᠯᠠᠭᠠᠨᠪᠠᠭᠠᠲᠤᠷ"));
    assertEquals("Devanagari", Script.getScript("काठमाडौँ"));
    assertEquals("Hebrew", Script.getScript("תל אביב"));
    assertEquals("Kannada", Script.getScript("ಬೆಂಗಳೂರು"));
    assertEquals("Bengali", Script.getScript("ঢাকা"));
    assertEquals("Myanmar", Script.getScript("ရန်ကုန်"));
    assertEquals("Khmer", Script.getScript("ភ្នំពេញ"));
    assertEquals("Lao", Script.getScript("ປາກເຊ"));
    assertEquals("Malayalam", Script.getScript("കൊല്ലം"));
    assertEquals("Gurmukhi", Script.getScript("ਜਲੰਧਰ"));
    assertEquals("Sinhala", Script.getScript("කොළඹ"));
    assertEquals("Tamil", Script.getScript("கொழும்பு"));
    assertEquals("Telugu", Script.getScript("హైదరాబాద్"));
    assertEquals("Thai", Script.getScript("กรุงเทพมหานคร"));
    // Katakana is used in Japanese
    assertEquals("Katakana", Script.getScript("パリス"));
    // Hiragana is used in Japanese
    assertEquals("Hiragana", Script.getScript("さいたま"));
  }

  @Test
  void nullInput() {
    assertEquals("Generic", Script.getScript(null));
  }

  @Test
  void emptyString() {
    assertEquals("Generic", Script.getScript(""));
  }

  @Test
  void whitespace() {
    assertEquals("Generic", Script.getScript(" "));
  }

  @Test
  void numbers() {
    assertEquals("Generic", Script.getScript("123"));
  }

  @Test
  void punctuation() {
    assertEquals("Generic", Script.getScript("!?-.,"));
  }

  @Test
  void greekNumber() {
    assertEquals("Greek", Script.getScript("Αθήνα 123"));
  }

  @Test
  void hanNumber() {
    assertEquals("Han", Script.getScript("台北101"));
  }

  @Test
  void latinPunctuation() {
    assertEquals("Latin", Script.getScript("Berlin!"));
  }

  @Test
  void nonAsciiLatin() {
    assertEquals("Latin", Script.getScript("Zürich"));
  }

  @Test
  void latinGreek() {
    assertEquals("Mixed", Script.getScript("Berlin Αθήνα"));
  }

  @Test
  void devanagariTamil() {
    assertEquals("Mixed", Script.getScript("काठमाडौँ கொழும்பு"));
  }
}
