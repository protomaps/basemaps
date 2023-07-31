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

  public void processNe(SourceFeature sf, FeatureCollector features) {
    var sourceLayer = sf.getSourceLayer();
    var kind = "";
    var kindDetail = "";
    var adminLevel = 2;
    var disputed = false;
    var themeMinZoom = 0;
    var themeMaxZoom = 0;

    if (sourceLayer.equals("ne_50m_admin_0_boundary_lines_land") ||
      sourceLayer.equals("ne_50m_admin_0_boundary_lines_disputed_areas") ||
      sourceLayer.equals("ne_50m_admin_0_boundary_lines_maritime_indicator_chn") ||
      sourceLayer.equals("ne_50m_admin_1_states_provinces_lines")) {
      themeMinZoom = 1;
      themeMaxZoom = 3;
      kind = "tz_boundary";
    } else if (sourceLayer.equals("ne_10m_admin_0_boundary_lines_land") ||
      sourceLayer.equals("ne_10m_admin_0_boundary_lines_map_units") ||
      sourceLayer.equals("ne_10m_admin_0_boundary_lines_disputed_areas") ||
      sourceLayer.equals("ne_10m_admin_0_boundary_lines_maritime_indicator_chn") ||
      sourceLayer.equals("ne_10m_admin_1_states_provinces_lines")) {
      themeMinZoom = 4;
      themeMaxZoom = 5;
      kind = "tz_boundary";
    }

    // TODO (nvkelso 2023-03-26)
    //      Compiler is fussy about booleans and strings, beware
    if (!kind.isEmpty()) {
      switch (sf.getString("featurecla")) {
        case "Disputed (please verify)" -> {
          kind = "country";
          kindDetail = "disputed";
          disputed = true;
        }
        case "Indefinite (please verify)" -> {
          kind = "country";
          kindDetail = "indefinite";
          disputed = true;
        }
        case "Indeterminant frontier" -> {
          kind = "country";
          kindDetail = "indeterminant";
          disputed = true;
        }
        case "International boundary (verify)" -> kind = "country";
        case "Lease limit" -> {
          kind = "lease_limit";
          adminLevel = 3;
        }
        case "Line of control (please verify)" -> {
          kind = "country";
          kindDetail = "line_of_control";
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
          kindDetail = "disputed_breakaway";
          adminLevel = 3;
        }
        case "Claim boundary" -> {
          kind = "unrecognized_country";
          kindDetail = "disputed_claim";
          adminLevel = 3;
        }
        case "Elusive frontier" -> {
          kind = "unrecognized_country";
          kindDetail = "disputed_elusive";
          adminLevel = 3;
        }
        case "Reference line" -> {
          kind = "unrecognized_country";
          kindDetail = "disputed_reference_line";
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

    if (sf.canBeLine() && sf.hasTag("min_zoom") && (!kind.isEmpty() && !kind.equals("tz_boundary"))) {
      var minZoom = Double.parseDouble(sf.getString("min_zoom")) - 1.0;

      features.line(this.name())
        // Don't label lines to reduce file size (and they aren't shown in styles anyhow)
        //.setAttr("name", sf.getString("name"))
        // Preview v4 schema (disabled)
        //.setAttr("pmap:min_zoom", sf.getLong("min_zoom"))
        .setAttr("pmap:min_admin_level", adminLevel)
        .setZoomRange(
          sf.getString("min_zoom") == null ? themeMinZoom : (int) minZoom,
          themeMaxZoom)
        .setAttr("pmap:ne_id", sf.getString("ne_id"))
        .setAttr("pmap:brk_a3", sf.getString("brk_a3"))
        .setAttr("pmap:kind", kind)
        .setAttr("pmap:kind_detail", kindDetail)
        .setAttr("disputed", disputed)
        .setBufferPixels(8);
    }
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    if (sf.canBeLine() && sf.hasTag("admin_level")) {
      // Beware coastlines and coastal waters (eg with admin borders in large estuaries)
      // like mouth of Columbia River between Oregon and Washington in USA
      if (sf.hasTag("natural", "coastline") || sf.hasTag("maritime", "yes")) {
        return;
      }
      List<OsmReader.RelationMember<AdminRecord>> recs = sf.relationInfo(AdminRecord.class);
      if (!recs.isEmpty()) {
        OptionalInt minAdminLevel = recs.stream().mapToInt(r -> r.relation().adminLevel).min();
        OptionalInt disputed = recs.stream().mapToInt(r -> r.relation().disputed).max();

        var kind = "";
        var kindDetail = "";

        var minZoom = 0;
        var themeMinZoom = 6;

        // Core Tilezen schema properties
        switch (minAdminLevel.getAsInt()) {
          case 2 -> {
            kind = "country";
            kindDetail = "2";
            // While country boundary lines should show up very early
            minZoom = 0;
            // Natural Earth is used for low zooms (for compilation and tile size reasons)
            themeMinZoom = 6;
          }
          case 4 -> {
            kind = "region";
            kindDetail = "4";
            // While region boundary lines should show up early-zooms
            minZoom = 6;
            // Natural Earth is used for low zooms (for compilation and tile size reasons)
            themeMinZoom = 6;
          }
          case 6 -> {
            kind = "county";
            kindDetail = "6";
            minZoom = 8;
            themeMinZoom = 8;
          }
          case 8 -> {
            kind = "locality";
            kindDetail = "8";
            minZoom = 10;
            themeMinZoom = 10;
          }
        }

        if (!kind.isEmpty() && !kindDetail.isEmpty()) {
          var line = features.line(this.name())
            .setId(FeatureId.create(sf))
            .setMinPixelSize(0)
            .setAttr("pmap:min_admin_level", minAdminLevel.getAsInt())
            .setAttr("pmap:kind", kind)
            .setAttr("pmap:kind_detail", kindDetail)
            // Preview v4 schema (disabled)
            //.setAttr("pmap:min_zoom", min_zoom)
            .setMinZoom(themeMinZoom);

          // Core Tilezen schema properties
          if (disputed.getAsInt() == 1) {
            line.setAttr("disputed", 1);
          }
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
