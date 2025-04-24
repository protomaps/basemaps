package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

@SuppressWarnings("java:S1192")
public class Earth implements ForwardingProfile.LayerPostProcessor {

  public static final String LAYER_NAME = "earth";

  public static final double BUFFER = 0.0625;
  public static final double MIN_AREA = 1.0;

  public static final double PIXEL_TOLERANCE = 0.2;

  @Override
  public String name() {
    return LAYER_NAME;
  }

  @SuppressWarnings("java:S1172")
  public void processPreparedOsm(SourceFeature sf, FeatureCollector features) {
    features.polygon(LAYER_NAME)
      .setId(1)
      .setAttr("kind", "earth")
      .setPixelTolerance(PIXEL_TOLERANCE)
      .setMinZoom(6)
      .setBufferPixels(8);
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    String sourceLayer = sf.getSourceLayer();
    if (!(sourceLayer.equals("ne_50m_land") || sourceLayer.equals("ne_10m_land"))) {
      return;
    }
    int minZoom = sourceLayer.equals("ne_50m_land") ? 0 : 5;
    int maxZoom = sourceLayer.equals("ne_50m_land") ? 4 : 5;

    features.polygon(LAYER_NAME)
      .setId(1)
      .setAttr("kind", "earth")
      .setZoomRange(minZoom, maxZoom)
      .setPixelTolerance(PIXEL_TOLERANCE)
      .setMinPixelSize(1.0)
      .setBufferPixels(8);
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && !sf.canBePolygon() && sf.hasTag("natural", "cliff")) {
      int minZoom = 12;
      var feat = features.line(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("min_zoom", minZoom + 1)
        .setAttr("kind", "cliff")
        .setPixelTolerance(PIXEL_TOLERANCE)
        .setMinZoom(minZoom);

      OsmNames.setOsmNames(feat, sf, 0);
    }

    if (sf.canBePolygon() && sf.hasTag("place", "island")) {
      var feat = features.innermostPoint(LAYER_NAME)
        .setId(FeatureId.create(sf))
        .setAttr("kind", "island")
        .setMinPixelSize(20)
        .setMinZoom(6);
      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    return FeatureMerge.mergeNearbyPolygons(items, MIN_AREA, MIN_AREA, 0.5, BUFFER);
  }
}
