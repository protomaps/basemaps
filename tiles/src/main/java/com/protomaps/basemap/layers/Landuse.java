package com.protomaps.basemap.layers;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.protomaps.basemap.feature.FeatureId;
import com.protomaps.basemap.names.OsmNames;
import com.protomaps.basemap.postprocess.Area;
import java.util.List;

public class Landuse implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {
  public static final String NAME = "landuse";

  public static void processFeature(SourceFeature sf, FeatureCollector features, String layerName,
    boolean ghostFeatures) {
    if (sf.canBePolygon() && (sf.hasTag("aeroway", "aerodrome", "runway") ||
      sf.hasTag("area:aeroway", "taxiway", "runway") ||
      sf.hasTag("amenity", "hospital", "school", "kindergarten", "university", "college") ||
      sf.hasTag("landuse", "recreation_ground", "industrial", "brownfield", "railway", "cemetery", "commercial",
        "grass", "orchard", "farmland", "farmyard", "residential") ||
      sf.hasTag("leisure", "park", "garden", "golf_course", "dog_park", "playground", "pitch") ||
      sf.hasTag("man_made", "pier") ||
      sf.hasTag("place", "neighbourhood") ||
      sf.hasTag("railway", "platform") ||
      (sf.hasTag("area", "yes") &&
        (sf.hasTag("highway", "pedestrian", "footway") || sf.hasTag("man_made", "bridge")))))
    {
      var poly = features.polygon(layerName)
        .setId(FeatureId.create(sf))
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
        .setZoomRange(5, 15)
        .setMinPixelSize(3.0);

      // NOTE: landuse labels for polygons are found in the pois layer
      //OsmNames.setOsmNames(poly, sf, 0);

      // What does this do?
      if (ghostFeatures) {
        poly.setAttr("isGhostFeature", true);
      }

      //poly.setAttr("pmap:area", "");

      String kind = "other";
      if (sf.hasTag("leisure")) {
        kind = "park";
      } else if (sf.hasTag("amenity")) {
        if (sf.hasTag("amenity", "hospital")) {
          kind = "hospital";
        } else {
          kind = "school";
        }
      } else if (sf.hasTag("landuse")) {
        if (sf.hasTag("landuse", "orchard", "farmland", "farmyard")) {
          kind = "farmland";
        } else if (sf.hasTag("landuse", "industrial", "brownfield")) {
          kind = "industrial";
        } else if (sf.hasTag("landuse", "cemetery")) {
          kind = "cemetery";
        }
      } else if (sf.hasTag("highway")) {
        kind = "pedestrian";
      } else if (sf.hasTag("man_made", "bridge")) {
        kind = "pedestrian";
      } else if (sf.hasTag("aeroway", "aerodrome")) {
        kind = "aerodrome";
      }

      if( sf.hasTag("boundary", "national_park") &&
              !(sf.hasTag("operator", "United States Forest Service", "US Forest Service", "U.S. Forest Service", "USDA Forest Service", "United States Department of Agriculture", "US National Forest Service", "United State Forest Service", "U.S. National Forest Service") ||
                      sf.hasTag("protection_title", "Conservation Area", "Conservation Park", "Environmental use", "Forest Reserve", "National Forest", "National Wildlife Refuge", "Nature Refuge", "Nature Reserve", "Protected Site", "Provincial Park", "Public Access Land", "Regional Reserve", "Resources Reserve", "State Forest", "State Game Land", "State Park", "Watershed Recreation Unit", "Wild Forest", "Wilderness Area", "Wilderness Study Area", "Wildlife Management", "Wildlife Management Area", "Wildlife Sanctuary")
              ) &&
              ( sf.hasTag("protect_class", "2", "3") ||
                      sf.hasTag("operator", "United States National Park Service", "National Park Service", "US National Park Service", "U.S. National Park Service", "US National Park service") ||
                      sf.hasTag("operator:en", "Parks Canada") ||
                      sf.hasTag("designation", "national_park") ||
                      sf.hasTag("protection_title", "National Park")
              )
      ) {
        kind = "national_park";
      }

      poly.setAttr("pmap:kind", kind);
    }
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public void processFeature(SourceFeature sf, FeatureCollector features) {
    processFeature(sf, features, NAME, false);
  }

  @Override
  public List<VectorTile.Feature> postProcess(int zoom, List<VectorTile.Feature> items) throws GeometryException {
    //items = Area.addAreaTag(items);
    if (zoom == 15)
      return items;
    int minArea = 400 / (4096 * 4096) * (256 * 256);
    if (zoom == 6)
      minArea = 600 / (4096 * 4096) * (256 * 256);
    else if (zoom <= 5)
      minArea = 800 / (4096 * 4096) * (256 * 256);
    items = Area.filterArea(items, minArea);
    return items;
  }
}
