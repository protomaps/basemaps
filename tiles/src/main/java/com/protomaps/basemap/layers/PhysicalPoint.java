package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class PhysicalPoint implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "physical_point";
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var alkaline = 0;
    var reservoir = 0;
    var theme_min_zoom = 0;
    var theme_max_zoom = 0;

     if( sourceLayer.equals("ne_10m_lakes")) {
      theme_min_zoom = 5;
      theme_max_zoom = 5;
     }

    switch (sf.getString("featurecla")) {
      case "Alkaline Lake" -> {
        kind = "lake";
        alkaline = 1;
      }
      case "Lake" -> kind = "lake";
      case "Reservoir" -> {
        kind = "lake";
        reservoir = 1;
      }
      case "Playa" -> kind = "playa";
    }

    if (kind != "" && sf.hasTag("min_zoom")) {
      var water_label_position = features.pointOnSurface(this.name())
              .setAttr("pmap:kind", kind)
              .setAttr("pmap:min_zoom", sf.getLong("min_zoom"))
              .setZoomRange(sf.getString("min_zoom") == null ? theme_min_zoom : (int) Double.parseDouble(sf.getString("min_zoom")), theme_max_zoom);
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() && (sf.hasTag("place", "sea", "ocean") || sf.hasTag("natural", "peak"))) {

      // TODO: rank based on ele

      int minzoom = 12;
      if (sf.hasTag("natural", "peak")) {
        minzoom = 13;
      }
      if (sf.hasTag("place", "sea")) {
        minzoom = 3;
      }

      var feat = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("place", sf.getString("place"))
        .setAttr("natural", sf.getString("natural"))
        .setAttr("ele", sf.getString("ele"))
        .setZoomRange(minzoom, 15);

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
