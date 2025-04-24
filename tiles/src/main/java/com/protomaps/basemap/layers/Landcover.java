package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Point;

@SuppressWarnings("java:S1192")
public class Landcover implements ForwardingProfile.LayerPostProcessor {

  static final Map<String, String> kindMapping = Map.of("urban", "urban_area", "crop", "farmland", "grass", "grassland",
    "trees", "forest", "snow", "glacier", "shrub", "scrub");

  static final Map<String, Integer> sortKeyMapping =
    Map.of("barren", 0, "snow", 1, "crop", 2, "shrub", 3, "grass", 4, "trees", 5);

  public void processNe(SourceFeature sf, FeatureCollector features) {
    // Daylight landcover uses ESA WorldCover which only goes to a latitude of roughly 80 deg S.
    // Parts of Antarctica therefore get no landcover = glacier from Daylight.
    // To fix this, we add glaciated areas from Natural Earth in Antarctica.
    if (sf.getSourceLayer().equals("ne_10m_glaciated_areas")) {
      try {
        Point centroid = (Point) sf.centroid();
        // Web Mercator Y = 0.7 is roughly 60 deg South, i.e., Antarctica.
        if (centroid.getY() > 0.7) {
          features.polygon(LAYER_NAME)
            .setId(1)
            .setAttr("kind", "glacier")
            .setMaxZoom(7)
            .setMinPixelSize(1.0)
            .setPixelTolerance(Earth.PIXEL_TOLERANCE);
        }
      } catch (GeometryException e) {
        e.log("Error: " + e);
      }
    }
  }

  public void processLandcover(SourceFeature sf, FeatureCollector features) {
    String daylightClass = sf.getString("class");
    String kind = kindMapping.getOrDefault(daylightClass, daylightClass);

    // polygons are disjoint and non-overlapping, but order them in archive in consistent way
    Integer sortKey = sortKeyMapping.getOrDefault(daylightClass, 6);

    features.polygon(LAYER_NAME)
      .setId(1L + sortKey)
      .setAttr("kind", kind)
      .setZoomRange(0, 7)
      .setSortKey(sortKey)
      .setMinPixelSize(1.0)
      .setPixelTolerance(Earth.PIXEL_TOLERANCE);
  }

  public static final String LAYER_NAME = "landcover";

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return FeatureMerge.mergeNearbyPolygons(items, 1.0, 1.0, 0.5, Earth.BUFFER);
  }
}
