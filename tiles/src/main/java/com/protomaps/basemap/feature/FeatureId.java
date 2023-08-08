package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmSourceFeature;

public class FeatureId {

  private FeatureId() {}

  public static long create(SourceFeature sf) {
    if (sf instanceof OsmSourceFeature osmFeature) {
      long elemType;
      var element = osmFeature.originalElement();
      if (element instanceof OsmElement.Relation) {
        elemType = 0x3;
      } else if (element instanceof OsmElement.Way) {
        elemType = 0x2;
      } else {
        elemType = 0x1;
      }
      return (elemType << 44) | element.id();
    }
    return sf.id();
  }
}
