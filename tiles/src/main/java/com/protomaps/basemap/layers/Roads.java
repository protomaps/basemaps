package com.protomaps.basemap.layers;

import static com.protomaps.basemap.postprocess.LinkSimplify.linkSimplify;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.locales.CartographicLocale;
import com.protomaps.basemap.locales.US;
import com.protomaps.basemap.names.OsmNames;
import java.util.*;

public class Roads implements ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "roads";
  }

  // Hardcoded to US for now
  private CartographicLocale locale = new US();

  public record Shield(String text, String network) {}

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && sf.hasTag("highway") &&
      !(sf.hasTag("highway", "proposed", "abandoned", "razed", "demolished", "removed", "construction", "elevator"))) {
      String kind = "other";
      String kindDetail = "";
      int minZoom = 15;
      int maxZoom = 15;
      int minZoomShieldText = 10;
      int minZoomNames = 14;

      String highway = sf.getString("highway");
      String service = "";

      Shield shield = locale.getShield(sf);
      Integer shieldTextLength = shield.text() == null ? null : shield.text().length();

      if (highway.equals("motorway") || highway.equals("motorway_link")) {
        // TODO: (nvkelso 20230622) Use Natural Earth for low zoom roads at zoom 5 and earlier
        //       as normally OSM roads would start at 6, but we start at 3 to match Protomaps v2
        kind = "highway";
        minZoom = 3;

        if (highway.equals("motorway")) {
          minZoomShieldText = 7;
        } else {
          minZoomShieldText = 12;
        }

        minZoomNames = 11;
      } else if (highway.equals("trunk") || highway.equals("trunk_link") || highway.equals("primary") ||
        highway.equals("primary_link")) {
        kind = "major_road";
        minZoom = 7;

        if (highway.equals("trunk")) {
          // Just trunk earlier zoom, otherwise road network looks choppy just with motorways then
          minZoom = 6;
          minZoomShieldText = 8;
        } else if (highway.equals("primary")) {
          minZoomShieldText = 10;
        } else if (highway.equals("trunk_link")) {
          minZoomShieldText = 12;
        } else {
          minZoomShieldText = 13;
        }

        minZoomNames = 12;
      } else if (highway.equals("secondary") || highway.equals("secondary_link") || highway.equals("tertiary") ||
        highway.equals("tertiary_link")) {
        kind = "medium_road";
        minZoom = 9;

        if (highway.equals("secondary")) {
          minZoomShieldText = 11;
          minZoomNames = 12;
        } else if (highway.equals("tertiary")) {
          minZoomShieldText = 12;
          minZoomNames = 13;
        } else {
          minZoomShieldText = 13;
          minZoomNames = 14;
        }
      } else if (highway.equals("residential") || highway.equals("service") || highway.equals("unclassified") ||
        highway.equals("road") || highway.equals("raceway")) {
        kind = "minor_road";
        minZoom = 12;
        minZoomShieldText = 12;
        minZoomNames = 14;

        if (highway.equals("service")) {
          kindDetail = "service";
          minZoom = 13;

          // push down "alley", "driveway", "parking_aisle", "drive-through" & etc
          if (sf.hasTag("service")) {
            minZoom = 14;
            service = sf.getString("service");
          }
        }
      } else if (sf.hasTag("highway", "pedestrian", "track", "path", "cycleway", "bridleway", "footway",
        "steps", "corridor")) {
        kind = "path";
        kindDetail = highway;
        minZoom = 12;
        minZoomShieldText = 12;
        minZoomNames = 14;

        if (sf.hasTag("highway", "path", "cycleway", "bridleway", "footway", "steps")) {
          minZoom = 13;
        }
        if (sf.hasTag("footway", "sidewalk", "crossing")) {
          minZoom = 14;
          kindDetail = sf.getString("footway", "");
        }
        if (sf.hasTag("highway", "corridor")) {
          minZoom = 14;
        }
      } else {
        kind = "other";
        kindDetail = sf.getString("service", "");
        minZoom = 14;
        minZoomShieldText = 14;
        minZoomNames = 14;
      }

      var feat = features.line("roads")
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // To power better client label collisions
        .setAttr("pmap:min_zoom", minZoom + 1)
        .setAttrWithMinzoom("ref", shield.text(), minZoomShieldText)
        .setAttrWithMinzoom("shield_text_length", shieldTextLength, minZoomShieldText)
        .setAttrWithMinzoom("network", shield.network(), minZoomShieldText)
        // Core OSM tags for different kinds of places
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setAttrWithMinzoom("oneway", sf.getString("oneway"), 14)
        .setMinPixelSize(0)
        .setPixelTolerance(0)
        .setZoomRange(minZoom, maxZoom);

      // Core Tilezen schema properties
      if (!kindDetail.isEmpty()) {
        feat.setAttr("pmap:kind_detail", kindDetail);
      } else {
        feat.setAttr("pmap:kind_detail", highway);
      }

      // Core OSM tags for different kinds of places
      if (!service.isEmpty()) {
        feat.setAttr("service", service);
      }

      if (sf.hasTag("highway", "motorway_link", "trunk_link", "primary_link", "secondary_link",
        "tertiary_link")) {
        feat.setAttr("pmap:link", 1);
      }

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficent
      // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
      if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
        feat.setAttrWithMinzoom("pmap:level", 1, 12);
      } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
        feat.setAttrWithMinzoom("pmap:level", -1, 12);
      } else {
        feat.setAttrWithMinzoom("pmap:level", 0, 12);
      }

      // Server sort features so client label collisions are pre-sorted
      feat.setSortKey(minZoom);

      OsmNames.setOsmNames(feat, sf, minZoomNames);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    // limit the application of LinkSimplify to where cloverleafs are unlikely to be at tile edges.
    // TODO: selectively apply each class depending on zoom level.
    if (zoom < 12) {
      items = linkSimplify(items, "highway", "motorway", "motorway_link");
      items = linkSimplify(items, "highway", "trunk", "trunk_link");
      items = linkSimplify(items, "highway", "primary", "primary_link");
      items = linkSimplify(items, "highway", "secondary", "secondary_link");
    }

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
