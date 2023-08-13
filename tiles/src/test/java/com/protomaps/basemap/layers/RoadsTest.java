package com.protomaps.basemap.layers;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.stats.Stats;
import com.protomaps.basemap.Basemap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoadsTest {

  final PlanetilerConfig params = PlanetilerConfig.defaults();
  final Stats stats = Stats.inMemory();
  final FeatureCollector.Factory featureCollectorFactory = new FeatureCollector.Factory(params, stats);
  final Basemap profile = new Basemap(null);

  @Test
  void layersTest(@TempDir Path tmpDir) throws Exception {
    var feature = SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      new HashMap<>(Map.of(
        "layer", "1",
        "highway", "motorway"
      )),
      "osm",
      null,
      0
    );
    var collector = featureCollectorFactory.get(feature);
    profile.processFeature(feature, collector);

    for (var x : collector) {
      assertEquals(1,x.getAttrsAtZoom(12).get("layer"));
    }
  }
}
