package com.protomaps.basemap.postprocess;

import com.onthegomap.planetiler.VectorTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HighRoadPlus {
  // Duplicates the geometry of any road feature with layer >= 2 or layer <= -2,
  // with identical tags, plus the addition of a `casing=true` tag.

  public static List<VectorTile.Feature> duplicate(List<VectorTile.Feature> items) {
    ArrayList<VectorTile.Feature> retval = new ArrayList<>();
    for (var item : items) {
      Object layerObj = item.attrs().get("layer");
      if (layerObj instanceof Long) {
        long layer = (Long) layerObj;
        if (layer <= -2 || layer >= 2) {
          retval.add(item.copyWithExtraAttrs(Map.of("casing", true)));
        }
      }

      retval.add(item);
    }
    return retval;
  }
}
