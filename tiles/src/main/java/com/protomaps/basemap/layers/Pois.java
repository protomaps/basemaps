package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeoUtils;
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

  private static final double WORLD_AREA_FOR_70K_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70_000)) / 256d, 2);
  private static final double LOG2 = Math.log(2);

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if ((sf.isPoint() || sf.canBePolygon()) && (sf.hasTag("aeroway", "aerodrome") ||
      sf.hasTag("amenity") ||
      sf.hasTag("attraction") ||
      sf.hasTag("boundary", "national_park", "protected_area") ||
      sf.hasTag("craft") ||
      sf.hasTag("historic") ||
      sf.hasTag("landuse", "cemetery", "recreation_ground", "winter_sports", "quarry", "park", "forest", "military") ||
      sf.hasTag("leisure") ||
      sf.hasTag("natural", "beach") ||
      sf.hasTag("railway", "station") ||
      sf.hasTag("shop") ||
      sf.hasTag("tourism"))) {
      String kind = "other";
      String kind_detail = "";
      Integer min_zoom = 15;

      if (sf.hasTag("aeroway", "aerodrome")) {
        kind = sf.getString("aeroway");
        min_zoom = 13;

        // Emphasize large international airports earlier
        if (kind == "aerodrome" && sf.hasTag("iata")) {
          min_zoom -= 2;
        }

        if (sf.hasTag("aerodrome")) {
          kind_detail = sf.getString("aerodrome");
        }
      } else if (sf.hasTag("amenity", "university", "college")) {
        kind = sf.getString("amenity");
        // One would think University should be earlier, but there are lots of dinky node only places
        // So if the university has a large area, it'll naturally improve it's zoom in the next section...
        min_zoom = 14;
      } else if (sf.hasTag("amenity", "hospital")) {
        kind = sf.getString("amenity");
        min_zoom = 12;
      } else if (sf.hasTag("amenity", "library", "post_office", "school", "townhall")) {
        kind = sf.getString("amenity");
        min_zoom = 13;
      } else if (sf.hasTag("amenity", "cafe")) {
        kind = sf.getString("amenity");
        min_zoom = 15;
      } else if (sf.hasTag("landuse", "cemetery")) {
        kind = sf.getString("landuse");
        min_zoom = 14;
      } else if (sf.hasTag("landuse", "military")) {
        kind = "military";
        if (sf.hasTag("military", "naval_base", "airfield")) {
          kind = sf.getString("military");
        }
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
        } else if (sf.hasTag("attraction")) {
          kind = sf.getString("attraction");
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
        } else if (sf.hasTag("natural")) {
          kind = sf.getString("natural");
        } else if (sf.hasTag("railway")) {
          kind = sf.getString("railway");
        } else if (sf.hasTag("shop")) {
          kind = sf.getString("shop");
        } else if (sf.hasTag("tourism")) {
          kind = sf.getString("tourism");
          // Boundary is most generic, so place last else we loose out
          // on nature_reserve detail versus all the protected_area
        } else if (sf.hasTag("boundary")) {
          kind = sf.getString("boundary");
        }
      }

      // National forests
      if (sf.hasTag("boundary", "national_park") &&
        sf.hasTag("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
          "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
          "United State Forest Service", "U.S. National Forest Service")) {
        kind = "forest";
      } else if (sf.hasTag("boundary", "national_park") &&
        sf.hasTag("protect_class", "6") &&
        sf.hasTag("protection_title", "National Forest")) {
        kind = "forest";
      } else if (sf.hasTag("landuse", "forest") &&
        sf.hasTag("protect_class", "6")) {
        kind = "forest";
      } else if (sf.hasTag("landuse", "forest") &&
        sf.hasTag("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
          "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
          "United State Forest Service", "U.S. National Forest Service")) {
        kind = "forest";
      } else if (sf.hasTag("landuse", "forest")) {
        kind = "forest";
      } else if (sf.hasTag("boundary", "protected_area") &&
        sf.hasTag("protect_class", "6") &&
        sf.hasTag("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
          "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
          "United State Forest Service", "U.S. National Forest Service")) {
        kind = "forest";
      }

      // National parks
      if (sf.hasTag("boundary", "national_park")) {
        if (!(sf.hasTag("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service",
          "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service",
          "United State Forest Service", "U.S. National Forest Service") ||
          sf.hasTag("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
            "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
            "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
            "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
            "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary")) &&
          (sf.hasTag("protect_class", "2", "3") ||
            sf.hasTag("operator", "United States National Park Service", "National Park Service",
              "US National Park Service", "U.S. National Park Service", "US National Park service") ||
            sf.hasTag("operator:en", "Parks Canada") ||
            sf.hasTag("designation", "national_park") ||
            sf.hasTag("protection_title", "National Park"))) {
          kind = "national_park";
          min_zoom = 11;
        } else {
          kind = "park";
        }
      }

      if (sf.hasTag("cuisine")) {
        kind_detail = sf.getString("cuisine");
      } else if (sf.hasTag("religion")) {
        kind_detail = sf.getString("religion");
      } else if (sf.hasTag("sport")) {
        kind_detail = sf.getString("sport");
      }

      // try first for polygon -> point representations
      if (sf.canBePolygon() && sf.hasTag("name") && sf.getString("name") != null) {
        Double way_area = 0.0;
        try {
          way_area = sf.worldGeometry().getEnvelopeInternal().getArea() / WORLD_AREA_FOR_70K_SQUARE_METERS;
        } catch (GeometryException e) {
          System.out.println(e);
        }

        Double height = 0.0;
        try {
          height = sf.getString("height") == null ? 0.0 : parseDoubleOrNull(sf.getString("height"));
        } catch (Exception e) {
          System.out.println("Problem getting height");
        }

        // Area zoom grading overrides the kind zoom grading in the section above.
        // Roughly shared with the water label area zoom grading in physical points layer
        //
        // Allowlist of kind values eligible for early zoom point labels
        if (kind.equals("aerodrome") ||
          kind.equals("college") ||
          kind.equals("forest") ||
          kind.equals("golf_course") ||
          kind.equals("military") ||
          kind.equals("national_park") ||
          kind.equals("nature_reserve") ||
          kind.equals("naval_base") ||
          kind.equals("park") ||
          kind.equals("protected_area") ||
          kind.equals("stadium") ||
          kind.equals("university")) {
          if (way_area > 300000) { // 500000000 sq meters (web mercator proj)
            min_zoom = 5;
          } else if (way_area > 25000) { // 500000000 sq meters (web mercator proj)
            min_zoom = 6;
          } else if (way_area > 8000) { // 500000000
            min_zoom = 7;
          } else if (way_area > 3000) { // 200000000
            min_zoom = 8;
          } else if (way_area > 200) { //  40000000
            min_zoom = 9;
          } else if (way_area > 25) { //   8000000
            min_zoom = 10;
          } else if (way_area > 2) { //    500000
            min_zoom = 11;
          } else if (way_area > 1) { //     50000
            min_zoom = 12;
          } else if (way_area > 0.2) { //     10000
            min_zoom = 13;
          }
        } else if (kind.equals("cemetery")) {
          if (way_area > 5) { //     50000
            min_zoom = 12;
          } else if (way_area > 1) { //     10000
            min_zoom = 13;
          }
          // Typically for "building" derived label placements for shops and other businesses
        } else {
          if (way_area > 10) { //    500000
            min_zoom = 11;
          } else if (way_area > 2) { //     50000
            min_zoom = 12;
          } else if (way_area > 0.5) { //     10000
            min_zoom = 13;
          }

          // Small but tall features should show up early as they have regional prominance.
          // Height measured in meters
          if (min_zoom >= 13 && height > 0.0) {
            if (height >= 100) {
              min_zoom = 11;
            } else if (height >= 20) {
              min_zoom = 12;
            } else if (height >= 10) {
              min_zoom = 13;
            }

            // Clamp certain kind values so medium tall buildings don't crowd downtown areas
            // NOTE: (nvkelso 20230623) Apply label grid to early zooms of POIs layer
            // NOTE: (nvkelso 20230624) Turn this into an allowlist instead of a blocklist
            if (kind.equals("hotel") || kind.equals("hostel") || kind.equals("parking") || kind.equals("bank") ||
              kind.equals("place_of_worship") || kind.equals("jewelry") || kind.equals("yes") ||
              kind.equals("restaurant") || kind.equals("coworking_space") || kind.equals("clothes") ||
              kind.equals("art")) {
              if (min_zoom == 12) {
                min_zoom = 13;
              }
            }
          }
        }

        var poly_label_position = features.pointOnSurface(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("pmap:kind", kind)
          // While other layers don't need min_zoom, POIs do for more predictable client-side label collisions
          // 512 px zooms versus 256 px logical zooms
          .setAttr("pmap:min_zoom", min_zoom + 1)
          //
          // DEBUG
          //.setAttr("pmap:area_debug", way_area)
          //
          // Core OSM tags for different kinds of places
          // Special airport only tag (to indicate if it's an airport with regular commercial flights)
          .setAttr("iata", sf.getString("iata"))
          // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
          //                      If an explicate value is needed it should be a kind, or included in kind_detail
          .setAttr("amenity", sf.getString("amenity"))
          .setAttr("attraction", sf.getString("attraction"))
          .setAttr("craft", sf.getString("craft"))
          .setAttr("historic", sf.getString("historic"))
          .setAttr("landuse", sf.getString("landuse"))
          .setAttr("leisure", sf.getString("leisure"))
          .setAttr("natural", sf.getString("natural"))
          .setAttr("railway", sf.getString("railway"))
          .setAttr("shop", sf.getString("shop"))
          .setAttr("tourism", sf.getString("tourism"))
          // Extra OSM tags for certain kinds of places
          // These are duplicate of what's in the kind_detail tag
          // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
          //                      If an explicate value is needed it should be a kind, or included in kind_detail
          .setAttr("cuisine", sf.getString("cuisine"))
          .setAttr("religion", sf.getString("religion"))
          .setAttr("sport", sf.getString("sport"))
          .setZoomRange(min_zoom, 15)
          .setBufferPixels(128);

        // Core Tilezen schema properties
        if (kind_detail != "") {
          poly_label_position.setAttr("pmap:kind_detail", kind_detail);
        }

        OsmNames.setOsmNames(poly_label_position, sf, 0);
      } else if (sf.isPoint()) {
        var point_feature = features.point(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("pmap:kind", kind)
          // While other layers don't need min_zoom, POIs do for more predictable client-side label collisions
          // 512 px zooms versus 256 px logical zooms
          .setAttr("pmap:min_zoom", min_zoom + 1)
          // Core OSM tags for different kinds of places
          // Special airport only tag (to indicate if it's an airport with regular commercial flights)
          .setAttr("iata", sf.getString("iata"))
          // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
          //                      If an explicate value is needed it should bea kind, or included in kind_detail
          .setAttr("amenity", sf.getString("amenity"))
          .setAttr("attraction", sf.getString("attraction"))
          .setAttr("craft", sf.getString("craft"))
          .setAttr("historic", sf.getString("historic"))
          .setAttr("landuse", sf.getString("landuse"))
          .setAttr("leisure", sf.getString("leisure"))
          .setAttr("natural", sf.getString("natural"))
          .setAttr("railway", sf.getString("railway"))
          .setAttr("shop", sf.getString("shop"))
          .setAttr("tourism", sf.getString("tourism"))
          // Extra OSM tags for certain kinds of places
          // These are duplicate of what's in the kind_detail tag
          // DEPRECATION WARNING: Marked for deprecation in v4 schema, do not use these for styling
          //                      If an explicate value is needed it should bea kind, or included in kind_detail
          .setAttr("cuisine", sf.getString("cuisine"))
          .setAttr("religion", sf.getString("religion"))
          .setAttr("sport", sf.getString("sport"))
          .setZoomRange(min_zoom, 15)
          .setBufferPixels(128);

        // Core Tilezen schema properties
        if (kind_detail != "") {
          point_feature.setAttr("pmap:kind_detail", kind_detail);
        }

        OsmNames.setOsmNames(point_feature, sf, 0);

        // Some features should only be visible at very late zooms when they don't have a name
        if (sf.hasTag("name") == false && (sf.hasTag("amenity", "atm", "bbq", "bench", "bicycle_parking",
          "bicycle_rental", "bicycle_repair_station", "boat_storage", "bureau_de_change", "car_rental", "car_sharing",
          "car_wash", "charging_station", "customs", "drinking_water", "fuel", "harbourmaster", "hunting_stand",
          "karaoke_box", "life_ring", "money_transfer", "motorcycle_parking", "parking", "picnic_table", "post_box",
          "ranger_station", "recycling", "sanitary_dump_station", "shelter", "shower", "taxi", "telephone", "toilets",
          "waste_basket", "waste_disposal", "water_point", "watering_place", "bicycle_rental", "motorcycle_parking",
          "charging_station") ||
          sf.hasTag("historic", "landmark", "wayside_cross") ||
          sf.hasTag("leisure", "dog_park", "firepit", "fishing", "pitch", "playground", "slipway", "swimming_area") ||
          sf.hasTag("tourism", "alpine_hut", "information", "picnic_site", "viewpoint", "wilderness_hut"))) {
          point_feature.setAttr("pmap:min_zoom", 17);
        }

        if (sf.hasTag("amenity", "clinic", "dentist", "doctors", "social_facility", "baby_hatch", "childcare",
          "car_sharing", "bureau_de_change", "emergency_phone", "karaoke", "karaoke_box", "money_transfer", "car_wash",
          "hunting_stand", "studio", "boat_storage", "gambling", "adult_gaming_centre", "sanitary_dump_station",
          "attraction", "animal", "water_slide", "roller_coaster", "summer_toboggan", "carousel", "amusement_ride",
          "maze") ||
          sf.hasTag("historic", "memorial") ||
          sf.hasTag("leisure", "pitch", "playground", "slipway") ||
          sf.hasTag("shop", "scuba_diving", "atv", "motorcycle", "snowmobile", "art", "bakery", "beauty", "bookmaker",
            "books", "butcher", "car", "car_parts", "car_repair", "clothes", "computer", "convenience", "fashion",
            "florist", "garden_centre", "gift", "golf", "greengrocer", "grocery", "hairdresser", "hifi", "jewelry",
            "lottery", "mobile_phone", "newsagent", "optician", "perfumery", "ship_chandler", "stationery", "tobacco",
            "travel_agency") ||
          sf.hasTag("tourism", "artwork", "hanami", "trail_riding_station", "bed_and_breakfast", "chalet",
            "guest_house", "hostel")) {
          point_feature.setAttr("pmap:min_zoom", 17);
        }
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {

    // TODO: (nvkelso 20230623) Consider adding a "pmap:rank" here for POIs, like for Places

    //items = Area.addAreaTag(items);
    return items;
  }
}
