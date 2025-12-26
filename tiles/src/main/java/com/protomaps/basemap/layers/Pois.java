package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.without;
import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.Expression;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.QrankDb;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;
import java.util.Map;


@SuppressWarnings("java:S1192")
public class Pois implements ForwardingProfile.LayerPostProcessor {

  private Map<String, int[][]> qrankGrading = Map.of(
    "station", new int[][]{{10, 50000}, {12, 20000}, {13, 10000}},
    "aerodrome", new int[][]{{10, 50000}, {12, 20000}, {13, 5000}, {14, 2500}},
    "park", new int[][]{{11, 20000}, {12, 10000}, {13, 5000}, {14, 2500}},
    "peak", new int[][]{{11, 20000}, {12, 10000}, {13, 5000}, {14, 2500}},
    "attraction", new int[][]{{12, 40000}, {13, 20000}, {14, 10000}},
    "university", new int[][]{{12, 40000}, {13, 20000}, {14, 10000}}
  );

  private QrankDb qrankDb;

  public Pois(QrankDb qrankDb) {
    this.qrankDb = qrankDb;
  }

  public static final String LAYER_NAME = "pois";

  private static final Expression with_operator_usfs = with("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service", "United State Forest Service", "U.S. National Forest Service");
  private static final Expression without_operator_usfs = without("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service", "United State Forest Service", "U.S. National Forest Service");
  private static final Expression without_protectionTitle_conservationEtc = without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve", "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site", "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest", "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area", "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary");

  private static final MultiExpression.Index<Map<String, Object>> index = MultiExpression.of(List.of(
    rule(
      use("kind", "other"),
      use("kindDetail", "")
    ),

    rule(
      with("boundary"),
      use("kind", fromTag("boundary"))
    ),

    // National forests

    rule(
      with("boundary", "national_park"),
      with_operator_usfs,
      use("kind", "forest")
    ),
    rule(
      with("boundary", "national_park"),
      with("protect_class", "6"),
      with("protection_title", "National Forest"),
      use("kind", "forest")
    ),
    rule(
      with("landuse", "forest"),
      with("protect_class", "6"),
      use("kind", "forest")
    ),
    rule(
      with("landuse", "forest"),
      with_operator_usfs,
      use("kind", "forest")
    ),
    rule(
      with("landuse", "forest"),
      use("kind", "forest")
    ),
    rule(
      with("boundary", "protected_area"),
      with("protect_class", "6"),
      with_operator_usfs,
      use("kind", "forest")
    ),
    rule(
      with("boundary", "national_park"),
      with("landuse", "forest"),
      use("kind", "forest")
    ),

    // National parks

    rule(
      with("boundary", "national_park"),
      use("kind", "park")
    ),

    rule(
      with("boundary", "national_park"),
      without_operator_usfs,
      without_protectionTitle_conservationEtc,
      with("protect_class", "2", "3"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without_operator_usfs,
      without_protectionTitle_conservationEtc,
      with("operator", "United States National Park Service", "National Park Service", "US National Park Service", "U.S. National Park Service", "US National Park service"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without_operator_usfs,
      without_protectionTitle_conservationEtc,
      with("operator:en", "Parks Canada"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without_operator_usfs,
      without_protectionTitle_conservationEtc,
      with("designation", "national_park"),
      use("kind", "national_park")
    ),
    rule(
      with("boundary", "national_park"),
      without_operator_usfs,
      without_protectionTitle_conservationEtc,
      with("protection_title", "National Park"),
      use("kind", "national_park")
    ),

    // Remaining amenities

    rule(
      with("amenity"),
      use("kind", fromTag("amenity"))
    ),
    rule(
      with("aeroway", "aerodrome"),
      use("kind", "aerodrome"),
      use("kindDetail", fromTag("aerodrome"))
    )
  )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // ~= pow((sqrt(70k) / (40m / 256)) / 256, 2) ~= 4.4e-11
  private static final double WORLD_AREA_FOR_70K_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70_000)) / 256d, 2);

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    var matches = index.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, "kind", "undefined");
    String kindDetail = getString(sf, matches, "kindDetail", "undefined");

