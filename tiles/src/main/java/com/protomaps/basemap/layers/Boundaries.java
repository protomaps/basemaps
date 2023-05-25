package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmReader;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.onthegomap.planetiler.util.Parse;
import com.protomaps.basemap.feature.FeatureId;
import java.util.List;
import java.util.OptionalInt;

public class Boundaries implements ForwardingProfile.OsmRelationPreprocessor, ForwardingProfile.FeatureProcessor,
  ForwardingProfile.FeaturePostProcessor {

  @Override
  public String name() {
    return "boundaries";
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine()) {
      if (sf.hasTag("natural", "coastline") || sf.hasTag("maritime", "yes")) {
        return;
      }
      List<OsmReader.RelationMember<AdminRecord>> recs = sf.relationInfo(AdminRecord.class);
      if (recs.size() > 0) {
        OptionalInt minAdminLevel = recs.stream().mapToInt(r -> r.relation().adminLevel).min();
        var line =
          features.line(this.name()).setId(FeatureId.create(sf)).setMinPixelSize(0).setAttr("pmap:min_admin_level",
            minAdminLevel.getAsInt());
        if (minAdminLevel.getAsInt() <= 2) {
          line.setMinZoom(0);
        } else if (minAdminLevel.getAsInt() <= 4) {
          line.setMinZoom(3);
        } else {
          line.setMinZoom(10);
        }

        // Disputed boundaries handling
        if( sf.hasTag("boundary", "disputed") ) {
          line.setAttr("disputed", 1)
                  .setAttr("claimed_by", sf.getString("claimed_by"))
                  .setAttr("recognized_by", sf.getString("recognized_by"))
                  .setAttr("disputed_by", sf.getString("disputed_by"))
                  // Normally we don't export names on boundaries... But has a hack
                  // for styling which disputed boundaries we include them here
                  .setAttr("disputed_name", sf.getString("name"));
        }
      }
    }
  }

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    if (relation.hasTag("type", "boundary") && relation.hasTag("boundary", "administrative")) {
      Integer adminLevel = Parse.parseIntOrNull(relation.getString("admin_level"));
      if (adminLevel == null || adminLevel > 8)
        return null;
      return List.of(new AdminRecord(relation.id(), adminLevel));
    }
    return null;
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    return FeatureMerge.mergeLineStrings(items,
      0.0,
      0.1,
      4
    );
  }

  private record AdminRecord(long id, int adminLevel) implements OsmRelationInfo {}
}
