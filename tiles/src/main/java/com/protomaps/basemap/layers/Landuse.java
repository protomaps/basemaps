package com.protomaps.basemap.layers;

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

public class Landuse implements ForwardingProfile.FeatureProcessor, ForwardingProfile.FeaturePostProcessor {
  public static final String NAME = "landuse";

  public static void processFeature(SourceFeature sf, FeatureCollector features, String layerName,
    boolean ghostFeatures) {
    if (sf.canBePolygon() && (sf.hasTag("aeroway", "aerodrome", "runway") ||
      sf.hasTag("area:aeroway", "taxiway", "runway") ||
      sf.hasTag("amenity", "hospital", "school", "kindergarten", "university", "college") ||
      sf.hasTag("boundary", "national_park", "protected_area") ||
      sf.hasTag("landuse", "recreation_ground", "industrial", "brownfield", "railway", "cemetery", "commercial",
        "grass", "orchard", "farmland", "farmyard", "residential") ||
      sf.hasTag("leisure", "park", "garden", "golf_course", "dog_park", "playground", "pitch", "nature_reserve") ||
      sf.hasTag("man_made", "pier") ||
      // TODO: (nvkelso 20230622) This use of the place tag here is dubious
      sf.hasTag("place", "neighbourhood") ||
      sf.hasTag("railway", "platform") ||
      (sf.hasTag("area", "yes") &&
        (sf.hasTag("highway", "pedestrian", "footway") || sf.hasTag("man_made", "bridge")))))
    {
      var poly = features.polygon(layerName)
        .setId(FeatureId.create(sf))
        .setAttr("aeroway", sf.getString("aeroway"))
        .setAttr("amenity", sf.getString("amenity"))
        .setAttr("area:aeroway", sf.getString("area:aeroway"))
        .setAttr("boundary", sf.getString("boundary"))
        .setAttr("highway", sf.getString("highway"))
        .setAttr("landuse", sf.getString("landuse"))
        .setAttr("leisure", sf.getString("leisure"))
        .setAttr("man_made", sf.getString("man_made"))
        .setAttr("place", sf.getString("place"))
        .setAttr("railway", sf.getString("railway"))
        .setAttr("sport", sf.getString("sport"))
        // NOTE: (nvkelso 20230622) Consider zoom 5 instead...
        //       But to match Protomaps v2 we do earlier
        .setZoomRange(2, 15)
        .setMinPixelSize(3.0);

      // NOTE: (nvkelso 20230622) landuse labels for polygons are found in the pois layer
      //OsmNames.setOsmNames(poly, sf, 0);

      String kind = "other";
      if (sf.hasTag("aeroway", "aerodrome")) {
        kind = sf.getString("aeroway");
      } else if (sf.hasTag("amenity",  "university","college", "hospital", "library", "post_office", "school", "townhall")) {
        kind = sf.getString("amenity");
      } else if (sf.hasTag("amenity", "cafe")) {
        kind = sf.getString("amenity");
      } else if (sf.hasTag("highway")) {
        kind = "pedestrian";
      } else if (sf.hasTag("landuse", "cemetery")) {
        kind = sf.getString("landuse");
      } else if (sf.hasTag("landuse", "orchard", "farmland", "farmyard")) {
        kind = "farmland";
      } else if (sf.hasTag("landuse", "industrial", "brownfield")) {
        kind = "industrial";
      } else if (sf.hasTag("leisure", "golf_course", "marina", "park", "stadium")) {
        kind = sf.getString("leisure");
      } else if (sf.hasTag("man_made", "bridge")) {
        kind = "pedestrian";
      } else if (sf.hasTag("shop", "grocery", "supermarket")) {
        kind = sf.getString("shop");
      } else if (sf.hasTag("tourism", "attraction", "camp_site", "hotel")) {
        kind = sf.getString("tourism");
      } else {
        // Avoid problem of too many "other" kinds
        // All these will default to min_zoom of 15
        // If a more specific min_zoom is needed (or sanitize kind values)
        // then add new logic in section above
        if (sf.hasTag("amenity")) {
          kind = sf.getString("amenity");
        } else if (sf.hasTag("craft")) {
          kind = sf.getString("craft");
        } else if (sf.hasTag("aeroway")) {
          kind = sf.getString("aeroway");
        } else if (sf.hasTag("historic")) {
          kind = sf.getString("historic");
        } else if (sf.hasTag("landuse")) {
          kind = sf.getString("landuse");
        } else if (sf.hasTag("leisure")) {
          kind = sf.getString("leisure");
        } else if (sf.hasTag("railway")) {
          kind = sf.getString("railway");
        } else if (sf.hasTag("shop")) {
          kind = sf.getString("shop");
        } else if (sf.hasTag("tourism")) {
          kind = sf.getString("tourism");
        // Boundary is most generic, so place last else we loose out
        // on nature_reserve detail versus all the protected_area
        } else if (sf.hasTag("boundary")) {
          kind = sf.getString("boundary");
        }
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

    return FeatureMerge.mergeNearbyPolygons(items, 3.125, 3.125, 0.5, 0.5);
  }
}
