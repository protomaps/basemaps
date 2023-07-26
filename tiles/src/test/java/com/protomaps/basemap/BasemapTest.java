package com.protomaps.basemap;

import com.onthegomap.planetiler.config.Arguments;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BasemapTest {

  @Test
  void integrationTest(@TempDir Path tmpDir) throws Exception {
    Path archivePath = tmpDir.resolve("output.pmtiles");
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "sf-downtown.osm.pbf");
    var osmPath = cwd.resolveSibling(pathFromRoot);

    Basemap.run(Arguments.of(
      "osm_path", osmPath,
      "tmp", tmpDir.toString(),
      "download", "true",
      "output", archivePath.toString()
    ));
  }
}
