package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.geo.GeometryException;
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
              .setZoomRange(sf.getString("min_zoom") == null ? theme_min_zoom : (int) Double.parseDouble(sf.getString("min_zoom")), theme_max_zoom)
              .setBufferPixels(128);
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

    if( sf.hasTag("name") && sf.getTag( "name") != null &&
            sf.canBePolygon() &&
            (sf.hasTag("water") ||
              sf.hasTag("waterway") ||
              sf.hasTag("natural", "water", "bay", "strait", "fjord") ||
              sf.hasTag("landuse", "reservoir") ||
              sf.hasTag("leisure", "swimming_pool")))
    {
      String kind = "other";
      Double way_area = 0.0;
      try { way_area = sf.area(); } catch(GeometryException e) {  System.out.println(e); }
      var feature_min_zoom = 17;
      var name_min_zoom = 17;
      var theme_min_zoom = 17;
      var kind_detail = "";
      var reservoir = false;
      var alkaline = false;

      var water_label_position = features.pointOnSurface(this.name())
              // This is the Tilezen way, when put into the "any geom type" water layer
              // For protomaps v3 this may not be neccesary?
              .setAttr("pmaps:label_position", true)
              .setAttr("natural", sf.getString("natural"))
              .setAttr("landuse", sf.getString("landuse"))
              .setAttr("leisure", sf.getString("leisure"))
              .setAttr("water", sf.getString("water"))
              .setAttr("waterway", sf.getString("waterway"))
              // Add less common attributes only at higher zooms
              .setAttrWithMinzoom("bridge", sf.getString("bridge"), 12)
              .setAttrWithMinzoom("tunnel", sf.getString("tunnel"), 12)
              .setAttrWithMinzoom("layer", sf.getString("layer"), 12)
              .setBufferPixels(128);

      // coallese values across tags to single kind value
      if (sf.hasTag("natural", "water", "bay", "strait", "fjord")) {
        kind = sf.getString("natural");
        if (sf.hasTag("water", "basin", "canal", "ditch", "drain", "lake", "river", "stream")) {
          kind_detail = sf.getString("water");
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
        kind = sf.getString("waterway");
      } else if (sf.hasTag("landuse", "basin", "reservoir")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("leisure", "swimming_pool")) {
        kind = "swimming_pool";
      } else if (sf.hasTag("amenity", "swimming_pool")) {
        kind = "swimming_pool";
      }

      water_label_position.setAttr("pmap:kind", kind);

      if ( kind_detail != "" ) {
        water_label_position.setAttr("pmap:kind_detail", kind_detail);
      }
      if (sf.hasTag("water", "reservoir") || reservoir) {
        water_label_position.setAttr("reservoir", true);
      }
      if (sf.hasTag("water", "lagoon", "salt", "salt_pool") || alkaline) {
        water_label_position.setAttr("alkaline", true);
      }
      if (sf.hasTag("intermittent", "yes")) {
        water_label_position.setAttr("intermittent", true);
      }

      if( way_area >    500000000) {
        name_min_zoom = 8;
      } else
      if( way_area >    200000000) {
        name_min_zoom = 9;
      } else
      if( way_area >    40000000) {
        name_min_zoom = 10;
      } else
      if( way_area >     8000000) {
        name_min_zoom = 11;
      } else
      if( way_area >     1000000) {
        name_min_zoom = 12;
      } else
      if( way_area >      500000) {
        name_min_zoom = 13;
      } else
      if( way_area >       50000) {
        name_min_zoom = 14;
      } else
      if( way_area >       10000) {
        name_min_zoom = 15;
      }

      OsmNames.setOsmNames(water_label_position, sf, name_min_zoom-1);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
