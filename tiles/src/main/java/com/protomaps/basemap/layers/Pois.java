package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
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
                sf.hasTag("railway","station"))) {
            features.point(this.name())
                    .setAttr("name", sf.getString("name"))
                    .setAttr("amenity", sf.getString("amenity"))
                    .setAttr("shop", sf.getString("shop"))
                    .setAttr("railway", sf.getString("railway"))
                    .setAttr("cuisine", sf.getString("cuisine"))
                    .setAttr("religion", sf.getString("religion"))
                    .setAttr("tourism", sf.getString("tourism"))
                    .setZoomRange(13, 15);
        }
    }

    @Override
    public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
        return items;
    }
}