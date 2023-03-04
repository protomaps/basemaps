package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import java.util.List;

public class Landuse implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {

    @Override
    public String name() {
        return "landuse";
    }

    @Override
    public void processFeature(SourceFeature sf, FeatureCollector features) {
        if (sf.canBePolygon() && (sf.hasTag("aeroway","aerodrome","runway") ||
                sf.hasTag("area:aeroway","taxiway","runway") ||
                sf.hasTag("amenity","hospital","school","kindergarten","university","college") ||
                sf.hasTag("landuse", "recreation_ground", "industrial","brownfield","railway","cemetery","commercial","grass","orchard","farmland","farmyard","residential") ||
                sf.hasTag("leisure","park","garden","golf_course","residential","dog_park","playground","pitch") ||
                sf.hasTag("man_made","pier") ||
                sf.hasTag("place","neighbourhood") ||
                sf.hasTag("railway", "platform"))) {
            var poly = features.polygon(this.name())
                    .setAttr("name", sf.getString("name"))
                    .setAttr("landuse", sf.getString("landuse"))
                    .setAttr("leisure", sf.getString("leisure"))
                    .setAttr("aeroway", sf.getString("aeroway"))
                    .setAttr("area:aeroway", sf.getString("area:aeroway"))
                    .setAttr("amenity", sf.getString("amenity"))
                    .setAttr("highway", sf.getString("highway"))
                    .setAttr("man_made", sf.getString("man_made"))
                    .setAttr("place", sf.getString("place"))
                    .setAttr("railway", sf.getString("railway"))
                    .setAttr("sport", sf.getString("sport"))
                    .setZoomRange(5, 15);


            poly.setAttr("pmap:area","");

            String kind = "other";
            if (sf.hasTag("leisure")) {
                kind = "park";
            } else if (sf.hasTag("amenity")) {
                if (sf.hasTag("amenity","hospital")) {
                    kind = "hospital";
                } else {
                    kind = "school";
                }
            } else if (sf.hasTag("landuse")) {
                if (sf.hasTag("landuse", "orchard", "farmland", "farmyard")) {
                    kind = "farmland";
                } else if (sf.hasTag("landuse","industrial","brownfield")) {
                    kind = "industrial";
                } else if (sf.hasTag("landuse","cemetery")) {
                    kind = "cemetery";
                }
            } else if (sf.hasTag("highway")) {
                kind = "pedestrian";
            } else if (sf.hasTag("man_made", "bridge")) {
                kind = "pedestrian";
            } else if (sf.hasTag("aeroway", "aerodrome")) {
                kind = "aerodrome";
            }
            poly.setAttr("pmap:kind",kind);
        }
    }

    @Override
    public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
        return items;
    }
}