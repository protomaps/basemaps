package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;
import static com.protomaps.basemap.feature.Matcher.atLeast;
import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.withinRange;
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

  // OSM tags to Protomaps kind/kind_detail mapping

  private static final MultiExpression.Index<Map<String, Object>> osmKindsIndex = MultiExpression.ofOrdered(List.of(

    // Everything is undefined at first
    rule(use("pm:kind", "pm:undefined"), use("pm:kindDetail", "pm:undefined")),

    // An initial set of tags we like
    rule(
      Expression.or(
        with("aeroway", "aerodrome"),
        with("amenity"),
        with("attraction"),
        with("boundary", "national_park", "protected_area"),
        with("craft"),
        with("highway", "bus_stop"),
        with("historic"),
        with("landuse", "cemetery", "recreation_ground", "winter_sports", "quarry", "park", "forest", "military",
          "village_green", "allotments"),
        with("leisure"),
        with("natural", "beach", "peak"),
        with("railway", "station"),
        with("shop"),
        Expression.and(with("tourism"), without("historic", "district"))
      ),
      use("pm:kind", "other")
    ),

    // Boundary is most generic, so place early else we lose out
    // on nature_reserve detail versus all the protected_area
    rule(with("boundary"), use("pm:kind", fromTag("boundary"))),

    // More specific kinds

    rule(with("historic"), without("historic", "yes"), use("pm:kind", fromTag("historic"))),
    rule(with("tourism"), use("pm:kind", fromTag("tourism"))),
    rule(with("shop"), use("pm:kind", fromTag("shop"))),
    rule(with("highway"), use("pm:kind", fromTag("highway"))),
    rule(with("railway"), use("pm:kind", fromTag("railway"))),
    rule(with("natural"), use("pm:kind", fromTag("natural"))),
    rule(with("leisure"), use("pm:kind", fromTag("leisure"))),
    rule(with("landuse"), use("pm:kind", fromTag("landuse"))),
    rule(with("aeroway"), use("pm:kind", fromTag("aeroway"))),
    rule(with("craft"), use("pm:kind", fromTag("craft"))),
    rule(with("attraction"), use("pm:kind", fromTag("attraction"))),
    rule(with("amenity"), use("pm:kind", fromTag("amenity"))),

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
      use("pm:kind", "forest")
    ),

    // National parks

    rule(with("boundary", "national_park"), use("pm:kind", "park")),
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
      use("pm:kind", "national_park")
    ),

    // Remaining things

    rule(with("natural", "peak"), use("pm:kind", fromTag("natural"))),
    rule(with("highway", "bus_stop"), use("pm:kind", fromTag("highway"))),
    rule(with("tourism", "attraction", "camp_site", "hotel"), use("pm:kind", fromTag("tourism"))),
    rule(with("shop", "grocery", "supermarket"), use("pm:kind", fromTag("shop"))),
    rule(with("leisure", "golf_course", "marina", "stadium", "park"), use("pm:kind", fromTag("leisure"))),

    rule(with("landuse", "military"), use("pm:kind", "military")),
    rule(
      with("landuse", "military"),
      with("military", "naval_base", "airfield"),
      use("pm:kind", fromTag("military"))
    ),

    rule(with("landuse", "cemetery"), use("pm:kind", fromTag("landuse"))),

    rule(
      with("aeroway", "aerodrome"),
      use("pm:kind", "aerodrome"),
      use("pm:kindDetail", fromTag("aerodrome"))
    ),

    // Additional details for certain classes of POI

    rule(with("sport"), use("pm:kindDetail", fromTag("sport"))),
    rule(with("religion"), use("pm:kindDetail", fromTag("religion"))),
    rule(with("cuisine"), use("pm:kindDetail", fromTag("cuisine")))

  )).index();


  // Overture properties to Protomaps kind/kind_detail mapping

  private static final MultiExpression.Index<Map<String, Object>> overtureKindsIndex =
    MultiExpression.ofOrdered(List.of(

      // Everything is undefined at first
      rule(use("pm:kind", "pm:undefined"), use("pm:kindDetail", "pm:undefined")),

      // Pull from basic_category
      rule(with("basic_category"), use("pm:kind", fromTag("basic_category"))),

      // Some basic categories don't match OSM-style expectations
      rule(with("basic_category", "accommodation"), with("categories.primary", "hostel"), use("pm:kind", "hostel")),
      rule(with("basic_category", "airport"), use("pm:kind", "aerodrome")),
      rule(with("basic_category", "college_university"), use("pm:kind", "college")),
      rule(with("basic_category", "grocery_store"), use("pm:kind", "supermarket")),
      rule(with("basic_category", "sport_stadium"), use("pm:kind", "stadium")),
      rule(with("basic_category", "place_of_learning", "middle_school"), use("pm:kind", "school"))

    )).index();

  // Protomaps kind/kind_detail to min_zoom mapping for points

  private static final MultiExpression.Index<Map<String, Object>> pointZoomsIndex = MultiExpression.ofOrdered(List.of(

    // Every point is zoom=15 at first
    rule(use("pm:minzoom", 15)),

    // Promote important point categories to earlier zooms

    rule(
      Expression.or(
        with("pm:kind", "university", "college"), // One would think University should be earlier, but there are lots of dinky node only places, so if the university has a large area, it'll naturally improve its zoom in another section...
        with("pm:kind", "cemetery"),
        with("pm:kind", "park"), // Lots of pocket parks and NODE parks, show those later than rest of leisure
        with("pm:kind", "grocery", "supermarket")
      ),
      use("pm:minzoom", 14)
    ),
    rule(
      Expression.or(
        with("pm:kind", "aerodrome"),
        with("pm:kind", "library", "post_office", "townhall"),
        with("pm:kind", "golf_course", "marina", "stadium"),
        with("pm:kind", "peak")
      ),
      use("pm:minzoom", 13)
    ),
    rule(with("pm:kind", "hospital"), use("pm:minzoom", 12)),
    rule(with("pm:kind", "national_park"), use("pm:minzoom", 11)),
    rule(with("pm:kind", "aerodrome"), with("pm:kind", "aerodrome"), with("iata"), use("pm:minzoom", 11)), // Emphasize large international airports earlier

    // Demote some unimportant point categories to very late zooms

    rule(with("pm:kind", "bus_stop"), use("pm:minzoom", 17)),
    rule(
      Expression.or(
        with("pm:kind", "clinic", "dentist", "doctors", "social_facility", "baby_hatch", "childcare",
          "car_sharing", "bureau_de_change", "emergency_phone", "karaoke", "karaoke_box", "money_transfer", "car_wash",
          "hunting_stand", "studio", "boat_storage", "gambling", "adult_gaming_centre", "sanitary_dump_station",
          "animal", "roller_coaster", "summer_toboggan", "carousel", "amusement_ride",
          "maze"),
        with("pm:kind", "memorial", "district"),
        with("pm:kind", "pitch", "playground", "slipway"),
        with("pm:kind", "scuba_diving", "atv", "motorcycle", "snowmobile", "art", "bakery", "beauty", "bookmaker",
          "books", "butcher", "car", "car_parts", "car_repair", "clothes", "computer", "convenience", "fashion",
          "florist", "garden_centre", "gift", "golf", "greengrocer", "grocery", "hairdresser", "hifi", "jewelry",
          "lottery", "mobile_phone", "newsagent", "optician", "perfumery", "ship_chandler", "stationery", "tobacco",
          "travel_agency"),
        with("pm:kind", "artwork", "hanami", "trail_riding_station", "bed_and_breakfast", "chalet",
          "guest_house", "hostel")
      ),
      use("pm:minzoom", 16)
    ),

    // Demote some unnamed point categories to very late zooms

    rule(
      without("name"),
      Expression.or(
        with("pm:kind", "atm", "bbq", "bench", "bicycle_parking",
          "bicycle_rental", "bicycle_repair_station", "boat_storage", "bureau_de_change", "car_rental", "car_sharing",
          "car_wash", "charging_station", "customs", "drinking_water", "fuel", "harbourmaster", "hunting_stand",
          "karaoke_box", "life_ring", "money_transfer", "motorcycle_parking", "parking", "picnic_table", "post_box",
          "ranger_station", "recycling", "sanitary_dump_station", "shelter", "shower", "taxi", "telephone", "toilets",
          "waste_basket", "waste_disposal", "water_point", "watering_place", "bicycle_rental", "motorcycle_parking",
          "charging_station"),
        with("pm:kind", "landmark", "wayside_cross"),
        with("pm:kind", "dog_park", "firepit", "fishing", "pitch", "playground", "slipway", "swimming_area"),
        with("pm:kind", "alpine_hut", "information", "picnic_site", "viewpoint", "wilderness_hut")
      ),
      use("pm:minzoom", 16)
    )

  )).index();

  // Shorthand expressions to save space below

  private static final Expression WITH_S_C = with("pm:kind", "cemetery", "school");
  private static final Expression WITH_N_P = with("pm:kind", "national_park");
  private static final Expression WITH_C_U = with("pm:kind", "college", "university");
  private static final Expression WITH_B_G =
    with("pm:kind", "forest", "park", "protected_area", "nature_reserve", "village_green");
  private static final Expression WITH_ETC =
    with("pm:kind", "aerodrome", "golf_course", "military", "naval_base", "stadium", "zoo");

  // Protomaps kind/kind_detail to min_zoom mapping for named polygons

  private static final MultiExpression.Index<Map<String, Object>> namedPolygonZoomsIndex =
    MultiExpression.ofOrdered(List.of(

      // Every named polygon is zoom=15 at first
      rule(use("pm:minzoom", 15)),

      // Size-graded polygons, generic at first then per-kind adjustments

      rule(withinRange("pm:wayarea", 10, 500), use("pm:minzoom", 14)),
      rule(withinRange("pm:wayarea", 500, 2000), use("pm:minzoom", 13)),
      rule(withinRange("pm:wayarea", 2000, 1e4), use("pm:minzoom", 12)),
      rule(atLeast("pm:wayarea", 1e4), use("pm:minzoom", 11)),

      rule(with("pm:kind", "playground"), use("pm:minzoom", 17)),
      rule(with("pm:kind", "allotments"), withinRange("pm:wayarea", 0, 10), use("pm:minzoom", 16)),
      rule(with("pm:kind", "allotments"), atLeast("pm:wayarea", 10), use("pm:minzoom", 15)),

      // Height-graded polygons, generic at first then per-kind adjustments
      // Small but tall features should show up early as they have regional prominence.
      // Height measured in meters

      rule(withinRange("pm:wayarea", 10, 2000), withinRange("pm:height", 10, 20), use("pm:minzoom", 13)),
      rule(withinRange("pm:wayarea", 10, 2000), withinRange("pm:height", 20, 100), use("pm:minzoom", 12)),
      rule(withinRange("pm:wayarea", 10, 2000), atLeast("pm:height", 100), use("pm:minzoom", 11)),

      // Clamp certain kind values so medium tall buildings don't crowd downtown areas
      // NOTE: (nvkelso 20230623) Apply label grid to early zooms of POIs layer
      // NOTE: (nvkelso 20230624) Turn this into an allowlist instead of a blocklist
      rule(
        with("pm:kind", "hotel", "hostel", "parking", "bank", "place_of_worship", "jewelry", "yes", "restaurant",
          "coworking_space", "clothes", "art", "school"),
        withinRange("pm:wayarea", 10, 2000),
        withinRange("pm:height", 20, 100),
        use("pm:minzoom", 13)
      ),
      // Discount tall self storage buildings
      rule(with("pm:kind", "storage_rental"), withinRange("pm:wayarea", 10, 2000), use("pm:minzoom", 14)),
      // Discount tall university buildings, require a related university landuse AOI
      rule(with("pm:kind", "university"), withinRange("pm:wayarea", 10, 2000), use("pm:minzoom", 13)),

      // Schools & Cemeteries

      rule(WITH_S_C, withinRange("pm:wayarea", 0, 10), use("pm:minzoom", 16)),
      rule(WITH_S_C, withinRange("pm:wayarea", 10, 100), use("pm:minzoom", 15)),
      rule(WITH_S_C, withinRange("pm:wayarea", 100, 1000), use("pm:minzoom", 14)),
      rule(WITH_S_C, withinRange("pm:wayarea", 1000, 5000), use("pm:minzoom", 13)),
      rule(WITH_S_C, atLeast("pm:wayarea", 5000), use("pm:minzoom", 12)),

      // National parks

      rule(WITH_N_P, withinRange("pm:wayarea", 0, 250), use("pm:minzoom", 17)),
      rule(WITH_N_P, withinRange("pm:wayarea", 250, 1000), use("pm:minzoom", 14)),
      rule(WITH_N_P, withinRange("pm:wayarea", 1000, 5000), use("pm:minzoom", 13)),
      rule(WITH_N_P, withinRange("pm:wayarea", 5000, 2e4), use("pm:minzoom", 12)),
      rule(WITH_N_P, withinRange("pm:wayarea", 2e4, 1e5), use("pm:minzoom", 11)),
      rule(WITH_N_P, withinRange("pm:wayarea", 1e5, 2.5e5), use("pm:minzoom", 10)),
      rule(WITH_N_P, withinRange("pm:wayarea", 2.5e5, 2e6), use("pm:minzoom", 9)),
      rule(WITH_N_P, withinRange("pm:wayarea", 2e6, 1e7), use("pm:minzoom", 8)),
      rule(WITH_N_P, withinRange("pm:wayarea", 1e7, 2.5e7), use("pm:minzoom", 7)),
      rule(WITH_N_P, withinRange("pm:wayarea", 2.5e7, 3e8), use("pm:minzoom", 6)),
      rule(WITH_N_P, atLeast("pm:wayarea", 3e8), use("pm:minzoom", 5)),

      // College and university polygons

      rule(WITH_C_U, withinRange("pm:wayarea", 0, 5000), use("pm:minzoom", 15)),
      rule(WITH_C_U, withinRange("pm:wayarea", 5000, 2e4), use("pm:minzoom", 14)),
      rule(WITH_C_U, withinRange("pm:wayarea", 2e4, 5e4), use("pm:minzoom", 13)),
      rule(WITH_C_U, withinRange("pm:wayarea", 5e4, 1e5), use("pm:minzoom", 12)),
      rule(WITH_C_U, withinRange("pm:wayarea", 1e5, 1.5e5), use("pm:minzoom", 11)),
      rule(WITH_C_U, withinRange("pm:wayarea", 1.5e5, 2.5e5), use("pm:minzoom", 10)),
      rule(WITH_C_U, withinRange("pm:wayarea", 2.5e5, 5e6), use("pm:minzoom", 9)),
      rule(WITH_C_U, withinRange("pm:wayarea", 5e6, 2e7), use("pm:minzoom", 8)),
      rule(WITH_C_U, atLeast("pm:wayarea", 2e7), use("pm:minzoom", 7)),
      rule(WITH_C_U, with("name", "Academy of Art University"), use("pm:minzoom", 14)), // Hack for weird San Francisco university

      // Big green polygons

      rule(WITH_B_G, withinRange("pm:wayarea", 0, 1), use("pm:minzoom", 17)),
      rule(WITH_B_G, withinRange("pm:wayarea", 1, 10), use("pm:minzoom", 16)),
      rule(WITH_B_G, withinRange("pm:wayarea", 10, 250), use("pm:minzoom", 15)),
      rule(WITH_B_G, withinRange("pm:wayarea", 250, 1000), use("pm:minzoom", 14)),
      rule(WITH_B_G, withinRange("pm:wayarea", 1000, 5000), use("pm:minzoom", 13)),
      rule(WITH_B_G, withinRange("pm:wayarea", 5000, 1.5e4), use("pm:minzoom", 12)),
      rule(WITH_B_G, withinRange("pm:wayarea", 1.5e4, 2.5e5), use("pm:minzoom", 11)),
      rule(WITH_B_G, withinRange("pm:wayarea", 2.5e5, 1e6), use("pm:minzoom", 10)),
      rule(WITH_B_G, withinRange("pm:wayarea", 1e6, 4e6), use("pm:minzoom", 9)),
      rule(WITH_B_G, withinRange("pm:wayarea", 4e6, 1e7), use("pm:minzoom", 8)),
      rule(WITH_B_G, atLeast("pm:wayarea", 1e7), use("pm:minzoom", 7)),

      // Remaining grab-bag of scaled kinds

      rule(WITH_ETC, withinRange("pm:wayarea", 250, 1000), use("pm:minzoom", 14)),
      rule(WITH_ETC, withinRange("pm:wayarea", 1000, 5000), use("pm:minzoom", 13)),
      rule(WITH_ETC, withinRange("pm:wayarea", 5000, 2e4), use("pm:minzoom", 12)),
      rule(WITH_ETC, withinRange("pm:wayarea", 2e4, 1e5), use("pm:minzoom", 11)),
      rule(WITH_ETC, withinRange("pm:wayarea", 1e5, 2.5e5), use("pm:minzoom", 10)),
      rule(WITH_ETC, withinRange("pm:wayarea", 2.5e5, 5e6), use("pm:minzoom", 9)),
      rule(WITH_ETC, withinRange("pm:wayarea", 5e6, 2e7), use("pm:minzoom", 8)),
      rule(WITH_ETC, atLeast("pm:wayarea", 2e7), use("pm:minzoom", 7))

    )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // ~= pow((sqrt(70) / (4e7 / 256)) / 256, 2) ~= 4.4e-14
  private static final double WORLD_AREA_FOR_70_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70)) / 256d, 2);

  private boolean isNamedPolygon(SourceFeature sf) {
    return sf.canBePolygon() && sf.hasTag("name") && sf.getString("name") != null;
  }

  public Matcher.SourceFeatureWithComputedTags computeExtraTags(SourceFeature sf, String kind) {
    Double wayArea = 0.0;
    Double height = 0.0;
    boolean hasNamedPolygon = isNamedPolygon(sf);

    if (hasNamedPolygon) {
      try {
        wayArea = sf.worldGeometry().getEnvelopeInternal().getArea() / WORLD_AREA_FOR_70_SQUARE_METERS;
      } catch (GeometryException e) {
        e.log("Exception in POI way calculation");
      }
      if (sf.hasTag("height")) {
        Double parsed = parseDoubleOrNull(sf.getString("height"));
        if (parsed != null) {
          height = parsed;
        }
      }
    }

    Map<String, Object> computedTags;

    if (hasNamedPolygon) {
      computedTags = Map.of("pm:kind", kind, "pm:wayarea", wayArea, "pm:height", height, "pm:hasNamedPolygon", true);
    } else {
      computedTags = Map.of("pm:kind", kind, "pm:wayarea", wayArea, "pm:height", height);
    }

    return new Matcher.SourceFeatureWithComputedTags(sf, computedTags);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    boolean hasNamedPolygon = isNamedPolygon(sf);

    // We only do POI display for points and named polygons
    if (!sf.isPoint() && !hasNamedPolygon)
      return;

    // Map the Protomaps "pm:kind" classification to incoming tags
    var kindMatches = osmKindsIndex.getMatches(sf);

    // Output feature and its basic values to assign
    FeatureCollector.Feature outputFeature;
    String kind = getString(sf, kindMatches, "pm:kind", "pm:undefined");
    String kindDetail = getString(sf, kindMatches, "pm:kindDetail", "pm:undefined");
    Integer minZoom;

    // Quickly eliminate any features with non-matching tags
    if (kind.equals("pm:undefined"))
      return;

    // QRank may override minZoom entirely
    String wikidata = sf.getString("wikidata");
    long qrank = (wikidata != null) ? qrankDb.get(wikidata) : 0;
    var qrankedZoom = QrankDb.assignZoom(qrankGrading, kind, qrank);

    if (qrankedZoom.isPresent()) {
      // Set minZoom from QRank
      minZoom = qrankedZoom.get();
    } else {
      // Calculate minZoom using zooms indexes
      var sf2 = computeExtraTags(sf, getString(sf, kindMatches, "pm:kind", "pm:undefined"));
      var zoomMatches = hasNamedPolygon ? namedPolygonZoomsIndex.getMatches(sf2) : pointZoomsIndex.getMatches(sf2);

      // Initial minZoom
      minZoom = getInteger(sf2, zoomMatches, "pm:minzoom", 99);

      // Adjusted minZoom
      if (hasNamedPolygon) {
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

        // Discount wilderness areas within US national forests and parks
        if (kind.equals("nature_reserve") && sf.getString("name").contains("Wilderness")) {
          minZoom += 1;
        }

        // very long text names should only be shown at later zooms
        if (minZoom < 14) {
          var nameLength = sf.getString("name").length();

          if (nameLength > 45) {
            minZoom += 2;
          } else if (nameLength > 30) {
            minZoom += 1;
          }
        }
      }
    }

    // Assign outputFeature
    if (hasNamedPolygon) {
      outputFeature = features.pointOnSurface(this.name())
        //.setAttr("area_debug", wayArea) // DEBUG
        .setAttr("elevation", sf.getString("ele"));
    } else if (sf.isPoint()) {
      outputFeature = features.point(this.name());
    } else {
      return;
    }

    // Populate final outputFeature attributes
    outputFeature
      // all POIs should receive their IDs at all zooms
      // (there is no merging of POIs like with lines and polygons in other layers)
      .setId(FeatureId.create(sf))
      // Core Tilezen schema properties
      .setAttr("kind", kind)
      // While other layers don't need min_zoom, POIs do for more predictable client-side label collisions
      // 512 px zooms versus 256 px logical zooms
      .setAttr("min_zoom", minZoom + 1)
      //
      .setBufferPixels(8)
      .setZoomRange(Math.min(minZoom, 15), 15)
      // Core OSM tags for different kinds of places
      // Special airport only tag (to indicate if it's an airport with regular commercial flights)
      .setAttr("iata", sf.getString("iata"));

    // Core Tilezen schema properties
    if (!kindDetail.equals("pm:undefined"))
      outputFeature.setAttr("kind_detail", kindDetail);

    OsmNames.setOsmNames(outputFeature, sf, 0);

    // Server sort features so client label collisions are pre-sorted
    // NOTE: (nvkelso 20230627) This could also include other params like the name
    outputFeature.setSortKey(minZoom * 1000);

    // Even with the categorical zoom bucketing above, we end up with too dense a point feature spread in downtown
    // areas, so cull the labels which wouldn't label at earlier zooms than the max_zoom of 15
    outputFeature.setPointLabelGridSizeAndLimit(14, 8, 1);
  }

  public void processOverture(SourceFeature sf, FeatureCollector features) {
    // Filter by type field - Overture transportation theme
    if (!"places".equals(sf.getString("theme"))) {
      return;
    }

    if (!"place".equals(sf.getString("type"))) {
      return;
    }

    // Map the Protomaps "pm:kind" classification to incoming tags
    var kindMatches = overtureKindsIndex.getMatches(sf);

    String kind = getString(sf, kindMatches, "pm:kind", "pm:undefined");
    Integer minZoom;

    // Quickly eliminate any features with non-matching tags
    if (kind.equals("pm:undefined"))
      return;

    // QRank may override minZoom entirely
    String wikidata = sf.getString("wikidata");
    long qrank = (wikidata != null) ? qrankDb.get(wikidata) : 0;
    var qrankedZoom = QrankDb.assignZoom(qrankGrading, kind, qrank);

    if (qrankedZoom.isPresent()) {
      // Set minZoom from QRank
      minZoom = qrankedZoom.get();
    } else {
      // Calculate minZoom using zooms indexes
      var sf2 = computeExtraTags(sf, getString(sf, kindMatches, "pm:kind", "pm:undefined"));
      var zoomMatches = pointZoomsIndex.getMatches(sf2);

      // Initial minZoom
      minZoom = getInteger(sf2, zoomMatches, "pm:minzoom", 99);
    }

    String name = sf.getString("names.primary");

    features.point(this.name())
      // all POIs should receive their IDs at all zooms
      // (there is no merging of POIs like with lines and polygons in other layers)
      //.setId(FeatureId.create(sf))
      // Core Tilezen schema properties
      .setAttr("kind", kind)
      .setAttr("name", name)
      // While other layers don't need min_zoom, POIs do for more predictable client-side label collisions
      // 512 px zooms versus 256 px logical zooms
      .setAttr("min_zoom", minZoom + 1)
      //
      .setBufferPixels(8)
      .setZoomRange(Math.min(minZoom, 15), 15);
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return items;
  }
}
