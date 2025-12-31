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
    public final double end;   // Fractional position 0.0-1.0

    public Segment(double start, double end) {
      this.start = start;
      this.end = end;
    }
  }

  /**
   * Split a LineString at fractional positions and return list of split LineStrings.
   *
   * @param line The LineString to split
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

    // Calculate total length
    double totalLength = 0.0;
    for (int i = 0; i < line.getNumPoints() - 1; i++) {
      Coordinate c1 = line.getCoordinateN(i);
      Coordinate c2 = line.getCoordinateN(i + 1);
      totalLength += c1.distance(c2);
    }

    // For each pair of split points, create a segment
    for (int i = 0; i < points.size() - 1; i++) {
      double startFrac = points.get(i);
      double endFrac = points.get(i + 1);

      Coordinate startCoord = getCoordinateAtFraction(line, startFrac, totalLength);
      Coordinate endCoord = getCoordinateAtFraction(line, endFrac, totalLength);

      if (startCoord != null && endCoord != null && !startCoord.equals2D(endCoord)) {
        LineString segment = GEOMETRY_FACTORY.createLineString(new Coordinate[]{startCoord, endCoord});
        segments.add(segment);
      }
    }

    return segments;
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
   * @param line The LineString
   * @param fraction Fractional position (0.0-1.0)
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
   * @param segStart Start of segment (0.0-1.0)
   * @param segEnd End of segment (0.0-1.0)
   * @param rangeStart Start of range (0.0-1.0)
   * @param rangeEnd End of range (0.0-1.0)
   * @return true if the segment overlaps with the range
   */
  public static boolean overlaps(double segStart, double segEnd, double rangeStart, double rangeEnd) {
    // Segments overlap if they share any fractional position
    return segEnd > rangeStart && segStart < rangeEnd;
  }
}
