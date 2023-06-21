package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class Pois implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "pois";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (!sf.canBeLine() && (sf.isPoint() || sf.canBePolygon()) && (
            sf.hasTag("amenity") ||
            sf.hasTag("craft") ||
            sf.hasTag("aeroway", "aerodrome") ||
            sf.hasTag("historic") ||
            sf.hasTag("leisure") ||
            sf.hasTag("railway", "station") ||
            sf.hasTag("shop") ||
            sf.hasTag("tourism") ))
    {
      String kind = "other";
      String kind_detail = "";
      Integer min_zoom = 15;

      if (sf.hasTag("aeroway", "aerodrome")) {
        kind = sf.getString("aeroway");
        min_zoom = 13;
      } else if (sf.hasTag("amenity",  "college", "hospital", "library", "post_office", "school", "townhall")) {
        kind = sf.getString("amenity");
        min_zoom = 13;
      } else if (sf.hasTag("amenity", "cafe")) {
        kind = sf.getString("amenity");
        min_zoom = 15;
      } else if (sf.hasTag("landuse", "cemetery")) {
        kind = sf.getString("landuse");
        min_zoom = 14;
      } else if (sf.hasTag("leisure", "golf_course", "marina", "park", "stadium")) {
        kind = sf.getString("leisure");
        min_zoom = 13;
      } else if (sf.hasTag("leisure")) {
        // This is dubious but existing behavior
        kind = "park";
        min_zoom = 14;
      } else if (sf.hasTag("shop", "grocery", "supermarket")) {
        kind = sf.getString("shop");
        min_zoom = 14;
      } else if (sf.hasTag("tourism", "attraction", "camp_site", "hotel")) {
        kind = sf.getString("tourism");
        min_zoom = 15;
      }

      if( sf.hasTag("boundary", "national_park") &&
              !(sf.hasTag("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service", "United State Forest Service", "U.S. National Forest Service") ||
                      sf.hasTag("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve", "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site", "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest", "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area", "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary")
              ) &&
              ( sf.hasTag("protect_class", "2", "3") ||
                      sf.hasTag("operator", "United States National Park Service", "National Park Service", "US National Park Service", "U.S. National Park Service", "US National Park service") ||
                      sf.hasTag("operator:en", "Parks Canada") ||
                      sf.hasTag("designation", "national_park") ||
                      sf.hasTag("protection_title", "National Park")
              )
      ) {
        kind = "national_park";
        min_zoom = 12;
      }

      if( sf.hasTag("cuisine") ) {
        kind_detail = sf.getString("cuisine");
      } else if ( sf.hasTag("religion") ) {
        kind_detail = sf.getString("religion");
      } else if ( sf.hasTag("sport") ) {
        kind_detail = sf.getString("sport");
      }

      // try first for polygon -> point representations
      if (sf.canBePolygon()) {
        Double way_area = 0.0;

        try { way_area = sf.area(); } catch(GeometryException e) {  System.out.println(e); }

        // Area zoom grading overrides the kind zoom grading in the section above
        if( way_area >     1000000) {
          min_zoom = 12;
        } else
        if( way_area >      500000) {
          min_zoom = 13;
        } else
        if( way_area >       50000) {
          min_zoom = 14;
        }

        var poly_label_position = features.pointOnSurface(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("pmap:kind", kind)
          .setAttr("pmap:kind_detail", kind_detail)
          // Core OSM tags for different kinds of places
          .setAttr("amenity", sf.getString("amenity"))
          .setAttr("craft", sf.getString("craft"))
          .setAttr("historic", sf.getString("historic"))
          .setAttr("leisure", sf.getString("leisure"))
          .setAttr("railway", sf.getString("railway"))
          .setAttr("shop", sf.getString("shop"))
          .setAttr("tourism", sf.getString("tourism"))
          // Extra OSM tags for certain kinds of places
          .setAttr("cuisine", sf.getString("cuisine"))
          .setAttr("iata", sf.getString("iata"))
          .setAttr("religion", sf.getString("religion"))
          .setAttr("sport", sf.getString("sport"))
          .setZoomRange(min_zoom, 15)
          .setBufferPixels(128);

        OsmNames.setOsmNames(poly_label_position, sf, 0);
      } else {
        var point_feature = features.point(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("pmap:kind", kind)
          .setAttr("pmap:kind_detail", kind_detail)
          // Core OSM tags for different kinds of places
          .setAttr("amenity", sf.getString("amenity"))
          .setAttr("craft", sf.getString("craft"))
          .setAttr("historic", sf.getString("historic"))
          .setAttr("leisure", sf.getString("leisure"))
          .setAttr("railway", sf.getString("railway"))
          .setAttr("shop", sf.getString("shop"))
          .setAttr("tourism", sf.getString("tourism"))
          // Extra OSM tags for certain kinds of places
          .setAttr("cuisine", sf.getString("cuisine"))
          .setAttr("iata", sf.getString("iata"))
          .setAttr("religion", sf.getString("religion"))
          .setAttr("sport", sf.getString("sport"))
          .setZoomRange(min_zoom, 15)
          .setBufferPixels(128);

        OsmNames.setOsmNames(point_feature, sf, 0);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
