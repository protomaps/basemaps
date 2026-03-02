package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.atLeast;
import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.without;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.SortKey;
import com.onthegomap.planetiler.util.ZoomFunction;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.Matcher;
import com.protomaps.basemap.names.OsmNames;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("java:S1192")
public class Places implements ForwardingProfile.LayerPostProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Places.class);

  private CountryCoder countryCoder;

  public Places(CountryCoder countryCoder) {
    this.countryCoder = countryCoder;
  }

  public static final String LAYER_NAME = "places";

  private static int[] popBreaks = {
    0,
    200,
    1000,
    2000,
    5000,
    10000,
    20000,
    50000,
    100000,
    200000,
    500000,
    1000000,
    5000000,
    10000000,
    20000000,
    50000000,
    100000000,
    1000000000
  };

  private static final MultiExpression.Index<Map<String, Object>> osmKindsIndex = MultiExpression.ofOrdered(List.of(

    rule(use("pm:kind", "pm:undefined")),
    rule(with("population"), use("pm:population", fromTag("population"))),

    rule(with("place", "country"), use("pm:kind", "country")),
    rule(with("place", "state", "province"), with("pm:country", "US", "CA", "BR", "IN", "CN", "AU"),
      use("pm:kind", "region")),
    rule(with("place", "city", "town"), use("pm:kind", "locality"), use("pm:kindDetail", fromTag("place"))),
    rule(with("place", "city"), without("population"), use("pm:population", 5000)),
    rule(with("place", "town"), without("population"), use("pm:population", 10000)),

    // Neighborhood-scale places

    rule(with("place", "neighbourhood", "suburb"), use("pm:kind", "neighbourhood")),
    rule(with("place", "suburb"), use("pm:kind", "neighbourhood"), use("pm:kindDetail", "suburb")),
    rule(with("place", "quarter"), use("pm:kind", "macrohood")),

    // Smaller places detailed in OSM but not fully tested for Overture

    rule(with("place", "village"), use("pm:kind", "locality"), use("pm:kindDetail", fromTag("place"))),
    rule(with("place", "village"), without("population"), use("pm:population", 2000)),
    rule(with("place", "locality"), use("pm:kind", "locality")),
    rule(with("place", "locality"), without("population"), use("pm:population", 1000)),
    rule(with("place", "hamlet"), use("pm:kind", "locality")),
    rule(with("place", "hamlet"), without("population"), use("pm:population", 200)),
    rule(with("place", "isolated_dwelling"), use("pm:kind", "locality")),
    rule(with("place", "isolated_dwelling"), without("population"), use("pm:population", 100)),
    rule(with("place", "farm"), use("pm:kind", "locality")),
    rule(with("place", "farm"), without("population"), use("pm:population", 50)),
    rule(with("place", "allotments"), use("pm:kind", "locality")),
    rule(with("place", "allotments"), without("population"), use("pm:population", 1000))

  )).index();

  // Overture properties to Protomaps kind mapping

  private static final MultiExpression.Index<Map<String, Object>> overtureKindsIndex =
    MultiExpression.ofOrdered(List.of(

      rule(with("subtype", "locality"), with("class", "city"), use("pm:kind", "locality"),
        use("pm:kindDetail", "city")),
      rule(with("subtype", "locality"), with("class", "town"), use("pm:kind", "locality"),
        use("pm:kindDetail", "town")),
      rule(with("subtype", "macrohood"), use("pm:kind", "macrohood")),
      rule(with("subtype", "neighborhood", "microhood"), use("pm:kind", "neighbourhood"),
        use("pm:kindDetail", "neighbourhood"))

    )).index();

  // Protomaps kind/kind_detail to min_zoom/max_zoom/kind_rank mapping

  private static final MultiExpression.Index<Map<String, Object>> zoomsIndex = MultiExpression.ofOrdered(List.of(
    // Top-level defaults
    rule(use("pm:minzoom", 12), use("pm:maxzoom", 15)),

    rule(with("pm:kind", "country"), use("pm:kindRank", 0), use("pm:minzoom", 5), use("pm:maxzoom", 8)),
    rule(with("pm:kind", "region"), use("pm:minzoom", 8), use("pm:maxzoom", 11)),
    rule(with("pm:kind", "region"), with("pm:country", "US", "CA", "BR", "IN", "CN", "AU"), use("pm:kindRank", 1)),

    rule(with("pm:kind", "locality"), use("pm:kindRank", 4), use("pm:minzoom", 7), use("pm:maxzoom", 15)),
    rule(with("pm:kind", "locality"), atLeast("pm:population", 1000), use("pm:minzoom", 12)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "city"), use("pm:kindRank", 2), use("pm:minzoom", 8)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "town"), use("pm:kindRank", 2), use("pm:minzoom", 9)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "village"), use("pm:kindRank", 3), use("pm:minzoom", 10)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "village"), atLeast("pm:population", 2000),
      use("pm:minzoom", 11)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "hamlet"), use("pm:kindRank", 5), use("pm:minzoom", 11)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "hamlet"), atLeast("pm:population", 200),
      use("pm:minzoom", 12)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "isolated_dwelling"), use("pm:kindRank", 6),
      use("pm:minzoom", 13)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "isolated_dwelling"), atLeast("pm:population", 100),
      use("pm:minzoom", 14)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "farm"), use("pm:kindRank", 7), use("pm:minzoom", 13)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "farm"), atLeast("pm:population", 50),
      use("pm:minzoom", 14)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "allotments"), use("pm:kindRank", 8),
      use("pm:minzoom", 13)),
    rule(with("pm:kind", "locality"), with("pm:kindDetail", "allotments"), atLeast("pm:population", 100),
      use("pm:minzoom", 14)),

    rule(with("pm:kind", "macrohood"), use("pm:kindRank", 10), use("pm:minzoom", 10)),
    rule(with("pm:kind", "neighbourhood"), use("pm:kindRank", 11), use("pm:minzoom", 12)),
    rule(with("pm:kind", "neighbourhood"), with("pm:kindDetail", "suburb"), use("pm:kindRank", 9),
      use("pm:minzoom", 12))
  )).index();

  private record WikidataConfig(int minZoom, int maxZoom, int rankMax) {}

  private static Map<String, WikidataConfig> readWikidataConfigs() {
    Map<String, WikidataConfig> wikidataConfigs = new HashMap<>();
    InputStream inputStream = Places.class.getResourceAsStream("/places.csv");

    if (inputStream == null) {
      LOGGER.error("File places.csv not found in resources.");
      return wikidataConfigs;
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line = br.readLine(); // skip header
      line = br.readLine();

      while (line != null) {
        List<String> columns = List.of(line.split(","));
        String wikidata = columns.get(0);
        Integer minZoom = Integer.parseInt(columns.get(1));
        Integer maxZoom = Integer.parseInt(columns.get(2));
        Integer rankMax = Integer.parseInt(columns.get(3));
        wikidataConfigs.put(wikidata, new WikidataConfig(minZoom, maxZoom, rankMax));
        line = br.readLine();
      }

    } catch (IOException e) {
      LOGGER.error("IOException", e);
    }
    return wikidataConfigs;
  }

  private static final Map<String, WikidataConfig> WIKIDATA_CONFIGS = readWikidataConfigs();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // Evaluates place layer sort ordering of inputs into an integer for the sort-key field.
  static int getSortKey(double minZoom, int kindRank, long population, String name) {
    return SortKey
      // (nvkelso 20230803) floats with significant single decimal precision
      //                    but results in "Too many possible values"
      // Order ASCENDING (smaller manually curated Natural Earth min_zoom win over larger values, across kinds)
      // minZoom is a float with 1 significant digit for manually curated places
      .orderByInt((int) (minZoom * 10), 0, 150)
      // Order ASCENDING (smaller values win, countries then locality then neighbourhood, breaks ties for same minZoom)
      .thenByInt(kindRank, 0, 12)
      // Order DESCENDING (larger values win, San Francisco rank 11 wins over Oakland rank 10)
      // Disabled to allow population log to have larger range
      //.thenByInt(populationRank, 15, 0)
      // Order DESCENDING (larger values win, Millbrea 40k wins over San Bruno 20k, both rank 7)
      .thenByLog(population, 40000000, 1, 100)
      // Order ASCENDING (shorter strings are better than longer strings for map display and adds predictability)
      .thenByInt(name == null ? 0 : name.length(), 0, 31)
      .get();
  }


  private static final ZoomFunction<Number> LOCALITY_GRID_SIZE_ZOOM_FUNCTION =
    ZoomFunction.fromMaxZoomThresholds(Map.of(
      14, 24,
      15, 16
    ), 0);

  private static final ZoomFunction<Number> LOCALITY_GRID_LIMIT_ZOOM_FUNCTION =
    ZoomFunction.fromMaxZoomThresholds(Map.of(
      11, 1,
      14, 2,
      15, 3
    ), 0);

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (!sf.isPoint() || !sf.hasTag("name") || !sf.hasTag("place")) {
      return;
    }

    try {
      Optional<String> code = countryCoder.getCountryCode(sf.latLonGeometry());
      if (code.isPresent()) {
        sf.setTag("pm:country", code.get());
      }
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = osmKindsIndex.getMatches(sf);

    String kind = getString(sf, matches, "pm:kind", "pm:undefined");
    String kindDetail = getString(sf, matches, "pm:kindDetail", "");
    Integer population = getInteger(sf, matches, "pm:population", 0);

    if ("pm:undefined".equals(kind)) {
      return;
    }

    Integer minZoom;
    Integer maxZoom;
    Integer kindRank;

    var sf2 = new Matcher.SourceFeatureWithComputedTags(sf, Map.of("pm:kind", kind, "pm:kindDetail", kindDetail));
    var zoomMatches = zoomsIndex.getMatches(sf2);

    minZoom = getInteger(sf2, zoomMatches, "pm:minzoom", 99);
    maxZoom = getInteger(sf2, zoomMatches, "pm:maxzoom", 99);
    kindRank = getInteger(sf2, zoomMatches, "pm:kindRank", 99);

    int populationRank = 0;

    for (int i = 0; i < popBreaks.length; i++) {
      if (population >= popBreaks[i]) {
        populationRank = i + 1;
      }
    }

    if (WIKIDATA_CONFIGS.containsKey(sf.getString("wikidata"))) {
      var wikidataConfig = WIKIDATA_CONFIGS.get(sf.getString("wikidata"));
      if (kind.equals("country") || kind.equals("region")) {
        minZoom = wikidataConfig.minZoom();
        maxZoom = wikidataConfig.maxZoom();
      }
      if (kind.equals("locality")) {
        minZoom = wikidataConfig.minZoom();
        populationRank = wikidataConfig.rankMax();
      }
    }

    var feat = features.point(this.name())
      .setId(FeatureId.create(sf))
      // Core Tilezen schema properties
      .setAttr("kind", kind)
      .setAttr("kind_detail", sf.getString("place"))
      .setAttr("min_zoom", minZoom + 1)
      // Core OSM tags for different kinds of places
      .setAttr("capital", sf.getString("capital"))
      .setAttr("population", population)
      .setAttr("population_rank", populationRank)
      // Generally we use NE and low zooms, and OSM at high zooms
      // With exceptions for country and region labels
      .setZoomRange(minZoom, maxZoom);

    // Instead of exporting ISO country_code_iso3166_1_alpha_2 (which are sparse), we export Wikidata IDs
    if (sf.hasTag("wikidata")) {
      feat.setAttr("wikidata", sf.getString("wikidata"));
    }

    int sortKey = getSortKey(minZoom, kindRank, population, sf.getString("name"));
    feat.setSortKey(sortKey);
    feat.setAttr("sort_key", sortKey);

    // This is only necessary when prepping for raster renderers
    feat.setBufferPixels(24);

    // We set the sort keys so the label grid can be sorted predictably (bonus: tile features also sorted)
    // NOTE: The buffer needs to be consistent with the innteral grid pixel sizes
    //feat.setPointLabelGridSizeAndLimit(13, 64, 4); // each cell in the 4x4 grid can have 4 items
    feat.setPointLabelGridPixelSize(LOCALITY_GRID_SIZE_ZOOM_FUNCTION)
      .setPointLabelGridLimit(LOCALITY_GRID_LIMIT_ZOOM_FUNCTION);

    // and also whenever you set a label grid size limit, make sure you increase the buffer size so no
    // label grid squares will be the consistent between adjacent tiles
    feat.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 64));

    OsmNames.setOsmNames(feat, sf, 0);
    OsmNames.setOsmRefs(feat, sf, 0);
  }

  public void processOverture(SourceFeature sf, FeatureCollector features) {
    // Filter by theme and type
    if (!"divisions".equals(sf.getString("theme"))) {
      return;
    }

    if (!"division".equals(sf.getString("type"))) {
      return;
    }

    // Must be a point with a name
    if (!sf.isPoint() || !sf.hasTag("names.primary")) {
      return;
    }

    var matches = overtureKindsIndex.getMatches(sf);

    String kind = getString(sf, matches, "pm:kind", "pm:undefined");
    String kindDetail = getString(sf, matches, "pm:kindDetail", "");

    if ("pm:undefined".equals(kind)) {
      return;
    }

    Integer minZoom;
    Integer maxZoom;
    Integer kindRank;

    var sf2 = new Matcher.SourceFeatureWithComputedTags(sf, Map.of("pm:kind", kind, "pm:kindDetail", kindDetail));
    var zoomMatches = zoomsIndex.getMatches(sf2);

    minZoom = getInteger(sf2, zoomMatches, "pm:minzoom", 99);
    maxZoom = getInteger(sf2, zoomMatches, "pm:maxzoom", 99);
    kindRank = getInteger(sf2, zoomMatches, "pm:kindRank", 99);

    // Extract name
    String name = sf.getString("names.primary");

    // Extract population (if available)
    Integer population = 0;
    if (sf.hasTag("population")) {
      Object popValue = sf.getTag("population");
      if (popValue instanceof Number number) {
        population = number.intValue();
      }
    }

    int populationRank = 0;

    for (int i = 0; i < popBreaks.length; i++) {
      if (population >= popBreaks[i]) {
        populationRank = i + 1;
      }
    }

    var feat = features.point(this.name())
      .setAttr("kind", kind)
      .setAttr("name", name)
      .setAttr("min_zoom", minZoom + 1)
      .setAttr("population", population)
      .setAttr("population_rank", populationRank)
      .setZoomRange(minZoom, maxZoom);

    if (kindDetail != null) {
      feat.setAttr("kind_detail", kindDetail);
    }

    int sortKey = getSortKey(minZoom, kindRank, population, name);
    feat.setSortKey(sortKey);
    feat.setAttr("sort_key", sortKey);

    feat.setBufferPixels(24);
    feat.setPointLabelGridPixelSize(LOCALITY_GRID_SIZE_ZOOM_FUNCTION)
      .setPointLabelGridLimit(LOCALITY_GRID_LIMIT_ZOOM_FUNCTION);
    feat.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 64));
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
