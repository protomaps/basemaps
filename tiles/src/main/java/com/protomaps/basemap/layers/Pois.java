package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import java.util.List;

public class Pois implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "pois";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.isPoint() && (sf.hasTag("amenity") ||
      sf.hasTag("shop") ||
      sf.hasTag("tourism") ||
      sf.hasTag("leisure") ||
      sf.hasTag("aeroway", "aerodrome") ||
      sf.hasTag("railway", "station"))) {
      var feature = features.point(this.name())
        .setId(FeatureId.create(sf))
        .setAttr("amenity", sf.getString("amenity"))
        .setAttr("shop", sf.getString("shop"))
        .setAttr("railway", sf.getString("railway"))
        .setAttr("cuisine", sf.getString("cuisine"))
        .setAttr("religion", sf.getString("religion"))
        .setAttr("tourism", sf.getString("tourism"))
        .setAttr("iata", sf.getString("iata"))
        .setZoomRange(13, 15);

      OsmNames.setOsmNames(feature, sf, 0);
    }

    String kind = "other";
    if (sf.hasTag("aeroway", "aerodrome" )) {
      kind = sf.getString("aeroway");
    } else if (sf.hasTag("amenity", "cafe", "college", "hospital",  "library",  "post_office",  "school",  "townhall")) {
      kind = sf.getString("amenity");
    } else if (sf.hasTag("landuse", "cemetery" )) {
      kind = sf.getString("landuse");
    } else if (sf.hasTag("leisure", "golf_course", "marina", "park", "stadium" )) {
      kind = sf.getString("leisure");
    } else if (sf.hasTag("leisure" )) {
      // This is dubious but existing behavior
      kind = "park";
    } else if (sf.hasTag("shop", "grocery", "supermarket" )) {
      kind = sf.getString("shop");
    } else if (sf.hasTag("tourism", "attraction", "camp_site",  "hotel" )) {
      kind = sf.getString("tourism");
    }

    feature.setAttr("pmap:kind", kind);
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return items;
  }
}
