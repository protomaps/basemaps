package com.protomaps.basemap.layers;

import static com.protomaps.basemap.feature.Matcher.fromTag;
import static com.protomaps.basemap.feature.Matcher.getInteger;
import static com.protomaps.basemap.feature.Matcher.getString;
import static com.protomaps.basemap.feature.Matcher.rule;
import static com.protomaps.basemap.feature.Matcher.use;
import static com.protomaps.basemap.feature.Matcher.with;
import static com.protomaps.basemap.feature.Matcher.without;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.expression.MultiExpression;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.util.SortKey;
import com.onthegomap.planetiler.util.ZoomFunction;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.feature.NaturalEarthDb;
import com.protomaps.basemap.names.OsmNames;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Places implements ForwardingProfile.LayerPostProcessor {

  private NaturalEarthDb naturalEarthDb;
  private CountryCoder countryCoder;

  public Places(NaturalEarthDb naturalEarthDb, CountryCoder countryCoder) {
    this.naturalEarthDb = naturalEarthDb;
    this.countryCoder = countryCoder;
  }

  public static final String LAYER_NAME = "places";

  private static final MultiExpression.Index<Map<String, Object>> index = MultiExpression.ofOrdered(List.of(
    rule(
      with("population"),
      use("population", fromTag("population"))
    ),
    rule(
      with("place", "country"),
      use("kind", "country"),
      use("minZoom", 5),
      use("maxZoom", 8),
      use("kindRank", 0)
    ),
    rule(
      with("""
          place
          state
          province
        """),
      with("""
          _country
          US
          CA
          BR
          IN
          CN
          AU
        """
      ),
      use("kind", "region"),
      use("minZoom", 8),
      use("maxZoom", 11),
      use("kindRank", 1)
    ),
    rule(
      with("""
          place
          city
          town
        """),
      use("kind", "locality"),
      use("minZoom", 7),
      use("maxZoom", 15),
      use("kindRank", 2)
    ),
    rule(
      with("place", "city"),
      without("population"),
      use("population", 5000),
      use("minZoom", 8)
    ),
    rule(
      with("place", "town"),
      without("population"),
      use("population", 10000),
      use("minZoom", 9)
    ),
    rule(
      with("place", "village"),
      use("kind", "locality"),
      use("minZoom", 10),
      use("maxZoom", 15),
      use("kindRank", 3)
    ),
    rule(
      with("place", "village"),
      without("population"),
      use("minZoom", 11),
      use("population", 2000)
    ),
    rule(
      with("place", "locality"),
      use("kind", "locality"),
      use("minZoom", 11),
      use("maxZoom", 15),
      use("kindRank", 4)
    ),
    rule(
      with("place", "locality"),
      without("population"),
      use("minZoom", 12),
      use("population", 1000)
    ),
    rule(
      with("place", "hamlet"),
      use("kind", "locality"),
      use("minZoom", 11),
      use("maxZoom", 15),
      use("kindRank", 5)
    ),
    rule(
      with("place", "hamlet"),
      without("population"),
      use("minZoom", 12),
      use("population", 200)
    ),
    rule(
      with("place", "isolated_dwelling"),
      use("kind", "locality"),
      use("minZoom", 13),
      use("maxZoom", 15),
      use("kindRank", 6)
    ),
    rule(
      with("place", "isolated_dwelling"),
      without("population"),
      use("minZoom", 14),
      use("population", 100)
    ),
    rule(
      with("place", "farm"),
      use("kind", "locality"),
      use("minZoom", 13),
      use("maxZoom", 15),
      use("kindRank", 7)
    ),
    rule(
      with("place", "farm"),
      without("population"),
      use("minZoom", 14),
      use("population", 50)
    ),
    rule(
      with("place", "allotments"),
      use("kind", "locality"),
      use("minZoom", 13),
      use("maxZoom", 15),
      use("kindRank", 8)
    ),
    rule(
      with("place", "allotments"),
      without("population"),
      use("minZoom", 14),
      use("population", 1000)
    ),
    rule(
      with("place", "suburb"),
      use("kind", "neighbourhood"),
      use("minZoom", 11),
      use("maxZoom", 15),
      use("kindRank", 9)
    ),
    rule(
      with("place", "quarter"),
      use("kind", "macrohood"),
      use("minZoom", 10),
      use("maxZoom", 15),
      use("kindRank", 10)
    ),
    rule(
      with("place", "neighbourhood"),
      use("kind", "neighbourhood"),
      use("minZoom", 12),
      use("maxZoom", 15),
      use("kindRank", 11)
    )
  )).index();

  private record MinMaxZoom(int minZoom, int maxZoom) {}

  private static Map<String, MinMaxZoom> createCountryZooms() {
    Map<String, MinMaxZoom> countryZooms = new HashMap<>();
    countryZooms.put("Q228", new MinMaxZoom(4, 9)); // Andorra
    countryZooms.put("Q878", new MinMaxZoom(3, 8)); // United Arab Emirates
    countryZooms.put("Q889", new MinMaxZoom(2, 6)); // Afghanistan
    countryZooms.put("Q781", new MinMaxZoom(4, 9)); // Antigua and Barb
    countryZooms.put("Q25228", new MinMaxZoom(4, 9)); // Anguilla
    countryZooms.put("Q222", new MinMaxZoom(4, 9)); // Albania
    countryZooms.put("Q399", new MinMaxZoom(4, 9)); // Armenia
    countryZooms.put("Q916", new MinMaxZoom(2, 6)); // Angola
    countryZooms.put("Q414", new MinMaxZoom(1, 6)); // Argentina
    countryZooms.put("Q40", new MinMaxZoom(2, 7)); // Austria
    countryZooms.put("Q408", new MinMaxZoom(1, 5)); // Australia
    countryZooms.put("Q21203", new MinMaxZoom(4, 9)); // Aruba
    countryZooms.put("Q227", new MinMaxZoom(3, 8)); // Azerbaijan
    countryZooms.put("Q225", new MinMaxZoom(3, 9)); // Bosnia and Herz
    countryZooms.put("Q244", new MinMaxZoom(3, 9)); // Barbados
    countryZooms.put("Q902", new MinMaxZoom(2, 7)); // Bangladesh
    countryZooms.put("Q31", new MinMaxZoom(3, 8)); // Belgium
    countryZooms.put("Q965", new MinMaxZoom(2, 7)); // Burkina Faso
    countryZooms.put("Q219", new MinMaxZoom(3, 8)); // Bulgaria
    countryZooms.put("Q398", new MinMaxZoom(3, 8)); // Bahrain
    countryZooms.put("Q967", new MinMaxZoom(3, 8)); // Burundi
    countryZooms.put("Q962", new MinMaxZoom(3, 8)); // Benin
    countryZooms.put("Q23635", new MinMaxZoom(3, 8)); // Bermuda
    countryZooms.put("Q921", new MinMaxZoom(3, 8)); // Brunei
    countryZooms.put("Q750", new MinMaxZoom(2, 7)); // Bolivia
    countryZooms.put("Q155", new MinMaxZoom(1, 5)); // Brazil
    countryZooms.put("Q778", new MinMaxZoom(3, 8)); // Bahamas
    countryZooms.put("Q917", new MinMaxZoom(3, 8)); // Bhutan
    countryZooms.put("Q963", new MinMaxZoom(3, 8)); // Botswana
    countryZooms.put("Q184", new MinMaxZoom(2, 7)); // Belarus
    countryZooms.put("Q242", new MinMaxZoom(4, 9)); // Belize
    countryZooms.put("Q16", new MinMaxZoom(1, 5)); // Canada
    countryZooms.put("Q974", new MinMaxZoom(1, 6)); // Dem. Rep. Congo
    countryZooms.put("Q929", new MinMaxZoom(3, 8)); // Central African Rep
    countryZooms.put("Q971", new MinMaxZoom(3, 8)); // Congo
    countryZooms.put("Q39", new MinMaxZoom(3, 8)); // Switzerland
    countryZooms.put("Q1008", new MinMaxZoom(2, 7)); // Côte d'Ivoire
    countryZooms.put("Q26988", new MinMaxZoom(3, 8)); // Cook Is
    countryZooms.put("Q298", new MinMaxZoom(1, 6)); // Chile
    countryZooms.put("Q1009", new MinMaxZoom(2, 7)); // Cameroon
    countryZooms.put("Q148", new MinMaxZoom(1, 5)); // China
    countryZooms.put("Q739", new MinMaxZoom(1, 6)); // Colombia
    countryZooms.put("Q800", new MinMaxZoom(2, 7)); // Costa Rica
    countryZooms.put("Q241", new MinMaxZoom(2, 7)); // Cuba
    countryZooms.put("Q1011", new MinMaxZoom(3, 8)); // Cabo Verde
    countryZooms.put("Q25279", new MinMaxZoom(4, 9)); // Curaçao
    countryZooms.put("Q229", new MinMaxZoom(3, 9)); // Cyprus
    countryZooms.put("Q213", new MinMaxZoom(3, 8)); // Czechia
    countryZooms.put("Q183", new MinMaxZoom(1, 6)); // Germany
    countryZooms.put("Q977", new MinMaxZoom(3, 8)); // Djibouti
    countryZooms.put("Q35", new MinMaxZoom(2, 7)); // Denmark
    countryZooms.put("Q784", new MinMaxZoom(3, 8)); // Dominica
    countryZooms.put("Q786", new MinMaxZoom(3, 9)); // Dominican Rep
    countryZooms.put("Q262", new MinMaxZoom(2, 6)); // Algeria
    countryZooms.put("Q736", new MinMaxZoom(2, 7)); // Ecuador
    countryZooms.put("Q191", new MinMaxZoom(2, 7)); // Estonia
    countryZooms.put("Q79", new MinMaxZoom(1, 6)); // Egypt
    countryZooms.put("Q986", new MinMaxZoom(3, 8)); // Eritrea
    countryZooms.put("Q29", new MinMaxZoom(1, 6)); // Spain
    countryZooms.put("Q115", new MinMaxZoom(1, 6)); // Ethiopia
    countryZooms.put("Q33", new MinMaxZoom(2, 7)); // Finland
    countryZooms.put("Q712", new MinMaxZoom(2, 7)); // Fiji
    countryZooms.put("Q9648", new MinMaxZoom(3, 8)); // Falkland Is
    countryZooms.put("Q702", new MinMaxZoom(4, 9)); // Micronesia
    countryZooms.put("Q4628", new MinMaxZoom(3, 8)); // Faeroe Is
    countryZooms.put("Q142", new MinMaxZoom(1, 6)); // France
    countryZooms.put("Q1000", new MinMaxZoom(2, 7)); // Gabon
    countryZooms.put("Q145", new MinMaxZoom(1, 6)); // United Kingdom
    countryZooms.put("Q769", new MinMaxZoom(3, 8)); // Grenada
    countryZooms.put("Q230", new MinMaxZoom(3, 8)); // Georgia
    countryZooms.put("Q25230", new MinMaxZoom(4, 9)); // Guernsey
    countryZooms.put("Q117", new MinMaxZoom(2, 7)); // Ghana
    countryZooms.put("Q1410", new MinMaxZoom(4, 9)); // Gibraltar
    countryZooms.put("Q223", new MinMaxZoom(1, 6)); // Greenland
    countryZooms.put("Q1005", new MinMaxZoom(4, 9)); // Gambia
    countryZooms.put("Q1006", new MinMaxZoom(2, 7)); // Guinea
    countryZooms.put("Q983", new MinMaxZoom(3, 8)); // Eq. Guinea
    countryZooms.put("Q41", new MinMaxZoom(2, 7)); // Greece
    countryZooms.put("Q35086", new MinMaxZoom(4, 8)); // S. Geo. and the Is
    countryZooms.put("Q774", new MinMaxZoom(2, 7)); // Guatemala
    countryZooms.put("Q1007", new MinMaxZoom(4, 9)); // Guinea-Bissau
    countryZooms.put("Q734", new MinMaxZoom(3, 8)); // Guyana
    countryZooms.put("Q783", new MinMaxZoom(3, 9)); // Honduras
    countryZooms.put("Q224", new MinMaxZoom(3, 8)); // Croatia
    countryZooms.put("Q790", new MinMaxZoom(3, 8)); // Haiti
    countryZooms.put("Q28", new MinMaxZoom(3, 8)); // Hungary
    countryZooms.put("Q252", new MinMaxZoom(1, 6)); // Indonesia
    countryZooms.put("Q27", new MinMaxZoom(2, 7)); // Ireland
    countryZooms.put("Q801", new MinMaxZoom(2, 7)); // Israel
    countryZooms.put("Q9676", new MinMaxZoom(4, 9)); // Isle of Man
    countryZooms.put("Q668", new MinMaxZoom(1, 6)); // India
    countryZooms.put("Q43448", new MinMaxZoom(4, 9)); // Br. Indian Ocean Ter
    countryZooms.put("Q796", new MinMaxZoom(2, 7)); // Iraq
    countryZooms.put("Q794", new MinMaxZoom(1, 6)); // Iran
    countryZooms.put("Q189", new MinMaxZoom(1, 6)); // Iceland
    countryZooms.put("Q38", new MinMaxZoom(1, 6)); // Italy
    countryZooms.put("Q785", new MinMaxZoom(4, 9)); // Jersey
    countryZooms.put("Q766", new MinMaxZoom(3, 8)); // Jamaica
    countryZooms.put("Q810", new MinMaxZoom(3, 8)); // Jordan
    countryZooms.put("Q17", new MinMaxZoom(1, 6)); // Japan
    countryZooms.put("Q114", new MinMaxZoom(1, 6)); // Kenya
    countryZooms.put("Q813", new MinMaxZoom(2, 7)); // Kyrgyzstan
    countryZooms.put("Q424", new MinMaxZoom(2, 7)); // Cambodia
    countryZooms.put("Q710", new MinMaxZoom(4, 9)); // Kiribati
    countryZooms.put("Q970", new MinMaxZoom(3, 8)); // Comoros
    countryZooms.put("Q763", new MinMaxZoom(4, 9)); // St. Kitts and Nevis
    countryZooms.put("Q423", new MinMaxZoom(2, 7)); // North Korea
    countryZooms.put("Q884", new MinMaxZoom(2, 6)); // South Korea
    countryZooms.put("Q817", new MinMaxZoom(4, 9)); // Kuwait
    countryZooms.put("Q5785", new MinMaxZoom(4, 9)); // Cayman Is
    countryZooms.put("Q232", new MinMaxZoom(2, 6)); // Kazakhstan
    countryZooms.put("Q819", new MinMaxZoom(3, 8)); // Laos
    countryZooms.put("Q822", new MinMaxZoom(3, 8)); // Lebanon
    countryZooms.put("Q760", new MinMaxZoom(4, 9)); // Saint Lucia
    countryZooms.put("Q347", new MinMaxZoom(4, 9)); // Liechtenstein
    countryZooms.put("Q854", new MinMaxZoom(2, 7)); // Sri Lanka
    countryZooms.put("Q1014", new MinMaxZoom(3, 8)); // Liberia
    countryZooms.put("Q1013", new MinMaxZoom(3, 8)); // Lesotho
    countryZooms.put("Q37", new MinMaxZoom(3, 8)); // Lithuania
    countryZooms.put("Q32", new MinMaxZoom(5, 9)); // Luxembourg
    countryZooms.put("Q211", new MinMaxZoom(3, 8)); // Latvia
    countryZooms.put("Q1016", new MinMaxZoom(2, 7)); // Libya
    countryZooms.put("Q1028", new MinMaxZoom(2, 7)); // Morocco
    countryZooms.put("Q235", new MinMaxZoom(4, 9)); // Monaco
    countryZooms.put("Q217", new MinMaxZoom(4, 9)); // Moldova
    countryZooms.put("Q236", new MinMaxZoom(4, 9)); // Montenegro
    countryZooms.put("Q1019", new MinMaxZoom(2, 6)); // Madagascar
    countryZooms.put("Q709", new MinMaxZoom(4, 9)); // Marshall Is
    countryZooms.put("Q221", new MinMaxZoom(4, 9)); // Macedonia
    countryZooms.put("Q912", new MinMaxZoom(2, 6)); // Mali
    countryZooms.put("Q836", new MinMaxZoom(2, 7)); // Myanmar
    countryZooms.put("Q711", new MinMaxZoom(2, 6)); // Mongolia
    countryZooms.put("Q1025", new MinMaxZoom(2, 7)); // Mauritania
    countryZooms.put("Q13353", new MinMaxZoom(4, 9)); // Montserrat
    countryZooms.put("Q233", new MinMaxZoom(3, 8)); // Malta
    countryZooms.put("Q1027", new MinMaxZoom(3, 8)); // Mauritius
    countryZooms.put("Q826", new MinMaxZoom(3, 8)); // Maldives
    countryZooms.put("Q1020", new MinMaxZoom(3, 8)); // Malawi
    countryZooms.put("Q96", new MinMaxZoom(1, 6)); // Mexico
    countryZooms.put("Q833", new MinMaxZoom(2, 7)); // Malaysia
    countryZooms.put("Q1029", new MinMaxZoom(2, 7)); // Mozambique
    countryZooms.put("Q1030", new MinMaxZoom(2, 7)); // Namibia
    countryZooms.put("Q1032", new MinMaxZoom(2, 7)); // Niger
    countryZooms.put("Q1033", new MinMaxZoom(1, 6)); // Nigeria
    countryZooms.put("Q811", new MinMaxZoom(3, 8)); // Nicaragua
    countryZooms.put("Q55", new MinMaxZoom(4, 9)); // Netherlands
    countryZooms.put("Q20", new MinMaxZoom(2, 6)); // Norway
    countryZooms.put("Q837", new MinMaxZoom(2, 7)); // Nepal
    countryZooms.put("Q697", new MinMaxZoom(4, 9)); // Nauru
    countryZooms.put("Q34020", new MinMaxZoom(3, 8)); // Niue
    countryZooms.put("Q664", new MinMaxZoom(1, 6)); // New Zealand
    countryZooms.put("Q842", new MinMaxZoom(3, 8)); // Oman
    countryZooms.put("Q804", new MinMaxZoom(3, 8)); // Panama
    countryZooms.put("Q419", new MinMaxZoom(1, 6)); // Peru
    countryZooms.put("Q691", new MinMaxZoom(1, 7)); // Papua New Guinea
    countryZooms.put("Q928", new MinMaxZoom(2, 6)); // Philippines
    countryZooms.put("Q843", new MinMaxZoom(2, 6)); // Pakistan
    countryZooms.put("Q35672", new MinMaxZoom(4, 8)); // Pitcairn Is
    countryZooms.put("Q45", new MinMaxZoom(2, 7)); // Portugal
    countryZooms.put("Q695", new MinMaxZoom(4, 9)); // Palau
    countryZooms.put("Q733", new MinMaxZoom(2, 7)); // Paraguay
    countryZooms.put("Q846", new MinMaxZoom(3, 8)); // Qatar
    countryZooms.put("Q218", new MinMaxZoom(2, 7)); // Romania
    countryZooms.put("Q403", new MinMaxZoom(3, 6)); // Serbia
    countryZooms.put("Q159", new MinMaxZoom(1, 4)); // Russia
    countryZooms.put("Q1037", new MinMaxZoom(2, 7)); // Rwanda
    countryZooms.put("Q851", new MinMaxZoom(2, 6)); // Saudi Arabia
    countryZooms.put("Q685", new MinMaxZoom(2, 7)); // Solomon Is
    countryZooms.put("Q1042", new MinMaxZoom(4, 9)); // Seychelles
    countryZooms.put("Q1049", new MinMaxZoom(2, 7)); // Sudan
    countryZooms.put("Q34", new MinMaxZoom(2, 6)); // Sweden
    countryZooms.put("Q334", new MinMaxZoom(3, 8)); // Singapore
    countryZooms.put("Q192184", new MinMaxZoom(4, 9)); // Saint Helena
    countryZooms.put("Q215", new MinMaxZoom(4, 9)); // Slovenia
    countryZooms.put("Q214", new MinMaxZoom(3, 8)); // Slovakia
    countryZooms.put("Q1044", new MinMaxZoom(3, 8)); // Sierra Leone
    countryZooms.put("Q238", new MinMaxZoom(4, 9)); // San Marino
    countryZooms.put("Q1041", new MinMaxZoom(2, 7)); // Senegal
    countryZooms.put("Q1045", new MinMaxZoom(3, 8)); // Somalia
    countryZooms.put("Q730", new MinMaxZoom(3, 8)); // Suriname
    countryZooms.put("Q958", new MinMaxZoom(2, 7)); // S. Sudan
    countryZooms.put("Q1039", new MinMaxZoom(4, 9)); // São Tomé and Principe
    countryZooms.put("Q792", new MinMaxZoom(4, 9)); // El Salvador
    countryZooms.put("Q26273", new MinMaxZoom(4, 9)); // Sint Maarten
    countryZooms.put("Q858", new MinMaxZoom(2, 7)); // Syria
    countryZooms.put("Q1050", new MinMaxZoom(3, 8)); // eSwatini
    countryZooms.put("Q18221", new MinMaxZoom(4, 9)); // Turks and Caicos Is
    countryZooms.put("Q657", new MinMaxZoom(2, 7)); // Chad
    countryZooms.put("Q945", new MinMaxZoom(4, 9)); // Togo
    countryZooms.put("Q869", new MinMaxZoom(2, 7)); // Thailand
    countryZooms.put("Q863", new MinMaxZoom(3, 8)); // Tajikistan
    countryZooms.put("Q36823", new MinMaxZoom(4, 9)); // Tokelau
    countryZooms.put("Q574", new MinMaxZoom(3, 8)); // Timor-Leste
    countryZooms.put("Q874", new MinMaxZoom(2, 7)); // Turkmenistan
    countryZooms.put("Q948", new MinMaxZoom(2, 7)); // Tunisia
    countryZooms.put("Q678", new MinMaxZoom(3, 8)); // Tonga
    countryZooms.put("Q43", new MinMaxZoom(1, 6)); // Turkey
    countryZooms.put("Q754", new MinMaxZoom(3, 9)); // Trinidad and Tobago
    countryZooms.put("Q672", new MinMaxZoom(4, 9)); // Tuvalu
    countryZooms.put("Q865", new MinMaxZoom(2, 7)); // Taiwan
    countryZooms.put("Q924", new MinMaxZoom(2, 7)); // Tanzania
    countryZooms.put("Q212", new MinMaxZoom(2, 6)); // Ukraine
    countryZooms.put("Q1036", new MinMaxZoom(2, 7)); // Uganda
    countryZooms.put("Q30", new MinMaxZoom(1, 5)); // United States of America
    countryZooms.put("Q77", new MinMaxZoom(2, 7)); // Uruguay
    countryZooms.put("Q265", new MinMaxZoom(2, 7)); // Uzbekistan
    countryZooms.put("Q237", new MinMaxZoom(4, 9)); // Vatican
    countryZooms.put("Q757", new MinMaxZoom(4, 9)); // St. Vin. and Gren
    countryZooms.put("Q717", new MinMaxZoom(2, 7)); // Venezuela
    countryZooms.put("Q25305", new MinMaxZoom(4, 9)); // British Virgin Is
    countryZooms.put("Q881", new MinMaxZoom(1, 6)); // Vietnam
    countryZooms.put("Q686", new MinMaxZoom(3, 8)); // Vanuatu
    countryZooms.put("Q683", new MinMaxZoom(2, 7)); // Samoa
    countryZooms.put("Q1246", new MinMaxZoom(4, 9)); // Kosovo
    countryZooms.put("Q805", new MinMaxZoom(2, 7)); // Yemen
    countryZooms.put("Q258", new MinMaxZoom(1, 6)); // South Africa
    countryZooms.put("Q953", new MinMaxZoom(2, 7)); // Zambia
    countryZooms.put("Q954", new MinMaxZoom(2, 7)); // Zimbabwe
    return countryZooms;
  }

  private static final Map<String, MinMaxZoom> COUNTRY_ZOOMS = createCountryZooms();

  @Override
  public String name() {
    return LAYER_NAME;
  }

  // private final AtomicInteger placeNumber = new AtomicInteger(0);

  // Evaluates place layer sort ordering of inputs into an integer for the sort-key field.
  static int getSortKey(double minZoom, int kindRank, int populationRank, long population, String name) {
    return SortKey
      // (nvkelso 20230803) floats with significant single decimal precision
      //                    but results in "Too many possible values"
      // Order ASCENDING (smaller manually curated Natural Earth min_zoom win over larger values, across kinds)
      // minZoom is a float with 1 significant digit for manually curated places
      .orderByInt((int) (minZoom * 10), 0, 150)
      // Order ASCENDING (smaller values win, countries then locality then neighbourhood, breaks ties for same minZoom)
      .thenByInt(kindRank, 0, 12)
      // Order DESCENDING (larger values win, San Francisco rank 11 wins over Oakland rank 10)
      // Disabled to allow population log to have larger range
      //.thenByInt(populationRank, 15, 0)
      // Order DESCENDING (larger values win, Millbrea 40k wins over San Bruno 20k, both rank 7)
      .thenByLog(population, 40000000, 1, 100)
      // Order ASCENDING (shorter strings are better than longer strings for map display and adds predictability)
      .thenByInt(name == null ? 0 : name.length(), 0, 31)
      .get();
  }


  // Offset by 1 here because of 256 versus 512 pixel tile sizes
  // and how the OSM processing assumes 512 tile size (while NE is 256)
  private static final int NE_ZOOM_OFFSET = 1;

  private static final ZoomFunction<Number> LOCALITY_GRID_SIZE_ZOOM_FUNCTION =
    ZoomFunction.fromMaxZoomThresholds(Map.of(
      14, 24,
      15, 16
    ), 0);

  private static final ZoomFunction<Number> LOCALITY_GRID_LIMIT_ZOOM_FUNCTION =
    ZoomFunction.fromMaxZoomThresholds(Map.of(
      11, 1,
      14, 2,
      15, 3
    ), 0);

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (!sf.isPoint() || !sf.hasTag("name") || !sf.hasTag("place")) {
      return;
    }

    try {
      Optional<String> code = countryCoder.getCountryCode(sf.latLonGeometry());
      if (code.isPresent()) {
        sf.setTag("_country", code.get());
      }
    } catch (GeometryException e) {
      // do nothing
    }

    var matches = index.getMatches(sf);
    if (matches.isEmpty()) {
      return;
    }

    String kind = getString(sf, matches, "kind", null);
    if (kind == null) {
      return;
    }

    Integer kindRank = getInteger(sf, matches, "kindRank", 6);
    Integer minZoom = getInteger(sf, matches, "minZoom", 12);
    Integer maxZoom = getInteger(sf, matches, "maxZoom", 15);
    Integer population = getInteger(sf, matches, "population", 0);

    int populationRank = 0;

    int[] popBreaks = {
      1000000000,
      100000000,
      50000000,
      20000000,
      10000000,
      5000000,
      1000000,
      500000,
      200000,
      100000,
      50000,
      20000,
      10000,
      5000,
      2000,
      1000,
      200,
      0};

    for (int i = 0; i < popBreaks.length; i++) {
      if (population >= popBreaks[i]) {
        populationRank = popBreaks.length - i;
        break;
      }
    }

    if (kind.equals("country") && COUNTRY_ZOOMS.containsKey(sf.getString("wikidata"))) {
      var minMaxZoom = COUNTRY_ZOOMS.get(sf.getString("wikidata"));
      minZoom = minMaxZoom.minZoom();
      maxZoom = minMaxZoom.maxZoom();
    }

    if (kind.equals("region")) {
      var neAdmin1 = naturalEarthDb.getAdmin1ByWikidata(sf.getString("wikidata"));
      if (neAdmin1 != null) {
        minZoom = (int) neAdmin1.minLabel() - NE_ZOOM_OFFSET;
        maxZoom = (int) neAdmin1.maxLabel() - NE_ZOOM_OFFSET;
      }
    }

    // Join OSM locality with nearby NE localities based on Wikidata ID and
    // harvest the min_zoom to achieve consistent label collisions at zoom 7+
    // By this zoom we get OSM points centered in feature better for area labels
    // While NE earlier aspires to be more the downtown area
    //
    // First scope down the NE <> OSM data join (to speed up total build time)
    if (kind.equals("locality")) {
      // We could add more fallback equivalency tests here, but 98% of NE places have a Wikidata ID
      var nePopulatedPlace = naturalEarthDb.getPopulatedPlaceByWikidata(sf.getString("wikidata"));
      if (nePopulatedPlace != null) {
        minZoom = (int) nePopulatedPlace.minZoom() - NE_ZOOM_OFFSET;
        // (nvkelso 20230815) We could set the population value here, too
        //                    But by the OSM zooms the value should be the incorporated value
        //                    While symbology should be for the metro population value
        populationRank = nePopulatedPlace.rankMax();
      }
    }

    var feat = features.point(this.name())
      .setId(FeatureId.create(sf))
      // Core Tilezen schema properties
      .setAttr("kind", kind)
      .setAttr("kind_detail", sf.getString("place"))
      .setAttr("min_zoom", minZoom + 1)
      // Core OSM tags for different kinds of places
      .setAttr("capital", sf.getString("capital"))
      .setAttr("population", population)
      .setAttr("population_rank", populationRank)
      // Generally we use NE and low zooms, and OSM at high zooms
      // With exceptions for country and region labels
      .setZoomRange((int) minZoom, (int) maxZoom);

    // Instead of exporting ISO country_code_iso3166_1_alpha_2 (which are sparse), we export Wikidata IDs
    if (sf.hasTag("wikidata")) {
      feat.setAttr("wikidata", sf.getString("wikidata"));
    }

    //feat.setSortKey(minZoom * 1000 + 400 - populationRank * 200 + placeNumber.incrementAndGet());
    int sortKey = getSortKey(minZoom, kindRank, populationRank, population, sf.getString("name"));
    feat.setSortKey(sortKey);
    feat.setAttr("sort_key", sortKey);

    // This is only necessary when prepping for raster renderers
    feat.setBufferPixels(24);

    // We set the sort keys so the label grid can be sorted predictably (bonus: tile features also sorted)
    // NOTE: The buffer needs to be consistent with the innteral grid pixel sizes
    //feat.setPointLabelGridSizeAndLimit(13, 64, 4); // each cell in the 4x4 grid can have 4 items
    feat.setPointLabelGridPixelSize(LOCALITY_GRID_SIZE_ZOOM_FUNCTION)
      .setPointLabelGridLimit(LOCALITY_GRID_LIMIT_ZOOM_FUNCTION);

    // and also whenever you set a label grid size limit, make sure you increase the buffer size so no
    // label grid squares will be the consistent between adjacent tiles
    feat.setBufferPixelOverrides(ZoomFunction.maxZoom(12, 64));

    OsmNames.setOsmNames(feat, sf, 0);
    OsmNames.setOsmRefs(feat, sf, 0);
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
