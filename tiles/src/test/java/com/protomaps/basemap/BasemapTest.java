package com.protomaps.basemap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BasemapTest {

  @Test
  void integrationTest(@TempDir Path tmpDir) throws Exception {
    Path archivePath = tmpDir.resolve("output.pmtiles");
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "sf-downtown.osm.pbf");
    var osmPath = cwd.resolveSibling(pathFromRoot);

    // comment this out until we can speed up the small builds
    //    Basemap.run(Arguments.of(
    //      "osm_path", osmPath,
    //      "tmp", tmpDir.toString(),
    //      "download", "true",
    //      "output", archivePath.toString()
    //    ));
  }

  @Test
  void testExtractBoundsFromValidGeoParquet() {
    // Test that bounds can be extracted from a valid GeoParquet file
    Path testFile = Path.of("src", "test", "resources", "test-bounds.parquet");

    Optional<String> boundsOpt = Basemap.extractBoundsFromGeoParquet(testFile);

    assertTrue(boundsOpt.isPresent(), "Should extract bounds from valid GeoParquet file");

    String boundsStr = boundsOpt.get();

    // Bounds string should be in format: minX,minY,maxX,maxY
    String[] parts = boundsStr.split(",");
    assertEquals(4, parts.length, "Bounds string should have 4 comma-separated values");

    // Parse the bounds values
    double minX = Double.parseDouble(parts[0]);
    double minY = Double.parseDouble(parts[1]);
    double maxX = Double.parseDouble(parts[2]);
    double maxY = Double.parseDouble(parts[3]);

    // Expected bounds from Alcatraz buildings test data:
    // minx: -122.4241767, miny: 37.8251652, maxx: -122.4214707, maxy: 37.827299
    assertEquals(-122.4241767, minX, 0.0001, "Min X should match");
    assertEquals(37.8251652, minY, 0.0001, "Min Y should match");
    assertEquals(-122.4214707, maxX, 0.0001, "Max X should match");
    assertEquals(37.827299, maxY, 0.0001, "Max Y should match");

    // Verify bounds are valid (minX < maxX, minY < maxY)
    assertTrue(minX < maxX, "Min X should be less than Max X");
    assertTrue(minY < maxY, "Min Y should be less than Max Y");
  }

  @Test
  void testExtractBoundsFromNonExistentFile() {
    // Test that a non-existent file returns empty
    Path nonExistentFile = Path.of("src", "test", "resources", "does-not-exist.parquet");

    Optional<String> boundsOpt = Basemap.extractBoundsFromGeoParquet(nonExistentFile);

    assertFalse(boundsOpt.isPresent(), "Should return empty for non-existent file");
  }

  @Test
  void testExtractBoundsFromInvalidFile(@TempDir Path tmpDir) throws Exception {
    // Test that an invalid (non-parquet) file returns empty
    Path invalidFile = tmpDir.resolve("invalid.parquet");
    java.nio.file.Files.writeString(invalidFile, "This is not a valid parquet file");

    Optional<String> boundsOpt = Basemap.extractBoundsFromGeoParquet(invalidFile);

    assertFalse(boundsOpt.isPresent(), "Should return empty for invalid parquet file");
  }
}
