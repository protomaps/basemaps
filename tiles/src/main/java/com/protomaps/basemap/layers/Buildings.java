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
    if (sf.canBePolygon() && (
            ( sf.hasTag("building") && !sf.hasTag("building", "no")) ||
            ( sf.hasTag("building:part") && !sf.hasTag("building:part", "no"))))
    {
      Double height = parseDoubleOrNull(sf.getString("height"));
      Integer min_zoom = 10;

      if (height == null) {
        Double levels = parseDoubleOrNull(sf.getString("building:levels"));
        if (levels != null) {
          height = Math.max(levels, 1) * 3 + 2;
        }
      }

      // Limit building:part features to later zooms
      // TODO: (nvkelso 20230621) this should be based on area and volume, too
      if( sf.hasTag("building:part") ) {
        min_zoom = 14;
      }

      var feature = features.polygon(this.name())
        .setId(FeatureId.create(sf))
        .setAttrWithMinzoom("building:part", sf.getString("building:part"), 13)
        .setAttrWithMinzoom("layer", sf.getString("layer"), 13)
        .setAttrWithMinzoom("height", height, 13)
        .setZoomRange(min_zoom, 15);

      // Names should mostly just be for POIs
      // Sometimes building name and address are useful items, but only at zoom 17+
      //OsmNames.setOsmNames(feature, sf, 13);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    if (zoom == 15) {
      return items;
    }
    items = Area.filterArea(items, 0);

    // DEBUG
    //items = Area.addAreaTag(items);

    if (zoom >= 15)
      return items;

    // TODO: (nvkelso 20230621) quantize height by zoom to facilitate better feature merging
    // at zoom 12 round height to nearest 20 meters
    // at zoom 13 round height to nearest 10 meters
    // at zoom 14 round height to nearest 5 meters

    return FeatureMerge.mergeNearbyPolygons(items, 3.125, 3.125, 0.5, 0.5);
  }
}
