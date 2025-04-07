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
import com.protomaps.basemap.feature.NaturalEarthDb;
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

public class Places implements ForwardingProfile.LayerPostProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Places.class);

  private NaturalEarthDb naturalEarthDb;
  private CountryCoder countryCoder;

  public Places(NaturalEarthDb naturalEarthDb, CountryCoder countryCoder) {
    this.naturalEarthDb = naturalEarthDb;
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

  private record MinMaxZoom(int minZoom, int maxZoom) {}

  private static Map<String, MinMaxZoom> createCountryZooms() {
    Map<String, MinMaxZoom> countryZooms = new HashMap<>();
    String filename = "/country-zooms.csv";
    InputStream inputStream = Places.class.getResourceAsStream(filename);

    if (inputStream == null) {
      LOGGER.error("Places Error: File \"" + filename + "\" not found in resources.");
      return countryZooms;
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      br.readLine(); // skip header
      String line = br.readLine();

      while (line != null) {
        List<String> columns = List.of(line.split(","));
        String wikidata = columns.get(0);
        Integer minZoom = Integer.parseInt(columns.get(1));
        Integer maxZoom = Integer.parseInt(columns.get(2));
        countryZooms.put(wikidata, new MinMaxZoom(minZoom, maxZoom));
        line = br.readLine();
      }

    } catch (IOException e) {
      LOGGER.error("Places Error", e);
    }
    return countryZooms;
  }

  private static final Map<String, MinMaxZoom> COUNTRY_ZOOMS = createCountryZooms();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // private final AtomicInteger placeNumber = new AtomicInteger(0);

  // Evaluates place layer sort ordering of inputs into an integer for the sort-key field.
  static int getSortKey(double minZoom, int kindRank, int populationRank, long population, String name) {
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


  // Offset by 1 here because of 256 versus 512 pixel tile sizes
  // and how the OSM processing assumes 512 tile size (while NE is 256)
  private static final int NE_ZOOM_OFFSET = 1;

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

    if (kind.equals("country") && COUNTRY_ZOOMS.containsKey(sf.getString("wikidata"))) {
      var minMaxZoom = COUNTRY_ZOOMS.get(sf.getString("wikidata"));
      minZoom = minMaxZoom.minZoom();
      maxZoom = minMaxZoom.maxZoom();
    }

    if (kind.equals("region")) {
      var neAdmin1 = naturalEarthDb.getAdmin1ByWikidata(sf.getString("wikidata"));
      if (neAdmin1 != null) {
        minZoom = (int) neAdmin1.minLabel() - NE_ZOOM_OFFSET;
        maxZoom = (int) neAdmin1.maxLabel() - NE_ZOOM_OFFSET;
      }
    }

    // Join OSM locality with nearby NE localities based on Wikidata ID and
    // harvest the min_zoom to achieve consistent label collisions at zoom 7+
    // By this zoom we get OSM points centered in feature better for area labels
    // While NE earlier aspires to be more the downtown area
    //
    // First scope down the NE <> OSM data join (to speed up total build time)
    if (kind.equals("locality")) {
      // We could add more fallback equivalency tests here, but 98% of NE places have a Wikidata ID
      var nePopulatedPlace = naturalEarthDb.getPopulatedPlaceByWikidata(sf.getString("wikidata"));
      if (nePopulatedPlace != null) {
        minZoom = (int) nePopulatedPlace.minZoom() - NE_ZOOM_OFFSET;
        // (nvkelso 20230815) We could set the population value here, too
        //                    But by the OSM zooms the value should be the incorporated value
        //                    While symbology should be for the metro population value
        populationRank = nePopulatedPlace.rankMax();
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
      .setZoomRange((int) minZoom, (int) maxZoom);

    // Instead of exporting ISO country_code_iso3166_1_alpha_2 (which are sparse), we export Wikidata IDs
    if (sf.hasTag("wikidata")) {
      feat.setAttr("wikidata", sf.getString("wikidata"));
    }

    //feat.setSortKey(minZoom * 1000 + 400 - populationRank * 200 + placeNumber.incrementAndGet());
    int sortKey = getSortKey(minZoom, kindRank, populationRank, population, sf.getString("name"));
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
