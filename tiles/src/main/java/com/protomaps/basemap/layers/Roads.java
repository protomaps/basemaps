package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import java.util.*;

public class Roads implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "roads";
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    if (sourceFeature.canBeLine() && sourceFeature.hasTag("highway") &&
      !(sourceFeature.hasTag("highway", "proposed", "construction"))) {
      String highway = sourceFeature.getString("highway");
      var feat = features.line("roads")
        .setId(FeatureId.create(sourceFeature))
        .setMinPixelSize(0)
        .setPixelTolerance(0)
        .setAttr("pmap:level", 0)
        .setAttr("name", sourceFeature.getString("name"));

      if (highway.equals("motorway") || highway.equals("motorway_link")) {
        feat.setAttr("pmap:kind", "highway").setZoomRange(6, 15);
      } else if (highway.equals("trunk") || highway.equals("trunk_link") || highway.equals("primary") ||
        highway.equals("primary_link")) {
        feat.setAttr("pmap:kind", "major_road").setZoomRange(7, 15);
      } else if (highway.equals("secondary") || highway.equals("secondary_link") || highway.equals("tertiary") ||
        highway.equals("tertiary_link")) {
        feat.setAttr("pmap:kind", "medium_road").setZoomRange(9, 15);
      } else if (highway.equals("residential") || highway.equals("service") || highway.equals("unclassified") ||
        highway.equals("road")) {
        feat.setAttr("pmap:kind", "minor_road").setZoomRange(12, 15);
      } else {
        feat.setAttr("pmap:kind", "other").setZoomRange(14, 15);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {

    // items = graphAnalyze(items, "highway", "motorway", "motorway_link");
    // items = graphAnalyze(items, "highway", "trunk", "trunk_link");
    // items = graphAnalyze(items, "highway", "primary", "primary_link");
    // items = graphAnalyze(items, "highway", "secondary", "secondary_link");

    items = FeatureMerge.mergeLineStrings(items,
      0.5, // after merging, remove lines that are still less than 0.5px long
      0.1, // simplify output linestrings using a 0.1px tolerance
      4 // remove any detail more than 4px outside the tile boundary
    );

    return items;
  }
}
