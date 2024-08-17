package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;

public class Natural implements ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "natural";
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() &&
      (sf.hasTag("natural", "wood", "glacier", "grass", "scrub", "sand", "wetland", "bare_rock") ||
        sf.hasTag("landuse", "forest", "meadow", "grass"))) {
      String kind = "other";
      if (sf.hasTag("natural")) {
        kind = sf.getString("natural");
      } else if (sf.hasTag("landuse")) {
        kind = sf.getString("landuse");
      }

      features.polygon(this.name())
        //.setId(FeatureId.create(sf))
        // Core Tilezen schema properties
        .setAttr("pmap:kind", kind)
        // NOTE: (nvkelso 20230622) Consider zoom 5 instead...
        //       But to match Protomaps v2 we do earlier
        .setZoomRange(2, 15)
        .setMinPixelSize(2.0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    if (zoom == 15)
      return items;

    int minArea = 400 / (4096 * 4096) * (256 * 256);
    if (zoom == 6)
      minArea = 600 / (4096 * 4096) * (256 * 256);
    else if (zoom <= 5)
      minArea = 800 / (4096 * 4096) * (256 * 256);
    items = Area.filterArea(items, minArea);

    return FeatureMerge.mergeNearbyPolygons(items, 3.125, 3.125, 0.5, 0.5);
  }
}
