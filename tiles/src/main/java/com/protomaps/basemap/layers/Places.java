package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseIntOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.NeNames;
import com.protomaps.basemap.names.OsmNames;
import java.util.ArrayList;
import java.util.List;

public class Places implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "places";
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var kind_detail = "";

    var theme_min_zoom = 0;
    var theme_max_zoom = 0;
    if (sourceLayer.equals("ne_10m_populated_places")) {
      theme_min_zoom = 1;
      theme_max_zoom = 8;
    }

    // Test for props because of Natural Earth funk
    if (sf.isPoint() && sf.hasTag("featurecla") && sf.hasTag("min_zoom")) {
      switch (sf.getString("featurecla")) {
        case "Admin-0 capital":
        case "Admin-0 capital alt":
        case "Admin-0 region capital":
          kind = "city";
          break;
        case "Admin-1 capital":
        case "Admin-1 region capital":
          kind = "city";
          break;
        case "Populated place":
          kind = "city";
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

    if (kind != "") {
      var feat = features.point(this.name())
        .setAttr("name", sf.getString("name"))
        .setAttr("pmap:min_zoom", sf.getLong("min_zoom"))
        .setZoomRange(
          sf.getString("min_zoom") == null ? theme_min_zoom : (int) Double.parseDouble(sf.getString("min_zoom")),
          theme_max_zoom)
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", kind_detail)
        .setAttr("population", sf.getString("pop_max"))
        .setAttr("population_rank", sf.getString("rank_max"))
        .setAttr("wikidata_id", sf.getString("wikidata"))
        .setBufferPixels(128);

      NeNames.setNeNames(feat, sf, 0);
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() &&
      (sf.hasTag("place", "suburb", "town", "village", "neighbourhood", "city", "country", "state", "province"))) {
      Integer population =
        sf.getString("population") == null ? 0 : parseIntOrNull(sf.getString("population"));
      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("place", sf.getString("place"))
        .setAttr("country_code_iso3166_1_alpha_2", sf.getString("country_code_iso3166_1_alpha_2"))
        .setAttr("capital", sf.getString("capital"));

      OsmNames.setOsmNames(feat, sf, 0);

      if (sf.hasTag("place", "country")) {
        feat.setAttr("pmap:kind", "country")
          .setZoomRange(0, 9);
      } else if (sf.hasTag("place", "state", "province")) {
        feat.setAttr("pmap:kind", "state")
          .setZoomRange(4, 11);
      } else if (sf.hasTag("place", "city")) {
        feat.setAttr("pmap:kind", "city")
          .setZoomRange(8, 15);
        if (population.equals(0)) {
          population = 10000;
        }
      } else if (sf.hasTag("place", "town")) {
        feat.setAttr("pmap:kind", "town")
          .setZoomRange(8, 15);
        if (population.equals(0)) {
          population = 5000;
        }
      } else if (sf.hasTag("place", "village")) {
        feat.setAttr("pmap:kind", "village")
          .setZoomRange(10, 15);
        if (population.equals(0)) {
          population = 2000;
        }
      } else if (sf.hasTag("place", "suburb")) {
        feat.setAttr("pmap:kind", "suburb")
          .setZoomRange(8, 15);
      } else {
        feat.setAttr("pmap:kind", "neighbourhood")
          .setZoomRange(12, 15);
      }

      if (population != null) {
        feat.setAttr("population", population);
        feat.setSortKey((int) Math.log(population));
        // TODO: use label grid
      } else {
        feat.setSortKey(0);
      }

      int population_rank = 0;

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

      feat.setAttr("population_rank", population_rank);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    // the ordering will match the sortrank for cities

    List<VectorTile.Feature> cities = new ArrayList<>();
    List<VectorTile.Feature> noncities = new ArrayList<>();

    for (VectorTile.Feature item : items) {
      if (item.attrs().get("pmap:kind").equals("city") || item.attrs().get("pmap:kind").equals("town") || item.attrs().get("pmap:kind").equals("village") ) {
        cities.add(item);
      } else {
        noncities.add(item);
      }
    }

    int endIndex = cities.size();
    int startIndex = Math.max(0, endIndex - 64);

    List<VectorTile.Feature> top64 = cities.subList(startIndex, endIndex);

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
