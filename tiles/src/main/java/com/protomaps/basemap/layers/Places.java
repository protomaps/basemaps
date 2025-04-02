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
      with("place", "country"),
      with("_country", "AD"), // Andorra
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AE"), // United Arab Emirates
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "AF"), // Afghanistan
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "AG"), // Antigua and Barb
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AI"), // Anguilla
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AL"), // Albania
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AM"), // Armenia
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AO"), // Angola
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "AQ"), // Antarctica
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "AR"), // Argentina
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "AS"), // American Samoa
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "AT"), // Austria
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "AU"), // Australia
      use("minZoom", 1),
      use("maxZoom", 5)
    ),
    rule(
      with("place", "country"),
      with("_country", "AW"), // Aruba
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AX"), // Åland
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "AZ"), // Azerbaijan
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BA"), // Bosnia and Herz
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "BB"), // Barbados
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "BD"), // Bangladesh
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "BE"), // Belgium
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BF"), // Burkina Faso
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "BG"), // Bulgaria
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BH"), // Bahrain
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BI"), // Burundi
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BJ"), // Benin
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BL"), // St-Barthélemy
      use("minZoom", 5),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "BM"), // Bermuda
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BN"), // Brunei
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BO"), // Bolivia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "BR"), // Brazil
      use("minZoom", 1),
      use("maxZoom", 5)
    ),
    rule(
      with("place", "country"),
      with("_country", "BS"), // Bahamas
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BT"), // Bhutan
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BV"), // Bouvet Island
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "BW"), // Botswana
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "BY"), // Belarus
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "BZ"), // Belize
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "CA"), // Canada
      use("minZoom", 1),
      use("maxZoom", 5)
    ),
    rule(
      with("place", "country"),
      with("_country", "CD"), // Dem. Rep. Congo
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "CF"), // Central African Rep
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "CG"), // Congo
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "CH"), // Switzerland
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "CI"), // Côte d'Ivoire
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "CK"), // Cook Is
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "CL"), // Chile
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "CM"), // Cameroon
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "CN"), // China
      use("minZoom", 1),
      use("maxZoom", 5)
    ),
    rule(
      with("place", "country"),
      with("_country", "CO"), // Colombia
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "CR"), // Costa Rica
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "CU"), // Cuba
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "CV"), // Cabo Verde
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "CW"), // Curaçao
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "CY"), // Cyprus
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "CZ"), // Czechia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "DE"), // Germany
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "DJ"), // Djibouti
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "DK"), // Denmark
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "DM"), // Dominica
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "DO"), // Dominican Rep
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "DZ"), // Algeria
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "EC"), // Ecuador
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "EE"), // Estonia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "EG"), // Egypt
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "EH"), // W. Sahara
      use("minZoom", 5),
      use("maxZoom", 10)
    ),
    rule(
      with("place", "country"),
      with("_country", "ER"), // Eritrea
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "ES"), // Spain
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "ET"), // Ethiopia
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "FI"), // Finland
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "FJ"), // Fiji
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "FK"), // Falkland Is
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "FM"), // Micronesia
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "FO"), // Faeroe Is
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "FR"), // France
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "GA"), // Gabon
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "GB"), // United Kingdom
      without("country_code_iso3166_1_alpha_2", "IO"), // British Indian Ocean Territory
      without("ISO3166-1:alpha2", "SH"), // Saint Helena, Ascension and Tristan da Cunha
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "GD"), // Grenada
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "GE"), // Georgia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "GF"), // French Guiana
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GG"), // Guernsey
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GH"), // Ghana
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "GI"), // Gibraltar
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GL"), // Greenland
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "GM"), // Gambia
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GN"), // Guinea
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "GP"), // Guadeloupe
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GQ"), // Eq. Guinea
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "GR"), // Greece
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "GS"), // S. Geo. and the Is
      use("minZoom", 4),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "GT"), // Guatemala
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "GU"), // Guam
      use("minZoom", 2),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GW"), // Guinea-Bissau
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "GY"), // Guyana
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "HK"), // Hong Kong
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "HM"), // Heard I. and McDonald Is
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "HN"), // Honduras
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "HR"), // Croatia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "HT"), // Haiti
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "HU"), // Hungary
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      // Defaulting to iso3166 alpha2 because the node for Indonesia is 
      // not inside the country polygon used by the country coder
      with("country_code_iso3166_1_alpha_2", "ID"), // Indonesia
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "IE"), // Ireland
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "IL"), // Israel
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "IM"), // Isle of Man
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "IN"), // India
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "IO"), // Br. Indian Ocean Ter
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "IQ"), // Iraq
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "IR"), // Iran
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "IS"), // Iceland
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "IT"), // Italy
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "JE"), // Jersey
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "JM"), // Jamaica
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "JO"), // Jordan
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "JP"), // Japan
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "KE"), // Kenya
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "KG"), // Kyrgyzstan
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "KH"), // Cambodia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "KI"), // Kiribati
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "KM"), // Comoros
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "KN"), // St. Kitts and Nevis
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "KP"), // North Korea
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "KR"), // South Korea
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "KW"), // Kuwait
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "KY"), // Cayman Is
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "KZ"), // Kazakhstan
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "LA"), // Laos
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "LB"), // Lebanon
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "LC"), // Saint Lucia
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "LI"), // Liechtenstein
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "LK"), // Sri Lanka
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "LR"), // Liberia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "LS"), // Lesotho
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "LT"), // Lithuania
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "LU"), // Luxembourg
      use("minZoom", 5),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "LV"), // Latvia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "LY"), // Libya
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "MA"), // Morocco
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "MC"), // Monaco
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MD"), // Moldova
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "ME"), // Montenegro
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MF"), // St-Martin
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MG"), // Madagascar
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "MH"), // Marshall Is
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MK"), // Macedonia
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "ML"), // Mali
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "MM"), // Myanmar
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "MN"), // Mongolia
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "MO"), // Macao
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "MP"), // N. Mariana Is
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MQ"), // Martinique
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MR"), // Mauritania
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "MS"), // Montserrat
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "MT"), // Malta
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "MU"), // Mauritius
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "MV"), // Maldives
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "MW"), // Malawi
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "MX"), // Mexico
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "MY"), // Malaysia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "MZ"), // Mozambique
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "NA"), // Namibia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "NC"), // New Caledonia
      use("minZoom", 4),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "NE"), // Niger
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "NF"), // Norfolk Island
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "NG"), // Nigeria
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "NI"), // Nicaragua
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "NL"), // Netherlands
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "NO"), // Norway
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "NP"), // Nepal
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "NR"), // Nauru
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "NU"), // Niue
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "NZ"), // New Zealand
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "OM"), // Oman
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "PA"), // Panama
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "PE"), // Peru
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "PF"), // Fr. Polynesia
      use("minZoom", 3),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "PG"), // Papua New Guinea
      use("minZoom", 1),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "PH"), // Philippines
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "PK"), // Pakistan
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "PL"), // Poland
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "PM"), // St. Pierre and Miquelon
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "PN"), // Pitcairn Is
      use("minZoom", 4),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "PR"), // Puerto Rico
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "PS"), // Palestine
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "PT"), // Portugal
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "PW"), // Palau
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "PY"), // Paraguay
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "QA"), // Qatar
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "RE"), // Réunion
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "RO"), // Romania
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "RS"), // Serbia
      use("minZoom", 3),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "RU"), // Russia
      use("minZoom", 1),
      use("maxZoom", 4)
    ),
    rule(
      with("place", "country"),
      with("_country", "RW"), // Rwanda
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "SA"), // Saudi Arabia
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "SB"), // Solomon Is
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "SC"), // Seychelles
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SD"), // Sudan
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "SE"), // Sweden
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "SG"), // Singapore
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "SH"), // Saint Helena
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SI"), // Slovenia
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SJ"), // Svalbard and Jan Mayen
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "SK"), // Slovakia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "SL"), // Sierra Leone
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "SM"), // San Marino
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SN"), // Senegal
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "SO"), // Somalia
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "SR"), // Suriname
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "SS"), // S. Sudan
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "ST"), // São Tomé and Principe
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SV"), // El Salvador
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SX"), // Sint Maarten
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "SY"), // Syria
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "SZ"), // eSwatini
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "TC"), // Turks and Caicos Is
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "TD"), // Chad
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "TF"), // Fr. S. Antarctic Lands
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "TG"), // Togo
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "TH"), // Thailand
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "TJ"), // Tajikistan
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "TK"), // Tokelau
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "TL"), // Timor-Leste
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "TM"), // Turkmenistan
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "TN"), // Tunisia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "TO"), // Tonga
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "TR"), // Turkey
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "TT"), // Trinidad and Tobago
      use("minZoom", 3),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "TV"), // Tuvalu
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "TW"), // Taiwan
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "TZ"), // Tanzania
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "UA"), // Ukraine
      use("minZoom", 2),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "UG"), // Uganda
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "UM"), // U.S. Minor Outlying Is
      use("minZoom", 6),
      use("maxZoom", 10)
    ),
    rule(
      with("place", "country"),
      with("_country", "US"), // United States of America
      use("minZoom", 1),
      use("maxZoom", 5)
    ),
    rule(
      with("place", "country"),
      with("_country", "UY"), // Uruguay
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "UZ"), // Uzbekistan
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "VA"), // Vatican
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "VC"), // St. Vin. and Gren
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "VE"), // Venezuela
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "VG"), // British Virgin Is
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "VI"), // U.S. Virgin Is
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "VN"), // Vietnam
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "VU"), // Vanuatu
      use("minZoom", 3),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "WF"), // Wallis and Futuna Is
      use("minZoom", 4),
      use("maxZoom", 8)
    ),
    rule(
      with("place", "country"),
      with("_country", "WS"), // Samoa
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "XK"), // Kosovo
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "YE"), // Yemen
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "YT"), // Mayotte
      use("minZoom", 4),
      use("maxZoom", 9)
    ),
    rule(
      with("place", "country"),
      with("_country", "ZA"), // South Africa
      use("minZoom", 1),
      use("maxZoom", 6)
    ),
    rule(
      with("place", "country"),
      with("_country", "ZM"), // Zambia
      use("minZoom", 2),
      use("maxZoom", 7)
    ),
    rule(
      with("place", "country"),
      with("_country", "ZW"), // Zimbabwe
      use("minZoom", 2),
      use("maxZoom", 7)
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
