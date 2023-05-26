package com.protomaps.basemap.layers;

import static com.protomaps.basemap.postprocess.LinkSimplify.linkSimplify;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.*;

public class Roads implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "roads";
  }

  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
    if (sourceFeature.canBeLine() && sourceFeature.hasTag("highway") &&
      !(sourceFeature.hasTag("highway", "proposed", "abandoned", "razed", "demolished", "removed", "construction"))) {
      String highway = sourceFeature.getString("highway");
      String shield_text = sourceFeature.getString("ref");
      Integer shield_text_length = (shield_text == null ? null : shield_text.length());
      var feat = features.line("roads")
        .setId(FeatureId.create(sourceFeature))
        .setMinPixelSize(0)
        .setPixelTolerance(0)
        .setAttr("highway", highway)
        .setAttrWithMinzoom("bridge", sourceFeature.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sourceFeature.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sourceFeature.getString("layer"), 12)
        .setAttrWithMinzoom("oneway", sourceFeature.getString("oneway"), 14)
        .setAttr("ref", shield_text);

      if (highway.equals("motorway") || highway.equals("motorway_link")) {
        feat.setAttr("pmap:kind", "highway").setZoomRange(6, 15);

        if (highway.equals("motorway") ) {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 7);
        } else {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 12);
        }

        OsmNames.setOsmNames(feat, sourceFeature, 11);
      } else if (highway.equals("trunk") || highway.equals("trunk_link") || highway.equals("primary") ||
        highway.equals("primary_link")) {
        feat.setAttr("pmap:kind", "major_road").setZoomRange(7, 15);

        if (highway.equals("trunk") ) {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 8);
        } else if (highway.equals("primary") ) {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 10);
        } else if (highway.equals("trunk_link") ) {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 12);
        } else {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 13);
        }

        OsmNames.setOsmNames(feat, sourceFeature, 12);
      } else if (highway.equals("secondary") || highway.equals("secondary_link") || highway.equals("tertiary") ||
        highway.equals("tertiary_link")) {
        feat.setAttr("pmap:kind", "medium_road").setZoomRange(9, 15)
            .setAttrWithMinzoom("ref_length", shield_text_length, 8);

        if (highway.equals("secondary") ) {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 11);
        } else if (highway.equals("tertiary") ) {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 12);
        } else {
          feat.setAttrWithMinzoom("ref_length", shield_text_length, 13);
        }

        OsmNames.setOsmNames(feat, sourceFeature, 13);
      } else if (highway.equals("residential") || highway.equals("service") || highway.equals("unclassified") ||
        highway.equals("road") || highway.equals("raceway")) {
        feat.setAttr("pmap:kind", "minor_road").setZoomRange(12, 15);
        OsmNames.setOsmNames(feat, sourceFeature, 14);
      } else if (sourceFeature.hasTag("highway","pedestrian", "track", "path", "cycleway", "bridleway", "footway", "steps", "corridor")) {
        feat.setAttr("pmap:kind", "path").setZoomRange(12, 15);
        feat.setAttr("pmap:kind_detail", highway).setZoomRange(12, 15);
        OsmNames.setOsmNames(feat, sourceFeature, 14);
      } else {
        feat.setAttr("pmap:kind", "other").setZoomRange(14, 15);
        OsmNames.setOsmNames(feat, sourceFeature, 14);
      }

      if( sourceFeature.hasTag("highway", "motorway_link", "trunk_link", "primary_link", "secondary_link", "tertiary_link" )) {
        feat.setAttr("pmap:link", 1).setZoomRange(12, 15);
      }

      if (sourceFeature.hasTag("bridge", "yes")) {
        feat.setAttrWithMinzoom("pmap:level", 1, 12);
      } else if (sourceFeature.hasTag("tunnel", "yes")) {
        feat.setAttrWithMinzoom("pmap:level", -1, 12);
      } else {
        feat.setAttrWithMinzoom("pmap:level", 0, 12);
      }

      if (sourceFeature.hasTag("network", "US:US")) {
        feat.setAttrWithMinzoom("network", "US:US", 7);
      } else if (sourceFeature.hasTag("network", "US:I")) {
        feat.setAttrWithMinzoom("network", "US:I", 7);
      } else if (sourceFeature.hasTag("network")) {
        feat.setAttrWithMinzoom("network", "other", 7);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {

    items = linkSimplify(items, "highway", "motorway", "motorway_link");
    items = linkSimplify(items, "highway", "trunk", "trunk_link");
    items = linkSimplify(items, "highway", "primary", "primary_link");
    items = linkSimplify(items, "highway", "secondary", "secondary_link");

    for (var item : items) {
      item.attrs().remove("highway");
      if (!item.attrs().containsKey("pmap:level")) {
        item.attrs().put("pmap:level", 0);
      }
    }

    items = FeatureMerge.mergeLineStrings(items,
      0.5, // after merging, remove lines that are still less than 0.5px long
      0.1, // simplify output linestrings using a 0.1px tolerance
      4 // remove any detail more than 4px outside the tile boundary
    );

    return items;
  }
}
