package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.TestUtils.assertSubmap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carrotsearch.hppc.LongLongHashMap;
import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.TestUtils;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.stats.Stats;
import com.protomaps.basemap.Basemap;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.QrankDb;
import com.protomaps.basemap.feature.WebsiteQidDb;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

abstract class LayerTest {

  final PlanetilerConfig params = PlanetilerConfig.defaults();
  final Stats stats = Stats.inMemory();
  final FeatureCollector.Factory featureCollectorFactory = new FeatureCollector.Factory(params, stats);

  final CountryCoder countryCoder = CountryCoder.fromJsonString(
    "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"iso1A2\":\"US\",\"nameEn\":\"United States\"},\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-124,47],[-124,25],[-71,25],[-71,47],[-124,47]]]]}}]}");


  final QrankDb qrankDb = new QrankDb(LongLongHashMap.from(
    new long[]{8888, 1165584, 2008530, 168756, 877714},
    new long[]{100000, 140740, 12197, 1604223, 9227}
  ));

  final WebsiteQidDb websiteQidDb = new WebsiteQidDb(Map.of(
    "iflyoak.com", 1165584L, // Oakland Airport Q1165584
    "oaklandzoo.org", 2008530L, // Oakland Zoo Q2008530
    "berkeley.edu", 168756L, // UC Berkeley Q168756
    "museumca.org", 877714L // OMCA Q877714
  ));

  final Basemap profile = new Basemap(qrankDb, websiteQidDb, countryCoder, null, "");

  static void assertFeatures(int zoom, List<Map<String, Object>> expected, Iterable<FeatureCollector.Feature> actual) {
    var expectedList = expected.stream().toList();
    var actualList = StreamSupport.stream(actual.spliterator(), false).toList();
    assertEquals(expectedList.size(), actualList.size(), () -> "size: " + actualList);
    for (int i = 0; i < expectedList.size(); i++) {
      assertSubmap(expectedList.get(i), TestUtils.toMap(actualList.get(i), zoom));
    }
  }

  FeatureCollector process(SourceFeature feature) {
    var collector = featureCollectorFactory.get(feature);
    profile.processFeature(feature, collector);
    return collector;
  }
}
