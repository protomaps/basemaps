package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import org.locationtech.jts.geom.Envelope;

public class SpatialFilter {
  public static boolean withinBounds(Envelope bounds, SourceFeature sf) {
    if (bounds != null) {
      try {
        if (!bounds.intersects(sf.latLonGeometry().getEnvelopeInternal())) {
          return false;
        }
      } catch (GeometryException e) {
        e.log("Geometry exception in spatial filter");
      }
    }
    return true;
  }
}
