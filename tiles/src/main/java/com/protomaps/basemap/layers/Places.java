package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.ArrayList;
import java.util.List;

public class Places implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "places";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() &&
      (sf.hasTag("place", "suburb", "town", "village", "neighbourhood", "city", "country", "state", "province"))) {
      Integer population = sf.getString("population") == null ? 0 : (int)Double.parseDouble(sf.getString("population"));
      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("place", sf.getString("place"))
        .setAttr("country_code_iso3166_1_alpha_2", sf.getString("country_code_iso3166_1_alpha_2"))
        .setAttr("capital", sf.getString("capital"));

      OsmNames.setOsmNames(feat, sf, 0);

      if (sf.hasTag("place", "country")) {
        feat.setAttr("pmap:kind", "country")
          .setZoomRange(0, 15);
      } else if (sf.hasTag("place", "state", "province")) {
        feat.setAttr("pmap:kind", "state")
          .setZoomRange(4, 15);
      } else if (sf.hasTag("place", "city")) {
        feat.setAttr("pmap:kind", "city")
          .setZoomRange(4, 15);
        if ( population.equals(0) ) {
          population = 10000;
        }
      } else if (sf.hasTag("place", "town")) {
        feat.setAttr("pmap:kind", "neighbourhood")
          .setZoomRange(8, 15);
        if ( population.equals(0) ) {
          population = 5000;
        }
      } else if (sf.hasTag("place", "village")) {
        feat.setAttr("pmap:kind", "neighbourhood")
                .setZoomRange(10, 15);
        if ( population.equals(0) ) {
          population = 2000;
        }
      } else if (sf.hasTag("place", "suburb")) {
        feat.setAttr("pmap:kind", "neighbourhood")
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

      Integer population_rank = 0;

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
        if( population >= pop_breaks[i]) {
          population_rank = pop_breaks.length - i;
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
      if (item.attrs().get("pmap:kind").equals("city")) {
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
