package com.protomaps.basemap.feature;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WebsiteQidDbTest {

  private WebsiteQidDb dbFromFixture() throws IOException, URISyntaxException {
    var resource = getClass().getClassLoader().getResource("website_qid_fixture.csv.gz");
    assertNotNull(resource, "Test fixture not found: website_qid_fixture.csv.gz");
    return WebsiteQidDb.fromCsv(Path.of(resource.toURI()));
  }

  @Test
  void parsesFixtureCsv() throws IOException, URISyntaxException {
    var db = dbFromFixture();
    assertEquals("Q2008530", db.getQid("http://www.oaklandzoo.org/"));
    assertEquals("Q877714", db.getQid("https://museumca.org/"));
  }

  @Test
  void stripsHttps() throws IOException, URISyntaxException {
    var db = dbFromFixture();
    assertEquals("Q2008530", db.getQid("https://oaklandzoo.org/"));
  }

  @Test
  void stripsHttp() throws IOException, URISyntaxException {
    var db = dbFromFixture();
    assertEquals("Q2008530", db.getQid("http://oaklandzoo.org/"));
  }

  @Test
  void stripsWww() throws IOException, URISyntaxException {
    var db = dbFromFixture();
    assertEquals("Q2008530", db.getQid("http://www.oaklandzoo.org/"));
  }

  @Test
  void stripsPath() throws IOException, URISyntaxException {
    var db = dbFromFixture();
    assertEquals("Q877714", db.getQid("https://museumca.org/visit/hours"));
  }

  @Test
  void missingDomainReturnsNull() throws IOException, URISyntaxException {
    var db = dbFromFixture();
    assertNull(db.getQid("https://example.com/"));
  }

  @Test
  void nullUrlReturnsNull() {
    var db = new WebsiteQidDb(Map.of());
    assertNull(db.getQid(null));
  }

  @Test
  void emptyUrlReturnsNull() {
    var db = new WebsiteQidDb(Map.of());
    assertNull(db.getQid(""));
  }

  @Test
  void inMemoryConstructor() {
    var db = new WebsiteQidDb(Map.of("iflyoak.com", 1165584L));
    assertEquals("Q1165584", db.getQid("https://www.iflyoak.com/flights"));
  }
}
