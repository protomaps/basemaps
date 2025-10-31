package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carrotsearch.hppc.LongLongHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

  @ParameterizedTest
  @CsvSource({
    "nonsense,0,-1",
    "station,0,-1",
    "station,25000,12",
    "station,12000,13",
    "aerodrome,5000,-1"
  })
  void testAssignZoom(String kind, int qrank, int expectedZoom) {
    var grading = Map.of(
      "station", new int[][]{{12, 20000}, {13, 10000}, {14, 5000}},
      "aerodrome", new int[][]{{11, 20000}, {12, 10000}}
    );
    assertEquals(expectedZoom == -1 ? Optional.empty() : Optional.of(expectedZoom),
      QrankDb.assignZoom(grading, kind, qrank));
  }
}
