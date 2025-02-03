package com.protomaps.basemap.postprocess;

import static com.onthegomap.planetiler.geo.GeoUtils.WORLD_BOUNDS;
import static com.onthegomap.planetiler.geo.GeoUtils.latLonToWorldCoords;
import static com.onthegomap.planetiler.render.TiledGeometry.getCoveredTiles;
import static com.onthegomap.planetiler.render.TiledGeometry.sliceIntoTiles;

import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.*;
import com.onthegomap.planetiler.reader.FileFormatException;
import com.onthegomap.planetiler.reader.geojson.GeoJson;
import com.onthegomap.planetiler.render.TiledGeometry;
import com.onthegomap.planetiler.stats.Stats;
import java.nio.file.Path;
import java.util.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.operation.overlayng.OverlayNG;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

public class Clip implements ForwardingProfile.TilePostProcessor {
  private final Map<Integer, Map<TileCoord, List<List<CoordinateSequence>>>> boundaryTilesByZoom;
  private final Map<Integer, TiledGeometry.CoveredTiles> coveredTilesByZoom;
  private final Stats stats;

  static final double DEFAULT_BUFFER = 4.0 / 256.0;

  // A TilePostProcessor that clips all layers to a given geometry.
  // the geometry must be in world coordinates ( world from 0 to 1 )
  public Clip(Stats stats, int minzoom, int maxzoom, boolean doBuffer, Geometry input) {
    this.stats = stats;
    double bufferAmount = 0;
    if (doBuffer) {
      var envelope = input.getEnvelope().getEnvelopeInternal();
      bufferAmount = Math.max(envelope.getWidth(), envelope.getHeight()) * DEFAULT_BUFFER;
    }
    var clipGeometry = input.buffer(bufferAmount);
    boundaryTilesByZoom = new HashMap<>();
    coveredTilesByZoom = new HashMap<>();
    try {
      for (var i = minzoom; i <= maxzoom; i++) {
        var extents = TileExtents.computeFromWorldBounds(i, WORLD_BOUNDS);
        double scale = 1 << i;
        Geometry scaled = AffineTransformation.scaleInstance(scale, scale).transform(clipGeometry);
        this.boundaryTilesByZoom.put(i,
          sliceIntoTiles(scaled, 0, DEFAULT_BUFFER, i, extents.getForZoom(i)).getTileData());
        this.coveredTilesByZoom.put(i, getCoveredTiles(scaled, i, extents.getForZoom(i)));
      }
    } catch (GeometryException e) {
      throw new Planetiler.PlanetilerException("Error clipping", e);
    }
  }

  public static Clip fromGeoJSONFile(Stats stats, int minzoom, int maxzoom, boolean doBuffer, Path path) {
    var g = GeoJson.from(path);
    if (g.count() == 0) {
      throw new FileFormatException("Empty clipping geometry");
    }
    var feature = g.iterator().next();
    return new Clip(stats, minzoom, maxzoom, doBuffer, latLonToWorldCoords(feature.geometry()));
  }

  // Copied from elsewhere in planetiler
  private static Polygon reassemblePolygon(List<CoordinateSequence> group) throws GeometryException {
    try {
      LinearRing first = GeoUtils.JTS_FACTORY.createLinearRing(group.getFirst());
      LinearRing[] rest = new LinearRing[group.size() - 1];
      for (int j = 1; j < group.size(); j++) {
        CoordinateSequence seq = group.get(j);
        CoordinateSequences.reverse(seq);
        rest[j - 1] = GeoUtils.JTS_FACTORY.createLinearRing(seq);
      }
      return GeoUtils.JTS_FACTORY.createPolygon(first, rest);
    } catch (IllegalArgumentException e) {
      throw new GeometryException("reassemble_polygon_failed", "Could not build polygon", e);
    }
  }

  // Copied from elsewhere in Planetiler
  static Geometry reassemblePolygons(List<List<CoordinateSequence>> groups) throws GeometryException {
    int numGeoms = groups.size();
    if (numGeoms == 1) {
      return reassemblePolygon(groups.getFirst());
    } else {
      Polygon[] polygons = new Polygon[numGeoms];
      for (int i = 0; i < numGeoms; i++) {
        polygons[i] = reassemblePolygon(groups.get(i));
      }
      return GeoUtils.JTS_FACTORY.createMultiPolygon(polygons);
    }
  }

  private boolean nonDegenerateGeometry(Geometry geom) {
    return !geom.isEmpty() && geom.getNumGeometries() > 0;
  }

  private Geometry fixGeometry(Geometry geom) throws GeometryException {
    if (geom instanceof Polygonal) {
      geom = GeoUtils.snapAndFixPolygon(geom, stats, "clip");
      return geom.reverse();
    }
    return geom;
  }

  private void addToFeatures(List<VectorTile.Feature> features, VectorTile.Feature feature, Geometry geom) {
    if (nonDegenerateGeometry(geom)) {
      if (geom instanceof GeometryCollection) {
        for (int i = 0; i < geom.getNumGeometries(); i++) {
          features.add(feature.copyWithNewGeometry(geom.getGeometryN(i)));
        }
      } else {
        features.add(feature.copyWithNewGeometry(geom));
      }
    }
  }

  @Override
  public Map<String, List<VectorTile.Feature>> postProcessTile(TileCoord tile,
    Map<String, List<VectorTile.Feature>> layers) throws GeometryException {

    var inCovering =
      this.coveredTilesByZoom.containsKey(tile.z()) && this.coveredTilesByZoom.get(tile.z()).test(tile.x(), tile.y());

    if (!inCovering)
      return Map.of();

    var inBoundary =
      this.boundaryTilesByZoom.containsKey(tile.z()) && this.boundaryTilesByZoom.get(tile.z()).containsKey(tile);

    if (!inBoundary)
      return layers;

    List<List<CoordinateSequence>> coords = boundaryTilesByZoom.get(tile.z()).get(tile);
    var clippingPoly = reassemblePolygons(coords);
    clippingPoly = GeoUtils.fixPolygon(clippingPoly);
    clippingPoly.reverse();
    Map<String, List<VectorTile.Feature>> output = new HashMap<>();

    for (Map.Entry<String, List<VectorTile.Feature>> layer : layers.entrySet()) {
      List<VectorTile.Feature> clippedFeatures = new ArrayList<>();
      for (var feature : layer.getValue()) {
        try {
          var clippedGeom =
            OverlayNGRobust.overlay(feature.geometry().decode(), clippingPoly, OverlayNG.INTERSECTION);
          if (nonDegenerateGeometry(clippedGeom)) {
            addToFeatures(clippedFeatures, feature, fixGeometry(clippedGeom));
          }
        } catch (GeometryException e) {
          e.log(stats, "clip", "Failed to clip geometry");
        }
      }
      if (!clippedFeatures.isEmpty())
        output.put(layer.getKey(), clippedFeatures);
    }
    return output;
  }
}
