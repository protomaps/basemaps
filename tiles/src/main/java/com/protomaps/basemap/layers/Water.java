package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.names.OsmNames;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;

public class Water implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "water";
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    features.polygon(this.name())
      .setAttr("pmap:kind", "water")
      .setZoomRange(6, 15).setBufferPixels(8);
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var alkaline = 0;
    var reservoir = 0;
    var theme_min_zoom = 0;
    var theme_max_zoom = 0;

    // Only process certain Natural Earth layers
    // Notably the landscan derived urban areas and NA roads supplement themes causes problems otherwise
    if (sourceLayer.equals("ne_110m_ocean") || sourceLayer.equals("ne_110m_lakes") || sourceLayer.equals("ne_50m_ocean") || sourceLayer.equals("ne_50m_lakes") || sourceLayer.equals("ne_10m_ocean") || sourceLayer.equals("ne_10m_lakes") ) {
      if (sourceLayer.equals("ne_110m_ocean")) {
        theme_min_zoom = 0;
        theme_max_zoom = 1;
      } else if (sourceLayer.equals("ne_110m_lakes")) {
        theme_min_zoom = 0;
        theme_max_zoom = 1;
      } else if (sourceLayer.equals("ne_50m_ocean")) {
        theme_min_zoom = 2;
        theme_max_zoom = 4;
      } else if (sourceLayer.equals("ne_50m_lakes")) {
        theme_min_zoom = 2;
        theme_max_zoom = 4;
      } else if (sourceLayer.equals("ne_10m_ocean")) {
        theme_min_zoom = 5;
        theme_max_zoom = 5;
      } else if (sourceLayer.equals("ne_10m_lakes")) {
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
        case "Ocean" -> kind = "ocean";
      }

      if (kind != "" && sf.hasTag("min_zoom")) {
        var feature = features.polygon(this.name())
                .setAttr("pmap:kind", kind)
                .setAttr("pmap:min_zoom", sf.getLong("min_zoom"))
                .setZoomRange(sf.getString("min_zoom") == null ? theme_min_zoom : (int) Double.parseDouble(sf.getString("min_zoom")), theme_max_zoom)
                .setMinPixelSize(3.0)
                .setBufferPixels(8);
      }
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() && (
              sf.hasTag("water") ||
              sf.hasTag("waterway") ||
              sf.hasTag("natural", "water") ||
              sf.hasTag("landuse", "reservoir") ||
              sf.hasTag("leisure", "swimming_pool")))
    {
      String kind = "other";
      String kind_detail = "";
      var reservoir = false;
      var alkaline = false;

      // coallese values across tags to single kind value
      if (sf.hasTag("natural", "water", "bay", "strait", "fjord")) {
        kind = sf.getString("natural");
        if (sf.hasTag("water", "basin", "canal", "ditch", "drain", "lake", "river", "stream")) {
          kind_detail = sf.getString("water");

          // This is a bug in Tilezen v1.9 that should be fixed in 2.0
          // But isn't present in Protomaps v2 so let's fix it preemtively
          if( kind_detail == "lake" ) {
            kind = "lake";
          }

          if (sf.hasTag("water", "lagoon", "oxbow", "pond", "reservoir", "wastewater")) {
            kind_detail = "lake";
          }
          if (sf.hasTag("water", "reservoir")) {
            reservoir = true;
          }
          if (sf.hasTag("water", "lagoon", "salt", "salt_pool")) {
            alkaline = true;
          }
        }
      } else if (sf.hasTag("waterway", "riverbank", "dock", "canal", "river", "stream", "ditch", "drain")) {
        kind = "water";
        kind_detail = sf.getString("waterway");
      } else if (sf.hasTag("landuse", "basin")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("landuse", "reservoir")) {
        kind = "water";
        kind_detail = sf.getString("landuse");
        reservoir = true;
      } else if (sf.hasTag("leisure", "swimming_pool")) {
        kind = "swimming_pool";
      } else if (sf.hasTag("amenity", "swimming_pool")) {
        kind = "swimming_pool";
      }

      var feature = features.polygon(this.name())
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // Core OSM tags for different kinds of places
        // Add less common attributes only at higher zooms
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 12)
        // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
        //                      If an explicate value is needed it should bea kind, or included in kind_detail
        .setAttr("natural", sf.getString("natural"))
        .setAttr("landuse", sf.getString("landuse"))
        .setAttr("leisure", sf.getString("leisure"))
        .setAttr("water", sf.getString("water"))
        .setAttr("waterway", sf.getString("waterway"))
        .setZoomRange(6, 15)
        .setMinPixelSize(3.0)
        .setBufferPixels(8);

      // Core Tilezen schema properties
      if (kind_detail != "") {
        feature.setAttr("pmap:kind_detail", kind_detail);
      }
      if (sf.hasTag("water", "reservoir") || reservoir) {
        feature.setAttr("reservoir", true);
      }
      if (sf.hasTag("water", "lagoon", "salt", "salt_pool") || alkaline) {
        feature.setAttr("alkaline", true);
      }
      if (sf.hasTag("intermittent", "yes")) {
        feature.setAttr("intermittent", true);
      }

      // NOTE: water labels for polygons are found in the physical_point layer (see also physical_line layer)
      //OsmNames.setOsmNames(feature, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    items = Area.addAreaTag(items);

    if (zoom == 15)
      return items;

    int minArea = 400 / (4096 * 4096) * (256 * 256);
    if (zoom == 6)
      minArea = 600 / (4096 * 4096) * (256 * 256);
    else if (zoom <= 5)
      minArea = 800 / (4096 * 4096) * (256 * 256);
    items = Area.filterArea(items, minArea);

    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}
