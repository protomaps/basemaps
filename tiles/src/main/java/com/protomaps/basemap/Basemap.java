package com.protomaps.basemap;

import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.config.Arguments;
import com.protomaps.basemap.layers.Boundaries;
import com.protomaps.basemap.layers.Buildings;
import com.protomaps.basemap.layers.Earth;
import com.protomaps.basemap.layers.Landuse;
import com.protomaps.basemap.layers.Natural;
import com.protomaps.basemap.layers.PhysicalLine;
import com.protomaps.basemap.layers.PhysicalPoint;
import com.protomaps.basemap.layers.Places;
import com.protomaps.basemap.layers.Pois;
import com.protomaps.basemap.layers.Roads;
import com.protomaps.basemap.layers.Transit;
import com.protomaps.basemap.layers.Water;
import java.nio.file.Path;
import org.locationtech.jts.geom.Envelope;


public class Basemap extends ForwardingProfile {

  public Basemap(Envelope earthWaterBounds) {

    var admin = new Boundaries();
    registerHandler(admin);
    registerSourceHandler("osm", admin);
    registerSourceHandler("ne", admin::processNe);

    var buildings = new Buildings();
    registerHandler(buildings);
    registerSourceHandler("osm", buildings);

    var landuse = new Landuse();
    registerHandler(landuse);
    registerSourceHandler("osm", landuse);

    var natural = new Natural();
    registerHandler(natural);
    registerSourceHandler("osm", natural);

    var physical_line = new PhysicalLine();
    registerHandler(physical_line);
    registerSourceHandler("osm", physical_line);

    var physical_point = new PhysicalPoint();
    registerHandler(physical_point);
    registerSourceHandler("osm", physical_point);
    registerSourceHandler("ne", physical_point::processNe);

    var place = new Places();
    registerHandler(place);
    registerSourceHandler("osm", place);
    registerSourceHandler("ne", place::processNe);

    var poi = new Pois();
    registerHandler(poi);
    registerSourceHandler("osm", poi);

    var roads = new Roads();
    registerHandler(roads);
    registerSourceHandler("osm", roads);

    var transit = new Transit();
    registerHandler(transit);
    registerSourceHandler("osm", transit);

    var water = new Water(earthWaterBounds);
    registerHandler(water);
    registerSourceHandler("osm", water);
    registerSourceHandler("osm_water", water::processPreparedOsm);
    registerSourceHandler("ne", water::processNe);

    var earth = new Earth(earthWaterBounds);
    registerHandler(earth);
    registerSourceHandler("osm_land", earth::processPreparedOsm);
    registerSourceHandler("ne", earth::processNe);

  }

  @Override
  public String name() {
    return "Basemap";
  }

  @Override
  public String description() {
    return "Basemap layers derived from OpenStreetMap and Natural Earth";
  }

  @Override
  public boolean isOverlay() {
    return false;
  }

  @Override
  public String attribution() {
    return """
      <a href="https://www.openstreetmap.org/copyright" target="_blank">&copy; OpenStreetMap contributors</a>
      """.trim();
  }

  public static void main(String[] args) throws Exception {
    run(Arguments.fromArgsOrConfigFile(args));
  }

  static void run(Arguments args) throws Exception {
    args = args.orElse(Arguments.of("maxzoom", 15));

    Path dataDir = Path.of("data");
    Path sourcesDir = dataDir.resolve("sources");

    // Limit the output of prepared OSM earth/water to a bounding box.
    // Used in combination with the `--bounds` argument to generate a complete planet based on NE at low zooms,
    // and a complete high-zoom tileset without having to tile earth/water outside the bbox.
    Envelope earthWaterBounds = args.bounds("osm-earth-water-bounds", "spatial bbox of osm earth+water");

    String area = args.getString("area", "geofabrik area to download", "monaco");

    var planetiler = Planetiler.create(args)
      .setProfile(new Basemap(earthWaterBounds))
      .addOsmSource("osm", Path.of("data", "sources", area + ".osm.pbf"), "geofabrik:" + area)
      .addNaturalEarthSource("ne", sourcesDir.resolve("natural_earth_vector.sqlite.zip"),
        "https://naciscdn.org/naturalearth/packages/natural_earth_vector.sqlite.zip")
      .setOutput(Path.of(area + ".pmtiles"));
    planetiler.addShapefileSource("osm_water", sourcesDir.resolve("water-polygons-split-3857.zip"),
      "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip")
      .addShapefileSource("osm_land", sourcesDir.resolve("land-polygons-split-3857.zip"),
        "https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip")
      .run();
  }
}
