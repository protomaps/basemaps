package com.protomaps.basemap.postprocess;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Area {

  private Area() {}

  public static List<VectorTile.Feature> filterArea(List<VectorTile.Feature> items, double minArea)
    throws GeometryException {
    List<VectorTile.Feature> result = new ArrayList(items.size());
    for (var item : items) {
      var area = item.geometry().decode().getEnvelopeInternal().getArea();

      if (minArea > 0 && area < minArea) {
        // do nothing
      } else {
        if (minArea < 30000 / (4096 * 4096) * (256 * 256)) {
          Set<String> keys = new HashSet<>(item.tags().keySet());
          for (String key : keys) {
            if (key.equals("name") || key.startsWith("name:")) {
              item.tags().remove(key);
            }
          }
        }

        result.add(item);
      }
    }

    return result;
  }
}
