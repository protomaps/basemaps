package com.protomaps.basemap.postprocess;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPolygon;
import static org.junit.jupiter.api.Assertions.*;

import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.geo.TileCoord;
import com.onthegomap.planetiler.reader.FileFormatException;
import com.onthegomap.planetiler.stats.Stats;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClipTest {
  private final Stats stats = Stats.inMemory();

  @Test
  void testLoadGeoJSON() {
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "clip.geojson");
    var clip = Clip.fromGeoJSONFile(stats, 0, 0, false, cwd.resolveSibling(pathFromRoot));
    assertNotNull(clip);
  }

  @Test
  void testLoadNonJSON() {
    Path cwd = Path.of("").toAbsolutePath();
    Path pathFromRoot = Path.of("tiles", "src", "test", "resources", "empty.geojson");
    Path path = cwd.resolveSibling(pathFromRoot);
    assertThrows(FileFormatException.class, () -> {
      Clip.fromGeoJSONFile(stats, 0, 0, false, path);
    });
  }

  @Test
  void testClipLine() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      // a horizontal line in the across the middle of the 0,0,0 tile.
      VectorTile.encodeGeometry(newLineString(0, 128, 256, 128)),
      Map.of("foo", "bar")
    ));

    // a rectangle that is 50% of the earths width, centered at null island.
    var n = new Clip(stats, 0, 0, false, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(0, 0, 0), Map.of("layer", unclipped));

    assertEquals(1, clipped.size());
    assertEquals(1, clipped.get("layer").size());
    assertEquals(newLineString(64, 128, 192, 128), clipped.get("layer").getFirst().geometry().decode());
  }

  @Test
  void testClipLineMulti() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      // a V shape that enters and leaves the clipping square
      VectorTile.encodeGeometry(newLineString(32, 128, 128, 224, 224, 128)),
      Map.of("foo", "bar")
    ));

    // a rectangle that is 50% of the earths width, centered at null island.
    var n = new Clip(stats, 0, 0, false, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(0, 0, 0), Map.of("layer", unclipped));

    assertEquals(1, clipped.size());
    assertEquals(2, clipped.get("layer").size());
    assertEquals(newLineString(64, 160, 96, 192), clipped.get("layer").get(0).geometry().decode());
    assertEquals(newLineString(160, 192, 192, 160), clipped.get("layer").get(1).geometry().decode());
  }

  @Test
  void testClipPolygon() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newPolygon(32, 160, 96, 160, 96, 224, 32, 224, 32, 160)),
      Map.of("foo", "bar")
    ));

    // a rectangle that is 50% of the earths width, centered at null island.
    var n = new Clip(stats, 0, 0, false, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(0, 0, 0), Map.of("layer", unclipped));

    assertEquals(1, clipped.size());
    assertEquals(1, clipped.get("layer").size());
    assertEquals(newPolygon(64, 160, 96, 160, 96, 192, 64, 192, 64, 160),
      clipped.get("layer").getFirst().geometry().decode());
  }

  @Test
  void testClipBelowMinZoom() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newLineString(0, 128, 256, 128)),
      Map.of("foo", "bar")
    ));

    var n = new Clip(stats, 1, 1, false, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(0, 0, 0), Map.of("layer", unclipped));
    assertEquals(0, clipped.size());
  }

  @Test
  void testClipWhollyOutside() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newLineString(0, 1, 5, 1)),
      Map.of("foo", "bar")
    ));

    var n = new Clip(stats, 0, 0, false, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(0, 0, 0), Map.of("layer", unclipped));
    assertEquals(0, clipped.size());
  }

  @Test
  void testClipInInterior() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newLineString(0, 1, 5, 1)),
      Map.of("foo", "bar")
    ));

    var n = new Clip(stats, 0, 3, false, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(3, 3, 3), Map.of("layer", unclipped));
    assertEquals(1, clipped.size());
    assertEquals(1, clipped.get("layer").size());
  }

  @Test
  void testClipLineBuffer() throws GeometryException {
    List<VectorTile.Feature> unclipped = new ArrayList<>();
    unclipped.add(new VectorTile.Feature("layer", 1,
      VectorTile.encodeGeometry(newLineString(0, 128, 256, 128)),
      Map.of("foo", "bar")
    ));

    // a rectangle that is 50% of the earths width, centered at null island.
    var n = new Clip(stats, 0, 0, true, newPolygon(0.25, 0.25, 0.75, 0.25, 0.75, 0.75, 0.25, 0.75, 0.25, 0.25));
    var clipped = n.postProcessTile(TileCoord.ofXYZ(0, 0, 0), Map.of("layer", unclipped));

    assertEquals(1, clipped.size());
    assertEquals(1, clipped.get("layer").size());
    assertEquals(newLineString(62, 128, 194, 128), clipped.get("layer").getFirst().geometry().decode());
  }
}
