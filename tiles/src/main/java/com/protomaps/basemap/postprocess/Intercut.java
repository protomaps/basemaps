package com.protomaps.basemap.postprocess;

import static com.onthegomap.planetiler.geo.GeoUtils.EMPTY_POLYGON;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeoUtils;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.geo.GeometryType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class Intercut {

  private Intercut() {}

  public static LinkedHashMap<String, Geometry> groupByKind(
    List<VectorTile.Feature> features
  ) {
    LinkedHashMap<String, List<VectorTile.Feature>> groupedByKind = new LinkedHashMap<>();
    for (VectorTile.Feature feature : features) {
      if (feature == null) {
        // ignore
      } else if (feature.geometry().geomType() != GeometryType.POLYGON) {
        // just ignore and pass through non-polygon features
      } else {
        groupedByKind
          .computeIfAbsent((String) feature.attrs().get("pmap:kind"), k -> new ArrayList<>())
          .add(feature);
      }
    }

    LinkedHashMap<String, Geometry> retval = new LinkedHashMap<>();
    for (Map.Entry<String, List<VectorTile.Feature>> entry : groupedByKind.entrySet()) {
      var collection = GeoUtils.createGeometryCollection(entry.getValue().stream().map(f -> {
        try {
          return f.geometry().decode();
        } catch (GeometryException e) {
          throw new RuntimeException(e);
        }
      }).toList());
      retval.put(entry.getKey(), collection.union());
      collection.union();
    }

    return retval;
  }

  public static List<VectorTile.Feature> intercut(List<VectorTile.Feature> items) throws GeometryException {
    var res = groupByKind(
      items.stream().filter(i -> (Boolean) i.attrs().getOrDefault("isGhostFeature", Boolean.TRUE)).toList());

    Map<String, Geometry> coverageUnion = new LinkedHashMap<>();

    Geometry total = EMPTY_POLYGON;
    // based on a total ordering of landuses
    for (Map.Entry<String, Geometry> entry : res.entrySet()) {
      coverageUnion.put(entry.getKey(), entry.getValue().difference(total));
      total = total.union(entry.getValue());
    }
    coverageUnion.entrySet().removeIf(i -> i.getValue().isEmpty());

    GeometryFactory gf = GeoUtils.JTS_FACTORY;
    Geometry outside = gf.toGeometry(new Envelope(-4, 256 + 4.0, -4, 256 + 4.0));
    outside = outside.difference(total);


    //    for (Map.Entry<String,Geometry> entry : coverageUnion.entrySet()) {
    //      var attrs = new LinkedHashMap<String, Object>();
    //      attrs.put("landuse_kind",entry.getKey());
    //      if (!entry.getValue().isEmpty()) {
    //        items.add(new VectorTile.Feature("roads",0, VectorTile.encodeGeometry(entry.getValue()), attrs));
    //      }
    //    }

    // use coverageUnion for intercutting
    ArrayList<VectorTile.Feature> newItems = new ArrayList<>();

    for (VectorTile.Feature item : items) {
      for (Map.Entry<String, Geometry> entry : coverageUnion.entrySet()) {
        var intersected = item.geometry().decode().intersection(entry.getValue());
        if (!intersected.isEmpty()) {
          var newAttrs = new LinkedHashMap<>(item.attrs());
          newAttrs.put("landuse_kind", entry.getKey());
          VectorTile.Feature feature =
            new VectorTile.Feature(item.layer(), item.id(), VectorTile.encodeGeometry(intersected), newAttrs);
          newItems.add(feature);
        }
      }

      // do the outside
      var intersected = item.geometry().decode().intersection(outside);
      if (!intersected.isEmpty()) {
        var newAttrs = new LinkedHashMap<>(item.attrs());
        newAttrs.put("landuse_kind", "NONE");
        VectorTile.Feature feature =
          new VectorTile.Feature(item.layer(), item.id(), VectorTile.encodeGeometry(intersected), newAttrs);
        newItems.add(feature);
      }
    }
    return newItems;
  }
}
