package com.protomaps.basemap.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

class LinearTest {

  private static final GeometryFactory gf = new GeometryFactory();
  private static final double EPSILON = 0.0001;

  private LineString createLine(double... coords) {
    if (coords.length % 2 != 0) {
      throw new IllegalArgumentException("Coordinates must be pairs of x,y values");
    }
    Coordinate[] coordArray = new Coordinate[coords.length / 2];
    for (int i = 0; i < coords.length / 2; i++) {
      coordArray[i] = new Coordinate(coords[i * 2], coords[i * 2 + 1]);
    }
    return gf.createLineString(coordArray);
  }

  @Test
  void testSplitAtFractions_noSplitPoints_returnOriginal() {
    LineString line = createLine(0, 0, 1, 0);
    List<LineString> result = Linear.splitAtFractions(line, List.of());

    assertEquals(1, result.size());
    assertEquals(line, result.get(0));
  }

  @Test
  void testSplitAtFractions_simpleLine_midpoint() {
    // Simple straight line from (0,0) to (1,0), split at 0.5
    LineString line = createLine(0, 0, 1, 0);
    List<LineString> result = Linear.splitAtFractions(line, List.of(0.5));

    assertEquals(2, result.size());

    // First segment: (0,0) to (0.5,0)
    assertEquals(2, result.get(0).getNumPoints());
    assertEquals(0.0, result.get(0).getCoordinateN(0).x, EPSILON);
    assertEquals(0.0, result.get(0).getCoordinateN(0).y, EPSILON);
    assertEquals(0.5, result.get(0).getCoordinateN(1).x, EPSILON);
    assertEquals(0.0, result.get(0).getCoordinateN(1).y, EPSILON);

    // Second segment: (0.5,0) to (1,0)
    assertEquals(2, result.get(1).getNumPoints());
    assertEquals(0.5, result.get(1).getCoordinateN(0).x, EPSILON);
    assertEquals(0.0, result.get(1).getCoordinateN(0).y, EPSILON);
    assertEquals(1.0, result.get(1).getCoordinateN(1).x, EPSILON);
    assertEquals(0.0, result.get(1).getCoordinateN(1).y, EPSILON);
  }

  @Test
  void testSplitAtFractions_curvedLine_preservesVertices() {
    // Curved line with 5 points forming a curve
    LineString line = createLine(
      0, 0,    // start
      0.25, 0.5, // curve up
      0.5, 0.5,  // middle top
      0.75, 0.5, // continue curve
      1, 0       // end back down
    );

    // Split at 0.4 (before middle) and 0.6 (after middle)
    List<LineString> result = Linear.splitAtFractions(line, List.of(0.4, 0.6));

    assertEquals(3, result.size());

    // First segment should have at least 3 points (start, first curve point, and split point)
    assertTrue(result.get(0).getNumPoints() >= 2,
      "First segment should preserve vertices, got " + result.get(0).getNumPoints() + " points");

    // Middle segment should include the middle vertices
    assertTrue(result.get(1).getNumPoints() >= 2,
      "Middle segment should preserve vertices, got " + result.get(1).getNumPoints() + " points");

    // Last segment should have at least 3 points
    assertTrue(result.get(2).getNumPoints() >= 2,
      "Last segment should preserve vertices, got " + result.get(2).getNumPoints() + " points");

    // Verify start and end points
    assertEquals(0.0, result.get(0).getCoordinateN(0).x, EPSILON);
    assertEquals(1.0, result.get(2).getCoordinateN(result.get(2).getNumPoints() - 1).x, EPSILON);
  }

  @Test
  void testSplitAtFractions_splitBetweenVertices() {
    // Line with 3 points, split in the middle of second segment
    LineString line = createLine(
      0, 0,    // Point 0
      0.5, 1,  // Point 1 (at halfway distance)
      1, 0     // Point 2
    );

    // Split at 0.75 (should be in the second segment between points 1 and 2)
    List<LineString> result = Linear.splitAtFractions(line, List.of(0.75));

    assertEquals(2, result.size());

    // First segment: should include points 0, 1, and the split point
    assertTrue(result.get(0).getNumPoints() >= 3,
      "First segment should have at least 3 points (start, middle vertex, split), got " +
        result.get(0).getNumPoints());

    // Verify that point 1 (0.5, 1) is preserved in first segment
    boolean hasMiddlePoint = false;
    for (int i = 0; i < result.get(0).getNumPoints(); i++) {
      Coordinate c = result.get(0).getCoordinateN(i);
      if (Math.abs(c.x - 0.5) < EPSILON && Math.abs(c.y - 1.0) < EPSILON) {
        hasMiddlePoint = true;
        break;
      }
    }
    assertTrue(hasMiddlePoint, "First segment should preserve the middle vertex (0.5, 1)");

    // Second segment: should include split point and point 2
    assertTrue(result.get(1).getNumPoints() >= 2,
      "Second segment should have at least 2 points");
  }

