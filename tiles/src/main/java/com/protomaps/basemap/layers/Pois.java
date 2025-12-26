package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;
import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.withPoint;
import static com.protomaps.basemap.feature.Matcher.without;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.Expression;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.Matcher;
import com.protomaps.basemap.feature.QrankDb;
import com.protomaps.basemap.names.OsmNames;
import java.util.HashMap;
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

  private static final Expression WITH_OPERATOR_USFS = with("operator", "United States Forest Service",
    "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture",
    "US National Forest Service", "United State Forest Service", "U.S. National Forest Service");

  private static final MultiExpression.Index<Map<String, Object>> kindsIndex = MultiExpression.of(List.of(

    // Everything is "other"/"" at first
    rule(use("kind", "other"), use("kindDetail", "")),

    // Boundary is most generic, so place early else we lose out
    // on nature_reserve detail versus all the protected_area
    rule(with("boundary"), use("kind", fromTag("boundary"))),

    // More specific kinds

    rule(with("historic"), without("historic", "yes"), use("kind", fromTag("historic"))),
    rule(with("tourism"), use("kind", fromTag("tourism"))),
    rule(with("shop"), use("kind", fromTag("shop"))),
    rule(with("highway"), use("kind", fromTag("highway"))),
    rule(with("railway"), use("kind", fromTag("railway"))),
    rule(with("natural"), use("kind", fromTag("natural"))),
    rule(with("leisure"), use("kind", fromTag("leisure"))),
    rule(with("landuse"), use("kind", fromTag("landuse"))),
    rule(with("aeroway"), use("kind", fromTag("aeroway"))),
    rule(with("craft"), use("kind", fromTag("craft"))),
    rule(with("attraction"), use("kind", fromTag("attraction"))),
    rule(with("amenity"), use("kind", fromTag("amenity"))),

    // National forests

    rule(
      Expression.or(
        with("landuse", "forest"),
        Expression.and(with("boundary", "national_park"), WITH_OPERATOR_USFS),
        Expression.and(
          with("boundary", "national_park"),
          with("protect_class", "6"),
          with("protection_title", "National Forest")
        ),
        Expression.and(
          with("boundary", "protected_area"),
          with("protect_class", "6"),
          WITH_OPERATOR_USFS
        )
      ),
      use("kind", "forest")
    ),

    // National parks

    rule(with("boundary", "national_park"), use("kind", "park")),
    rule(
      with("boundary", "national_park"),
      Expression.not(WITH_OPERATOR_USFS),
      without("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve",
        "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site",
        "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest",
        "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area",
        "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary"),
      Expression.or(
        with("protect_class", "2", "3"),
        with("operator", "United States National Park Service", "National Park Service", "US National Park Service",
          "U.S. National Park Service", "US National Park service"),
        with("operator:en", "Parks Canada"),
        with("designation", "national_park"),
        with("protection_title", "National Park")
      ),
      use("kind", "national_park")
    ),

    // Remaining things

    rule(with("natural", "peak"), use("kind", fromTag("natural"))),
    rule(with("highway", "bus_stop"), use("kind", fromTag("highway"))),
    rule(with("tourism", "attraction", "camp_site", "hotel"), use("kind", fromTag("tourism"))),
    rule(with("shop", "grocery", "supermarket"), use("kind", fromTag("shop"))),
    rule(with("leisure", "golf_course", "marina", "stadium", "park"), use("kind", fromTag("leisure"))),

    rule(with("landuse", "military"), use("kind", "military")),
    rule(
      with("landuse", "military"),
      with("military", "naval_base", "airfield"),
      use("kind", fromTag("military"))
    ),

    rule(with("landuse", "cemetery"), use("kind", fromTag("landuse"))),

    rule(
      with("aeroway", "aerodrome"),
      use("kind", "aerodrome"),
      use("kindDetail", fromTag("aerodrome"))
    ),

    // Additional details for certain classes of POI

    rule(with("sport"), use("kindDetail", fromTag("sport"))),
    rule(with("religion"), use("kindDetail", fromTag("religion"))),
    rule(with("cuisine"), use("kindDetail", fromTag("cuisine")))

  )).index();

  private static final MultiExpression.Index<Map<String, Object>> zoomsIndex = MultiExpression.of(List.of(

    // Everything is zoom=15 at first
    rule(use("minZoom", 15)),

    rule(with("protomaps-basemaps:kind", "national_park"), use("minZoom", 11)),
    rule(with("natural", "peak"), use("minZoom", 13)),
    rule(with("highway", "bus_stop"), use("minZoom", 17)),
    rule(with("tourism", "attraction", "camp_site", "hotel"), use("minZoom", 15)),
    rule(with("shop", "grocery", "supermarket"), use("minZoom", 14)),
    rule(with("leisure", "golf_course", "marina", "stadium"), use("minZoom", 13)),
    rule(with("leisure", "park"), use("minZoom", 14)), // Lots of pocket parks and NODE parks, show those later than rest of leisure
    rule(with("landuse", "cemetery"), use("minZoom", 14)),
    rule(with("amenity", "cafe"), use("minZoom", 15)),
    rule(with("amenity", "school"), use("minZoom", 15)),
    rule(with("amenity", "library", "post_office", "townhall"), use("minZoom", 13)),
    rule(with("amenity", "hospital"), use("minZoom", 12)),
    rule(with("amenity", "university", "college"), use("minZoom", 14)), // One would think University should be earlier, but there are lots of dinky node only places, so if the university has a large area, it'll naturally improve its zoom in another section...
    rule(with("aeroway", "aerodrome"), use("minZoom", 13)),

    // Emphasize large international airports earlier
    rule(
      with("aeroway", "aerodrome"),
      with("protomaps-basemaps:kind", "aerodrome"),
      with("iata"),
      use("minZoom", 11)
    ),

    rule(
      withPoint(),
      Expression.or(
        with("amenity", "clinic", "dentist", "doctors", "social_facility", "baby_hatch", "childcare",
          "car_sharing", "bureau_de_change", "emergency_phone", "karaoke", "karaoke_box", "money_transfer", "car_wash",
          "hunting_stand", "studio", "boat_storage", "gambling", "adult_gaming_centre", "sanitary_dump_station",
          "attraction", "animal", "water_slide", "roller_coaster", "summer_toboggan", "carousel", "amusement_ride",
          "maze"),
        with("historic", "memorial", "district"),
        with("leisure", "pitch", "playground", "slipway"),
        with("shop", "scuba_diving", "atv", "motorcycle", "snowmobile", "art", "bakery", "beauty", "bookmaker",
          "books", "butcher", "car", "car_parts", "car_repair", "clothes", "computer", "convenience", "fashion",
          "florist", "garden_centre", "gift", "golf", "greengrocer", "grocery", "hairdresser", "hifi", "jewelry",
          "lottery", "mobile_phone", "newsagent", "optician", "perfumery", "ship_chandler", "stationery", "tobacco",
          "travel_agency"),
        with("tourism", "artwork", "hanami", "trail_riding_station", "bed_and_breakfast", "chalet",
          "guest_house", "hostel")
      ),
      use("minZoom", 16)
    ),

    // Some features should only be visible at very late zooms when they don't have a name
    rule(
      withPoint(),
      without("name"),
      Expression.or(
        with("amenity", "atm", "bbq", "bench", "bicycle_parking",
          "bicycle_rental", "bicycle_repair_station", "boat_storage", "bureau_de_change", "car_rental", "car_sharing",
          "car_wash", "charging_station", "customs", "drinking_water", "fuel", "harbourmaster", "hunting_stand",
          "karaoke_box", "life_ring", "money_transfer", "motorcycle_parking", "parking", "picnic_table", "post_box",
          "ranger_station", "recycling", "sanitary_dump_station", "shelter", "shower", "taxi", "telephone", "toilets",
          "waste_basket", "waste_disposal", "water_point", "watering_place", "bicycle_rental", "motorcycle_parking",
          "charging_station"),
        with("historic", "landmark", "wayside_cross"),
        with("leisure", "dog_park", "firepit", "fishing", "pitch", "playground", "slipway", "swimming_area"),
        with("tourism", "alpine_hut", "information", "picnic_site", "viewpoint", "wilderness_hut")
      ),
      use("minZoom", 16)
    ),

    rule(
      with("protomaps-basemaps:hasNamedPolygon"),
      with("protomaps-basemaps:kind", "playground"),
      use("minZoom", 17)
    ),
    rule(
      with("protomaps-basemaps:hasNamedPolygon"),
      with("protomaps-basemaps:kind", "allotments"),
      use("minZoom", 16)
    ),
    rule(
      with("protomaps-basemaps:hasNamedPolygon"),
      Expression.or(with("protomaps-basemaps:kind", "cemetery"), with("protomaps-basemaps:kind", "school")),
      use("minZoom", 16)
    ),
    rule(
      with("protomaps-basemaps:hasNamedPolygon"),
      Expression.or(
        with("protomaps-basemaps:kind", "forest"),
        with("protomaps-basemaps:kind", "park"),
        with("protomaps-basemaps:kind", "protected_area"),
        with("protomaps-basemaps:kind", "nature_reserve"),
        with("protomaps-basemaps:kind", "village_green")
      ),
      use("minZoom", 17)
    ),
    rule(
      with("protomaps-basemaps:hasNamedPolygon"),
      Expression.or(with("protomaps-basemaps:kind", "college"), with("protomaps-basemaps:kind", "university")),
      use("minZoom", 15)
    ),
    rule(
      with("protomaps-basemaps:hasNamedPolygon"),
      Expression.or(
        with("protomaps-basemaps:kind", "national_park"),
        with("protomaps-basemaps:kind", "aerodrome"),
        with("protomaps-basemaps:kind", "golf_course"),
        with("protomaps-basemaps:kind", "military"),
        with("protomaps-basemaps:kind", "naval_base"),
        with("protomaps-basemaps:kind", "stadium"),
        with("protomaps-basemaps:kind", "zoo")
      ),
      use("minZoom", 14)
    )

  )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // ~= pow((sqrt(7e4) / (4e7 / 256)) / 256, 2) ~= 4.4e-11
  private static final double WORLD_AREA_FOR_70K_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70_000)) / 256d, 2);

  public Matcher.SourceFeatureWithComputedTags computeExtraTags(SourceFeature sf, String kind) {
    Map<String, Object> computedTags = new HashMap<>(Map.of(
      "protomaps-basemaps:kind", kind,
      "protomaps-basemaps:wayArea", 0.0,
      "protomaps-basemaps:height", 0.0
    ));

    if (sf.canBePolygon() && sf.hasTag("name") && sf.getString("name") != null) {
      computedTags.put("protomaps-basemaps:hasNamedPolygon", true);
      try {
        Double area = sf.worldGeometry().getEnvelopeInternal().getArea() / WORLD_AREA_FOR_70K_SQUARE_METERS;
        computedTags.put("protomaps-basemaps:wayArea", area);
      } catch (GeometryException e) {
        e.log("Exception in POI way calculation");
      }
      if (sf.hasTag("height")) {
        Double parsed = parseDoubleOrNull(sf.getString("height"));
        if (parsed != null) {
          computedTags.put("protomaps-basemaps:height", parsed);
        }
      }
    }

    return new Matcher.SourceFeatureWithComputedTags(sf, computedTags);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    var kindMatches = kindsIndex.getMatches(sf);
    if (kindMatches.isEmpty()) {
      return;
    }

    // Calculate dimensions and create a wrapper with computed tags
    var sf2 = computeExtraTags(sf, getString(sf, kindMatches, "kind", "undefined"));
    var zoomMatches = zoomsIndex.getMatches(sf2);
    if (zoomMatches.isEmpty()) {
      return;
    }

    String kind = getString(sf2, kindMatches, "kind", "undefined");
    String kindDetail = getString(sf2, kindMatches, "kindDetail", "undefined");
    Integer minZoom = getInteger(sf2, zoomMatches, "minZoom", 99);

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
      long qrank = 0;

      String wikidata = sf.getString("wikidata");
      if (wikidata != null) {
        qrank = qrankDb.get(wikidata);
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
            //minZoom = 14;
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
            //minZoom = 14;
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
            //minZoom = 15;
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
            //minZoom = 17;
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
            //minZoom = 16;
          }
          // Typically for "building" derived label placements for shops and other businesses
        } else if (kind.equals("allotments")) {
          if (wayArea > 0.01) {
            minZoom = 15;
          } else {
            //minZoom = 16;
          }
        } else if (kind.equals("playground")) {
          // minZoom = 17;
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
