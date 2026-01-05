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

  // Internal tags used to reference calculated values between matchers
  private static final String KIND = "protomaps-basemaps:kind";
  private static final String KIND_DETAIL = "protomaps-basemaps:kindDetail";
  private static final String MINZOOM = "protomaps-basemaps:minZoom";
  private static final String WAYAREA = "protomaps-basemaps:wayArea";
  private static final String HEIGHT = "protomaps-basemaps:height";
  private static final String HAS_NAMED_POLYGON = "protomaps-basemaps:hasNamedPolygon";
  private static final String UNDEFINED = "protomaps-basemaps:undefined";

  private static final Expression WITH_OPERATOR_USFS = with("operator", "United States Forest Service",
    "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture",
    "US National Forest Service", "United State Forest Service", "U.S. National Forest Service");

  private static final MultiExpression.Index<Map<String, Object>> kindsIndex = MultiExpression.ofOrdered(List.of(

    // Everything is undefined at first
    rule(use(KIND, UNDEFINED), use(KIND_DETAIL, UNDEFINED)),

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
      use(KIND, "other")
    ),

    // Boundary is most generic, so place early else we lose out
    // on nature_reserve detail versus all the protected_area
    rule(with("boundary"), use(KIND, fromTag("boundary"))),

    // More specific kinds

    rule(with("historic"), without("historic", "yes"), use(KIND, fromTag("historic"))),
    rule(with("tourism"), use(KIND, fromTag("tourism"))),
    rule(with("shop"), use(KIND, fromTag("shop"))),
    rule(with("highway"), use(KIND, fromTag("highway"))),
    rule(with("railway"), use(KIND, fromTag("railway"))),
    rule(with("natural"), use(KIND, fromTag("natural"))),
    rule(with("leisure"), use(KIND, fromTag("leisure"))),
    rule(with("landuse"), use(KIND, fromTag("landuse"))),
    rule(with("aeroway"), use(KIND, fromTag("aeroway"))),
    rule(with("craft"), use(KIND, fromTag("craft"))),
    rule(with("attraction"), use(KIND, fromTag("attraction"))),
    rule(with("amenity"), use(KIND, fromTag("amenity"))),

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
      use(KIND, "forest")
    ),

    // National parks

    rule(with("boundary", "national_park"), use(KIND, "park")),
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
      use(KIND, "national_park")
    ),

    // Remaining things

    rule(with("natural", "peak"), use(KIND, fromTag("natural"))),
    rule(with("highway", "bus_stop"), use(KIND, fromTag("highway"))),
    rule(with("tourism", "attraction", "camp_site", "hotel"), use(KIND, fromTag("tourism"))),
    rule(with("shop", "grocery", "supermarket"), use(KIND, fromTag("shop"))),
    rule(with("leisure", "golf_course", "marina", "stadium", "park"), use(KIND, fromTag("leisure"))),

    rule(with("landuse", "military"), use(KIND, "military")),
    rule(
      with("landuse", "military"),
      with("military", "naval_base", "airfield"),
      use(KIND, fromTag("military"))
    ),

    rule(with("landuse", "cemetery"), use(KIND, fromTag("landuse"))),

    rule(
      with("aeroway", "aerodrome"),
      use(KIND, "aerodrome"),
      use(KIND_DETAIL, fromTag("aerodrome"))
    ),

    // Additional details for certain classes of POI

    rule(with("sport"), use(KIND_DETAIL, fromTag("sport"))),
    rule(with("religion"), use(KIND_DETAIL, fromTag("religion"))),
    rule(with("cuisine"), use(KIND_DETAIL, fromTag("cuisine")))

  )).index();

  private static final MultiExpression.Index<Map<String, Object>> pointZoomsIndex = MultiExpression.ofOrdered(List.of(

    // Every point is zoom=15 at first
    rule(use(MINZOOM, 15)),

    // Promote important point categories to earlier zooms

    rule(
      Expression.or(
        with("amenity", "university", "college"), // One would think University should be earlier, but there are lots of dinky node only places, so if the university has a large area, it'll naturally improve its zoom in another section...
        with("landuse", "cemetery"),
        with("leisure", "park"), // Lots of pocket parks and NODE parks, show those later than rest of leisure
        with("shop", "grocery", "supermarket")
      ),
      use(MINZOOM, 14)
    ),
    rule(
      Expression.or(
        with("aeroway", "aerodrome"),
        with("amenity", "library", "post_office", "townhall"),
        with("leisure", "golf_course", "marina", "stadium"),
        with("natural", "peak")
      ),
      use(MINZOOM, 13)
    ),
    rule(with("amenity", "hospital"), use(MINZOOM, 12)),
    rule(with(KIND, "national_park"), use(MINZOOM, 11)),
    rule(with("aeroway", "aerodrome"), with(KIND, "aerodrome"), with("iata"), use(MINZOOM, 11)), // Emphasize large international airports earlier

    // Demote some unimportant point categories to very late zooms

    rule(with("highway", "bus_stop"), use(MINZOOM, 17)),
    rule(
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
      use(MINZOOM, 16)
    ),

    // Demote some unnamed point categories to very late zooms

    rule(
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
      use(MINZOOM, 16)
    )

  )).index();

  // Shorthand expressions to save space below

  private static final Expression WITH_S_C = with(KIND, "cemetery", "school");
  private static final Expression WITH_N_P = with(KIND, "national_park");
  private static final Expression WITH_C_U = with(KIND, "college", "university");
  private static final Expression WITH_B_G =
    with(KIND, "forest", "park", "protected_area", "nature_reserve", "village_green");
  private static final Expression WITH_ETC =
    with(KIND, "aerodrome", "golf_course", "military", "naval_base", "stadium", "zoo");

  private static final MultiExpression.Index<Map<String, Object>> namedPolygonZoomsIndex =
    MultiExpression.ofOrdered(List.of(

      // Every named polygon is zoom=15 at first
      rule(use(MINZOOM, 15)),

      // Size-graded polygons, generic at first then per-kind adjustments

      rule(withinRange(WAYAREA, 10, 500), use(MINZOOM, 14)),
      rule(withinRange(WAYAREA, 500, 2000), use(MINZOOM, 13)),
      rule(withinRange(WAYAREA, 2000, 1e4), use(MINZOOM, 12)),
      rule(atLeast(WAYAREA, 1e4), use(MINZOOM, 11)),

      rule(with(KIND, "playground"), use(MINZOOM, 17)),
      rule(with(KIND, "allotments"), withinRange(WAYAREA, 0, 10), use(MINZOOM, 16)),
      rule(with(KIND, "allotments"), atLeast(WAYAREA, 10), use(MINZOOM, 15)),

      // Height-graded polygons, generic at first then per-kind adjustments
      // Small but tall features should show up early as they have regional prominence.
      // Height measured in meters

      rule(withinRange(WAYAREA, 10, 2000), withinRange(HEIGHT, 10, 20), use(MINZOOM, 13)),
      rule(withinRange(WAYAREA, 10, 2000), withinRange(HEIGHT, 20, 100), use(MINZOOM, 12)),
      rule(withinRange(WAYAREA, 10, 2000), atLeast(HEIGHT, 100), use(MINZOOM, 11)),

      // Clamp certain kind values so medium tall buildings don't crowd downtown areas
      // NOTE: (nvkelso 20230623) Apply label grid to early zooms of POIs layer
      // NOTE: (nvkelso 20230624) Turn this into an allowlist instead of a blocklist
      rule(
        with(KIND, "hotel", "hostel", "parking", "bank", "place_of_worship", "jewelry", "yes", "restaurant",
          "coworking_space", "clothes", "art", "school"),
        withinRange(WAYAREA, 10, 2000),
        withinRange(HEIGHT, 20, 100),
        use(MINZOOM, 13)
      ),
      // Discount tall self storage buildings
      rule(with(KIND, "storage_rental"), withinRange(WAYAREA, 10, 2000), use(MINZOOM, 14)),
      // Discount tall university buildings, require a related university landuse AOI
      rule(with(KIND, "university"), withinRange(WAYAREA, 10, 2000), use(MINZOOM, 13)),

      // Schools & Cemeteries

      rule(WITH_S_C, withinRange(WAYAREA, 0, 10), use(MINZOOM, 16)),
      rule(WITH_S_C, withinRange(WAYAREA, 10, 100), use(MINZOOM, 15)),
      rule(WITH_S_C, withinRange(WAYAREA, 100, 1000), use(MINZOOM, 14)),
      rule(WITH_S_C, withinRange(WAYAREA, 1000, 5000), use(MINZOOM, 13)),
      rule(WITH_S_C, atLeast(WAYAREA, 5000), use(MINZOOM, 12)),

      // National parks

      rule(WITH_N_P, withinRange(WAYAREA, 0, 250), use(MINZOOM, 17)),
      rule(WITH_N_P, withinRange(WAYAREA, 250, 1000), use(MINZOOM, 14)),
      rule(WITH_N_P, withinRange(WAYAREA, 1000, 5000), use(MINZOOM, 13)),
      rule(WITH_N_P, withinRange(WAYAREA, 5000, 2e4), use(MINZOOM, 12)),
      rule(WITH_N_P, withinRange(WAYAREA, 2e4, 1e5), use(MINZOOM, 11)),
      rule(WITH_N_P, withinRange(WAYAREA, 1e5, 2.5e5), use(MINZOOM, 10)),
      rule(WITH_N_P, withinRange(WAYAREA, 2.5e5, 2e6), use(MINZOOM, 9)),
      rule(WITH_N_P, withinRange(WAYAREA, 2e6, 1e7), use(MINZOOM, 8)),
      rule(WITH_N_P, withinRange(WAYAREA, 1e7, 2.5e7), use(MINZOOM, 7)),
      rule(WITH_N_P, withinRange(WAYAREA, 2.5e7, 3e8), use(MINZOOM, 6)),
      rule(WITH_N_P, atLeast(WAYAREA, 3e8), use(MINZOOM, 5)),

      // College and university polygons

      rule(WITH_C_U, withinRange(WAYAREA, 0, 5000), use(MINZOOM, 15)),
      rule(WITH_C_U, withinRange(WAYAREA, 5000, 2e4), use(MINZOOM, 14)),
      rule(WITH_C_U, withinRange(WAYAREA, 2e4, 5e4), use(MINZOOM, 13)),
      rule(WITH_C_U, withinRange(WAYAREA, 5e4, 1e5), use(MINZOOM, 12)),
      rule(WITH_C_U, withinRange(WAYAREA, 1e5, 1.5e5), use(MINZOOM, 11)),
      rule(WITH_C_U, withinRange(WAYAREA, 1.5e5, 2.5e5), use(MINZOOM, 10)),
      rule(WITH_C_U, withinRange(WAYAREA, 2.5e5, 5e6), use(MINZOOM, 9)),
      rule(WITH_C_U, withinRange(WAYAREA, 5e6, 2e7), use(MINZOOM, 8)),
      rule(WITH_C_U, atLeast(WAYAREA, 2e7), use(MINZOOM, 7)),
      rule(WITH_C_U, with("name", "Academy of Art University"), use(MINZOOM, 14)), // Hack for weird San Francisco university

      // Big green polygons

      rule(WITH_B_G, withinRange(WAYAREA, 0, 1), use(MINZOOM, 17)),
      rule(WITH_B_G, withinRange(WAYAREA, 1, 10), use(MINZOOM, 16)),
      rule(WITH_B_G, withinRange(WAYAREA, 10, 250), use(MINZOOM, 15)),
      rule(WITH_B_G, withinRange(WAYAREA, 250, 1000), use(MINZOOM, 14)),
      rule(WITH_B_G, withinRange(WAYAREA, 1000, 5000), use(MINZOOM, 13)),
      rule(WITH_B_G, withinRange(WAYAREA, 5000, 1.5e4), use(MINZOOM, 12)),
      rule(WITH_B_G, withinRange(WAYAREA, 1.5e4, 2.5e5), use(MINZOOM, 11)),
      rule(WITH_B_G, withinRange(WAYAREA, 2.5e5, 1e6), use(MINZOOM, 10)),
      rule(WITH_B_G, withinRange(WAYAREA, 1e6, 4e6), use(MINZOOM, 9)),
      rule(WITH_B_G, withinRange(WAYAREA, 4e6, 1e7), use(MINZOOM, 8)),
      rule(WITH_B_G, atLeast(WAYAREA, 1e7), use(MINZOOM, 7)),

      // Remaining grab-bag of scaled kinds

      rule(WITH_ETC, withinRange(WAYAREA, 250, 1000), use(MINZOOM, 14)),
      rule(WITH_ETC, withinRange(WAYAREA, 1000, 5000), use(MINZOOM, 13)),
      rule(WITH_ETC, withinRange(WAYAREA, 5000, 2e4), use(MINZOOM, 12)),
      rule(WITH_ETC, withinRange(WAYAREA, 2e4, 1e5), use(MINZOOM, 11)),
      rule(WITH_ETC, withinRange(WAYAREA, 1e5, 2.5e5), use(MINZOOM, 10)),
      rule(WITH_ETC, withinRange(WAYAREA, 2.5e5, 5e6), use(MINZOOM, 9)),
      rule(WITH_ETC, withinRange(WAYAREA, 5e6, 2e7), use(MINZOOM, 8)),
      rule(WITH_ETC, atLeast(WAYAREA, 2e7), use(MINZOOM, 7))

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
      computedTags = Map.of(KIND, kind, WAYAREA, wayArea, HEIGHT, height, HAS_NAMED_POLYGON, true);
    } else {
      computedTags = Map.of(KIND, kind, WAYAREA, wayArea, HEIGHT, height);
    }

    return new Matcher.SourceFeatureWithComputedTags(sf, computedTags);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    boolean hasNamedPolygon = isNamedPolygon(sf);

    // We only do POI display for points and named polygons
    if (!sf.isPoint() && !hasNamedPolygon)
      return;

    // Map the Protomaps KIND classification to incoming tags
    var kindMatches = kindsIndex.getMatches(sf);

    // Output feature and its basic values to assign
    FeatureCollector.Feature outputFeature;
    String kind = getString(sf, kindMatches, KIND, UNDEFINED);
    String kindDetail = getString(sf, kindMatches, KIND_DETAIL, UNDEFINED);
    Integer minZoom;

    // Quickly eliminate any features with non-matching tags
    if (kind.equals(UNDEFINED))
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
      var sf2 = computeExtraTags(sf, getString(sf, kindMatches, KIND, UNDEFINED));
      var zoomMatches = hasNamedPolygon ? namedPolygonZoomsIndex.getMatches(sf2) : pointZoomsIndex.getMatches(sf2);
      if (zoomMatches.isEmpty())
        return;

      // Initial minZoom
      minZoom = getInteger(sf2, zoomMatches, MINZOOM, 99);

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
    if (!kindDetail.equals(UNDEFINED))
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