    if ((sf.isPoint() || sf.canBePolygon()) && (sf.hasTag("aeroway", "aerodrome") ||
      sf.hasTag("amenity") ||
      sf.hasTag("attraction") ||
      sf.hasTag("boundary", "national_park", "protected_area") ||
      sf.hasTag("craft") ||
      sf.hasTag("historic") ||
      sf.hasTag("landuse", "cemetery", "recreation_ground", "winter_sports", "quarry", "park", "forest", "military",
        "village_green", "allotments") ||
      sf.hasTag("leisure") ||
      sf.hasTag("natural", "beach", "peak") ||
      sf.hasTag("railway", "station") ||
      sf.hasTag("highway", "bus_stop") ||
      sf.hasTag("shop") ||
      sf.hasTag("tourism") &&
        (!sf.hasTag("historic", "district")))) {
      Integer minZoom = 15;
      long qrank = 0;

      String wikidata = sf.getString("wikidata");
      if (wikidata != null) {
        qrank = qrankDb.get(wikidata);
      }

      if (sf.hasTag("aeroway", "aerodrome")) {
        minZoom = 13;

        // Emphasize large international airports earlier
        if (kind.equals("aerodrome") && sf.hasTag("iata")) {
          minZoom -= 2;
        }
      } else if (sf.hasTag("amenity", "university", "college")) {
        // One would think University should be earlier, but there are lots of dinky node only places
        // So if the university has a large area, it'll naturally improve it's zoom in the next section...
        minZoom = 14;
      } else if (sf.hasTag("amenity", "hospital")) {
        minZoom = 12;
      } else if (sf.hasTag("amenity", "library", "post_office", "townhall")) {
        minZoom = 13;
      } else if (sf.hasTag("amenity", "school")) {
        minZoom = 15;
      } else if (sf.hasTag("amenity", "cafe")) {
        minZoom = 15;
      } else if (sf.hasTag("landuse", "cemetery")) {
        kind = sf.getString("landuse");
        minZoom = 14;
      } else if (sf.hasTag("landuse", "military")) {
        kind = "military";
        if (sf.hasTag("military", "naval_base", "airfield")) {
          kind = sf.getString("military");
        }
      } else if (sf.hasTag("leisure", "park")) {
        kind = "park";
        // Lots of pocket parks and NODE parks, show those later than rest of leisure
        minZoom = 14;
      } else if (sf.hasTag("leisure", "golf_course", "marina", "stadium")) {
        kind = sf.getString("leisure");
        minZoom = 13;
      } else if (sf.hasTag("shop", "grocery", "supermarket")) {
        kind = sf.getString("shop");
        minZoom = 14;
      } else if (sf.hasTag("tourism", "attraction", "camp_site", "hotel")) {
        kind = sf.getString("tourism");
        minZoom = 15;
      } else if (sf.hasTag("highway", "bus_stop")) {
        kind = sf.getString("highway");
        minZoom = 17;
      } else if (sf.hasTag("natural", "peak")) {
        kind = sf.getString("natural");
        minZoom = 13;
      } else {
        // Avoid problem of too many "other" kinds
        // All these will default to min_zoom of 15
        // If a more specific min_zoom is needed (or sanitize kind values)
        // then add new logic in section above
        if (sf.hasTag("attraction")) {
          kind = sf.getString("attraction");
        } else if (sf.hasTag("craft")) {
          kind = sf.getString("craft");
        } else if (sf.hasTag("aeroway")) {
          kind = sf.getString("aeroway");
        } else if (sf.hasTag("landuse")) {
          kind = sf.getString("landuse");
        } else if (sf.hasTag("leisure")) {
          kind = sf.getString("leisure");
        } else if (sf.hasTag("natural")) {
          kind = sf.getString("natural");
        } else if (sf.hasTag("railway")) {
          kind = sf.getString("railway");
        } else if (sf.hasTag("highway")) {
          kind = sf.getString("highway");
        } else if (sf.hasTag("shop")) {
          kind = sf.getString("shop");
        } else if (sf.hasTag("tourism")) {
          kind = sf.getString("tourism");
          // Boundary is most generic, so place last else we loose out
          // on nature_reserve detail versus all the protected_area
        } else if (sf.hasTag("historic") && !sf.hasTag("historic", "yes")) {
          kind = sf.getString("historic");
        } else if (sf.hasTag("boundary")) {
          // kind = sf.getString("boundary");
        }
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
          // kind = "national_park";
          minZoom = 11;
        } else {
          // kind = "park";
        }
      }

      if (sf.hasTag("cuisine")) {
        kindDetail = sf.getString("cuisine");
      } else if (sf.hasTag("religion")) {
        kindDetail = sf.getString("religion");
      } else if (sf.hasTag("sport")) {
        kindDetail = sf.getString("sport");
      }

