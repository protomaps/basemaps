package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class Pois implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "pois";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() && (sf.hasTag("amenity") ||
      sf.hasTag("shop") ||
      sf.hasTag("tourism") ||
      sf.hasTag("railway", "station") ||
      sf.hasTag("office") ||
      sf.hasTag("historic") ||
      sf.hasTag("leisure") ||
      sf.hasTag("craft") ||
      sf.hasTag("sport"))) {

      String kind = "node";
      String tag = "amenity";
      if (sf.hasTag("amenity")) {
        tag = kind = "amenity";
      } else if (sf.hasTag("shop")) {
        tag = kind = "shop";
      } else if (sf.hasTag("tourism")) {
        tag = kind = "tourism";
      } else if (sf.hasTag("office")) {
        tag = kind = "office";
      } else if (sf.hasTag("historic")) {
        tag = kind = "historic";
      } else if (sf.hasTag("leisure")) {
        tag = kind = "leisure";
      } else if (sf.hasTag("craft")) {
        tag = kind = "craft";
      } else if (sf.hasTag("sport")) {
        tag = kind = "sport";
      }
      if (kind != "node") {
        tag = sf.getString(kind);
      }

      var feature = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("amenity", sf.getString("amenity"))
        .setAttr("shop", sf.getString("shop"))
        .setAttr("railway", sf.getString("railway"))
        .setAttr("cuisine", sf.getString("cuisine"))
        .setAttr("religion", sf.getString("religion"))
        .setAttr("tourism", sf.getString("tourism"))
        .setAttr("tourism", sf.getString("sport"))

        // Allows zoom rendering by star rating
        .setAttr("stars", sf.getString("stars"))

        // Allows dynamic icon by generic kind or specific tag
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:tag", tag)

        .setZoomRange(12, 15);

      OsmNames.setOsmNames(feature, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
