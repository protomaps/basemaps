package com.protomaps.basemap.postprocess;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.geo.GeometryType;
import java.util.*;
import org.locationtech.jts.geom.Coordinate;


public class LinkSimplify {

  private LinkSimplify() {}

  /**
   * Post-processing to remove "hairballs" from road networks.
   * <p>
   * OpenStreetMap uses the highway=motorway_link tag to connect motorways to motorways, as well as connect motorways to
   * lower-class highway features. If we include all ways at all zooms, motorways will grow "hairballs" once the
   * lower-class features are generalized away.
   * <p>
   * LinkSimplify uses some very basic heuristics to distinguish "connectors" and "offramps": If a link way's 2
   * endpoints both touch (a motorway at any point | another link at an endpoint), keep it. (we're not guaranteed
   * connectors are a single way, it can be multiple) Otherwise throw it away (an offramp)
   * <p>
   * The above logic generalizes to any passed tag, not just highway=motorway | motorway_link.
   * </p>
   */
  public static List<VectorTile.Feature> linkSimplify(List<VectorTile.Feature> items, String key, String mainval,
    String linkval) throws GeometryException {

    Map<Coordinate, Integer> degrees = new HashMap<>();

    for (VectorTile.Feature item : items) {
      if (item.geometry().geomType() == GeometryType.LINE) {
        if (item.tags().containsKey(key) && item.tags().get(key).equals(linkval)) {
          Coordinate[] coordinates = item.geometry().decode().getCoordinates();
          if (coordinates.length == 0)
            continue;
          Coordinate start = coordinates[0];
          Coordinate end = coordinates[coordinates.length - 1];
          if (degrees.containsKey(start)) {
            degrees.put(start, degrees.get(start) + 1);
          } else {
            degrees.put(start, 1);
          }
          if (degrees.containsKey(end)) {
            degrees.put(end, degrees.get(end) + 1);
          } else {
            degrees.put(end, 1);
          }

        } else if (item.tags().containsKey(key) && item.tags().get(key).equals(mainval)) {
          Coordinate[] coordinates = item.geometry().decode().getCoordinates();
          for (Coordinate c : coordinates) {
            if (degrees.containsKey(c)) {
              degrees.put(c, degrees.get(c) + 1);
            } else {
              degrees.put(c, 1);
            }
          }
        }
      }
    }

    List<VectorTile.Feature> output = new ArrayList<>();

    for (VectorTile.Feature item : items) {
      if (item.geometry().geomType() == GeometryType.LINE && item.tags().containsKey(key) &&
        item.tags().get(key).equals(linkval)) {
        Coordinate[] coordinates = item.geometry().decode().getCoordinates();
        if (coordinates.length == 0)
          continue;
        Coordinate start = coordinates[0];
        Coordinate end = coordinates[coordinates.length - 1];
        if (degrees.get(start) >= 2 && degrees.get(end) >= 2) {
          output.add(item);
        }
      } else {
        output.add(item);
      }
    }
    return output;
  }
}