      // try first for polygon -> point representations
      if (sf.canBePolygon() && sf.hasTag("name") && sf.getString("name") != null) {
        Double wayArea = 0.0;
        try {
          wayArea = sf.worldGeometry().getEnvelopeInternal().getArea() / WORLD_AREA_FOR_70K_SQUARE_METERS;
        } catch (GeometryException e) {
          e.log("Exception in POI way calculation");
        }

        double height = 0.0;
        if (sf.hasTag("height")) {
          Double parsed = parseDoubleOrNull(sf.getString("height"));
          if (parsed != null) {
            height = parsed;
          }
        }

        // Area zoom grading overrides the kind zoom grading in the section above.
        // Roughly shared with the water label area zoom grading in physical points layer
        //
        // Allowlist of kind values eligible for early zoom point labels
        if (kind.equals("national_park")) {
          if (wayArea > 300000) { // 500000000 sq meters (web mercator proj)
            minZoom = 5;
          } else if (wayArea > 25000) { // 500000000 sq meters (web mercator proj)
            minZoom = 6;
          } else if (wayArea > 10000) { // 500000000
            minZoom = 7;
          } else if (wayArea > 2000) { // 200000000
            minZoom = 8;
          } else if (wayArea > 250) { //  40000000
            minZoom = 9;
          } else if (wayArea > 100) { //   8000000
            minZoom = 10;
          } else if (wayArea > 20) { //    500000
            minZoom = 11;
          } else if (wayArea > 5) {
            minZoom = 12;
          } else if (wayArea > 1) {
            minZoom = 13;
          } else if (wayArea > 0.25) {
            minZoom = 14;
          }
        } else if (kind.equals("aerodrome") ||
          kind.equals("golf_course") ||
          kind.equals("military") ||
          kind.equals("naval_base") ||
          kind.equals("stadium") ||
          kind.equals("zoo")) {
          if (wayArea > 20000) { // 500000000
            minZoom = 7;
          } else if (wayArea > 5000) { // 200000000
            minZoom = 8;
          } else if (wayArea > 250) { //  40000000
            minZoom = 9;
          } else if (wayArea > 100) { //   8000000
            minZoom = 10;
          } else if (wayArea > 20) { //    500000
            minZoom = 11;
          } else if (wayArea > 5) {
            minZoom = 12;
          } else if (wayArea > 1) {
            minZoom = 13;
          } else if (wayArea > 0.25) {
            minZoom = 14;
          }

          // Emphasize large international airports earlier
          // Because the area grading resets the earlier dispensation
          if (kind.equals("aerodrome")) {
            if (sf.hasTag("iata")) {
              // prioritize international airports over regional airports
              minZoom -= 2;

              // but don't show international airports tooooo early
              if (minZoom < 10) {
                minZoom = 10;
              }
            } else {
              // and show other airports only once their polygon begins to be visible
              if (minZoom < 12) {
                minZoom = 12;
              }
            }
          }
        } else if (kind.equals("college") ||
          kind.equals("university")) {
          if (wayArea > 20000) {
            minZoom = 7;
          } else if (wayArea > 5000) {
            minZoom = 8;
          } else if (wayArea > 250) {
            minZoom = 9;
          } else if (wayArea > 150) {
            minZoom = 10;
          } else if (wayArea > 100) {
            minZoom = 11;
          } else if (wayArea > 50) {
            minZoom = 12;
          } else if (wayArea > 20) {
            minZoom = 13;
          } else if (wayArea > 5) {
            minZoom = 14;
          } else {
            minZoom = 15;
          }

          // Hack for weird San Francisco university
          if (sf.getString("name").equals("Academy of Art University")) {
            minZoom = 14;
          }
        } else if (kind.equals("forest") ||
          kind.equals("park") ||
          kind.equals("protected_area") ||
          kind.equals("nature_reserve") ||
          kind.equals("village_green")) {
          if (wayArea > 10000) {
            minZoom = 7;
          } else if (wayArea > 4000) {
            minZoom = 8;
          } else if (wayArea > 1000) {
            minZoom = 9;
          } else if (wayArea > 250) {
            minZoom = 10;
          } else if (wayArea > 15) {
            minZoom = 11;
          } else if (wayArea > 5) {
            minZoom = 12;
          } else if (wayArea > 1) {
            minZoom = 13;
          } else if (wayArea > 0.25) {
            minZoom = 14;
          } else if (wayArea > 0.01) {
            minZoom = 15;
          } else if (wayArea > 0.001) {
            minZoom = 16;
          } else {
            minZoom = 17;
          }

          // Discount wilderness areas within US national forests and parks
          if (kind.equals("nature_reserve") && sf.getString("name").contains("Wilderness")) {
            minZoom = minZoom + 1;
          }
        } else if (kind.equals("cemetery") ||
          kind.equals("school")) {
          if (wayArea > 5) {
            minZoom = 12;
          } else if (wayArea > 1) {
            minZoom = 13;
          } else if (wayArea > 0.1) {
            minZoom = 14;
          } else if (wayArea > 0.01) {
            minZoom = 15;
          } else {
            minZoom = 16;
          }
          // Typically for "building" derived label placements for shops and other businesses
        } else if (kind.equals("allotments")) {
          if (wayArea > 0.01) {
            minZoom = 15;
          } else {
            minZoom = 16;
          }
        } else if (kind.equals("playground")) {
          minZoom = 17;
        } else {
          if (wayArea > 10) {
            minZoom = 11;
          } else if (wayArea > 2) {
            minZoom = 12;
          } else if (wayArea > 0.5) {
            minZoom = 13;
          } else if (wayArea > 0.01) {
            minZoom = 14;
          }

          // Small but tall features should show up early as they have regional prominance.
          // Height measured in meters
          if (minZoom >= 13 && height > 0.0) {
            if (height >= 100) {
              minZoom = 11;
            } else if (height >= 20) {
              minZoom = 12;
            } else if (height >= 10) {
              minZoom = 13;
            }

            // Clamp certain kind values so medium tall buildings don't crowd downtown areas
            // NOTE: (nvkelso 20230623) Apply label grid to early zooms of POIs layer
            // NOTE: (nvkelso 20230624) Turn this into an allowlist instead of a blocklist
            if (kind.equals("hotel") || kind.equals("hostel") || kind.equals("parking") || kind.equals("bank") ||
              kind.equals("place_of_worship") || kind.equals("jewelry") || kind.equals("yes") ||
              kind.equals("restaurant") || kind.equals("coworking_space") || kind.equals("clothes") ||
              kind.equals("art") || kind.equals("school")) {
              if (minZoom == 12) {
                minZoom = 13;
              }
            }

            // Discount tall self storage buildings
            if (kind.equals("storage_rental")) {
              minZoom = 14;
            }

            // Discount tall university buildings, require a related university landuse AOI
            if (kind.equals("university")) {
              minZoom = 13;
            }
          }
        }

        // very long text names should only be shown at later zooms
        if (minZoom < 14) {
          var nameLength = sf.getString("name").length();

          if (nameLength > 30) {
            if (nameLength > 45) {
              minZoom += 2;
            } else {
              minZoom += 1;
            }
          }
        }

        var rankedZoom = QrankDb.assignZoom(qrankGrading, kind, qrank);
        if (rankedZoom.isPresent())
          minZoom = rankedZoom.get();

        var polyLabelPosition = features.pointOnSurface(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("kind", kind)
          // While other layers don't need min_zoom, POIs do for more predictable client-side label collisions
          // 512 px zooms versus 256 px logical zooms
          .setAttr("min_zoom", minZoom + 1)
          //
          // DEBUG
          //.setAttr("area_debug", wayArea)
          //
          // Core OSM tags for different kinds of places
          // Special airport only tag (to indicate if it's an airport with regular commercial flights)
          .setAttr("iata", sf.getString("iata"))
          .setAttr("elevation", sf.getString("ele"))
          // Extra OSM tags for certain kinds of places
          // These are duplicate of what's in the kind_detail tag
          .setBufferPixels(8)
          .setZoomRange(Math.min(15, minZoom), 15);

        // Core Tilezen schema properties
        if (!kindDetail.isEmpty()) {
          polyLabelPosition.setAttr("kind_detail", kindDetail);
        }

        OsmNames.setOsmNames(polyLabelPosition, sf, 0);

        // Server sort features so client label collisions are pre-sorted
        // NOTE: (nvkelso 20230627) This could also include other params like the name
        polyLabelPosition.setSortKey(minZoom * 1000);

        // Even with the categorical zoom bucketing above, we end up with too dense a point feature spread in downtown
        // areas, so cull the labels which wouldn't label at earlier zooms than the max_zoom of 15
        polyLabelPosition.setPointLabelGridSizeAndLimit(14, 8, 1);

      } else if (sf.isPoint()) {
        var rankedZoom = QrankDb.assignZoom(qrankGrading, kind, qrank);
        if (rankedZoom.isPresent())
          minZoom = rankedZoom.get();

        var pointFeature = features.point(this.name())
          // all POIs should receive their IDs at all zooms
          // (there is no merging of POIs like with lines and polygons in other layers)
          .setId(FeatureId.create(sf))
          // Core Tilezen schema properties
          .setAttr("kind", kind)
          // While other layers don't need min_zoom, POIs do for more predictable client-side label collisions
          // 512 px zooms versus 256 px logical zooms
          .setAttr("min_zoom", minZoom + 1)
          // Core OSM tags for different kinds of places
          // Special airport only tag (to indicate if it's an airport with regular commercial flights)
          .setAttr("iata", sf.getString("iata"))
          .setBufferPixels(8)
          .setZoomRange(Math.min(minZoom, 15), 15);

        // Core Tilezen schema properties
        if (!kindDetail.isEmpty()) {
          pointFeature.setAttr("kind_detail", kindDetail);
        }

        OsmNames.setOsmNames(pointFeature, sf, 0);

        // Some features should only be visible at very late zooms when they don't have a name
        if (!sf.hasTag("name") && (sf.hasTag("amenity", "atm", "bbq", "bench", "bicycle_parking",
          "bicycle_rental", "bicycle_repair_station", "boat_storage", "bureau_de_change", "car_rental", "car_sharing",
          "car_wash", "charging_station", "customs", "drinking_water", "fuel", "harbourmaster", "hunting_stand",
          "karaoke_box", "life_ring", "money_transfer", "motorcycle_parking", "parking", "picnic_table", "post_box",
          "ranger_station", "recycling", "sanitary_dump_station", "shelter", "shower", "taxi", "telephone", "toilets",
          "waste_basket", "waste_disposal", "water_point", "watering_place", "bicycle_rental", "motorcycle_parking",
          "charging_station") ||
          sf.hasTag("historic", "landmark", "wayside_cross") ||
          sf.hasTag("leisure", "dog_park", "firepit", "fishing", "pitch", "playground", "slipway", "swimming_area") ||
          sf.hasTag("tourism", "alpine_hut", "information", "picnic_site", "viewpoint", "wilderness_hut"))) {
          pointFeature.setAttr("min_zoom", 17);
        }

        if (sf.hasTag("amenity", "clinic", "dentist", "doctors", "social_facility", "baby_hatch", "childcare",
          "car_sharing", "bureau_de_change", "emergency_phone", "karaoke", "karaoke_box", "money_transfer", "car_wash",
          "hunting_stand", "studio", "boat_storage", "gambling", "adult_gaming_centre", "sanitary_dump_station",
          "attraction", "animal", "water_slide", "roller_coaster", "summer_toboggan", "carousel", "amusement_ride",
          "maze") ||
          sf.hasTag("historic", "memorial", "district") ||
          sf.hasTag("leisure", "pitch", "playground", "slipway") ||
          sf.hasTag("shop", "scuba_diving", "atv", "motorcycle", "snowmobile", "art", "bakery", "beauty", "bookmaker",
            "books", "butcher", "car", "car_parts", "car_repair", "clothes", "computer", "convenience", "fashion",
            "florist", "garden_centre", "gift", "golf", "greengrocer", "grocery", "hairdresser", "hifi", "jewelry",
            "lottery", "mobile_phone", "newsagent", "optician", "perfumery", "ship_chandler", "stationery", "tobacco",
            "travel_agency") ||
          sf.hasTag("tourism", "artwork", "hanami", "trail_riding_station", "bed_and_breakfast", "chalet",
            "guest_house", "hostel")) {
          pointFeature.setAttr("min_zoom", 17);
        }

        // Server sort features so client label collisions are pre-sorted
        // NOTE: (nvkelso 20230627) This could also include other params like the name
        pointFeature.setSortKey(minZoom * 1000);

        // Even with the categorical zoom bucketing above, we end up with too dense a point feature spread in downtown
        // areas, so cull the labels which wouldn't label at earlier zooms than the max_zoom of 15
        pointFeature.setPointLabelGridSizeAndLimit(14, 8, 1);
      }
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return items;
  }
}
