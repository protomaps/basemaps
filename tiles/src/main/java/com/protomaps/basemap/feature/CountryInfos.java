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
    Afghanistan|AF|3|7|Q889
    Åland|AX|5|10|Q5689
    Albania|AL|5|10|Q222
    Algeria|DZ|2.5|7|Q262
    American Samoa|AS|4|9|Q16641
    Andorra|AD|5|10|Q228
    Angola|AO|3|7|Q916
    Anguilla|AI|5|10|Q25228
    Antarctica|AQ|4|9|Q51
    Antigua and Barb.|AG|5|9.5|Q781
    Argentina|AR|2|7|Q414
    Armenia|AM|5|10|Q399
    Aruba|AW|5|10|Q21203
    Ashmore and Cartier Is.|AU|4.5|9.5|Q133888
    Australia|AU|1.7|5.7|Q408
    Austria|AT|3|8|Q40
    Azerbaijan|AZ|4|9|Q227
    Bahamas|BS|4|9|Q778
    Bahrain|BH|4|9|Q398
    Baikonur|KZ|6|9.5|Q165413
    Bangladesh|BD|3|8|Q902
    Barbados|BB|4.5|9.5|Q244
    Belarus|BY|3|8|Q184
    Belgium|BE|4|9|Q31
    Belize|BZ|5|10|Q242
    Benin|BJ|4|9|Q962
    Bermuda|BM|4|9|Q23635
    Bhutan|BT|4|9|Q917
    Bolivia|BO|3|7.5|Q750
    Bosnia and Herz.|BA|4.5|6.8|Q225
    Botswana|BW|4|9|Q963
    Br. Indian Ocean Ter.|IO|5|9.5|Q43448
    Brazil|BR|1.7|5.7|Q155
    Brazilian I.|BR|7.7|9|Q2093778
    British Virgin Is.|VG|5|9.5|Q25305
    Brunei|BN|4|9|Q921
    Bulgaria|BG|4|9|Q219
    Burkina Faso|BF|3|8|Q965
    Burundi|BI|4|9|Q967
    Cabo Verde|CV|4|9|Q1011
    Cambodia|KH|3|8|Q424
    Cameroon|CM|3|8|Q1009
    Canada|CA|1.7|5.7|Q16
    Cayman Is.|KY|5|9.5|Q5785
    Central African Rep.|CF|4|9|Q929
    Chad|TD|3|8|Q657
    Chile|CL|1.7|6.7|Q298
    China|CN|1.7|5.7|Q148
    Clipperton I.|FR|5|9.5|Q161258
    Colombia|CO|3|7|Q739
    Comoros|KM|4|9|Q970
    Congo|CG|4|9|Q971
    Cook Is.|CK|4|9|Q26988
    Coral Sea Is.|AU|4.5|9.5|Q172216
    Costa Rica|CR|2.5|8|Q800
    Côte d'Ivoire|CI|2.5|8|Q1008
    Croatia|HR|4|9|Q224
    Cuba|CU|2.7|8|Q241
    Curaçao|CW|5|10|Q25279
    Cyprus|CY|4.5|9.5|Q229
    Czechia|CZ|4|9|Q213
    Dem. Rep. Congo|CD|2|7|Q974
    Denmark|DK|3|8|Q35
    Djibouti|DJ|4|9|Q977
    Dominica|DM|4|9|Q784
    Dominican Rep.|DO|4.5|9.5|Q786
    Ecuador|EC|3|8|Q736
    Egypt|EG|1.7|6.7|Q79
    El Salvador|SV|5|10|Q792
    Eq. Guinea|GQ|4|9|Q983
    Eritrea|ER|4|9|Q986
    Estonia|EE|3|8|Q191
    eSwatini|SZ|4|9|Q1050
    Ethiopia|ET|2|7|Q115
    Faeroe Is.|FO|4|9|Q4628
    Falkland Is.|FK|4.5|9|Q9648
    Fiji|FJ|3|8|Q712
    Finland|FI|3|8|Q33
    Fr. Polynesia|PF|3.5|8.5|Q30971
    Fr. S. Antarctic Lands|TF|4|9|Q129003
    France|FR|1.7|6.7|Q142
    Gabon|GA|3|8|Q1000
    Gambia|GM|5|10|Q1005
    Georgia|GE|4|9|Q230
    Germany|DE|1.7|6.7|Q183
    Ghana|GH|2.7|8|Q117
    Gibraltar|GI|5|9.5|Q1410
    Greece|GR|2.7|8|Q41
    Greenland|GL|1.7|6.7|Q223
    Grenada|GD|4|9|Q769
    Guam|GU|3|10|Q16635
    Guatemala|GT|3|8|Q774
    Guernsey|GG|5|10|Q25230
    Guinea-Bissau|GW|5|10|Q1007
    Guinea|GN|3|8|Q1006
    Guyana|GY|4|9|Q734
    Haiti|HT|4|9|Q790
    Heard I. and McDonald Is.|HM|4.5|9.5|Q131198
    Honduras|HN|4.5|9.5|Q783
    Hong Kong|HK|4|9|Q8646
    Hungary|HU|4|9|Q28
    Iceland|IS|2|7|Q189
    India|IN|1.7|6.7|Q668
    Indian Ocean Ter.|AU|5|9.5|Q4824275
    Indonesia|ID|1.7|6.7|Q252
    Iran|IR|2.5|6.7|Q794
    Iraq|IQ|3|7.5|Q796
    Ireland|IE|3|8|Q27
    Isle of Man|IM|5|10|Q9676
    Israel|IL|3|8|Q801
    Italy|IT|2|7|Q38
    Jamaica|JM|4|9|Q766
    Japan|JP|1.7|7|Q17
    Jersey|JE|5|10|Q785
    Jordan|JO|4|9|Q810
    Kazakhstan|KZ|2.7|7|Q232
    Kenya|KE|1.7|6.7|Q114
    Kiribati|KI|5|10|Q710
    Kosovo|XK|5|10|Q1246
    Kuwait|KW|5|10|Q817
    Kyrgyzstan|KG|3|8|Q813
    Laos|LA|4|9|Q819
    Latvia|LV|4|9|Q211
    Lebanon|LB|4|9|Q822
    Lesotho|LS|4|9|Q1013
    Liberia|LR|4|9|Q1014
    Libya|LY|3|8|Q1016
    Liechtenstein|LI|5|10|Q347
    Lithuania|LT|4|9|Q37
    Luxembourg|LU|5.7|10|Q32
    Macao|MO|4|9|Q14773
    Madagascar|MG|2.7|7|Q1019
    Malawi|MW|4|9|Q1020
    Malaysia|MY|3|8|Q833
    Maldives|MV|4|9|Q826
    Mali|ML|3|7|Q912
    Malta|MT|4|9|Q233
    Marshall Is.|MH|5|10|Q709
    Mauritania|MR|3|8|Q1025
    Mauritius|MU|4|9|Q1027
    Mexico|MX|2|6.7|Q96
    Micronesia|FM|5|10|Q702
    Moldova|MD|5|10|Q217
    Monaco|MC|5|10|Q235
    Mongolia|MN|3|7|Q711
    Montenegro|ME|5|10|Q236
    Montserrat|MS|5|10|Q13353
    Morocco|MA|2.7|8|Q1028
    Mozambique|MZ|3|8|Q1029
    Myanmar|MM|3|8|Q836
    N. Mariana Is.|MP|5|10|Q16644
    Namibia|NA|3|7.5|Q1030
    Nauru|NR|5|10|Q697
    Nepal|NP|3|8|Q837
    Netherlands|NL|4|10|Q55
    New Caledonia|NC|4.6|8|Q33788
    New Zealand|NZ|2|6.7|Q664
    Nicaragua|NI|4|9|Q811
    Niger|NE|3|8|Q1032
    Nigeria|NG|1.7|6.7|Q1033
    Niue|NU|4|9|Q34020
    Norfolk Island|NF|4.5|9.5|Q31057
    North Korea|KP|3|8|Q423
    North Macedonia|MK|5|10|Q221
    Norway|NO|3|7|Q20
    Oman|OM|4|9|Q842
    Pakistan|PK|2.7|7|Q843
    Palau|PW|5|10|Q695
    Palestine|PS|4.5|9.5|Q23792
    Panama|PA|4|9|Q804
    Papua New Guinea|PG|2.5|7.5|Q691
    Paraguay|PY|3|8|Q733
    Peru|PE|2|7|Q419
    Philippines|PH|2.5|7|Q928
    Pitcairn Is.|PN|5|9|Q35672
    Poland|PL|2.5|7|Q36
    Portugal|PT|3|8|Q45
    Puerto Rico|PR|3|8|Q1183
    Qatar|QA|4|9|Q846
    Romania|RO|3|8|Q218
    Russia|RU|1.7|5.2|Q159
    Rwanda|RW|3|8|Q1037
    S. Geo. and the Is.|GS|5|9|Q35086
    S. Sudan|SS|3|8|Q958
    Saint Helena|SH|5|10|Q192184
    Saint Lucia|LC|5|9.5|Q760
    Samoa|WS|3|8|Q683
    San Marino|SM|5|10|Q238
    São Tomé and Principe|ST|5|10|Q1039
    Saudi Arabia|SA|1.7|7|Q851
    Senegal|SN|2.7|8|Q1041
    Serbia|RS|4|7|Q403
    Seychelles|SC|5|10|Q1042
    Sierra Leone|SL|4|9|Q1044
    Singapore|SG|4|9|Q334
    Sint Maarten|SX|5|10|Q26273
    Slovakia|SK|4|9|Q214
    Slovenia|SI|5|10|Q215
    Solomon Is.|SB|3|8|Q685
    Somalia|SO|4|9|Q1045
    South Africa|ZA|1.7|6.7|Q258
    South Korea|KR|2.5|7|Q884
    Spain|ES|2|7|Q29
    Sri Lanka|LK|3|8|Q854
    St-Barthélemy|BL|5.7|10|Q25362
    St-Martin|MF|5|10|Q126125
    St. Kitts and Nevis|KN|5|10|Q763
    St. Pierre and Miquelon|PM|5|10|Q34617
    St. Vin. and Gren.|VC|5|10|Q757
    Sudan|SD|2.5|8|Q1049
    Suriname|SR|4|9|Q730
    Sweden|SE|2|7|Q34
    Switzerland|CH|4|9|Q39
    Syria|SY|3|8|Q858
    Taiwan|TW|4.5|8|Q865
    Tajikistan|TJ|4|9|Q863
    Tanzania|TZ|3|8|Q924
    Thailand|TH|2.7|8|Q869
    Timor-Leste|TL|4|9|Q574
    Togo|TG|5|10|Q945
    Tonga|TO|4|9|Q678
    Trinidad and Tobago|TT|4.5|9.5|Q754
    Tunisia|TN|3|8|Q948
    Turkey|TR|2|7|Q43
    Turkmenistan|TM|3|8|Q874
    Turks and Caicos Is.|TC|5|10|Q18221
    Tuvalu|TV|5|10|Q672
    U.S. Minor Outlying Is.|UM|7|11|Q16645
    U.S. Virgin Is.|VI|5|10|Q11703
    Uganda|UG|3|8|Q1036
    Ukraine|UA|2.7|7|Q212
    United Arab Emirates|AE|4|9|Q878
    United Kingdom|GB|1.7|6.7|Q145
    United States of America|US|1.7|5.7|Q30
    Uruguay|UY|3|8|Q77
    Uzbekistan|UZ|3|8|Q265
    Vanuatu|VU|4|9|Q686
    Vatican|VA|5|10|Q237
    Venezuela|VE|2.5|7.5|Q717
    Vietnam|VN|2|7|Q881
    W. Sahara|EH|6|11|Q6250
    Wallis and Futuna Is.|WF|4.7|9|Q35555
    Yemen|YE|3|8|Q805
    Zambia|ZM|3|8|Q953
    Zimbabwe|ZW|2.5|8|Q954
    """;

  public record CountryInfo(String name, String isoCode, double minZoom, double maxZoom, String wikidata) {}

  private static final HashMap<String, CountryInfo> countryInfoByISO;
  private static final HashMap<String, CountryInfo> countryInfoByWikidata;

  static CountryInfo unknownInfo = new CountryInfo("UNKNOWN_COUNTRY", "XX", 7.0, 10.0, "QXXX");

  static {
    countryInfoByISO = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      countryInfoByISO.put(parts[1],
        new CountryInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0, parts[4]));
    }
  }

  static {
    countryInfoByWikidata = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      countryInfoByWikidata.put(parts[4],
        new CountryInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0, parts[4]));
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

  public static CountryInfos.CountryInfo getByWikidata(SourceFeature sf) {
    var wikidata = sf.hasTag("wikidata") ? sf.getString("wikidata") : (
      sf.hasTag("wikidataid") ? sf.getString("wikidataid") : "QXXX"
    );
    if (countryInfoByWikidata.containsKey(wikidata)) {
      return countryInfoByWikidata.get(wikidata);
    }
    return unknownInfo;
  }
}
