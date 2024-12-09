package com.protomaps.basemap.layers;
import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.onthegomap.planetiler.geo.GeometryType;
import com.protomaps.basemap.feature.NaturalEarthDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Housenumbers  implements ForwardingProfile.LayerPostProcesser {
  private static final Logger LOGGER = LoggerFactory.getLogger(Housenumbers.class);

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (!sf.hasTag("addr:housenumber")) {
      return;
    }
    FeatureCollector.Feature feature = null;
    if (sf.isPoint()) {
      feature = features.point(this.name());
    } else if (sf.canBePolygon()) {
      feature = features.centroid(this.name());
    }
    if (feature != null) {
      feature
        .setId(FeatureId.create(sf))
        .setAttr("housenumber", sf.getString("addr:housenumber"))
        .setAttr("street", sf.getString("addr:street"))
        .setZoomRange(14, 15);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    HashMap<Map<String, Object>, List<VectorTile.Feature>> grouped = new LinkedHashMap<>();
    for (VectorTile.Feature item : items) {
      grouped.computeIfAbsent(item.tags(), k -> new ArrayList<>()).add(item);
    }

    return grouped.values().stream().map(g -> {
      var feature = g.getFirst();
      // drop `addr:street` to save space
      feature.tags().remove("addr:street");
      return feature;
    }).toList();
  }

  @Override
  public String name() {
    return "housenumbers";
  }

}
