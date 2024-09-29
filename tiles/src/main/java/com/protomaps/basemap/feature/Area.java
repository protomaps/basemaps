package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.reader.SourceFeature;

public class Area {
  public static double worldAreaToSquareMeters(SourceFeature sf) {
    try {
      double oneSideWorld = Math.sqrt(sf.area());
      double oneSidePixels = oneSideWorld * 256d;
      double oneSideMeters = oneSidePixels * GeoUtils.metersPerPixelAtEquator(0);
      double raw_value = Math.pow(oneSideMeters, 2);
      if (raw_value < 501) {
        return 500;
      } else if (raw_value < 5001) {
        return 5000;
      } else {
        return 50000;
      }
    } catch (Exception e) {
      return 0.0;
    }
  }
}
