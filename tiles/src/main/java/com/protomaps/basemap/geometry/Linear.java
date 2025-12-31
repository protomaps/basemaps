package com.protomaps.basemap.geometry;

import java.util.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Utility class for linear geometry operations, particularly splitting LineStrings at fractional positions.
 */
public class Linear {

  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  /**
   * Represents a segment of a line with fractional start/end positions.
   */
  public static class Segment {
    public final double start; // Fractional position 0.0-1.0
    public final double end; // Fractional position 0.0-1.0

    public Segment(double start, double end) {
      this.start = start;
      this.end = end;
    }
  }

  /**
   * Split a LineString at fractional positions and return list of split LineStrings.
   * Preserves all intermediate vertices between split points to maintain curve geometry.
   *
   * @param line        The LineString to split
   * @param splitPoints List of fractional positions (0.0-1.0) where to split
   * @return List of LineString segments
   */
  public static List<LineString> splitAtFractions(LineString line, List<Double> splitPoints) {
    if (splitPoints.isEmpty()) {
      return List.of(line);
    }

    // Sort split points and remove duplicates, ensure 0.0 and 1.0 are included
    Set<Double> pointSet = new TreeSet<>();
    pointSet.add(0.0);
    pointSet.addAll(splitPoints);
    pointSet.add(1.0);

    List<Double> points = new ArrayList<>(pointSet);
    List<LineString> segments = new ArrayList<>();

    // Calculate total length and cumulative distances at each vertex
    double[] cumulativeDistances = new double[line.getNumPoints()];
    cumulativeDistances[0] = 0.0;
    double totalLength = 0.0;

    for (int i = 0; i < line.getNumPoints() - 1; i++) {
      Coordinate c1 = line.getCoordinateN(i);
      Coordinate c2 = line.getCoordinateN(i + 1);
      totalLength += c1.distance(c2);
      cumulativeDistances[i + 1] = totalLength;
    }

    // For each pair of split points, create a segment preserving intermediate vertices
    for (int i = 0; i < points.size() - 1; i++) {
      double startFrac = points.get(i);
      double endFrac = points.get(i + 1);

      List<Coordinate> segmentCoords = extractSegmentCoordinates(
        line, startFrac, endFrac, totalLength, cumulativeDistances);

      if (segmentCoords.size() >= 2) {
        LineString segment = GEOMETRY_FACTORY.createLineString(segmentCoords.toArray(new Coordinate[0]));
        segments.add(segment);
      }
    }

    return segments;
  }

  /**
   * Extract all coordinates between startFrac and endFrac, preserving intermediate vertices.
   *
   * @param line                 The source LineString
   * @param startFrac            Start fraction (0.0-1.0)
   * @param endFrac              End fraction (0.0-1.0)
   * @param totalLength          Total length of the line
   * @param cumulativeDistances  Cumulative distances at each vertex
   * @return List of coordinates for the segment
   */
  private static List<Coordinate> extractSegmentCoordinates(
    LineString line, double startFrac, double endFrac,
    double totalLength, double[] cumulativeDistances) {

    List<Coordinate> coords = new ArrayList<>();
    double startDist = startFrac * totalLength;
    double endDist = endFrac * totalLength;

    // Find the segment containing the start position
    int startSegmentIdx = -1;
    for (int i = 0; i < line.getNumPoints() - 1; i++) {
      if (cumulativeDistances[i] <= startDist && startDist <= cumulativeDistances[i + 1]) {
        startSegmentIdx = i;
        break;
      }
    }

    // Find the segment containing the end position
    int endSegmentIdx = -1;
    for (int i = 0; i < line.getNumPoints() - 1; i++) {
      if (cumulativeDistances[i] <= endDist && endDist <= cumulativeDistances[i + 1]) {
        endSegmentIdx = i;
        break;
      }
    }

    if (startSegmentIdx == -1 || endSegmentIdx == -1) {
      // Fallback to simple 2-point line
      Coordinate start = getCoordinateAtFraction(line, startFrac, totalLength);
      Coordinate end = getCoordinateAtFraction(line, endFrac, totalLength);
      if (start != null && end != null) {
        coords.add(start);
        coords.add(end);
      }
      return coords;
    }

    // Add the start coordinate (interpolated if not at a vertex)
    if (Math.abs(cumulativeDistances[startSegmentIdx] - startDist) < 1e-10) {
      // Start is at a vertex
      coords.add(line.getCoordinateN(startSegmentIdx));
    } else {
      // Interpolate within the start segment
      Coordinate c1 = line.getCoordinateN(startSegmentIdx);
      Coordinate c2 = line.getCoordinateN(startSegmentIdx + 1);
      double segmentLength = c1.distance(c2);
      double distIntoSegment = startDist - cumulativeDistances[startSegmentIdx];
      double segmentFraction = distIntoSegment / segmentLength;
      double x = c1.x + (c2.x - c1.x) * segmentFraction;
      double y = c1.y + (c2.y - c1.y) * segmentFraction;
      coords.add(new Coordinate(x, y));
    }

    // Add all intermediate vertices between start and end segments
    for (int i = startSegmentIdx + 1; i <= endSegmentIdx; i++) {
      // Don't duplicate if this vertex is exactly at startDist
      if (Math.abs(cumulativeDistances[i] - startDist) > 1e-10) {
        coords.add(line.getCoordinateN(i));
      }
    }

    // Add the end coordinate (interpolated if not at a vertex)
    if (Math.abs(cumulativeDistances[endSegmentIdx + 1] - endDist) < 1e-10) {
      // End is exactly at a vertex
      // Only add if not already added (could be same as last intermediate vertex)
      Coordinate lastCoord = coords.isEmpty() ? null : coords.get(coords.size() - 1);
      Coordinate endVertex = line.getCoordinateN(endSegmentIdx + 1);
      if (lastCoord == null || !lastCoord.equals2D(endVertex)) {
        coords.add(endVertex);
      }
    } else {
      // Interpolate within the end segment
      Coordinate c1 = line.getCoordinateN(endSegmentIdx);
      Coordinate c2 = line.getCoordinateN(endSegmentIdx + 1);
      double segmentLength = c1.distance(c2);
      double distIntoSegment = endDist - cumulativeDistances[endSegmentIdx];
      double segmentFraction = distIntoSegment / segmentLength;
      double x = c1.x + (c2.x - c1.x) * segmentFraction;
      double y = c1.y + (c2.y - c1.y) * segmentFraction;
      coords.add(new Coordinate(x, y));
    }

    return coords;
  }

