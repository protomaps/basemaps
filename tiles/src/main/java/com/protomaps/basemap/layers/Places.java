package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseIntOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.ZoomFunction;
import com.protomaps.basemap.feature.CountryInfos;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.RegionInfos;
import com.protomaps.basemap.names.NeNames;
import com.protomaps.basemap.names.OsmNames;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Places implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "places";
  }

  private final AtomicInteger placeNumber = new AtomicInteger(0);

  // Evaluates place layer sort ordering of inputs into an integer for the sort-key field.
  /*
  static int getSortKey(float min_zoom, int population_rank, long population, String name) {
    return SortKey
            // ORDER BY "min_zoom" ASC NULLS LAST,
            .orderByDouble(min_zoom == null ? 15.0 : min_zoom, 0.0, 15.0, 1)
            // population_rank DESC NULLS LAST,
            .thenByInt(population_rank == null ? 15 : population_rank, 0, 15)
            // population DESC NULLS LAST,
            .thenByLog(population, 1, 1 << (SORT_KEY_BITS - 13) - 1)
            // length(name) ASC
            .thenByInt(name == null ? 0 : name.length(), 0, 31)
            .get();
  }
   */

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var kind_detail = "";

    var theme_min_zoom = 0;
    var theme_max_zoom = 0;
    if (sourceLayer.equals("ne_10m_populated_places")) {
      theme_min_zoom = 1;
      theme_max_zoom = 6;
    }

    // Test for props because of Natural Earth funk
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
          kind_detail = "hamlet";
          break;
        case "Scientific station":
          kind = "locality";
          kind_detail = "scientific_station";
          break;
      }
    }

    var min_zoom = sf.getString("min_zoom") == null ? 10 : (int) Double.parseDouble(sf.getString("min_zoom"));
    int population_rank = sf.getString("rank_max") == null ? 0 : (int) Double.parseDouble(sf.getString("rank_max"));
    if (kind != "") {
      var feat = features.point(this.name())
        .setAttr("name", sf.getString("name"))
        .setAttr("pmap:min_zoom", sf.getLong("min_zoom"))
        .setZoomRange(
          sf.getString("min_zoom") == null ? theme_min_zoom : (int) Double.parseDouble(sf.getString("min_zoom")),
          theme_max_zoom)
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", kind_detail)
        .setAttr("population", parseIntOrNull(sf.getString("pop_max")))
        .setAttr("pmap:population_rank", population_rank)
        .setAttr("wikidata_id", sf.getString("wikidata"))
        .setBufferPixels(128)
        // we set the sort keys so the label grid can be sorted predictably (bonus: tile features also sorted)
        .setSortKey(min_zoom)
        .setPointLabelGridPixelSize(7, 16);

      NeNames.setNeNames(feat, sf, 0);
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() &&
      (sf.hasTag("place", "suburb", "town", "village", "neighbourhood", "quarter", "city", "country", "state",
        "province"))) {
      String kind = "other";
      int min_zoom = 12;
      int max_zoom = 15;
      long population = sf.getString("population") == null ? 0 : parseIntOrNull(sf.getString("population"));
      int population_rank = 0;
      String place = sf.getString("place");

      switch (place) {
        case "country":
          kind = "country";
          var countryInfo = CountryInfos.getByName(sf);
          min_zoom = (int) countryInfo.minZoom();
          max_zoom = (int) countryInfo.maxZoom();
          break;
        case "state":
        case "province":
          kind = "region";
          var regionInfo = RegionInfos.getByName(sf);
          min_zoom = (int) regionInfo.minZoom();
          max_zoom = (int) regionInfo.maxZoom();
          break;
        case "city":
        case "town":
          kind = "locality";
          // TODO: these should be from data join to Natural Earth, and if fail data join then default to 8
          min_zoom = 7;
          max_zoom = 15;
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
          // TODO: these should be from data join to Natural Earth, and if fail data join then default to 8
          min_zoom = 10;
          max_zoom = 15;
          if (population == 0) {
            population = 2000;
          }
          break;
        case "suburb":
          kind = "neighbourhood";
          min_zoom = 11;
          max_zoom = 15;
          break;
        case "quarter":
          kind = "macrohood";
          min_zoom = 10;
          max_zoom = 15;
          break;
        case "neighbourhood":
          kind = "neighbourhood";
          min_zoom = 12;
          max_zoom = 15;
          break;
      }

      int[] pop_breaks = {
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

      for (int i = 0; i < pop_breaks.length; i++) {
        if (population >= pop_breaks[i]) {
          population_rank = pop_breaks.length - i;
          break;
        }
      }

      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", place)
        .setAttr("pmap:min_zoom", min_zoom + 1)
        // Core OSM tags for different kinds of places
        .setAttr("capital", sf.getString("capital"))
        // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
        //                      If an explicate value is needed it should be a kind, or included in kind_detail
        .setAttr("place", sf.getString("place"))
        .setAttr("country_code_iso3166_1_alpha_2", sf.getString("country_code_iso3166_1_alpha_2"))
        .setZoomRange(min_zoom, max_zoom);

      if (population > 0) {
        feat.setAttr("population", population)
          .setAttr("pmap:population_rank", population_rank);

        feat.setSortKey(min_zoom * 1000 + 400 - population_rank * 200 + placeNumber.incrementAndGet());
        //feat.setSortKey(getSortKey("pmap:min_zoom",  "pmap:population_rank", "population", "name"));
      } else {
        feat.setSortKey(min_zoom * 1000);
      }

      OsmNames.setOsmNames(feat, sf, 0);

      // we set the sort keys so the label grid can be sorted predictably (bonus: tile features also sorted)
      feat.setPointLabelGridSizeAndLimit(12, 5, 2);

      // and also whenever you set a label grid size limit, make sure you increase the buffer size so no
      // label grid squares will be the consistent between adjacent tiles
      feat.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 32));
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    // the ordering will match the sortrank for cities

    List<VectorTile.Feature> cities = new ArrayList<>();
    List<VectorTile.Feature> noncities = new ArrayList<>();

    for (VectorTile.Feature item : items) {
      if (item.attrs().get("pmap:kind").equals("locality")) {
        cities.add(item);
      } else {
        noncities.add(item);
      }
    }

    int endIndex = cities.size();
    int startIndex = Math.max(0, endIndex - 64);

    List<VectorTile.Feature> top64 = cities.subList(startIndex, endIndex);

    // This should always be less than 64 now that a label grid is being used
    // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
    for (int i = 0; i < top64.size(); i++) {
      if (top64.size() - i < 16) {
        top64.get(i).attrs().put("pmap:rank", 1);
      } else if (top64.size() - i < 32) {
        top64.get(i).attrs().put("pmap:rank", 2);
      } else if (top64.size() - i < 48) {
        top64.get(i).attrs().put("pmap:rank", 3);
      } else {
        top64.get(i).attrs().put("pmap:rank", 4);
      }
    }

    noncities.addAll(top64);
    return noncities;
  }
}
