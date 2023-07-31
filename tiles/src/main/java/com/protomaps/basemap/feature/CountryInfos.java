package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Query hardcoded information about countries.
 * <p>
 * Retrieve embedded hardcoded data about countries, addressable by names that should match OSM and NE.
 * </p>
 */
public class CountryInfos {
  private static String data = """
    Andorra|AD|11.0|11.0
    United Arab Emirates|AE|8.7|11.0
    Afghanistan|AF|6.7|11.0
    Antigua and Barb|AG|11.0|11.0
    Anguilla|AI|11.5|11.5
    Albania|AL|9.0|11.0
    Armenia|AM|9.0|11.0
    Angola|AO|6.6|11.0
    Antarctica|AQ|14.5|14.5
    Argentina|AR|6.0|11.0
    American Samoa|AS|9.0|11.0
    Austria|AT|7.8|11.0
    Australia|AU|4.6|8.1
    Aruba|AW|11.0|11.0
    Aland|AX|9.0|11.0
    Azerbaijan|AZ|9.2|11.0
    Bosnia and Herz|BA|9.0|11.0
    Barbados|BB|11.0|11.0
    Bangladesh|BD|7.7|11.0
    Belgium|BE|8.7|11.0
    Burkina Faso|BF|9.0|11.0
    Bulgaria|BG|8.0|11.0
    Bahrain|BH|11.0|11.0
    Burundi|BI|10.0|11.0
    Benin|BJ|7.7|11.0
    St-Barthelemy|BL|11.0|11.0
    Bermuda|BM|11.0|11.0
    Brunei|BN|9.0|11.0
    Bolivia|BO|6.6|11.0
    Brazil|BR|3.7|8.5
    Bahamas|BS|8.0|11.0
    Bhutan|BT|8.7|11.0
    Botswana|BW|6.0|11.0
    Belarus|BY|6.6|11.0
    Belize|BZ|8.0|11.0
    Canada|CA|3.5|7.5
    Democratic Republic of the Congo|CD|6.0|11.0
    Central African Republic|CF|7.0|11.0
    Republic of the Congo|CG|7.6|11.0
    Switzerland|CH|8.7|11.0
    Ivory Coast|CI|7.7|11.0
    Cook Islands|CK|11.0|11.0
    Chile|CL|6.0|11.0
    Cameroon|CM|6.7|11.0
    China|CN|5.0|10.3
    Colombia|CO|7.0|11.2
    Costa Rica|CR|7.7|11.0
    Cuba|CU|7.7|11.0
    Cabo Verde|CV|11.0|11.0
    Curacao|CW|11.0|11.0
    Cyprus|CY|8.7|11.0
    Czechia|CZ|7.7|11.0
    Germany|DE|6.6|11.0
    Djibouti|DJ|9.0|11.0
    Denmark|DK|7.7|11.0
    Dominica|DM|11.0|11.0
    Dominican Rep|DO|10.0|11.0
    Algeria|DZ|7.7|11.0
    Ecuador|EC|8.0|11.0
    Estonia|EE|7.0|11.0
    Egypt|EG|7.7|11.0
    Western Sahara|EH|18.0|18.0
    Eritrea|ER|8.7|11.0
    Spain|ES|7.7|11.0
    Ethiopia|ET|6.6|11.0
    Finland|FI|6.7|11.0
    Fiji|FJ|8.0|11.0
    Falkland Islands|FK|18.0|18.0
    Micronesia|FM|11.0|11.0
    Faeroe Islands|FO|8.0|11.0
    Gabon|GA|7.7|11.0
    United Kingdom|GB|10.0|11.0
    Grenada|GD|11.0|11.0
    Georgia|GE|7.7|11.0
    Guernsey|GG|18.0|18.0
    Ghana|GH|7.7|11.0
    Gibraltar|GI|18.0|18.0
    Greenland|GL|8.0|11.0
    Gambia|GM|8.0|11.0
    Guinea|GN|8.0|11.0
    Equatorial Guinea|GQ|8.7|11.0
    Greece|GR|7.7|11.0
    South Georgia and the Islands|GS|18.0|18.0
    Guatemala|GT|8.0|11.0
    Guam|GU|18.0|18.0
    Guinea-Bissau|GW|8.7|11.0
    Guyana|GY|8.0|11.0
    Hong Kong|HK|11.0|11.0
    Heard Island and McDonald Islands|HM|18.0|18.0
    Honduras|HN|8.0|11.0
    Croatia|HR|9.0|11.0
    Haiti|HT|7.7|11.0
    Hungary|HU|8.5|11.0
    Indonesia|ID|5.0|10.1
    Ireland|IE|8.2|11.0
    Israel|IL|8.4|11.0
    Isle of Man|IM|18.0|18.0
    India|IN|4.6|10.1
    British Indian Ocean Territory|IO|18.0|18.0
    Iraq|IQ|6.7|11.0
    Iran|IR|6.6|11.0
    Iceland|IS|6.0|11.0
    Italy|IT|9.0|11.0
    Jersey|JE|18.0|18.0
    Jamaica|JM|10.0|11.0
    Jordan|JO|8.7|11.0
    Japan|JP|7.0|11.0
    Kenya|KE|6.6|11.0
    Kyrgyzstan|KG|6.7|11.0
    Cambodia|KH|8.1|11.0
    Kiribati|KI|18.0|18.0
    Comoros|KM|11.0|11.0
    St. Kitts and Nevis|KN|11.0|11.0
    North Korea|KP|8.0|11.0
    South Korea|KR|8.0|11.0
    Kuwait|KW|8.7|11.0
    Cayman Islands|KY|11.0|11.0
    Kazakhstan|KZ|6.0|11.0
    Laos|LA|8.0|11.0
    Lebanon|LB|8.7|11.0
    Saint Lucia|LC|11.0|11.0
    Liechtenstein|LI|11.0|11.0
    Sri Lanka|LK|8.7|11.0
    Liberia|LR|8.0|11.0
    Lesotho|LS|8.7|11.0
    Lithuania|LT|7.0|11.0
    Luxembourg|LU|8.7|11.0
    Latvia|LV|10.0|11.0
    Libya|LY|7.7|11.0
    Morocco|MA|7.7|11.0
    Monaco|MC|18.0|18.0
    Moldova|MD|10.0|11.0
    Montenegro|ME|10.0|11.0
    St-Martin|MF|11.0|11.0
    Madagascar|MG|7.0|11.0
    Marshall Islands|MH|11.0|11.0
    Macedonia|MK|10.0|11.0
    Mali|ML|6.6|11.0
    Myanmar|MM|7.0|11.0
    Mongolia|MN|6.0|11.0
    Macao|MO|18.0|18.0
    Northern Mariana Islands|MP|11.0|11.0
    Mauritania|MR|6.6|11.0
    Montserrat|MS|11.0|11.0
    Malta|MT|11.0|11.0
    Mauritius|MU|11.0|11.0
    Maldives|MV|10.0|11.0
    Malawi|MW|8.7|11.0
    Mexico|MX|6.9|11.2
    Malaysia|MY|7.2|11.0
    Mozambique|MZ|6.6|11.0
    Namibia|NA|6.0|11.0
    New Caledonia|NC|6.7|11.0
    Niger|NE|6.6|11.0
    Norfolk Island|NF|9.0|11.0
    Nigeria|NG|6.6|11.0
    Nicaragua|NI|7.7|11.0
    Netherlands|NL|8.6|11.0
    Nepal|NP|7.7|11.0
    Nauru|NR|9.0|11.0
    Niue|NU|18.0|18.0
    New Zealand|NZ|8.5|11.3
    Oman|OM|8.7|11.0
    Panama|PA|7.7|11.0
    Peru|PE|6.6|11.0
    French Polynesia|PF|11.0|11.0
    Papua New Guinea|PG|7.0|11.0
    Philippines|PH|8.0|11.0
    Pakistan|PK|5.0|10.5
    Poland|PL|6.7|11.0
    Saint Pierre and Miquelon|PM|11.0|11.0
    Pitcairn Islands|PN|18.0|18.0
    Puerto Rico|PR|11.0|11.0
    Palestine|PS|18.0|18.0
    Portugal|PT|8.0|11.0
    Palau|PW|11.0|11.0
    Paraguay|PY|6.7|11.0
    Qatar|QA|8.7|11.0
    Romania|RO|8.0|11.0
    Serbia|RS|9.0|11.0
    Russia|RU|5.0|10.2
    Rwanda|RW|8.7|11.0
    Saudi Arabia|SA|6.6|11.0
    Solomon Islands|SB|7.0|11.0
    Seychelles|SC|11.0|11.0
    Sudan|SD|6.6|11.0
    Sweden|SE|6.7|11.0
    Singapore|SG|11.0|11.0
    Saint Helena|SH|11.0|11.0
    Slovenia|SI|11.0|11.0
    Slovakia|SK|7.7|11.0
    Sierra Leone|SL|7.8|11.0
    San Marino|SM|11.0|11.0
    Senegal|SN|7.7|11.0
    Somalia|SO|7.0|11.0
    Suriname|SR|8.0|11.0
    South Sudan|SS|6.6|11.0
    Sao Tome and Principe|ST|11.0|11.0
    El Salvador|SV|10.0|11.0
    Sint Maarten|SX|11.0|11.0
    Syria|SY|7.7|11.5
    eSwatini|SZ|8.7|11.0
    Turks and Caicos Islands|TC|11.0|11.0
    Chad|TD|6.6|11.0
    French Southern Antarctic Lands|TF|11.0|11.0
    Togo|TG|7.7|11.0
    Thailand|TH|8.2|11.0
    Tajikistan|TJ|6.7|11.0
    Timor-Leste|TL|9.0|11.0
    Turkmenistan|TM|6.6|11.0
    Tunisia|TN|7.7|11.0
    Tonga|TO|11.0|11.0
    Turkiye|TR|7.0|11.0
    Trinidad and Tobago|TT|10.0|11.0
    Tuvalu|TV|18.0|18.0
    Taiwan|TW|8.7|11.0
    Tanzania|TZ|6.7|11.0
    Ukraine|UA|6.7|11.0
    Uganda|UG|10.0|11.0
    U.S. Minor Outlying Islands|UM|11.0|11.0
    United States of America|US|3.5|7.5
    Uruguay|UY|8.0|11.0
    Uzbekistan|UZ|6.6|11.0
    Vatican|VA|18.0|18.0
    Saint Vincent and Grenadines|VC|11.0|11.0
    Venezuela|VE|7.1|11.3
    British Virgin Islands|VG|18.0|18.0
    U.S. Virgin Islands|VI|11.0|11.0
    Vietnam|VN|8.3|11.0
    Vanuatu|VU|9.0|11.0
    Wallis and Futuna Islands|WF|9.0|11.0
    Samoa|WS|9.0|11.0
    Kosovo|XK|10.0|11.0
    Yemen|YE|8.7|11.0
    South Africa|ZA|4.6|10.1
    Zambia|ZM|6.6|11.0
    Zimbabwe|ZW|6.7|11.0
    """;

  public record CountryInfo(String name, String isoCode, double minZoom, double maxZoom) {}

  private static final HashMap<String, CountryInfo> countryInfoByISO;
  static CountryInfo unknownInfo = new CountryInfo("UNKNOWN_COUNTRY", "XX", 8.0, 11.0);

  static {
    countryInfoByISO = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      countryInfoByISO.put(parts[1],
        new CountryInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0));
    }
  }

  public static CountryInfo getByISO(SourceFeature sf) {
    var isoCode = sf.hasTag("ISO3166-1:alpha2") ? sf.getString("ISO3166-1:alpha2") : (
      sf.hasTag("country_code_iso3166_1_alpha_2") ? sf.getString("country_code_iso3166_1_alpha_2") : (
        sf.hasTag("ISO3166-1") ? sf.getString("ISO3166-1") : "XX")
    );
    if (countryInfoByISO.containsKey(isoCode)) {
      return countryInfoByISO.get(isoCode);
    }
    return unknownInfo;
  }
}
