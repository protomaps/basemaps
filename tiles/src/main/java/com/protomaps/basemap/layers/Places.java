package com.protomaps.basemap.layers;

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

  // Internal tags used to reference calculated values between matchers
  private static final String KIND = "protomaps-basemaps:kind";
  private static final String KIND_DETAIL = "protomaps-basemaps:kindDetail";
  private static final String KIND_RANK = "protomaps-basemaps:kindRank";
  private static final String POPULATION = "protomaps-basemaps:population";
  private static final String MINZOOM = "protomaps-basemaps:minZoom";
  private static final String MAXZOOM = "protomaps-basemaps:maxZoom";
  private static final String COUNTRY = "protomaps-basemaps:country";
  private static final String UNDEFINED = "protomaps-basemaps:undefined";

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

    rule(use(KIND, UNDEFINED)),
    rule(with("population"), use(POPULATION, fromTag("population"))),

    rule(with("place", "country"), use(KIND, "country")),
    rule(
      with("place", "state", "province"),
      with(COUNTRY, "US", "CA", "BR", "IN", "CN", "AU"),
      use(KIND, "region"),
      use(KIND_RANK, 1) // TODO: move this down to zoomsIndex
    ),
    rule(with("place", "city", "town"), use(KIND, "locality"), use(KIND_DETAIL, fromTag("place"))),
    rule(with("place", "city"), without("population"), use(POPULATION, 5000)),
    rule(with("place", "town"), without("population"), use(POPULATION, 10000)),

    // Neighborhood-scale places

    rule(with("place", "neighbourhood", "suburb"), use(KIND, "neighbourhood")),
    rule(with("place", "suburb"), use(KIND, "neighbourhood"), use(KIND_DETAIL, "suburb")),
    rule(with("place", "quarter"), use(KIND, "macrohood")),

    // Smaller places detailed in OSM but not fully tested for Overture
    // TODO: move these zoom and rank settings down to zoomsIndex

    rule(
      with("place", "village"),
      use(KIND, "locality"),
      use(KIND_DETAIL, fromTag("place")),
      use(KIND_RANK, 3)
    ),
    rule(
      with("place", "village"),
      without("population"),
      use(MINZOOM, 11),
      use(POPULATION, 2000)
    ),
    rule(
      with("place", "locality"),
      use(KIND, "locality"),
      use(MINZOOM, 11),
      use(KIND_RANK, 4)
    ),
    rule(
      with("place", "locality"),
      without("population"),
      use(MINZOOM, 12),
      use(POPULATION, 1000)
    ),
    rule(
      with("place", "hamlet"),
      use(KIND, "locality"),
      use(MINZOOM, 11),
      use(KIND_RANK, 5)
    ),
    rule(
      with("place", "hamlet"),
      without("population"),
      use(MINZOOM, 12),
      use(POPULATION, 200)
    ),
    rule(
      with("place", "isolated_dwelling"),
      use(KIND, "locality"),
      use(MINZOOM, 13),
      use(KIND_RANK, 6)
    ),
    rule(
      with("place", "isolated_dwelling"),
      without("population"),
      use(MINZOOM, 14),
      use(POPULATION, 100)
    ),
    rule(
      with("place", "farm"),
      use(KIND, "locality"),
      use(MINZOOM, 13),
      use(KIND_RANK, 7)
    ),
    rule(
      with("place", "farm"),
      without("population"),
      use(MINZOOM, 14),
      use(POPULATION, 50)
    ),
    rule(
      with("place", "allotments"),
      use(KIND, "locality"),
      use(MINZOOM, 13),
      use(KIND_RANK, 8)
    ),
    rule(
      with("place", "allotments"),
      without("population"),
      use(MINZOOM, 14),
      use(POPULATION, 1000)
    )

  )).index();

  // Overture properties to Protomaps kind mapping

  private static final MultiExpression.Index<Map<String, Object>> overtureKindsIndex =
    MultiExpression.ofOrdered(List.of(
      rule(
        with("subtype", "locality"),
        with("class", "city"),
        use(KIND, "locality"),
        use(KIND_DETAIL, "city")
      ),
      rule(
        with("subtype", "locality"),
        with("class", "town"),
        use(KIND, "locality"),
        use(KIND_DETAIL, "town")
      ),
      rule(
        with("subtype", "macrohood"),
        use(KIND, "macrohood")
      ),
      rule(
        with("subtype", "neighborhood", "microhood"),
        use(KIND, "neighbourhood"),
        use(KIND_DETAIL, "neighbourhood")
      )
    )).index();

  // Protomaps kind/kind_detail to min_zoom/max_zoom/kind_rank mapping

  private static final MultiExpression.Index<Map<String, Object>> zoomsIndex = MultiExpression.ofOrdered(List.of(
    // Top-level defaults
    rule(use(MINZOOM, 12), use(MAXZOOM, 15)),

    rule(with(KIND, "country"), use(KIND_RANK, 0), use(MINZOOM, 5), use(MAXZOOM, 8)),
    rule(with(KIND, "region"), use(MINZOOM, 8), use(MAXZOOM, 11)),

    rule(with(KIND, "locality"), use(MINZOOM, 7)),
    rule(with(KIND, "locality"), with(KIND_DETAIL, "city"), use(KIND_RANK, 2), use(MINZOOM, 8)),
    rule(with(KIND, "locality"), with(KIND_DETAIL, "town"), use(KIND_RANK, 2), use(MINZOOM, 9)),
    rule(with(KIND, "locality"), with(KIND_DETAIL, "village"), use(MINZOOM, 10), use(MAXZOOM, 15)),

    rule(with(KIND, "macrohood"), use(KIND_RANK, 10), use(MINZOOM, 10)),
    rule(with(KIND, "neighbourhood"), use(KIND_RANK, 11), use(MINZOOM, 12)),
    rule(with(KIND, "neighbourhood"), with(KIND_DETAIL, "suburb"), use(KIND_RANK, 9), use(MINZOOM, 12))
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
        sf.setTag(COUNTRY, code.get());
      }
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = osmKindsIndex.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, KIND, UNDEFINED);
    String kindDetail = getString(sf, matches, KIND_DETAIL, "");
    Integer population = getInteger(sf, matches, POPULATION, 0);

    if (UNDEFINED.equals(kind)) {
      return;
    }

    Integer minZoom;
    Integer maxZoom;
    Integer kindRank;

    var sf2 = new Matcher.SourceFeatureWithComputedTags(sf, Map.of(KIND, kind, KIND_DETAIL, kindDetail));
    var zoomMatches = zoomsIndex.getMatches(sf2);
    if (zoomMatches.isEmpty())
      return;

    minZoom = getInteger(sf2, zoomMatches, MINZOOM, 99);
    maxZoom = getInteger(sf2, zoomMatches, MAXZOOM, 99);
    kindRank = getInteger(sf2, zoomMatches, KIND_RANK, 99);

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
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, KIND, UNDEFINED);
    String kindDetail = getString(sf, matches, KIND_DETAIL, "");

    if (UNDEFINED.equals(kind)) {
      return;
    }

    Integer minZoom;
    Integer maxZoom;
    Integer kindRank;

    var sf2 = new Matcher.SourceFeatureWithComputedTags(sf, Map.of(KIND, kind, KIND_DETAIL, kindDetail));
    var zoomMatches = zoomsIndex.getMatches(sf2);
    if (zoomMatches.isEmpty())
      return;

    minZoom = getInteger(sf2, zoomMatches, MINZOOM, 99);
    maxZoom = getInteger(sf2, zoomMatches, MAXZOOM, 99);
    kindRank = getInteger(sf2, zoomMatches, KIND_RANK, 99);

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
