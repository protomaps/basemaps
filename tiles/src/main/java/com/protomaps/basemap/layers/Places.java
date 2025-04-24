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

  private static final MultiExpression.Index<Map<String, Object>> index = MultiExpression.ofOrdered(List.of(
    rule(
      with("population"),
      use("population", fromTag("population"))
    ),
    rule(
      with("place", "country"),
      use("kind", "country"),
      use("minZoom", 5),
      use("maxZoom", 8),
      use("kindRank", 0)
    ),
    rule(
      with("""
          place
          state
          province
        """),
      with("""
          _country
          US
          CA
          BR
          IN
          CN
          AU
        """
      ),
      use("kind", "region"),
      use("minZoom", 8),
      use("maxZoom", 11),
      use("kindRank", 1)
    ),
    rule(
      with("""
          place
          city
          town
        """),
      use("kind", "locality"),
      use("minZoom", 7),
      use("maxZoom", 15),
      use("kindRank", 2)
    ),
    rule(
      with("place", "city"),
      without("population"),
      use("population", 5000),
      use("minZoom", 8)
    ),
    rule(
      with("place", "town"),
      without("population"),
      use("population", 10000),
      use("minZoom", 9)
    ),
    rule(
      with("place", "village"),
      use("kind", "locality"),
      use("minZoom", 10),
      use("maxZoom", 15),
      use("kindRank", 3)
    ),
    rule(
      with("place", "village"),
      without("population"),
      use("minZoom", 11),
      use("population", 2000)
    ),
    rule(
      with("place", "locality"),
      use("kind", "locality"),
      use("minZoom", 11),
      use("maxZoom", 15),
      use("kindRank", 4)
    ),
    rule(
      with("place", "locality"),
      without("population"),
      use("minZoom", 12),
      use("population", 1000)
    ),
    rule(
      with("place", "hamlet"),
      use("kind", "locality"),
      use("minZoom", 11),
      use("maxZoom", 15),
      use("kindRank", 5)
    ),
    rule(
      with("place", "hamlet"),
      without("population"),
      use("minZoom", 12),
      use("population", 200)
    ),
    rule(
      with("place", "isolated_dwelling"),
      use("kind", "locality"),
      use("minZoom", 13),
      use("maxZoom", 15),
      use("kindRank", 6)
    ),
    rule(
      with("place", "isolated_dwelling"),
      without("population"),
      use("minZoom", 14),
      use("population", 100)
    ),
    rule(
      with("place", "farm"),
      use("kind", "locality"),
      use("minZoom", 13),
      use("maxZoom", 15),
      use("kindRank", 7)
    ),
    rule(
      with("place", "farm"),
      without("population"),
      use("minZoom", 14),
      use("population", 50)
    ),
    rule(
      with("place", "allotments"),
      use("kind", "locality"),
      use("minZoom", 13),
      use("maxZoom", 15),
      use("kindRank", 8)
    ),
    rule(
      with("place", "allotments"),
      without("population"),
      use("minZoom", 14),
      use("population", 1000)
    ),
    rule(
      with("place", "suburb"),
      use("kind", "neighbourhood"),
      use("minZoom", 11),
      use("maxZoom", 15),
      use("kindRank", 9)
    ),
    rule(
      with("place", "quarter"),
      use("kind", "macrohood"),
      use("minZoom", 10),
      use("maxZoom", 15),
      use("kindRank", 10)
    ),
    rule(
      with("place", "neighbourhood"),
      use("kind", "neighbourhood"),
      use("minZoom", 12),
      use("maxZoom", 15),
      use("kindRank", 11)
    )
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
        sf.setTag("_country", code.get());
      }
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = index.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, "kind", null);
    if (kind == null) {
      return;
    }

    Integer kindRank = getInteger(sf, matches, "kindRank", 6);
    Integer minZoom = getInteger(sf, matches, "minZoom", 12);
    Integer maxZoom = getInteger(sf, matches, "maxZoom", 15);
    Integer population = getInteger(sf, matches, "population", 0);

    int populationRank = 0;

    int[] popBreaks = {
      1000000000,
      100000000,
      50000000,
      20000000,
      10000000,
      5000000,
      1000000,
      500000,
      200000,
      100000,
      50000,
      20000,
      10000,
      5000,
      2000,
      1000,
      200,
      0};

    for (int i = 0; i < popBreaks.length; i++) {
      if (population >= popBreaks[i]) {
        populationRank = popBreaks.length - i;
        break;
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

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
