package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;

public class Pois implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "pois";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if ( (sf.isPoint() || sf.canBePolygon()) && (
            sf.hasTag("amenity") ||
            sf.hasTag("boundary", "national_park", "protected_area") ||
            sf.hasTag("craft") ||
            sf.hasTag("aeroway", "aerodrome") ||
            sf.hasTag("historic") ||
            sf.hasTag("landuse", "cemetery", "recreation_ground", "winter_sports", "quarry", "park", "forest", "military") ||
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
      } else if (sf.hasTag("amenity",  "university","college", "hospital", "library", "post_office", "school", "townhall")) {
        kind = sf.getString("amenity");
        min_zoom = 13;
      } else if (sf.hasTag("amenity", "cafe")) {
        kind = sf.getString("amenity");
        min_zoom = 15;
      } else if (sf.hasTag("landuse", "cemetery")) {
        kind = sf.getString("landuse");
        min_zoom = 14;
      } else if (sf.hasTag("boundary", "national_park", "protected_area")) {
        kind = sf.getString("boundary");
        min_zoom = 11;
      } else if (sf.hasTag("leisure", "golf_course", "marina", "park", "stadium")) {
        kind = sf.getString("leisure");
        min_zoom = 13;
      } else if (sf.hasTag("shop", "grocery", "supermarket")) {
        kind = sf.getString("shop");
        min_zoom = 14;
      } else if (sf.hasTag("tourism", "attraction", "camp_site", "hotel")) {
        kind = sf.getString("tourism");
        min_zoom = 15;
      } else {
        // Avoid problem of too many "other" kinds
        // All these will default to min_zoom of 15
        // If a more specific min_zoom is needed (or sanitize kind values)
        // then add new logic in section above
        if (sf.hasTag("amenity")) {
          kind = sf.getString("amenity");
        } else if (sf.hasTag("boundary")) {
          kind = sf.getString("boundary");
        } else if (sf.hasTag("craft")) {
          kind = sf.getString("craft");
        } else if (sf.hasTag("aeroway")) {
          kind = sf.getString("aeroway");
        } else if (sf.hasTag("historic")) {
          kind = sf.getString("historic");
        } else if (sf.hasTag("landuse")) {
          kind = sf.getString("landuse");
        } else if (sf.hasTag("leisure")) {
          kind = sf.getString("leisure");
        } else if (sf.hasTag("railway")) {
          kind = sf.getString("railway");
        } else if (sf.hasTag("shop")) {
          kind = sf.getString("shop");
        } else if (sf.hasTag("tourism")) {
          kind = sf.getString("tourism");
        }
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
        min_zoom = 11;
      }

      if( sf.hasTag("cuisine") ) {
        kind_detail = sf.getString("cuisine");
      } else if ( sf.hasTag("religion") ) {
        kind_detail = sf.getString("religion");
      } else if ( sf.hasTag("sport") ) {
        kind_detail = sf.getString("sport");
      }

      // try first for polygon -> point representations
      if (sf.canBePolygon() && sf.hasTag("name" ) && sf.getString("name" ) != null) {
        Double way_area = 0.0;

        try { way_area = sf.area(); } catch(GeometryException e) {  System.out.println(e); }

        // Area zoom grading overrides the kind zoom grading in the section above
        if (way_area > 400) {     //8000000
          min_zoom = 9;
        } else if (way_area > 50) {     //1000000
          min_zoom = 10;
        } else if (way_area > 10) {     //500000
          min_zoom = 11;
        } else
        // size of a stadium is 5 to 7 range
        if (way_area > 2) {     //50000
          min_zoom = 12;
        } else if (way_area > 0.5) {     //10000
          min_zoom = 13;
        }

        var poly_label_position = features.pointOnSurface(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("pmap:kind", kind)
          // DEBUG
          .setAttr("pmap:area_debug", way_area)
          // Core OSM tags for different kinds of places
          .setAttr("amenity", sf.getString("amenity"))
          .setAttr("craft", sf.getString("craft"))
          .setAttr("historic", sf.getString("historic"))
          .setAttr("leisure", sf.getString("leisure"))
          .setAttr("railway", sf.getString("railway"))
          .setAttr("shop", sf.getString("shop"))
          .setAttr("tourism", sf.getString("tourism"))
          // Extra OSM tags for certain kinds of places
          // These are duplicate of what's in the kind_detail tag
          .setAttr("cuisine", sf.getString("cuisine"))
          .setAttr("religion", sf.getString("religion"))
          .setAttr("sport", sf.getString("sport"))
          // Special airport only tag (to indicate if it's an airport with regular commercial flights)
          .setAttr("iata", sf.getString("iata"))
          .setZoomRange(min_zoom, 15)
          .setBufferPixels(128);

        // Core Tilezen schema properties
        if( kind_detail != "") {
          poly_label_position.setAttr("pmap:kind_detail", kind_detail);
        }

        OsmNames.setOsmNames(poly_label_position, sf, 0);
      } else if( sf.isPoint() ){
        var point_feature = features.point(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("pmap:kind", kind)
          // Core OSM tags for different kinds of places
          .setAttr("amenity", sf.getString("amenity"))
          .setAttr("craft", sf.getString("craft"))
          .setAttr("historic", sf.getString("historic"))
          .setAttr("leisure", sf.getString("leisure"))
          .setAttr("railway", sf.getString("railway"))
          .setAttr("shop", sf.getString("shop"))
          .setAttr("tourism", sf.getString("tourism"))
          // Extra OSM tags for certain kinds of places
          // These are duplicate of what's in the kind_detail tag
          .setAttr("cuisine", sf.getString("cuisine"))
          .setAttr("religion", sf.getString("religion"))
          .setAttr("sport", sf.getString("sport"))
          // Special airport only tag (to indicate if it's an airport with regular commercial flights)
          .setAttr("iata", sf.getString("iata"))
          .setZoomRange(min_zoom, 15)
          .setBufferPixels(128);

        // Core Tilezen schema properties
        if( kind_detail != "") {
          point_feature.setAttr("pmap:kind_detail", kind_detail);
        }

        OsmNames.setOsmNames(point_feature, sf, 0);

        // Some features should only be visible at very late zooms when they don't have a name
        if(sf.hasTag("name" ) == false && (
                sf.hasTag("amenity", "atm", "bbq", "bench", "bicycle_parking", "bicycle_rental", "bicycle_repair_station", "boat_storage", "bureau_de_change", "car_rental", "car_sharing", "car_wash", "charging_station", "customs", "drinking_water", "fuel", "harbourmaster", "hunting_stand", "karaoke_box", "life_ring", "money_transfer", "motorcycle_parking", "parking", "picnic_table", "post_box", "ranger_station", "recycling", "sanitary_dump_station", "shelter", "shower", "taxi", "telephone", "toilets", "waste_basket", "waste_disposal", "water_point", "watering_place", "bicycle_rental", "motorcycle_parking", "charging_station") ||
                sf.hasTag("historic", "landmark", "wayside_cross") ||
                sf.hasTag("leisure", "dog_park", "firepit", "fishing", "pitch", "playground", "slipway", "swimming_area") ||
                sf.hasTag("tourism", "alpine_hut", "information", "picnic_site", "viewpoint", "wilderness_hut") )
        ) {
          point_feature.setAttr("pmap:min_zoom", 17);
        }

        if ( sf.hasTag("amenity", "clinic", "dentist", "doctors", "social_facility", "baby_hatch", "childcare", "car_sharing", "bureau_de_change", "emergency_phone", "karaoke", "karaoke_box", "money_transfer", "car_wash", "hunting_stand", "studio", "boat_storage", "gambling", "adult_gaming_centre", "sanitary_dump_station", "attraction", "animal", "water_slide", "roller_coaster", "summer_toboggan", "carousel", "amusement_ride", "maze") ||
                sf.hasTag("historic", "memorial") ||
                sf.hasTag("leisure", "pitch", "playground", "slipway") ||
                sf.hasTag("shop", "scuba_diving", "atv", "motorcycle", "snowmobile", "art", "bakery", "beauty", "bookmaker", "books", "butcher", "car", "car_parts", "car_repair", "clothes", "computer", "convenience", "fashion", "florist", "garden_centre", "gift", "golf", "greengrocer", "grocery", "hairdresser", "hifi", "jewelry", "lottery", "mobile_phone", "newsagent", "optician", "perfumery", "ship_chandler", "stationery", "tobacco", "travel_agency") ||
                sf.hasTag("tourism", "artwork", "hanami", "trail_riding_station", "bed_and_breakfast", "chalet", "guest_house", "hostel")
        ) {
          point_feature.setAttr("pmap:min_zoom", 17);
        }
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    //items = Area.addAreaTag(items);
    return items;
  }
}
