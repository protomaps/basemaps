package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseIntOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.SortKey;
import com.onthegomap.planetiler.util.ZoomFunction;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.NaturalEarthDb;
import com.protomaps.basemap.names.NeNames;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Places implements ForwardingProfile.FeaturePostProcessor {

  private NaturalEarthDb naturalEarthDb;

  public Places(NaturalEarthDb naturalEarthDb) {
    this.naturalEarthDb = naturalEarthDb;
  }

  @Override
  public String name() {
    return "places";
  }

  private final AtomicInteger placeNumber = new AtomicInteger(0);

  // Evaluates place layer sort ordering of inputs into an integer for the sort-key field.
  static int getSortKey(double minZoom, int kindRank, int populationRank, long population, String name) {
    return SortKey
      // (nvkelso 20230803) floats with significant single decimal precision
      //                    but results in "Too many possible values"
      // Order ASCENDING (smaller manually curated Natural Earth min_zoom win over larger values, across kinds)
      .orderByInt((int) minZoom, 0, 15)
      // Order ASCENDING (smaller values win, countries then locality then neighbourhood, breaks ties for same minZoom)
      .thenByInt(kindRank, 0, 6)
      // Order DESCENDING (larger values win, San Francisco rank 11 wins over Oakland rank 10)
      .thenByInt(populationRank, 15, 0)
      // Order DESCENDING (larger values win, Millbrea 40k wins over San Bruno 20k, both rank 7)
      .thenByLog(population, 1000000000, 1, 100)
      // Order ASCENDING (shorter strings are better than longer strings for map display and adds predictability)
      .thenByInt(name == null ? 0 : name.length(), 0, 31)
      .get();
  }


  // Offset by 1 here because of 256 versus 512 pixel tile sizes
  // and how the OSM processing assumes 512 tile size (while NE is 256)
  private static final int NE_ZOOM_OFFSET = 1;

  private static final ZoomFunction<Number> LOCALITY_GRID_SIZE_ZOOM_FUNCTION =
    ZoomFunction.fromMaxZoomThresholds(Map.of(
      6, 32,
      7, 64
    ), 0);

  private static final ZoomFunction<Number> LOCALITY_GRID_LIMIT_ZOOM_FUNCTION =
    ZoomFunction.fromMaxZoomThresholds(Map.of(
      6, 8,
      7, 6,
      9, 4
    ), 0);

  /*
  This generates zoom 0 to zoom 6.
   */
  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();

    if (!sourceLayer.equals("ne_10m_populated_places")) {
      return;
    }

    // (nvkelso 20230817) We could omit the rest of this function and rely solely on
    //                    OSM for features (but using the NE attributes above.
    //                    We don't do that because OSM has too many names which
    //                    would bloat low zoom file size. Once OSM name localization
    //                    is configurable the below logic should be removed.

    // Setup low zoom content
    var kind = "";
    var kindDetail = "";

    // Test for props because of Natural Earth funk
    // Test for tz_place because of zoom 0 funk
    if (sf.isPoint() && sf.hasTag("featurecla") && sf.hasTag("min_zoom")) {
      switch (sf.getString("featurecla")) {
        case "Admin-0 capital":
        case "Admin-0 capital alt":
        case "Admin-0 region capital":
          kind = "locality";
          break;
        case "Admin-1 capital":
        case "Admin-1 region capital":
          kind = "locality";
          break;
        case "Populated place":
          kind = "locality";
          break;
        case "Historic place":
          kind = "locality";
          kindDetail = "hamlet";
          break;
        case "Scientific station":
          kind = "locality";
          kindDetail = "scientific_station";
          break;
        default:
          // Important to reset to empty string here
          kind = "";
          break;
      }
    }

    if (!kind.isEmpty()) {
      double minZoom = sf.getString("min_zoom") == null ? 10.0f : Double.parseDouble(sf.getString("min_zoom"));
      int populationRank = sf.getString("rank_max") == null ? 0 : (int) Double.parseDouble(sf.getString("rank_max"));
      int population = parseIntOrNull(sf.getString("pop_max"));

      var feat = features.point(this.name())
        .setAttr("name", sf.getString("name"))
        .setAttr("pmap:min_zoom", minZoom)
        // We subtract 1 to achieve intended compilation balance vis-a-vis 256 zooms in NE and 512 zooms in Planetiler
        .setZoomRange((int) minZoom - 1, 6)
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", kindDetail)
        .setAttr("population", population)
        .setAttr("pmap:population_rank", populationRank)
        // Server sort features so client label collisions are pre-sorted
        // we also set the sort keys so the label grid can be sorted predictably (bonus: tile features also sorted)
        // since all these are locality, we hard code kindRank to 2 (needs to match OSM section below)
        .setSortKey(getSortKey(minZoom, 2, populationRank, population, sf.getString("name")));

      // NOTE: The buffer needs to be consistent with the innteral grid pixel sizes
      feat.setPointLabelGridPixelSize(LOCALITY_GRID_SIZE_ZOOM_FUNCTION)
        .setPointLabelGridLimit(LOCALITY_GRID_LIMIT_ZOOM_FUNCTION)
        .setBufferPixels(64);

      if (sf.hasTag("wikidataid")) {
        feat.setAttr("wikidata", sf.getString("wikidataid"));
      }

      NeNames.setNeNames(feat, sf, 0);
    }
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() && sf.hasTag("name") &&
      (sf.hasTag("place", "suburb", "town", "village", "neighbourhood", "quarter", "city", "country", "state",
        "province"))) {
      String kind = "other";
      int kindRank = 6;

      int themeMinZoom = 7;
      double minZoom = 12.0;
      double maxZoom = 15.0;
      long population = 0;
      if (sf.hasTag("population")) {
        Integer parsed = parseIntOrNull(sf.getString("population"));
        if (parsed != null) {
          population = parsed;
        }
      }

      int populationRank = 0;
      String place = sf.getString("place");

      switch (place) {
        case "country":
          // (nvkelso 20230802) OSM countries are allowlisted to show up at earlier zooms
          //                    TODO: Really these should switch over to NE source
          themeMinZoom = 0;
          kind = "country";
          minZoom = 5.0;
          maxZoom = 8.0;
          var neAdmin0 = naturalEarthDb.getAdmin0ByWikidata(sf.getString("wikidata"));
          if (neAdmin0 != null) {
            minZoom = neAdmin0.minLabel() - NE_ZOOM_OFFSET;
            maxZoom = neAdmin0.maxLabel() - NE_ZOOM_OFFSET;
          }

          kindRank = 0;
          break;
        case "state":
        case "province":
          // (nvkelso 20230802) OSM regions are allowlisted to show up at earlier zooms
          //                    TODO: Really these should switch over to NE source
          themeMinZoom = 0;
          kind = "region";
          minZoom = 8.0;
          maxZoom = 11.0;
          var neAdmin1 = naturalEarthDb.getAdmin1ByWikidata(sf.getString("wikidata"));
          if (neAdmin1 != null) {
            minZoom = neAdmin1.minLabel() - NE_ZOOM_OFFSET;
            maxZoom = neAdmin1.maxLabel() - NE_ZOOM_OFFSET;
          }
          kindRank = 1;
          break;
        case "city":
        case "town":
          kind = "locality";
          // This minZoom can be changed to smaller value in the NE data join step below
          minZoom = 7.0f;
          maxZoom = 15.0f;
          kindRank = 2;
          if (population == 0) {
            if (place.equals("town")) {
              population = 10000;
            } else {
              population = 5000;
            }
          }
          break;
        case "village":
          kind = "locality";
          // This minZoom can be changed to smaller value in the NE data join step below
          minZoom = 10.0f;
          maxZoom = 15.0f;
          kindRank = 3;
          if (population == 0) {
            population = 2000;
          }
          break;
        case "suburb":
          kind = "neighbourhood";
          minZoom = 11.0f;
          maxZoom = 15.0f;
          kindRank = 4;
          break;
        case "quarter":
          kind = "macrohood";
          minZoom = 10.0f;
          maxZoom = 15.0f;
          kindRank = 5;
          break;
        case "neighbourhood":
          kind = "neighbourhood";
          minZoom = 12.0f;
          maxZoom = 15.0f;
          kindRank = 6;
          break;
      }

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
          minZoom = nePopulatedPlace.minZoom() - NE_ZOOM_OFFSET;
          // (nvkelso 20230815) We could set the population value here, too
          //                    But by the OSM zooms the value should be the incorporated value
          //                    While symbology should be for the metro population value
          populationRank = nePopulatedPlace.rankMax();
        }
      }

      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", place)
        .setAttr("pmap:min_zoom", minZoom + 1)
        // Core OSM tags for different kinds of places
        .setAttr("capital", sf.getString("capital"))
        .setAttr("population", population)
        .setAttr("pmap:population_rank", populationRank)
        // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
        //                      If an explicate value is needed it should be a kind, or included in kind_detail
        .setAttr("place", sf.getString("place"))
        // Generally we use NE and low zooms, and OSM at high zooms
        // With exceptions for country and region labels
        .setZoomRange(Math.max((int) minZoom, themeMinZoom), (int) maxZoom);

      // Instead of exporting ISO country_code_iso3166_1_alpha_2 (which are sparse), we export Wikidata IDs
      if (sf.hasTag("wikidata")) {
        feat.setAttr("wikidata", sf.getString("wikidata"));
      }

      //feat.setSortKey(minZoom * 1000 + 400 - populationRank * 200 + placeNumber.incrementAndGet());
      feat.setSortKey(getSortKey(minZoom, kindRank, populationRank, population, sf.getString("name")));

      // We set the sort keys so the label grid can be sorted predictably (bonus: tile features also sorted)
      // NOTE: The buffer needs to be consistent with the innteral grid pixel sizes
      //feat.setPointLabelGridSizeAndLimit(13, 64, 4); // each cell in the 4x4 grid can have 4 items
      feat.setPointLabelGridPixelSize(LOCALITY_GRID_SIZE_ZOOM_FUNCTION)
        .setPointLabelGridLimit(LOCALITY_GRID_LIMIT_ZOOM_FUNCTION)
        .setBufferPixels(64);

      // and also whenever you set a label grid size limit, make sure you increase the buffer size so no
      // label grid squares will be the consistent between adjacent tiles
      feat.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 64));

      OsmNames.setOsmNames(feat, sf, 0);
      OsmNames.setOsmRefs(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
