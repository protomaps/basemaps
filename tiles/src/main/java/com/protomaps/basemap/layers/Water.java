package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.NeNames;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

@SuppressWarnings("java:S1192") // Duplicated string literals
public class Water implements ForwardingProfile.LayerPostProcessor {

  private static final double WORLD_AREA_FOR_70K_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70_000)) / 256d, 2);

  public static final String LAYER_NAME = "water";

  @Override
  public String name() {
    return LAYER_NAME;
  }

  public void processPreparedOsm(SourceFeature ignoredSf, FeatureCollector features) {
    features.polygon(LAYER_NAME)
      .setId(0)
      .setAttr("kind", "ocean")
      .setAttr("sort_rank", 200)
      .setZoomRange(6, 15).setBufferPixels(8);
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var themeMinZoom = 0;
    var themeMaxZoom = 0;

    // Only process certain Natural Earth layers
    // Notably the landscan derived urban areas and NA roads supplement themes causes problems otherwise
    if (sourceLayer.equals("ne_50m_ocean") || sourceLayer.equals("ne_50m_lakes") ||
      sourceLayer.equals("ne_10m_ocean") ||
      sourceLayer.equals("ne_10m_lakes")) {
      if (sourceLayer.equals("ne_50m_ocean")) {
        themeMinZoom = 0;
        themeMaxZoom = 4;
      } else if (sourceLayer.equals("ne_50m_lakes")) {
        themeMinZoom = 0;
        themeMaxZoom = 4;
      } else if (sourceLayer.equals("ne_10m_ocean")) {
        themeMinZoom = 5;
        themeMaxZoom = 5;
      } else if (sourceLayer.equals("ne_10m_lakes")) {
        themeMinZoom = 5;
        themeMaxZoom = 5;
      }

      switch (sf.getString("featurecla")) {
        case "Alkaline Lake", "Lake", "Reservoir" -> kind = "lake";
        case "Playa" -> kind = "playa";
        case "Ocean" -> kind = "ocean";
      }

      if (!kind.isEmpty() && sf.hasTag("min_zoom")) {
        features.polygon(LAYER_NAME)
          // Core Tilezen schema properties
          .setAttr("kind", kind)
          .setAttr("sort_rank", 200)
          //.setAttr("min_zoom", sf.getLong("min_zoom"))
          .setZoomRange((int) sf.getLong("min_zoom") - 1, themeMaxZoom)
          // (nvkelso 20230802) Don't set setMinPixelSize here else small islands chains like Hawaii are garbled
          .setBufferPixels(8);
      }

      if (sourceLayer.equals("ne_10m_lakes")) {
        var minZoom = sf.getLong("min_label");
        if (!kind.isEmpty() && sf.hasTag("min_label") && sf.hasTag("name")) {
          var waterLabelPosition = features.pointOnSurface(LAYER_NAME)
            .setAttr("kind", kind)
            .setAttr("min_zoom", minZoom + 1)
            .setZoomRange(sf.getString("min_label") == null ? themeMinZoom :
              (int) Double.parseDouble(sf.getString("min_label")) + 1, themeMaxZoom)
            .setBufferPixels(128);

          // Server sort features so client label collisions are pre-sorted
          waterLabelPosition.setSortKey((int) minZoom);

          NeNames.setNeNames(waterLabelPosition, sf, 0);
        }
      }
    }
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    // polygons
    if (sf.canBePolygon() && (sf.hasTag("water") ||
      (sf.hasTag("waterway") && !sf.hasTag("waterway", "dam")) ||
      sf.hasTag("natural", "water") ||
      sf.hasTag("landuse", "reservoir") ||
      sf.hasTag("leisure", "swimming_pool"))) {
      String kind = "other";
      String kindDetail = "";

      // coalesce values across tags to single kind value
      if (sf.hasTag("natural", "water", "bay", "strait", "fjord")) {
        kind = sf.getString("natural");

        if (sf.hasTag("amenity", "fountain")) {
          kind = "fountain";
        }
        if (sf.hasTag("water", "basin", "canal", "ditch", "drain", "lake", "river", "stream")) {
          kindDetail = sf.getString("water");

          // This is a bug in Tilezen v1.9 that should be fixed in 2.0
          // But isn't present in Protomaps v2 so let's fix it preemptively
          if (kindDetail.equals("lake")) {
            kind = "lake";
          }

          if (sf.hasTag("water", "lagoon", "oxbow", "pond", "reservoir", "wastewater")) {
            kindDetail = "lake";
          }
        }
      } else if (sf.hasTag("waterway", "riverbank", "dock", "canal", "river", "stream", "ditch", "drain")) {
        kind = "water";
        kindDetail = sf.getString("waterway");
      } else if (sf.hasTag("landuse", "basin")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("landuse", "reservoir")) {
        kind = "water";
        kindDetail = sf.getString("landuse");
      } else if (sf.hasTag("leisure", "swimming_pool")) {
        kind = "swimming_pool";
      } else if (sf.hasTag("amenity", "swimming_pool")) {
        kind = "swimming_pool";
      }

      var feature = features.polygon(LAYER_NAME)
        // Core Tilezen schema properties
        .setAttr("kind", kind)
        .setAttr("sort_rank", 200)
        // Core OSM tags for different kinds of places
        // Add less common attributes only at higher zooms
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setZoomRange(6, 15)
        .setMinPixelSize(1.0)
        .setBufferPixels(8);

      // Core Tilezen schema properties
      if (!kindDetail.isEmpty()) {
        feature.setAttr("kind_detail", kindDetail);
      }
      if (sf.hasTag("intermittent", "yes")) {
        feature.setAttr("intermittent", true);
      }

      //OsmNames.setOsmNames(feature, sf, 0);
    }

    // lines
    if (sf.canBeLine() && !sf.canBePolygon() && sf.hasTag("waterway") &&
      (!sf.hasTag("waterway", "riverbank", "reservoir", "dam"))) {
      int minZoom = 12;
      String kind = "other";
      if (sf.hasTag("waterway")) {
        kind = sf.getString("waterway");
        if (sf.hasTag("waterway", "river")) {
          minZoom = 9;
        }
      }

      var feat = features.line(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", kind)
        // Used for client-side label collisions
        .setAttr("min_zoom", minZoom + 1)
        // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
        //.setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        //.setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setAttr("sort_rank", 200)
        .setZoomRange(minZoom, 15);

      // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
      if (sf.hasTag("intermittent", "yes")) {
        feat.setAttr("intermittent", true);
      }

      // Set "brunnel" (bridge / tunnel) property where "level" = 1 is a bridge, 0 is ground level, and -1 is a tunnel
      // Because of MapLibre performance and draw order limitations, generally the boolean is sufficient
      // See also: "layer" for more complicated ±6 layering for more sophisticated graphics libraries
      if (sf.hasTag("bridge") && !sf.hasTag("bridge", "no")) {
        feat.setAttr("level", 1);
      } else if (sf.hasTag("tunnel") && !sf.hasTag("tunnel", "no")) {
        feat.setAttr("level", -1);
      } else if (sf.hasTag("layer", "-6", "-5", "-4", "-3", "-2", "-1")) {
        feat.setAttr("level", -1);
      } else {
        feat.setAttr("level", 0);
      }


      // Server sort features so client label collisions are pre-sorted
      feat.setSortKey(minZoom);

      OsmNames.setOsmNames(feat, sf, 0);
    }

    // points
    if (sf.isPoint() && sf.hasTag("place", "sea", "ocean")) {
      String kind = "";
      int minZoom = 12;
      if (sf.hasTag("place", "ocean")) {
        kind = "ocean";
        minZoom = 0;
      }
      if (sf.hasTag("place", "sea")) {
        kind = "sea";
        minZoom = 3;
      }

      var feat = features.point(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", kind)
        // Used for client-side label collisions
        .setAttr("min_zoom", minZoom + 1)
        .setZoomRange(minZoom, 15);

      // Server sort features so client label collisions are pre-sorted
      feat.setSortKey(minZoom);
      OsmNames.setOsmNames(feat, sf, 0);
    }

    if (sf.hasTag("name") && sf.getTag("name") != null &&
      sf.canBePolygon() &&
      (sf.hasTag("water") ||
        sf.hasTag("waterway") ||
        // bay, strait, fjord are included here only (not in water layer) because
        // OSM treats them as "overlay" label features over the normal water polys
        sf.hasTag("natural", "water", "bay", "strait", "fjord") ||
        sf.hasTag("landuse", "reservoir") ||
        sf.hasTag("leisure", "swimming_pool"))) {
      String kind = "other";
      var kindDetail = "";
      var nameMinZoom = 15;
      Double wayArea = 0.0;

      try {
        wayArea = sf.area() / WORLD_AREA_FOR_70K_SQUARE_METERS;
      } catch (GeometryException e) {
        e.log("Exception in way area calculation");
      }

      // coalesce values across tags to single kind value
      if (sf.hasTag("amenity", "fountain")) {
        kind = "fountain";
      } else if (sf.hasTag("natural", "water", "bay", "strait", "fjord")) {
        kind = sf.getString("natural");
        if (sf.hasTag("water", "basin", "canal", "ditch", "drain", "lake", "river", "stream")) {
          kindDetail = sf.getString("water");

          // This is a bug in Tilezen v1.9 that should be fixed in 2.0
          // But isn't present in Protomaps v2 so let's fix it preemptively
          if (kindDetail.equals("lake")) {
            kind = "lake";
          }

          if (sf.hasTag("water", "lagoon", "oxbow", "pond", "reservoir", "wastewater")) {
            kindDetail = "lake";
          }
        }
      } else if (sf.hasTag("waterway", "riverbank", "dock", "canal", "river", "stream", "ditch", "drain")) {
        kind = sf.getString("waterway");
      } else if (sf.hasTag("landuse", "basin", "reservoir")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("leisure", "swimming_pool")) {
        kind = "swimming_pool";
      } else if (sf.hasTag("amenity", "swimming_pool")) {
        kind = "swimming_pool";
      }

      // We don't want to show too many water labels at early zooms else it crowds the map
      // TODO: (nvkelso 20230621) These numbers are super wonky, they should instead be sq meters in web mercator prj
      // Zoom 5 and earlier from Natural Earth instead (see above)
      if (wayArea > 25000) { //500000000
        nameMinZoom = 6;
      } else if (wayArea > 8000) { //500000000
        nameMinZoom = 7;
      } else if (wayArea > 3000) { //200000000
        nameMinZoom = 8;
      } else if (wayArea > 500) { //40000000
        nameMinZoom = 9;
      } else if (wayArea > 200) { //8000000
        nameMinZoom = 10;
      } else if (wayArea > 30) { //1000000
        nameMinZoom = 11;
      } else if (wayArea > 25) { //500000
        nameMinZoom = 12;
      } else if (wayArea > 0.5) { //50000
        nameMinZoom = 13;
      } else if (wayArea > 0.05) { //10000
        nameMinZoom = 14;
      }

      var waterLabelPosition = features.pointOnSurface(LAYER_NAME)
        // Core Tilezen schema properties
        .setAttr("kind", kind)
        .setAttr("kind_detail", kindDetail)
        // While other layers don't need min_zoom, physical point labels do for more
        // predictable client-side label collisions
        // 512 px zooms versus 256 px logical zooms
        .setAttr("min_zoom", nameMinZoom + 1)
        // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
        .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
        .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
        .setAttrWithMinzoom("layer", Parse.parseIntOrNull(sf.getString("layer")), 12)
        .setZoomRange(nameMinZoom, 15)
        .setAttr("sort_rank", 200)
        .setBufferPixels(128);

      // Add less common core Tilezen attributes only at higher zooms (will continue to v4)
      if (!kindDetail.isEmpty()) {
        waterLabelPosition.setAttr("kind_detail", kindDetail);
      }
      if (sf.hasTag("intermittent", "yes")) {
        waterLabelPosition.setAttr("intermittent", true);
      }

      // Server sort features so client label collisions are pre-sorted
      waterLabelPosition.setSortKey(nameMinZoom);

      OsmNames.setOsmNames(waterLabelPosition, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    // TODO filter to only polygons
    return FeatureMerge.mergeOverlappingPolygons(items, 1);
  }
}