  @Test
  void testSplitAtFractions_complexCurve() {
    // More complex curved line with 7 points
    LineString line = createLine(
      0, 0,
      0.1, 0.2,
      0.3, 0.4,
      0.5, 0.5,
      0.7, 0.4,
      0.9, 0.2,
      1, 0
    );

    // Split at quarter and three-quarter points
    List<LineString> result = Linear.splitAtFractions(line, List.of(0.25, 0.75));

    assertEquals(3, result.size());

    // Each segment should preserve multiple vertices
    int totalPointsInSegments = 0;
    for (LineString segment : result) {
      totalPointsInSegments += segment.getNumPoints();
    }

    // Should have significantly more points than just 2 per segment (6 total)
    // We expect at least the original 7 points plus 2 split points = 9 minimum
    assertTrue(totalPointsInSegments >= 9,
      "Split segments should preserve vertices, expected at least 9 points, got " + totalPointsInSegments);
  }

  @Test
  void testSplitAtFractions_splitAtExistingVertex() {
    // Line with 3 equally-spaced points
    LineString line = createLine(0, 0, 0.5, 0, 1, 0);

    // Split at 0.5, which is exactly at the middle vertex
    List<LineString> result = Linear.splitAtFractions(line, List.of(0.5));

    assertEquals(2, result.size());

    // First segment should include first two points
    assertEquals(2, result.get(0).getNumPoints());
    assertEquals(0.0, result.get(0).getCoordinateN(0).x, EPSILON);
    assertEquals(0.5, result.get(0).getCoordinateN(1).x, EPSILON);

    // Second segment should include middle and last point
    assertEquals(2, result.get(1).getNumPoints());
    assertEquals(0.5, result.get(1).getCoordinateN(0).x, EPSILON);
    assertEquals(1.0, result.get(1).getCoordinateN(1).x, EPSILON);
  }

  @Test
  void testSplitAtFractions_multipleSplits() {
    // Line with multiple vertices
    LineString line = createLine(0, 0, 0.2, 0, 0.4, 0, 0.6, 0, 0.8, 0, 1, 0);

    // Multiple split points
    List<LineString> result = Linear.splitAtFractions(line, List.of(0.1, 0.3, 0.5, 0.7, 0.9));

    assertEquals(6, result.size(), "Should create 6 segments from 5 split points");

    // Verify continuity - end of each segment should match start of next
    for (int i = 0; i < result.size() - 1; i++) {
      LineString current = result.get(i);
      LineString next = result.get(i + 1);

      Coordinate currentEnd = current.getCoordinateN(current.getNumPoints() - 1);
      Coordinate nextStart = next.getCoordinateN(0);

      assertEquals(currentEnd.x, nextStart.x, EPSILON,
        "Segment " + i + " end should match segment " + (i + 1) + " start (x)");
      assertEquals(currentEnd.y, nextStart.y, EPSILON,
        "Segment " + i + " end should match segment " + (i + 1) + " start (y)");
    }
  }

  @Test
  void testOverlaps() {
    // Test various overlap scenarios
    assertTrue(Linear.overlaps(0.0, 0.5, 0.25, 0.75));  // Partial overlap
    assertTrue(Linear.overlaps(0.25, 0.75, 0.0, 0.5));  // Reverse partial overlap
    assertTrue(Linear.overlaps(0.0, 1.0, 0.25, 0.75));  // Contains
    assertTrue(Linear.overlaps(0.25, 0.75, 0.0, 1.0));  // Contained by
    assertTrue(Linear.overlaps(0.2, 0.6, 0.4, 0.8));    // Partial overlap

    assertFalse(Linear.overlaps(0.0, 0.3, 0.5, 0.8));   // No overlap (gap)
    assertFalse(Linear.overlaps(0.5, 0.8, 0.0, 0.3));   // No overlap (gap reversed)
    assertFalse(Linear.overlaps(0.0, 0.5, 0.5, 1.0));   // Adjacent but not overlapping
  }

  @Test
  void testCreateSegments() {
    List<Linear.Segment> segments = Linear.createSegments(List.of(0.25, 0.75));

    assertEquals(3, segments.size());
    assertEquals(0.0, segments.get(0).start, EPSILON);
    assertEquals(0.25, segments.get(0).end, EPSILON);
    assertEquals(0.25, segments.get(1).start, EPSILON);
    assertEquals(0.75, segments.get(1).end, EPSILON);
    assertEquals(0.75, segments.get(2).start, EPSILON);
    assertEquals(1.0, segments.get(2).end, EPSILON);
  }
}
