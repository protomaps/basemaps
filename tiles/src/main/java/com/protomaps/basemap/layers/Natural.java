package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;

public class Natural implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "natural";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() && (sf.hasTag("natural", "wood", "glacier", "scrub", "sand", "wetland", "bare_rock") ||
      sf.hasTag("landuse", "forest", "meadow") || sf.hasTag("leisure", "nature_reserve") ||
      sf.hasTag("boundary", "national_park", "protected_area"))) {
      var feat = features.polygon(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("natural", sf.getString("natural"))
        .setAttr("boundary", sf.getString("boundary"))
        .setAttr("landuse", sf.getString("landuse"))
        .setAttr("leisure", sf.getString("leisure"))
        .setZoomRange(5, 15)
        .setMinPixelSize(3.0);

      // NOTE: landuse labels for polygons are found in the pois layer
      //OsmNames.setOsmNames(feat, sf, 0);

      String kind = "other";
      if (sf.hasTag("natural")) {
        kind = sf.getString("natural");
      } else if (sf.hasTag("boundary")) {
        kind = sf.getString("boundary");
      } else if (sf.hasTag("landuse")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("leisure")) {
        kind = sf.getString("leisure");
      }
      feat.setAttr("pmap:kind", kind);

    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    //items = Area.addAreaTag(items);
    if (zoom == 15)
      return items;
    int minArea = 400 / (4096 * 4096) * (256 * 256);
    if (zoom == 6)
      minArea = 600 / (4096 * 4096) * (256 * 256);
    else if (zoom <= 5)
      minArea = 800 / (4096 * 4096) * (256 * 256);
    items = Area.filterArea(items, minArea);
    return items;
  }
}
