package com.protomaps.basemap.layers;

import static com.onthegomap.planetiler.util.Parse.parseDoubleOrNull;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;

public class Buildings implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "buildings";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBePolygon() && (sf.hasTag("building"))) {
      Double height = parseDoubleOrNull(sf.getString("height"));
      var feature = features.polygon(this.name())
        .setId(FeatureId.create(sf))
        .setAttrWithMinzoom("building:part", sf.getString("building:part"), 13)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 13)
        .setAttrWithMinzoom("height", height, 13)
        .setZoomRange(10, 15);

      OsmNames.setOsmNames(feature, sf, 13);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    if (zoom == 15) {
      return items;
    }
    items = Area.filterArea(items, 0);

    if (zoom >= 14)
      return items;
    return FeatureMerge.mergeNearbyPolygons(items, 3.125, 3.125, 0.5, 0.5);
  }
}
