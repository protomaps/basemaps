package com.protomaps.basemap.locales;

import com.onthegomap.planetiler.reader.SourceFeature;

/*
 * Logic specific to the 50 US states.
 * <p>
 * Assigns highway shield text and networks.
 */
public class US extends CartographicLocale {

  @Override
  public Shield getShield(SourceFeature sf) {
    String ref = sf.getString("ref");
    String network = "other";

    if (ref != null) {
      String firstRef = ref.split(";")[0];
      String shieldText = firstRef;
      if (firstRef.startsWith("US ")) {
        shieldText = firstRef.replace("US ", "");
        network = "US";
      } else if (firstRef.startsWith("I ")) {
        shieldText = firstRef.replace("I ", "");
        network = "US:I";
      }
      return new Shield(strip(shieldText), network);
    }

    return new Shield(null, null);
  }
}
