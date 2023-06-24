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
      String kind = "other";
      String kind_detail = "";
      int min_zoom = 15;
      int max_zoom = 15;
      int min_zoom_shield_text = 10;
      int min_zoom_names = 14;

      String highway = sourceFeature.getString("highway");
      String service = "";
      String shield_text = sourceFeature.getString("ref");
      String network_val = sourceFeature.getString("network");
      shield_text = (shield_text == null ? null : shield_text.split(";")[0]);
      if (shield_text != null) {
        if (shield_text.contains("US ")) {
          shield_text = shield_text.replaceAll("US ", "");
          network_val = "US:US";
        } else if (shield_text.contains("I ")) {
          shield_text = shield_text.replaceAll("I ", "");
          network_val = "US:I";
        } else {
          // This should be replaced by walking the way's relations (which reliably set network)
          network_val = "other";
        }
      }
      shield_text = (shield_text == null ? null : shield_text.replaceAll("\\s", ""));
      Integer shield_text_length = (shield_text == null ? null : shield_text.length());

      if (highway.equals("motorway") || highway.equals("motorway_link")) {
        // TODO: (nvkelso 20230622) Use Natural Earth for low zoom roads at zoom 5 and earlier
        //       as normally OSM roads would start at 6, but we start at 3 to match Protomaps v2
        kind = "highway";
        min_zoom = 3;

        if (highway.equals("motorway")) {
          min_zoom_shield_text = 7;
        } else {
          min_zoom_shield_text = 12;
        }

        min_zoom_names = 11;
      } else if (highway.equals("trunk") || highway.equals("trunk_link") || highway.equals("primary") ||
        highway.equals("primary_link")) {
        kind = "major_road";
        min_zoom = 7;

        if (highway.equals("trunk")) {
          // Just trunk earlier zoom, otherwise road network looks choppy just with motorways then
          min_zoom = 6;
          min_zoom_shield_text = 8;
        } else if (highway.equals("primary")) {
          min_zoom_shield_text = 10;
        } else if (highway.equals("trunk_link")) {
          min_zoom_shield_text = 12;
        } else {
          min_zoom_shield_text = 13;
        }

        min_zoom_names = 12;
      } else if (highway.equals("secondary") || highway.equals("secondary_link") || highway.equals("tertiary") ||
        highway.equals("tertiary_link")) {
        kind = "medium_road";
        min_zoom = 9;

        if (highway.equals("secondary")) {
          min_zoom_shield_text = 11;
          min_zoom_names = 12;
        } else if (highway.equals("tertiary")) {
          min_zoom_shield_text = 12;
          min_zoom_names = 13;
        } else {
          min_zoom_shield_text = 13;
          min_zoom_names = 14;
        }
      } else if (highway.equals("residential") || highway.equals("service") || highway.equals("unclassified") ||
        highway.equals("road") || highway.equals("raceway")) {
        kind = "minor_road";
        min_zoom = 12;
        min_zoom_shield_text = 12;
        min_zoom_names = 14;

        if (highway.equals("service")) {
          kind_detail = "service";
          min_zoom = 13;

          // push down "alley", "driveway", "parking_aisle", "drive-through" & etc
          if (sourceFeature.hasTag("service")) {
            min_zoom = 14;
            service = sourceFeature.getString("service");
          }
        }
      } else if (sourceFeature.hasTag("highway", "pedestrian", "track", "path", "cycleway", "bridleway", "footway",
        "steps", "corridor")) {
        kind = "path";
        kind_detail = highway;
        min_zoom = 12;
        min_zoom_shield_text = 12;
        min_zoom_names = 14;

        if (sourceFeature.hasTag("highway", "path", "cycleway", "bridleway", "footway", "steps")) {
          min_zoom = 13;
        }
        if (sourceFeature.hasTag("footway", "sidewalk", "crossing")) {
          min_zoom = 14;
          kind_detail = sourceFeature.getString("footway");
        }
        if (sourceFeature.hasTag("highway", "corridor")) {
          min_zoom = 14;
        }
      } else {
        kind = "other";
        kind_detail = sourceFeature.getString("service");
        min_zoom = 14;
        min_zoom_shield_text = 14;
        min_zoom_names = 14;
      }

      var feat = features.line("roads")
        .setId(FeatureId.create(sourceFeature))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", kind_detail)
        .setAttrWithMinzoom("ref", shield_text, min_zoom_shield_text)
        .setAttrWithMinzoom("shield_text_length", shield_text_length, min_zoom_shield_text)
        .setAttrWithMinzoom("network", network_val, min_zoom_shield_text)
        // Core OSM tags for different kinds of places
        .setAttrWithMinzoom("bridge", sourceFeature.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sourceFeature.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sourceFeature.getString("layer"), 12)
        .setAttrWithMinzoom("oneway", sourceFeature.getString("oneway"), 14)
        // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
        //                      If an explicate value is needed it should bea kind, or included in kind_detail
        .setAttr("highway", highway)
        .setMinPixelSize(0)
        .setPixelTolerance(0)
        .setZoomRange(min_zoom, max_zoom);

      // Core OSM tags for different kinds of places
      if (service != "") {
        feat.setAttr("service", service);
      }

      // Core Tilezen schema properties
      if (kind_detail != "") {
        feat.setAttr("pmap:kind_detail", kind_detail);
      }

      if (sourceFeature.hasTag("highway", "motorway_link", "trunk_link", "primary_link", "secondary_link",
        "tertiary_link")) {
        feat.setAttr("pmap:link", 1).setZoomRange(12, 15);
      }

      if (sourceFeature.hasTag("bridge", "yes")) {
        feat.setAttrWithMinzoom("pmap:level", 1, 12);
      } else if (sourceFeature.hasTag("tunnel", "yes")) {
        feat.setAttrWithMinzoom("pmap:level", -1, 12);
      } else {
        feat.setAttrWithMinzoom("pmap:level", 0, 12);
      }

      OsmNames.setOsmNames(feat, sourceFeature, min_zoom_names);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    if (zoom < 12) {
      items = linkSimplify(items, "highway", "motorway", "motorway_link");
      items = linkSimplify(items, "highway", "trunk", "trunk_link");
      items = linkSimplify(items, "highway", "primary", "primary_link");
      items = linkSimplify(items, "highway", "secondary", "secondary_link");
    }

    // NOTE: (nvkelso 20230623) Why is this neccesary?
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
