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
      sf.hasTag("man_made", "pier") ||
      sf.hasTag("route", "ferry") ||
      sf.hasTag("aeroway", "runway", "taxiway")) &&
      (!sf.hasTag("railway", "abandoned", "razed", "demolished", "removed", "construction", "platform", "proposed"))) {

      int minzoom = 11;

      if (sf.hasTag("aeroway", "runway")) {
        minzoom = 9;
      } else
      if (sf.hasTag("aeroway", "taxiway")) {
        minzoom = 10;
      } else
      if (sf.hasTag("service", "yard", "siding", "crossover")) {
        minzoom = 13;
      } else
      if (sf.hasTag("man_made", "pier")) {
        minzoom = 13;
      }

      String kind = "other";
      String kind_detail = "other";
      if (sf.hasTag("aeroway")) {
        kind = "aeroway";
        kind_detail = sf.getString("aeroway");
      } else if (sf.hasTag("railway", "disused", "funicular", "light_rail", "miniature", "monorail", "narrow_gauge", "preserved", "subway", "tram")) {
        kind = "rail";
        kind_detail = sf.getString("railway");
        minzoom = 14;
      } else if (sf.hasTag("railway")) {
        kind = "rail";
        kind_detail = sf.getString("railway");

        if( kind_detail.equals("service") ) {
          minzoom = 13;

          // eg a rail yard
          if( sf.hasTag("service") ) {
            minzoom = 14;
          }
        }
      } else if (sf.hasTag("ferry")) {
        kind = "ferry";
        kind_detail = sf.getString("ferry");
      } else if (sf.hasTag("man_made", "pier")) {
        kind = "pier";
      } else if (sf.hasTag("aerialway")) {
        kind = "aerialway";
        kind_detail = sf.getString("aerialway");
      }

      var feature = features.line(this.name())
        .setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // Core OSM tags for different kinds of places
        .setAttr("aerialway", sf.getString("aerialway"))
        .setAttr("aeroway", sf.getString("aeroway"))
        .setAttr("highspeed", sf.getString("highspeed"))
        .setAttr("layer", sf.getString("layer"))
        .setAttr("man_made", sf.getString("pier"))
        .setAttr("network", sf.getString("network"))
        .setAttr("railway", sf.getString("railway"))
        .setAttr("ref", sf.getString("ref"))
        .setAttr("route", sf.getString("route"))
        .setAttr("service", sf.getString("service"))
        .setZoomRange(minzoom, 15);

      if( kind_detail != "other" ) {
        feature.setAttr("pmap:kind_detail", kind_detail);
      }

      if( kind == "pier" ) {
        feature.setMinPixelSize(2);
      }

      OsmNames.setOsmNames(feature, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
