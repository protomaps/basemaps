package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;
import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.withPoint;
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

  // Internal tags used to reference calculated values between matchers
  private static final String KIND = "protomaps-basemaps:kind";
  private static final String WAYAREA = "protomaps-basemaps:wayArea";
  private static final String HEIGHT = "protomaps-basemaps:height";
  private static final String HAS_NAMED_POLYGON = "protomaps-basemaps:hasNamedPolygon";

  private static final Expression WITH_OPERATOR_USFS = with("operator", "United States Forest Service",
    "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture",
    "US National Forest Service", "United State Forest Service", "U.S. National Forest Service");

  private static final MultiExpression.Index<Map<String, Object>> kindsIndex = MultiExpression.ofOrdered(List.of(

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

  // Shorthand expressions to save space below

  private static final Expression with_named_polygon = with(HAS_NAMED_POLYGON);
  private static final Expression with_s_c_named_poly =
    Expression.and(with_named_polygon, with(KIND, "cemetery", "school"));
  private static final Expression with_n_p_named_poly =
    Expression.and(with_named_polygon, with(KIND, "national_park"));
  private static final Expression with_c_u_named_poly =
    Expression.and(with_named_polygon, with(KIND, "college", "university"));
  private static final Expression with_b_g_named_poly = Expression.and(with_named_polygon,
    with(KIND, "forest", "park", "protected_area", "nature_reserve", "village_green"));
  private static final Expression with_etc_named_poly = Expression.and(with_named_polygon,
    with(KIND, "aerodrome", "golf_course", "military", "naval_base", "stadium", "zoo"));

  private static final MultiExpression.Index<Map<String, Object>> zoomsIndex = MultiExpression.ofOrdered(List.of(

    // Everything with a point or a valid tag is zoom=15 at first
    rule(
      Expression.or(
        withPoint(),
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
      use("minZoom", 15)
    ),

    // Promote important point categories to earlier zooms

    rule(
      withPoint(),
      Expression.or(
        with("amenity", "university", "college"), // One would think University should be earlier, but there are lots of dinky node only places, so if the university has a large area, it'll naturally improve its zoom in another section...
        with("landuse", "cemetery"),
        with("leisure", "park"), // Lots of pocket parks and NODE parks, show those later than rest of leisure
        with("shop", "grocery", "supermarket")
      ),
      use("minZoom", 14)
    ),
    rule(
      withPoint(),
      Expression.or(
        with("aeroway", "aerodrome"),
        with("amenity", "library", "post_office", "townhall"),
        with("leisure", "golf_course", "marina", "stadium"),
        with("natural", "peak")
      ),
      use("minZoom", 13)
    ),
    rule(withPoint(), with("amenity", "hospital"), use("minZoom", 12)),
    rule(withPoint(), with(KIND, "national_park"), use("minZoom", 11)),
    rule(withPoint(), with("aeroway", "aerodrome"), with(KIND, "aerodrome"), with("iata"), use("minZoom", 11)), // Emphasize large international airports earlier

    // Demote some unimportant point categories to very late zooms

    rule(with("highway", "bus_stop"), use("minZoom", 17)),
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

    // Demote some unnamed point categories to very late zooms

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

    // Size-graded polygons, generic at first then per-kind adjustments

    rule(with_named_polygon, withinRange(WAYAREA, 10, 500), use("minZoom", 14)),
    rule(with_named_polygon, withinRange(WAYAREA, 500, 2000), use("minZoom", 13)),
    rule(with_named_polygon, withinRange(WAYAREA, 2000, 1e4), use("minZoom", 12)),
    rule(with_named_polygon, withinRange(WAYAREA, 1e4), use("minZoom", 11)),

    rule(with_named_polygon, with(KIND, "playground"), use("minZoom", 17)),
    rule(with_named_polygon, with(KIND, "allotments"), withinRange(WAYAREA, 0, 10), use("minZoom", 16)),
    rule(with_named_polygon, with(KIND, "allotments"), withinRange(WAYAREA, 10), use("minZoom", 15)),

    // Height-graded polygons, generic at first then per-kind adjustments
    // Small but tall features should show up early as they have regional prominence.
    // Height measured in meters

    rule(with_named_polygon, withinRange(WAYAREA, 10, 2000), withinRange(HEIGHT, 10, 20), use("minZoom", 13)),
    rule(with_named_polygon, withinRange(WAYAREA, 10, 2000), withinRange(HEIGHT, 20, 100), use("minZoom", 12)),
    rule(with_named_polygon, withinRange(WAYAREA, 10, 2000), withinRange(HEIGHT, 100), use("minZoom", 11)),

    // Clamp certain kind values so medium tall buildings don't crowd downtown areas
    // NOTE: (nvkelso 20230623) Apply label grid to early zooms of POIs layer
    // NOTE: (nvkelso 20230624) Turn this into an allowlist instead of a blocklist
    rule(
      with_named_polygon,
      with(KIND, "hotel", "hostel", "parking", "bank", "place_of_worship", "jewelry", "yes", "restaurant",
        "coworking_space", "clothes", "art", "school"),
      withinRange(WAYAREA, 10, 2000),
      withinRange(HEIGHT, 20, 100),
      use("minZoom", 13)
    ),
    // Discount tall self storage buildings
    rule(with_named_polygon, with(KIND, "storage_rental"), withinRange(WAYAREA, 10, 2000), use("minZoom", 14)),
    // Discount tall university buildings, require a related university landuse AOI
    rule(with_named_polygon, with(KIND, "university"), withinRange(WAYAREA, 10, 2000), use("minZoom", 13)),

    // Schools & Cemeteries

    rule(with_s_c_named_poly, withinRange(WAYAREA, 0, 10), use("minZoom", 16)),
    rule(with_s_c_named_poly, withinRange(WAYAREA, 10, 100), use("minZoom", 15)),
    rule(with_s_c_named_poly, withinRange(WAYAREA, 100, 1000), use("minZoom", 14)),
    rule(with_s_c_named_poly, withinRange(WAYAREA, 1000, 5000), use("minZoom", 13)),
    rule(with_s_c_named_poly, withinRange(WAYAREA, 5000), use("minZoom", 12)),

    // National parks

    rule(with_n_p_named_poly, withinRange(WAYAREA, 0, 250), use("minZoom", 17)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 250, 1000), use("minZoom", 14)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 1000, 5000), use("minZoom", 13)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 5000, 2e4), use("minZoom", 12)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 2e4, 1e5), use("minZoom", 11)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 1e5, 2.5e5), use("minZoom", 10)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 2.5e5, 2e6), use("minZoom", 9)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 2e6, 1e7), use("minZoom", 8)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 1e7, 2.5e7), use("minZoom", 7)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 2.5e7, 3e8), use("minZoom", 6)),
    rule(with_n_p_named_poly, withinRange(WAYAREA, 3e8), use("minZoom", 5)),

    // College and university polygons

    rule(with_c_u_named_poly, withinRange(WAYAREA, 0, 5000), use("minZoom", 15)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 5000, 2e4), use("minZoom", 14)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 2e4, 5e4), use("minZoom", 13)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 5e4, 1e5), use("minZoom", 12)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 1e5, 1.5e5), use("minZoom", 11)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 1.5e5, 2.5e5), use("minZoom", 10)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 2.5e5, 5e6), use("minZoom", 9)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 5e6, 2e7), use("minZoom", 8)),
    rule(with_c_u_named_poly, withinRange(WAYAREA, 2e7), use("minZoom", 7)),
    rule(with_c_u_named_poly, with("name", "Academy of Art University"), use("minZoom", 14)), // Hack for weird San Francisco university

    // Big green polygons

    rule(with_b_g_named_poly, withinRange(WAYAREA, 0, 1), use("minZoom", 17)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 1, 10), use("minZoom", 16)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 10, 250), use("minZoom", 15)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 250, 1000), use("minZoom", 14)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 1000, 5000), use("minZoom", 13)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 5000, 1.5e4), use("minZoom", 12)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 1.5e4, 2.5e5), use("minZoom", 11)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 2.5e5, 1e6), use("minZoom", 10)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 1e6, 4e6), use("minZoom", 9)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 4e6, 1e7), use("minZoom", 8)),
    rule(with_b_g_named_poly, withinRange(WAYAREA, 1e7), use("minZoom", 7)),

    // Remaining grab-bag of scaled kinds

    rule(with_etc_named_poly, withinRange(WAYAREA, 250, 1000), use("minZoom", 14)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 1000, 5000), use("minZoom", 13)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 5000, 2e4), use("minZoom", 12)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 2e4, 1e5), use("minZoom", 11)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 1e5, 2.5e5), use("minZoom", 10)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 2.5e5, 5e6), use("minZoom", 9)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 5e6, 2e7), use("minZoom", 8)),
    rule(with_etc_named_poly, withinRange(WAYAREA, 2e7), use("minZoom", 7))

  )).index();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // ~= pow((sqrt(70) / (4e7 / 256)) / 256, 2) ~= 4.4e-14
  private static final double WORLD_AREA_FOR_70_SQUARE_METERS =
    Math.pow(GeoUtils.metersToPixelAtEquator(0, Math.sqrt(70)) / 256d, 2);

  public Matcher.SourceFeatureWithComputedTags computeExtraTags(SourceFeature sf, String kind) {
    Double wayArea = 0.0;
    Double height = 0.0;
    Boolean hasNamedPolygon = false;

    if (sf.canBePolygon() && sf.hasTag("name") && sf.getString("name") != null) {
      hasNamedPolygon = true;
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
      computedTags = Map.of(KIND, kind, WAYAREA, wayArea, HEIGHT, height, HAS_NAMED_POLYGON, true);
    } else {
      computedTags = Map.of(KIND, kind, WAYAREA, wayArea, HEIGHT, height);
    }

    return new Matcher.SourceFeatureWithComputedTags(sf, computedTags);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    // We only do POI display for points and named polygons
    if (!(sf.isPoint() || sf.canBePolygon() && sf.hasTag("name") && sf.getString("name") != null))
      return;

    // Map the Protomaps "kind" classification to incoming tags
    var kindMatches = kindsIndex.getMatches(sf);
    if (kindMatches.isEmpty())
      return;

    // Output feature and its basic values to assign
    FeatureCollector.Feature outputFeature;
    String kind = getString(sf, kindMatches, "kind", "undefined");
    String kindDetail = getString(sf, kindMatches, "kindDetail", "undefined");
    Integer minZoom;

    // QRank may override minZoom entirely
    String wikidata = sf.getString("wikidata");
    long qrank = (wikidata != null) ? qrankDb.get(wikidata) : 0;
    var qrankedZoom = QrankDb.assignZoom(qrankGrading, kind, qrank);

    if (qrankedZoom.isPresent()) {
      // Set minZoom from QRank
      minZoom = qrankedZoom.get();
    } else {
      // Calculate minZoom using zoomsIndex
      var sf2 = computeExtraTags(sf, getString(sf, kindMatches, "kind", "undefined"));
      var zoomMatches = zoomsIndex.getMatches(sf2);
      if (zoomMatches.isEmpty())
        return;

      // Initial minZoom
      minZoom = getInteger(sf2, zoomMatches, "minZoom", 99);

      // Adjusted minZoom
      if (sf.canBePolygon()) {
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
    if (sf.canBePolygon()) {
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
    if (!kindDetail.isEmpty())
      outputFeature.setAttr("kind_detail", kindDetail);

    OsmNames.setOsmNames(outputFeature, sf, 0);

    // Server sort features so client label collisions are pre-sorted
    // NOTE: (nvkelso 20230627) This could also include other params like the name
    outputFeature.setSortKey(minZoom * 1000);

    // Even with the categorical zoom bucketing above, we end up with too dense a point feature spread in downtown
    // areas, so cull the labels which wouldn't label at earlier zooms than the max_zoom of 15
    outputFeature.setPointLabelGridSizeAndLimit(14, 8, 1);
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return items;
  }
}
