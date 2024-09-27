package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SourceFeature;

public class Area {
  public static double worldAreaToSquareMeters(SourceFeature sf) {
    try {
      double oneSideWorld = Math.sqrt(sf.area());
      double oneSidePixels = oneSideWorld * 256d;
      double oneSideMeters = oneSidePixels * GeoUtils.metersPerPixelAtEquator(0);
      return Math.pow(oneSideMeters, 2);
    } catch (Exception e) {
      return 0.0;
    }
  }
}
