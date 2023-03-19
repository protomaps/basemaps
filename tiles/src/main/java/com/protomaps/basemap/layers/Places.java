package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import java.util.List;

public class Places implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "places";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() &&
      (sf.hasTag("place", "suburb", "town", "village", "neighbourhood", "city", "country", "state"))) {
      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("name", sf.getString("name"))
        .setAttr("place", sf.getString("place"));


      if (sf.hasTag("place", "country")) {
        feat.setAttr("pmap:kind", "country")
          .setZoomRange(0, 15);
      } else if (sf.hasTag("place", "state", "province")) {
        feat.setAttr("pmap:kind", "state")
          .setZoomRange(4, 15);
      } else if (sf.hasTag("place", "city")) {
        feat.setAttr("pmap:kind", "city")
          .setZoomRange(4, 15);
        if (sf.getString("population") != null && Parse.parseIntOrNull(sf.getString("population")) > 200000) {
          feat.setAttr("pmap:rank", 1);
        } else {
          feat.setAttr("pmap:rank", 2);
        }
      } else if (sf.hasTag("place", "suburb")) {
        feat.setAttr("pmap:kind", "neighbourhood")
          .setZoomRange(8, 15);
      } else if (sf.hasTag("place", "town")) {
        feat.setAttr("pmap:kind", "neighbourhood")
          .setZoomRange(8, 15);
      } else if (sf.hasTag("place", "village")) {
        feat.setAttr("pmap:kind", "neighbourhood")
          .setZoomRange(10, 15);
      } else {
        feat.setAttr("pmap:kind", "neighbourhood")
          .setZoomRange(12, 15);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
