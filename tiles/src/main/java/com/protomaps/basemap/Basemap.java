package com.protomaps.basemap;

import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.util.Downloader;
import com.protomaps.basemap.feature.NaturalEarthDb;
import com.protomaps.basemap.feature.QrankDb;
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


public class Basemap extends ForwardingProfile {

  public Basemap(NaturalEarthDb naturalEarthDb, QrankDb qrankDb) {

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

    var physicalLine = new PhysicalLine();
    registerHandler(physicalLine);
    registerSourceHandler("osm", physicalLine);

    var physicalPoint = new PhysicalPoint();
    registerHandler(physicalPoint);
    registerSourceHandler("osm", physicalPoint);
    registerSourceHandler("ne", physicalPoint::processNe);

    var place = new Places(naturalEarthDb);
    registerHandler(place);
    registerSourceHandler("osm", place);
    registerSourceHandler("ne", place::processNe);

    var poi = new Pois(qrankDb);
    registerHandler(poi);
    registerSourceHandler("osm", poi);

    var roads = new Roads();
    registerHandler(roads);
    registerSourceHandler("osm", roads);

    var transit = new Transit();
    registerHandler(transit);
    registerSourceHandler("osm", transit);

    var water = new Water();
    registerHandler(water);
    registerSourceHandler("osm", water);
    registerSourceHandler("osm_water", water::processPreparedOsm);
    registerSourceHandler("ne", water::processNe);

    var earth = new Earth();
    registerHandler(earth);
    registerSourceHandler("osm_land", earth::processPreparedOsm);
    registerSourceHandler("ne", earth::processNe);
  }

  @Override
  public String name() {
    return "Protomaps Basemap";
  }

  @Override
  public String description() {
    return "Basemap layers derived from OpenStreetMap and Natural Earth";
  }

  @Override
  public String version() {
    return "3.0.0-pre2";
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

    Path nePath = sourcesDir.resolve("natural_earth_vector.sqlite.zip");
    String neUrl = "https://naciscdn.org/naturalearth/packages/natural_earth_vector.sqlite.zip";

    String area = args.getString("area", "geofabrik area to download", "monaco");

    var planetiler = Planetiler.create(args)
      .addNaturalEarthSource("ne", nePath, neUrl)
      .addOsmSource("osm", Path.of("data", "sources", area + ".osm.pbf"), "geofabrik:" + area)
      .addShapefileSource("osm_water", sourcesDir.resolve("water-polygons-split-3857.zip"),
        "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip")
      .addShapefileSource("osm_land", sourcesDir.resolve("land-polygons-split-3857.zip"),
        "https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip");

    Downloader.create(planetiler.config()).add("ne", neUrl, nePath)
      .add("qrank", "https://qrank.wmcloud.org/download/qrank.csv.gz", sourcesDir.resolve("qrank.csv.gz")).run();

    var tmpDir = nePath.resolveSibling(nePath.getFileName() + "-unzipped");
    var naturalEarthDb = NaturalEarthDb.fromSqlite(nePath, tmpDir);
    var qrankDb = QrankDb.fromCsv(sourcesDir.resolve("qrank.csv.gz"));

    planetiler.setProfile(new Basemap(naturalEarthDb, qrankDb)).setOutput(Path.of(area + ".pmtiles"))
      .run();
  }
}
