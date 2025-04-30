package com.protomaps.basemap.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FontRegistryTest {
  @Test
  void testLoadFontBundle() {
    FontRegistry fontRegistry = FontRegistry.getInstance();
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "pgf-encoding-fixture.zip");
    String zipFilePath = cwd.resolveSibling(pathFromRoot).toString();
    fontRegistry.setZipFilePath(zipFilePath);

    String name = "NotoSansDevanagari-Regular";
    String version = "1";
    String script = "Devanagari";

    fontRegistry.loadFontBundle(name, version, script);

    assertEquals(name, fontRegistry.getName(script));
    assertEquals(version, fontRegistry.getVersion(script));

    List<String> scripts = fontRegistry.getScripts();
    assertEquals(1, scripts.size());
    assertEquals(script, scripts.get(0));

    String glyphKey = FontRegistry.getGlyphKey(66, 0, 0, 4, 0);
    assertEquals("66|0|0|4|0", glyphKey);

    Map<String, Integer> encoding = fontRegistry.getEncoding(script);
    assertEquals(63743, encoding.get(glyphKey));

  }
}