  /**
   * Create list of Segments representing the split ranges between all split points.
   *
   * @param splitPoints List of fractional positions (0.0-1.0) where to split
   * @return List of Segment objects with start/end fractions
   */
  public static List<Segment> createSegments(List<Double> splitPoints) {
    if (splitPoints.isEmpty()) {
      return List.of(new Segment(0.0, 1.0));
    }

    // Sort split points and remove duplicates, ensure 0.0 and 1.0 are included
    Set<Double> pointSet = new TreeSet<>();
    pointSet.add(0.0);
    pointSet.addAll(splitPoints);
    pointSet.add(1.0);

    List<Double> points = new ArrayList<>(pointSet);
    List<Segment> segments = new ArrayList<>();

    for (int i = 0; i < points.size() - 1; i++) {
      segments.add(new Segment(points.get(i), points.get(i + 1)));
    }

    return segments;
  }

  /**
   * Get coordinate at fractional position along line.
   *
   * @param line        The LineString
   * @param fraction    Fractional position (0.0-1.0)
   * @param totalLength Pre-calculated total length of the line
   * @return Coordinate at the fractional position
   */
  private static Coordinate getCoordinateAtFraction(LineString line, double fraction, double totalLength) {
    if (fraction <= 0.0) {
      return line.getCoordinateN(0);
    }
    if (fraction >= 1.0) {
      return line.getCoordinateN(line.getNumPoints() - 1);
    }

    double targetDist = fraction * totalLength;
    double currentDist = 0.0;

    for (int i = 0; i < line.getNumPoints() - 1; i++) {
      Coordinate c1 = line.getCoordinateN(i);
      Coordinate c2 = line.getCoordinateN(i + 1);
      double segmentLength = c1.distance(c2);

      if (currentDist + segmentLength >= targetDist) {
        // Interpolate within this segment
        double segmentFraction = (targetDist - currentDist) / segmentLength;
        double x = c1.x + (c2.x - c1.x) * segmentFraction;
        double y = c1.y + (c2.y - c1.y) * segmentFraction;
        return new Coordinate(x, y);
      }

      currentDist += segmentLength;
    }

    return line.getCoordinateN(line.getNumPoints() - 1);
  }

  /**
   * Check if a segment defined by [segStart, segEnd] overlaps with a range [rangeStart, rangeEnd].
   *
   * @param segStart   Start of segment (0.0-1.0)
   * @param segEnd     End of segment (0.0-1.0)
   * @param rangeStart Start of range (0.0-1.0)
   * @param rangeEnd   End of range (0.0-1.0)
   * @return true if the segment overlaps with the range
   */
  public static boolean overlaps(double segStart, double segEnd, double rangeStart, double rangeEnd) {
    // Segments overlap if they share any fractional position
    return segEnd > rangeStart && segStart < rangeEnd;
  }
}
