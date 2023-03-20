package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
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
        .setZoomRange(5, 15);

      OsmNames.setOsmNames(feat, sf, 0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
