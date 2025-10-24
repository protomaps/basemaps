package com.protomaps.basemap.locales;

import com.onthegomap.planetiler.reader.SourceFeature;

public class NL extends CartographicLocale {
  @Override
  public CartographicLocale.Shield getShield(SourceFeature sf) {
    String ref = sf.getString("ref");
    String network = "other";

    if (ref != null) {
      String firstRef = ref.split(";")[0];
      String shieldText = firstRef;
      if (firstRef.startsWith("S")) {
        network = "NL:S-road";
      }
      return new CartographicLocale.Shield(strip(shieldText), network);
    }

    return new CartographicLocale.Shield(null, null);
  }
}
