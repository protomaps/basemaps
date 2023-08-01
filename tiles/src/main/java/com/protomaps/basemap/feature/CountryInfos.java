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
    Afghanistan|افغانستان|Q889|AF|3|7
    Aland|Åland|Q5689|AX|5|10
    Albania|Shqipëria|Q222|AL|5|10
    Algeria|Algérie|Q262|DZ|2.5|7
    American Samoa|American Samoa|Q16641|AS|4|9
    Andorra|Andorra|Q228|AD|5|10
    Angola|Angola|Q916|AO|3|7
    Anguilla|Anguilla|Q25228|AI|5|10
    Antarctica|Antarctica|Q51|AQ|4|9
    Antigua and Barbuda|Antigua and Barbuda|Q781|AG|5|9.5
    Argentina|Argentina|Q414|AR|2|7
    Armenia|Հայաստան|Q399|AM|5|10
    Aruba|Aruba|Q21203|AW|5|10
    Ashmore and Cartier Islands|Ashmore and Cartier Islands|Q133888|AU|4.5|9.5
    Australia|Australia|Q408|AU|1.7|5.7
    Austria|Österreich|Q40|AT|3|8
    Azerbaijan|Azərbaycan|Q227|AZ|4|9
    Bahamas|Bahamas|Q778|BS|4|9
    Bahrain|البحرين|Q398|BH|4|9
    Baikonur|Baikonur|Q165413|KZ|6|9.5
    Bangladesh|বাংলাদেশ|Q902|BD|3|8
    Barbados|Barbados|Q244|BB|4.5|9.5
    Belarus|Беларусь|Q184|BY|3|8
    Belgium|België - Belgique - Belgien|Q31|BE|4|9
    Belize|Belize|Q242|BZ|5|10
    Benin|Bénin|Q962|BJ|4|9
    Bermuda|Bermuda|Q23635|BM|4|9
    Bhutan|འབྲུགཡུལ་|Q917|BT|4|9
    Bolivia|Bolivia|Q750|BO|3|7.5
    Bosnia and Herzegovina|Bosna i Hercegovina|Q225|BA|4.5|6.8
    Botswana|Botswana|Q963|BW|4|9
    Brazil|Brasil|Q155|BR|1.7|5.7
    Brazilian Island|Brazilian Island|Q2093778|BR|7.7|9
    British Indian Ocean Territory|British Indian Ocean Territory|Q43448|IO|5|9.5
    British Virgin Islands|British Virgin Islands|Q25305|VG|5|9.5
    Brunei|Brunei Darussalam|Q921|BN|4|9
    Bulgaria|Бългaрия|Q219|BG|4|9
    Burkina Faso|Burkina Faso|Q965|BF|3|8
    Burundi|Burundi|Q967|BI|4|9
    Cambodia|កម្ពុជា|Q424|KH|3|8
    Cameroon|Cameroun|Q1009|CM|3|8
    Canada|Canada|Q16|CA|1.7|5.7
    Cape Verde|Cabo Verde|Q1011|CV|4|9
    Cayman Islands|Cayman Islands|Q5785|KY|5|9.5
    Central African Republic|Centrafrique|Q929|CF|4|9
    Chad|Tchad تشاد|Q657|TD|3|8
    Chile|Chile|Q298|CL|1.7|6.7
    China|中国|Q148|CN|1.7|5.7
    Clipperton Island|Clipperton Island|Q161258|FR|5|9.5
    Colombia|Colombia|Q739|CO|3|7
    Comoros|Comores Komori جزر القمر|Q970|KM|4|9
    Cook Islands|Kūki ʻĀirani|Q26988|CK|4|9
    Coral Sea Islands|Coral Sea Islands|Q172216|AU|4.5|9.5
    Costa Rica|Costa Rica|Q800|CR|2.5|8
    Croatia|Hrvatska|Q224|HR|4|9
    Cuba|Cuba|Q241|CU|2.7|8
    Curacao|Curaçao|Q25279|CW|5|10
    Cyprus|Κύπρος - Kıbrıs|Q229|CY|4.5|9.5
    Czechia|Česko|Q213|CZ|4|9
    Democratic Republic of the Congo|République démocratique du Congo|Q974|CD|2|7
    Denmark|Danmark|Q35|DK|3|8
    Djibouti|Djibouti جيبوتي|Q977|DJ|4|9
    Dominica|Dominica|Q784|DM|4|9
    Dominican Republic|República Dominicana|Q786|DO|4.5|9.5
    East Timor|Timor-Leste|Q574|TL|4|9
    Ecuador|Ecuador|Q736|EC|3|8
    Egypt|مصر|Q79|EG|1.7|6.7
    El Salvador|El Salvador|Q792|SV|5|10
    Equatorial Guinea|Guinea Ecuatorial|Q983|GQ|4|9
    Eritrea|ኤርትራ Eritrea إرتريا|Q986|ER|4|9
    Estonia|Eesti|Q191|EE|3|8
    eSwatini|eSwatini|Q1050|SZ|4|9
    Ethiopia|ኢትዮጵያ / Ethiopia|Q115|ET|2|7
    Falkland Islands|Falkland Islands (Malvinas)|Q9648|FK|4.5|9
    Faroe Islands|Føroyar|Q4628|FO|4|9
    Federated States of Micronesia|Federated States of Micronesia|Q702|FM|5|10
    Fiji|Fiji|Q712|FJ|3|8
    Finland|Suomi / Finland|Q33|FI|3|8
    France|France|Q142|FR|1.7|6.7
    French Polynesia|French Polynesia|Q30971|PF|3.5|8.5
    French Southern Antarctic Lands|French Southern Antarctic Lands|Q129003|TF|4|9
    Gabon|Gabon|Q1000|GA|3|8
    Georgia|საქართველო|Q230|GE|4|9
    Germany|Deutschland|Q183|DE|1.7|6.7
    Ghana|Ghana|Q117|GH|2.7|8
    Gibraltar|Gibraltar|Q1410|GI|5|9.5
    Greece|Ελλάδα|Q41|GR|2.7|8
    Greenland|Kalaallit Nunaat|Q223|GL|1.7|6.7
    Grenada|Grenada|Q769|GD|4|9
    Guam|Guam|Q16635|GU|3|10
    Guatemala|Guatemala|Q774|GT|3|8
    Guernsey|Guernsey|Q25230|GG|5|10
    Guinea|Guinée|Q1006|GN|3|8
    Guinea-Bissau|Guiné-Bissau|Q1007|GW|5|10
    Guyana|Guyana|Q734|GY|4|9
    Haiti|Haiti|Q790|HT|4|9
    Heard Island and McDonald Islands|Heard Island and McDonald Islands|Q131198|HM|4.5|9.5
    Honduras|Honduras|Q783|HN|4.5|9.5
    Hong Kong|Hong Kong|Q8646|HK|4|9
    Hungary|Magyarország|Q28|HU|4|9
    Iceland|Ísland|Q189|IS|2|7
    India|India|Q668|IN|1.7|6.7
    Indian Ocean Territory|Indian Ocean Territory|Q4824275|AU|5|9.5
    Indonesia|Indonesia|Q252|ID|1.7|6.7
    Iran|ایران|Q794|IR|2.5|6.7
    Iraq|العراق|Q796|IQ|3|7.5
    Ireland|Ireland|Q27|IE|3|8
    Isle of Man|Isle of Man|Q9676|IM|5|10
    Israel|ישראל|Q801|IL|3|8
    Italy|Italia|Q38|IT|2|7
    Ivory Coast|Côte d'Ivoire|Q1008|CI|2.5|8
    Jamaica|Jamaica|Q766|JM|4|9
    Japan|日本|Q17|JP|1.7|7
    Jersey|Jersey|Q785|JE|5|10
    Jordan|الأردن|Q810|JO|4|9
    Kazakhstan|Қазақстан|Q232|KZ|2.7|7
    Kenya|Kenya|Q114|KE|1.7|6.7
    Kiribati|Kiribati|Q710|KI|5|10
    Kosovo|Kosovë / Kosovo|Q1246|XK|5|10
    Kuwait|الكويت|Q817|KW|5|10
    Kyrgyzstan|Кыргызстан|Q813|KG|3|8
    Laos|ປະເທດລາວ|Q819|LA|4|9
    Latvia|Latvija|Q211|LV|4|9
    Lebanon|لبنان|Q822|LB|4|9
    Lesotho|Lesotho|Q1013|LS|4|9
    Liberia|Liberia|Q1014|LR|4|9
    Libya|ليبيا|Q1016|LY|3|8
    Liechtenstein|Liechtenstein|Q347|LI|5|10
    Lithuania|Lietuva|Q37|LT|4|9
    Luxembourg|Lëtzebuerg|Q32|LU|5.7|10
    Macao|Macao|Q14773|MO|4|9
    Madagascar|Madagascar|Q1019|MG|2.7|7
    Malawi|Malawi|Q1020|MW|4|9
    Malaysia|Malaysia|Q833|MY|3|8
    Maldives|ދިވެހިރާއްޖެ|Q826|MV|4|9
    Mali|Mali|Q912|ML|3|7
    Malta|Malta|Q233|MT|4|9
    Marshall Islands|Marshall Islands|Q709|MH|5|10
    Mauritania|موريتانيا|Q1025|MR|3|8
    Mauritius|Mauritius / Maurice|Q1027|MU|4|9
    Mexico|México|Q96|MX|2|6.7
    Moldova|Moldova|Q217|MD|5|10
    Monaco|Monaco|Q235|MC|5|10
    Mongolia|Монгол Улс|Q711|MN|3|7
    Montenegro|Crna Gora / Црна Гора|Q236|ME|5|10
    Montserrat|Montserrat|Q13353|MS|5|10
    Morocco|Maroc ⵍⵎⴰⵖⵔⵉⴱ المغرب|Q1028|MA|2.7|8
    Mozambique|Mozambique|Q1029|MZ|3|8
    Myanmar|မြန်မာ|Q836|MM|3|8
    Namibia|Namibia|Q1030|NA|3|7.5
    Nauru|Naoero|Q697|NR|5|10
    Nepal|Nepal|Q837|NP|3|8
    Netherlands|Nederland|Q55|NL|4|10
    New Caledonia|New Caledonia|Q33788|NC|4.6|8
    New Zealand|New Zealand/Aotearoa|Q664|NZ|2|6.7
    Nicaragua|Nicaragua|Q811|NI|4|9
    Niger|Niger|Q1032|NE|3|8
    Nigeria|Nigeria|Q1033|NG|1.7|6.7
    Niue|Niue|Q34020|NU|4|9
    Norfolk Island|Norfolk Island|Q31057|NF|4.5|9.5
    North Korea|조선민주주의인민공화국|Q423|KP|3|8
    North Macedonia|Северна Македонија|Q221|MK|5|10
    Northern Mariana Islands|Northern Mariana Islands|Q16644|MP|5|10
    Norway|Norge|Q20|NO|3|7
    Oman|عمان|Q842|OM|4|9
    Pakistan|پاکستان|Q843|PK|2.7|7
    Palau|Belau|Q695|PW|5|10
    Palestine|Palestine|Q23792|PS|4.5|9.5
    Panama|Panama|Q804|PA|4|9
    Papua New Guinea|Papua New Guinea|Q691|PG|2.5|7.5
    Paraguay|Paraguay|Q733|PY|3|8
    Peru|Perú|Q419|PE|2|7
    Philippines|Philippines|Q928|PH|2.5|7
    Pitcairn|Pitcairn|Q35672|PN|5|9
    Poland|Polska|Q36|PL|2.5|7
    Portugal|Portugal|Q45|PT|3|8
    Puerto Rico|Puerto Rico|Q1183|PR|3|8
    Qatar|قطر|Q846|QA|4|9
    Republic of the Congo|République du Congo|Q971|CG|4|9
    Romania|România|Q218|RO|3|8
    Russia|Россия|Q159|RU|1.7|5.2
    Rwanda|Rwanda|Q1037|RW|3|8
    Saint Helena, Ascension and Tristan da Cunha|Saint Helena, Ascension and Tristan da Cunha|Q192184|SH|5|10
    Saint Kitts and Nevis|Saint Kitts and Nevis|Q763|KN|5|10
    Saint Lucia|Saint Lucia|Q760|LC|5|9.5
    Saint Pierre and Miquelon|Saint Pierre and Miquelon|Q34617|PM|5|10
    Saint Vincent and the Grenadines|Saint Vincent and the Grenadines|Q757|VC|5|10
    Saint-Barthélemy|Saint-Barthélemy|Q25362|BL|5.7|10
    Saint-Martin|Saint-Martin|Q126125|MF|5|10
    Samoa|Sāmoa|Q683|WS|3|8
    San Marino|San Marino|Q238|SM|5|10
    Sao Tome and Príncipe|São Tomé e Príncipe|Q1039|ST|5|10
    Saudi Arabia|السعودية|Q851|SA|1.7|7
    Senegal|Sénégal|Q1041|SN|2.7|8
    Serbia|Србија|Q403|RS|4|7
    Seychelles|Seychelles|Q1042|SC|5|10
    Sierra Leone|Sierra Leone|Q1044|SL|4|9
    Singapore|Singapore|Q334|SG|4|9
    Sint Maarten|Sint Maarten|Q26273|SX|5|10
    Slovakia|Slovensko|Q214|SK|4|9
    Slovenia|Slovenija|Q215|SI|5|10
    Solomon Islands|Solomon Islands|Q685|SB|3|8
    Somalia|الصومال|Q1045|SO|4|9
    South Africa|South Africa|Q258|ZA|1.7|6.7
    South Georgia and South Sandwich Islands|South Georgia and South Sandwich Islands|Q35086|GS|5|9
    South Korea|대한민국|Q884|KR|2.5|7
    South Sudan|South Sudan|Q958|SS|3|8
    Spain|España|Q29|ES|2|7
    Sri Lanka|ශ්‍රී ලංකාව இலங்கை|Q854|LK|3|8
    Sudan|السودان|Q1049|SD|2.5|8
    Suriname|Suriname|Q730|SR|4|9
    Sweden|Sverige|Q34|SE|2|7
    Switzerland|Schweiz/Suisse/Svizzera/Svizra|Q39|CH|4|9
    Syria|سوريا|Q858|SY|3|8
    Taiwan|臺灣|Q865|TW|4.5|8
    Tajikistan|Тоҷикистон|Q863|TJ|4|9
    Tanzania|Tanzania|Q924|TZ|3|8
    Thailand|ประเทศไทย|Q869|TH|2.7|8
    The Gambia|Gambia|Q1005|GM|5|10
    Togo|Togo|Q945|TG|5|10
    Tonga|Tonga|Q678|TO|4|9
    Trinidad and Tobago|Trinidad and Tobago|Q754|TT|4.5|9.5
    Tunisia|تونس|Q948|TN|3|8
    Turkey|Türkiye|Q43|TR|2|7
    Turkmenistan|Türkmenistan|Q874|TM|3|8
    Turks and Caicos Islands|Turks and Caicos Islands|Q18221|TC|5|10
    Tuvalu|Tuvalu|Q672|TV|5|10
    Uganda|Uganda|Q1036|UG|3|8
    Ukraine|Україна|Q212|UA|2.7|7
    United Arab Emirates|الإمارات العربية المتحدة|Q878|AE|4|9
    United Kingdom|United Kingdom|Q145|GB|1.7|6.7
    United States|United States of America|Q30|US|1.7|5.7
    United States Minor Outlying Islands|United States Minor Outlying Islands|Q16645|UM|7|11
    United States Virgin Islands|United States Virgin Islands|Q11703|VI|5|10
    Uruguay|Uruguay|Q77|UY|3|8
    Uzbekistan|O'zbekiston|Q265|UZ|3|8
    Vanuatu|Vanuatu|Q686|VU|4|9
    Vatican City|Civitas Vaticana - Città del Vaticano|Q237|VA|5|10
    Venezuela|Venezuela|Q717|VE|2.5|7.5
    Vietnam|Việt Nam|Q881|VN|2|7
    Wallis and Futuna Islands|Wallis and Futuna Islands|Q35555|WF|4.7|9
    Western Sahara|Western Sahara|Q6250|EH|6|11
    Yemen|اليَمَن|Q805|YE|3|8
    Zambia|Zambia|Q953|ZM|3|8
    Zimbabwe|Zimbabwe|Q954|ZW|2.5|8
    """;

  public record CountryInfo(String nameEnglish, String nameLocal, String wikidata, String isoCode, double minZoom, double maxZoom) {}

  private static final HashMap<String, CountryInfo> countryInfoByWikidata;
  private static final HashMap<String, CountryInfo> countryInfoByISO;

  static CountryInfo unknownInfo = new CountryInfo("UNKNOWN_COUNTRY", "UNKNOWN_COUNTRY", "QXXX", "XX", 5.0, 8.0);

  static {
    countryInfoByWikidata = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      countryInfoByWikidata.put(parts[2],
        new CountryInfo(parts[0], parts[1], parts[2], parts[3], Double.parseDouble(parts[4]) - 1.0, Double.parseDouble(parts[5]) - 1.0));
    }
  }

  static {
    countryInfoByISO = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      countryInfoByISO.put(parts[3],
        new CountryInfo(parts[0], parts[1], parts[2], parts[3], Double.parseDouble(parts[4]) - 1.0, Double.parseDouble(parts[5]) - 1.0));
    }
  }

  // (nvkelso 20230731) 100% of country nodes have wikidata
  public static CountryInfos.CountryInfo getByWikidata(SourceFeature sf) {
    var wikidata = sf.hasTag("wikidata") ? sf.getString("wikidata") : "QXXX";
    if (countryInfoByWikidata.containsKey(wikidata)) {
      return countryInfoByWikidata.get(wikidata);
    }
    return unknownInfo;
  }

  // (nvkelso 20230731) 51% of country nodes have country_code_iso3166_1_alpha_2
  // (nvkelso 20230731) 50% of country nodes have ISO3166-1:alpha2
  // (nvkelso 20230731) 28% of country nodes have ISO3166-1
  public static CountryInfos.CountryInfo getByISO(SourceFeature sf) {
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
