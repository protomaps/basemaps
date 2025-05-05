package com.protomaps.basemap.locales;

import com.onthegomap.planetiler.reader.SourceFeature;

/*
 * Encapsulates country-specific logic applied to OpenStreetMap tags.
 * <p>
 * This is a grab-bag of logic functions for determining output tags in tiled features
 * based on a spatial join of input features to polygon locales.
 * CartographicLocale is the parent class that applies to locales outside of any
 * polygon, locales that are unimplemented, or default behavior when a locale
 * does not override a method.
 *
 * Each implemented locale is named by 2-letter ISO code.
 */
public class CartographicLocale {

  public record Shield(String text, String network) {}

  protected String strip(String s) {
    if (s != null) {
      return s.replaceAll("\\s", "");
    }
    return null;
  }

  public Shield getShield(SourceFeature sf) {
    String ref = sf.getString("ref");
    if (ref != null) {
      String firstRef = ref.split(";")[0];
      return new Shield(strip(firstRef), "other");
    }
    return new Shield(null, null);
  }
}
