package com.protomaps.basemap.locales;

import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.layers.Roads;

/*
 * Logic specific to the 50 US states.
 * <p>
 * Assigns highway shield text and networks.
 */
public class US extends CartographicLocale {

  @Override
  public Roads.Shield getShield(SourceFeature sf) {
    String ref = sf.getString("ref");
    String network = "other";

    if (ref != null) {
      String firstRef = ref.split(";")[0];
      String shieldText = firstRef;
      if (firstRef.startsWith("US ")) {
        shieldText = firstRef.replace("US ", "");
        network = "US:US";
      } else if (firstRef.startsWith("I ")) {
        shieldText = firstRef.replace("I ", "");
        network = "US:US_I";
      }
      return new Roads.Shield(strip(shieldText), network);
    }

    return new Roads.Shield(null, null);
  }
}
