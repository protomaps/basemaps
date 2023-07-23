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
    Alabama|US|3.5|7.5
    Alaska|US|3.5|7.5
    Arizona|US|3.5|7.5
    Arkansas|US|3.5|7.5
    California|US|3.5|7.5
    Colorado|US|3.5|7.5
    Connecticut|US|3.5|7.5
    Delaware|US|3.5|7.5
    Florida|US|3.5|7.5
    Georgia|US|3.5|7.5
    Hawaii|US|3.5|7.5
    Idaho|US|3.5|7.5
    Illinois|US|3.5|7.5
    Indiana|US|3.5|7.5
    Iowa|US|3.5|7.5
    Kansas|US|3.5|7.5
    Kentucky|US|3.5|7.5
    Louisiana|US|3.5|7.5
    Maine|US|3.5|7.5
    Maryland|US|3.5|7.5
    Massachusetts|US|3.5|7.5
    Michigan|US|3.5|7.5
    Minnesota|US|3.5|7.5
    Mississippi|US|3.5|7.5
    Missouri|US|3.5|7.5
    Montana|US|3.5|7.5
    Nebraska|US|3.5|7.5
    Nevada|US|3.5|7.5
    New Hampshire|US|3.5|7.5
    New Jersey|US|3.5|7.5
    New Mexico|US|3.5|7.5
    New York|US|3.5|7.5
    North Carolina|US|3.5|7.5
    North Dakota|US|3.5|7.5
    Ohio|US|3.5|7.5
    Oklahoma|US|3.5|7.5
    Oregon|US|3.5|7.5
    Pennsylvania|US|3.5|7.5
    Rhode Island|US|3.5|7.5
    South Carolina|US|3.5|7.5
    South Dakota|US|3.5|7.5
    Tennessee|US|3.5|7.5
    Texas|US|3.5|7.5
    Utah|US|3.5|7.5
    Vermont|US|3.5|7.5
    Virginia|US|3.5|7.5
    Washington|US|3.5|7.5
    West Virginia|US|3.5|7.5
    Wisconsin|US|3.5|7.5
    Wyoming|US|3.5|7.5
    Alberta|CA|3.5|7.5
    British Columbia|CA|3.5|7.5
    Manitoba|CA|3.5|7.5
    New Brunswick|CA|3.5|7.5
    Newfoundland and Labrador|CA|3.5|7.5
    Northwest Territories|CA|3.5|7.5
    Nova Scotia|CA|3.5|7.5
    Nunavut|CA|3.5|7.5
    Ontario|CA|3.5|7.5
    Prince Edward Island|CA|3.5|7.5
    Quebec|CA|3.5|7.5
    Saskatchewan|CA|3.5|7.5
    Yukon|CA|3.5|7.5
    New South Wales|AU|4.6|8.1
    Queensland|AU|4.6|8.1
    South Australia|AU|4.6|8.1
    Tasmania|AU|4.6|8.1
    Victoria|AU|4.6|8.1
    Western Australia|AU|4.6|8.1
    Australian Capital Territory|AU|4.6|8.1
    Northern Territory|AU|4.6|8.1
    """;

  public record RegionInfo(String name, String countryIsoCode, double minZoom, double maxZoom) {}

  private static final HashMap<String, RegionInfo> regionInfoByName;
  static RegionInfo unknownInfo = new RegionInfo("UNKNOWN_REGION", "XX", 8.0, 11.0);

  static {
    regionInfoByName = new HashMap<>();
    Scanner s = new Scanner(data);
    while (s.hasNextLine()) {
      String line = s.nextLine();
      String[] parts = line.split("\\|");
      regionInfoByName.put(parts[0],
        new RegionInfo(parts[0], parts[1], Double.parseDouble(parts[2]) - 1.0, Double.parseDouble(parts[3]) - 1.0));
    }
  }

  public static RegionInfo getByName(SourceFeature sf) {
    var name = sf.hasTag("name:en") ? sf.getString("name:en") : sf.getString("name");
    if (regionInfoByName.containsKey(name)) {
      return regionInfoByName.get(name);
    }
    return unknownInfo;
  }
}
