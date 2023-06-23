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
      // Beware coastlines and coastal waters (eg with admin borders in large estuaries)
      // like mouth of Columbia River between Oregon and Washington in USA
      if (sf.hasTag("natural", "coastline") || sf.hasTag("maritime", "yes")) {
        return;
      }
      List<OsmReader.RelationMember<AdminRecord>> recs = sf.relationInfo(AdminRecord.class);
      if (recs.size() > 0) {
        OptionalInt minAdminLevel = recs.stream().mapToInt(r -> r.relation().adminLevel).min();
        OptionalInt disputed = recs.stream().mapToInt(r -> r.relation().disputed).max();
        var line =
          features.line(this.name()).setId(FeatureId.create(sf)).setMinPixelSize(0).setAttr("pmap:min_admin_level",
            minAdminLevel.getAsInt());
        if (minAdminLevel.getAsInt() <= 2) {
          line.setMinZoom(0);
        } else if (minAdminLevel.getAsInt() <= 4) {
          line.setMinZoom(3);
        } else if (minAdminLevel.getAsInt() <= 6) {
          line.setMinZoom(8);
        } else {
          line.setMinZoom(10);
        }

        switch (minAdminLevel.getAsInt()) {
          case 2 -> {
            line.setAttr("pmap:kind", "country");
          }
          case 4 -> {
            line.setAttr("pmap:kind", "region");
          }
          case 6 -> {
            line.setAttr("pmap:kind", "county");
          }
          case 8 -> {
            line.setAttr("pmap:kind", "locality");
          }
        }

        if (disputed.getAsInt() == 1) {
          line.setAttr("disputed", 1);
        }
      }
    }
  }

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    if (relation.hasTag("type", "boundary") &&
      (relation.hasTag("boundary", "administrative") || relation.hasTag("boundary", "disputed"))) {
      Integer adminLevel = Parse.parseIntOrNull(relation.getString("admin_level"));
      Integer disputed = relation.hasTag("boundary", "disputed") ? 1 : 0;

      if (adminLevel == null || adminLevel > 8)
        return null;
      return List.of(new AdminRecord(relation.id(), adminLevel, disputed));
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

  private record AdminRecord(long id, int adminLevel, int disputed) implements OsmRelationInfo {}
}
