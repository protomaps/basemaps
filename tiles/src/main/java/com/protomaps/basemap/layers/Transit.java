package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class Transit implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "transit";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    // todo: exclude railway stations, levels
    if (sf.canBeLine() && (sf.hasTag("railway") ||
      sf.hasTag("aerialway", "cable_car") ||
      sf.hasTag("route", "ferry") ||
      sf.hasTag("aeroway", "runway", "taxiway")) &&
      (!sf.hasTag("railway", "abandoned", "construction", "platform", "proposed"))) {

      int minzoom = 11;

      if (sf.hasTag("service", "yard", "siding", "crossover")) {
        minzoom = 13;
      }

      var feature = features.line(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("railway", sf.getString("railway"))
        .setAttr("route", sf.getString("route"))
        .setAttr("aeroway", sf.getString("aeroway"))
        .setAttr("service", sf.getString("service"))
        .setAttr("aerialway", sf.getString("aerialway"))
        .setAttr("network", sf.getString("network"))
        .setAttr("ref", sf.getString("ref"))
        .setAttr("highspeed", sf.getString("highspeed"))
        .setAttr("layer", sf.getString("layer"))
        .setZoomRange(minzoom, 15);

      String kind = "other";
      if (sf.hasTag("aeroway")) {
        kind = "aeroway";
      } else if (sf.hasTag("railway")) {
        kind = "railway";
      } else if (sf.hasTag("ferry")) {
        kind = "ferry";
      } else if (sf.hasTag("aerialway")) {
        kind = "aerialway";
      }

      feature.setAttr("pmap:kind", kind);

      OsmNames.setOsmNames(feature, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
