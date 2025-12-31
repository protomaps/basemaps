package com.protomaps.basemap.geometry;

import java.util.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;

/**
 * Utility class for linear geometry operations, particularly splitting LineStrings at fractional positions.
 */
public class Linear {

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

    // Use JTS LengthIndexedLine for efficient extraction
    double totalLength = line.getLength();
    LengthIndexedLine indexedLine = new LengthIndexedLine(line);

    // For each pair of split points, create a segment preserving intermediate vertices
    for (int i = 0; i < points.size() - 1; i++) {
      double startFrac = points.get(i);
      double endFrac = points.get(i + 1);

      double startLength = startFrac * totalLength;
      double endLength = endFrac * totalLength;

      Geometry segment = indexedLine.extractLine(startLength, endLength);
      if (segment instanceof LineString ls && ls.getNumPoints() >= 2) {
        segments.add(ls);
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
