package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Query hardcoded information about sub-national regions.
 * <p>
 * Embedded hand-curated data on sub-national regions of significant extents, to assist in labeling. Includes US and
 * Australian states, and Canadian provinces.
 * </p>
 */
public class RegionInfos {

  private static String data = """
    Alabama|US-AL|3.5|7.5
    Alaska|US-AK|3.5|7.5
    Arizona|US-AZ|3.5|7.5
    Arkansas|US-AR|3.5|7.5
    California|US-CA|3.5|7.5
    Colorado|US-CO|3.5|7.5
    Connecticut|US-CT|3.5|7.5
    Delaware|US-DE|3.5|7.5
    Florida|US-FL|3.5|7.5
    Georgia|US-GA|3.5|7.5
    Hawaii|US-HI|3.5|7.5
    Idaho|US-ID|3.5|7.5
    Illinois|US-IL|3.5|7.5
    Indiana|US-IN|3.5|7.5
    Iowa|US-IA|3.5|7.5
    Kansas|US-KS|3.5|7.5
    Kentucky|US-KY|3.5|7.5
    Louisiana|US-LA|3.5|7.5
    Maine|US-ME|3.5|7.5
    Maryland|US-MD|3.5|7.5
    Massachusetts|US-MA|3.5|7.5
    Michigan|US-MI|3.5|7.5
    Minnesota|US-MN|3.5|7.5
    Mississippi|US-MS|3.5|7.5
    Missouri|US-MO|3.5|7.5
    Montana|US-MT|3.5|7.5
    Nebraska|US-NE|3.5|7.5
    Nevada|US-NV|3.5|7.5
    New Hampshire|US-NH|3.5|7.5
    New Jersey|US-NJ|3.5|7.5
    New Mexico|US-NM|3.5|7.5
    New York|US-NY|3.5|7.5
    North Carolina|US-NC|3.5|7.5
    North Dakota|US-ND|3.5|7.5
    Ohio|US-OH|3.5|7.5
    Oklahoma|US-OK|3.5|7.5
    Oregon|US-OR|3.5|7.5
    Pennsylvania|US-PA|3.5|7.5
    Rhode Island|US-RI|3.5|7.5
    South Carolina|US-SC|3.5|7.5
    South Dakota|US-SD|3.5|7.5
    Tennessee|US-TN|3.5|7.5
    Texas|US-TX|3.5|7.5
    Utah|US-UT|3.5|7.5
    Vermont|US-VT|3.5|7.5
    Virginia|US-VA|3.5|7.5
    Washington|US-WA|3.5|7.5
    West Virginia|US-WV|3.5|7.5
    Wisconsin|US-WI|3.5|7.5
    Wyoming|US-WY|3.5|7.5
    Alberta|CA-AB|3.5|7.5
    British Columbia|CA-BC|3.5|7.5
    Manitoba|CA-MB|3.5|7.5
    New Brunswick|CA-NB|3.5|7.5
    Newfoundland and Labrador|CA-NL|3.5|7.5
    Northwest Territories|CA-NT|3.5|7.5
    Nova Scotia|CA-NS|3.5|7.5
    Nunavut|CA-NU|3.5|7.5
    Ontario|CA-ON|3.5|7.5
    Prince Edward Island|CA-PE|3.5|7.5
    Quebec|CA-QC|3.5|7.5
    Saskatchewan|CA-SK|3.5|7.5
    Yukon|CA-YT|3.5|7.5
    New South Wales|AU-NSW|4.6|8.1
    Queensland|AU-QLD|4.6|8.1
    South Australia|AU-SA|4.6|8.1
    Tasmania|AU-TAS|4.6|8.1
    Victoria|AU-VIC|4.6|8.1
    Western Australia|AU-WA|4.6|8.1
    Australian Capital Territory|AU-ACT|4.6|8.1
    Northern Territory|AU-NT|4.6|8.1
    """;

  public record RegionInfo(String name, String regionIsoCode, double minZoom, double maxZoom) {}

  private static final HashMap<String, RegionInfo> regionInfoByISO;
  static RegionInfo unknownInfo = new RegionInfo("UNKNOWN_REGION", "XX", 8.0, 11.0);

  static {
    regionInfoByISO = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      regionInfoByISO.put(parts[1],
        new RegionInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0));
    }
  }

  public static RegionInfos.RegionInfo getByISO(SourceFeature sf) {
    var isoCode = sf.hasTag("ISO3166-2") ? sf.getString("ISO3166-2") : "XX-XX";
    if (regionInfoByISO.containsKey(isoCode)) {
      return regionInfoByISO.get(isoCode);
    }
    return unknownInfo;
  }
}
