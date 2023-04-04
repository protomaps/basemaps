package com.protomaps.basemap.postprocess;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.geo.GeometryType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;

public class LinkSimplify {
  public static List<VectorTile.Feature> linkSimplify(List<VectorTile.Feature> items, String key, String mainval,
    String linkval) {
    Map<Coordinate, Integer> degrees = new HashMap<>();

    for (VectorTile.Feature item : items) {
      if (item.geometry().geomType() == GeometryType.LINE && item.attrs().get(key).equals(linkval)) {
        try {
          Coordinate[] coordinates = item.geometry().decode().getCoordinates();
          if (coordinates.length == 0)
            continue;
          Coordinate start = coordinates[0];
          Coordinate end = coordinates[coordinates.length - 1];
          degrees.put(start, 0);
          degrees.put(end, 0);
        } catch (GeometryException e) {

        }
      }
    }

    for (VectorTile.Feature item : items) {
      if (item.geometry().geomType() == GeometryType.LINE && item.attrs().get(key).equals(mainval)) {
        try {
          Coordinate[] coordinates = item.geometry().decode().getCoordinates();
          if (coordinates.length == 0)
            continue;
          Coordinate start = coordinates[0];
          Coordinate end = coordinates[coordinates.length - 1];
          if (degrees.containsKey(start)) {
            degrees.put(start, degrees.get(start) + 1);
          }
          if (degrees.containsKey(end)) {
            degrees.put(end, degrees.get(end) + 1);
          }
        } catch (GeometryException e) {
        }
      }
    }

    List<VectorTile.Feature> retval = new ArrayList<>();

    for (VectorTile.Feature item : items) {
      if (item.geometry().geomType() == GeometryType.LINE && item.attrs().get(key).equals(linkval)) {
        try {
          Coordinate[] coordinates = item.geometry().decode().getCoordinates();
          if (coordinates.length == 0)
            continue;
          Coordinate start = coordinates[0];
          Coordinate end = coordinates[coordinates.length - 1];
          if (degrees.get(start) == 1 && degrees.get(end) == 1) {
            retval.add(item);
          }
        } catch (GeometryException e) {
        }
      } else {
        retval.add(item);
      }
    }
    return retval;
  }
}
