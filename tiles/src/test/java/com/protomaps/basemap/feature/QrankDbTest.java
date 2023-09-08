package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carrotsearch.hppc.LongLongHashMap;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class QrankDbTest {
  @Test
  void testParseCsv() throws IOException {
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "qrank_fixture.csv.gz");
    var db = QrankDb.fromCsv(cwd.resolveSibling(pathFromRoot));
    assertEquals(1, db.get(1));
    assertEquals(2, db.get("Q2"));
  }

  @Test
  void testLookup() {
    var hashMap = new LongLongHashMap();
    hashMap.put(1, 1234);
    var db = new QrankDb(hashMap);
    assertEquals(1234, db.get(1));
    assertEquals(1234, db.get("Q1"));
    assertEquals(0, db.get("Q2"));
  }

  @Test
  void testLookupMultiple() {
    var hashMap = new LongLongHashMap();
    hashMap.put(1, 1234);
    var db = new QrankDb(hashMap);
    assertEquals(0, db.get("abcdef"));
    assertEquals(0, db.get("1;Q2"));
  }

  @Test
  void testLookupMalformed() {
    var hashMap = new LongLongHashMap();
    hashMap.put(1, 1234);
    var db = new QrankDb(hashMap);
    assertEquals(0, db.get("abcdef"));
    assertEquals(0, db.get("1"));
    assertEquals(0, db.get("1;Q2"));
  }
}
