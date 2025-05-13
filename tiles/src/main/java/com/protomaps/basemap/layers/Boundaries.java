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
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

@SuppressWarnings("java:S1192")
public class Boundaries implements ForwardingProfile.OsmRelationPreprocessor,
  ForwardingProfile.LayerPostProcessor {

  public static final String LAYER_NAME = "boundaries";

  @Override
  public String name() {
    return LAYER_NAME;
  }

  public void processNe(SourceFeature sf, FeatureCollector features) {
    String sourceLayer = sf.getSourceLayer();
    String kind = "";
    int adminLevel = 2;
    boolean disputed = false;
    int themeMinZoom = 0;
    int themeMaxZoom = 0;

    if (sourceLayer.equals("ne_10m_admin_0_boundary_lines_land") ||
      sourceLayer.equals("ne_10m_admin_0_boundary_lines_map_units") ||
      sourceLayer.equals("ne_10m_admin_0_boundary_lines_disputed_areas") ||
      sourceLayer.equals("ne_10m_admin_1_states_provinces_lines")) {
      themeMinZoom = 4;
      themeMaxZoom = 5;
      kind = "tz_boundary";
    }

    if (!kind.isEmpty()) {
      switch (sf.getString("featurecla")) {
        case "Disputed (please verify)" -> {
          kind = "country";
          disputed = true;
        }
        case "Indefinite (please verify)" -> {
          kind = "country";
          disputed = true;
        }
        case "Indeterminant frontier" -> {
          kind = "country";
          disputed = true;
        }
        case "International boundary (verify)" -> kind = "country";
        case "Lease limit" -> {
          kind = "lease_limit";
          adminLevel = 3;
        }
        case "Line of control (please verify)" -> {
          kind = "country";
          disputed = true;
        }
        case "Overlay limit" -> {
          kind = "overlay_limit";
          adminLevel = 3;
        }
        case "Unrecognized" -> kind = "unrecognized_country";
        case "Map unit boundary" -> {
          kind = "map_unit";
          adminLevel = 3;
        }
        case "Breakaway" -> {
          kind = "unrecognized_country";
          adminLevel = 3;
        }
        case "Claim boundary" -> {
          kind = "unrecognized_country";
          adminLevel = 3;
        }
        case "Elusive frontier" -> {
          kind = "unrecognized_country";
          adminLevel = 3;
        }
        case "Reference line" -> {
          kind = "unrecognized_country";
          adminLevel = 3;
        }
        case "Admin-1 region boundary" -> {
          kind = "macroregion";
          adminLevel = 3;
        }
        case "Admin-1 boundary" -> {
          kind = "region";
          adminLevel = 4;
        }
        case "Admin-1 statistical boundary" -> {
          kind = "region";
          adminLevel = 4;
        }
        case "Admin-1 statistical meta bounds" -> {
          kind = "region";
          adminLevel = 4;
        }
        case "1st Order Admin Lines" -> {
          kind = "region";
          adminLevel = 4;
        }
        case "Unrecognized Admin-1 region boundary" -> {
          kind = "unrecognized_macroregion";
          adminLevel = 4;
        }
        case "Unrecognized Admin-1 boundary" -> {
          kind = "unrecognized_region";
          adminLevel = 4;
        }
        case "Unrecognized Admin-1 statistical boundary" -> {
          kind = "unrecognized_region";
          adminLevel = 4;
        }
        case "Unrecognized Admin-1 statistical meta bounds" -> {
          kind = "unrecognized_region";
          adminLevel = 4;
        }
        default -> kind = "";
      }
    }

    if (sf.canBeLine() && sf.getString("min_zoom") != null && (!kind.isEmpty() && !kind.equals("tz_boundary"))) {
      var minZoom = Double.parseDouble(sf.getString("min_zoom")) - 1.0;
      int sortRank = 289 - (disputed ? 1 : 0);
      features.line(this.name())
        .setAttr("kind", kind)
        .setAttr("kind_detail", adminLevel)
        .setAttr("sort_rank", sortRank)
        .setSortKey(sortRank)
        .setAttr("disputed", disputed ? true : null)
        .setAttr("brk_a3", sf.getString("brk_a3"))
        .setZoomRange(
          sf.getString("min_zoom") == null ? themeMinZoom : (int) minZoom,
          themeMaxZoom)
        .setMinPixelSize(0)
        .setBufferPixels(8);
    }
  }

  public void processOsm(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine()) {
      // Beware coastlines and coastal waters (eg with admin borders in large estuaries)
      // like mouth of Columbia River between Oregon and Washington in USA
      if (sf.hasTag("natural", "coastline") || sf.hasTag("maritime", "yes")) {
        return;
      }
      List<OsmReader.RelationMember<AdminRecord>> recs = sf.relationInfo(AdminRecord.class);
      if (!recs.isEmpty()) {
        OptionalInt minAdminLevel = recs.stream().mapToInt(r -> r.relation().adminLevel).min();
        OptionalInt disputedRelation = recs.stream().mapToInt(r -> r.relation().disputed).max();

        var kind = "";

        int themeMinZoom = 0;

        // Core Tilezen schema properties
        switch (minAdminLevel.getAsInt()) {
          case 2 -> {
            kind = "country";
            // While country boundary lines should show up very early
            // Natural Earth is used for low zooms (for compilation and tile size reasons)
            themeMinZoom = 6;
          }
          // used in Colombia, Brazil, Kenya (historical)
          case 3 -> {
            kind = "region";
            themeMinZoom = 6;
          }
          case 4 -> {
            kind = "region";
            // While region boundary lines should show up early-zooms
            // Natural Earth is used for low zooms (for compilation and tile size reasons)
            themeMinZoom = 6;
          }
          // used in Colombia, Brazil
          case 5 -> {
            kind = "county";
            themeMinZoom = 8;
          }
          case 6 -> {
            kind = "county";
            themeMinZoom = 8;
          }
          case 8 -> {
            kind = "locality";
            themeMinZoom = 10;
          }
          default -> {
            kind = "locality";
            themeMinZoom = 10;
          }
        }

        if (!kind.isEmpty()) {
          boolean disputed = disputedRelation.getAsInt() == 1 || sf.hasTag("boundary", "disputed", "claim") ||
            sf.hasTag("disputed", "yes") ||
            sf.hasTag("disputed_by") || sf.hasTag("claimed_by");
          int sortRank = 289 - (disputed ? 1 : 0);
          features.line(this.name())
            .setId(FeatureId.create(sf))
            .setAttr("kind", kind)
            .setAttr("kind_detail", minAdminLevel.getAsInt())
            .setAttr("sort_rank", sortRank)
            .setSortKey(sortRank)
            .setAttr("disputed", disputed ? true : null)
            .setMinPixelSize(0)
            .setMinZoom(themeMinZoom);
        }
      }
    }
  }

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    if (relation.hasTag("type", "boundary") &&
      relation.hasTag("boundary", "administrative", "disputed", "claim")) {
      Integer adminLevel = Parse.parseIntOrNull(relation.getString("admin_level"));
      Integer disputed = relation.hasTag("boundary", "disputed") || relation.hasTag("disputed", "yes") ||
        relation.hasTag("disputed_by") || relation.hasTag("claimed_by") ? 1 : 0;

      if (adminLevel == null || adminLevel > 8)
        return new ArrayList<>();
      return List.of(new AdminRecord(relation.id(), adminLevel, disputed));
    }
    return new ArrayList<>();
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) {
    var tolerance = 0.4;
    if (zoom < 6) {
      tolerance = 0.2;
    }
    return FeatureMerge.mergeLineStrings(items,
      0.0,
      tolerance,
      4,
      true
    );
  }

  private record AdminRecord(long id, int adminLevel, int disputed) implements OsmRelationInfo {}
}
