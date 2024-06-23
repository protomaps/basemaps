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
import com.protomaps.basemap.layers.Landcover;
import com.protomaps.basemap.layers.Landuse;
import com.protomaps.basemap.layers.Natural;
import com.protomaps.basemap.layers.PhysicalLine;
import com.protomaps.basemap.layers.PhysicalPoint;
import com.protomaps.basemap.layers.Places;
import com.protomaps.basemap.layers.Pois;
import com.protomaps.basemap.layers.Roads;
import com.protomaps.basemap.layers.Transit;
import com.protomaps.basemap.layers.Water;
import com.protomaps.basemap.text.FontRegistry;

import java.nio.file.Path;


public class Basemap extends ForwardingProfile {

  public Basemap(NaturalEarthDb naturalEarthDb, QrankDb qrankDb, FontRegistry fontRegistry) {

    var admin = new Boundaries();
    registerHandler(admin);
    registerSourceHandler("osm", admin::processOsm);
    registerSourceHandler("ne", admin::processNe);

    var buildings = new Buildings();
    registerHandler(buildings);
    registerSourceHandler("osm", buildings::processOsm);

    var landuse = new Landuse();
    registerHandler(landuse);
    registerSourceHandler("osm", landuse::processOsm);

    var landcover = new Landcover();
    registerHandler(landcover);
    registerSourceHandler("landcover", landcover::processLandcover);

    var natural = new Natural();
    registerHandler(natural);
    registerSourceHandler("osm", natural::processOsm);

    var physicalLine = new PhysicalLine(fontRegistry);
    registerHandler(physicalLine);
    registerSourceHandler("osm", physicalLine::processOsm);

    var physicalPoint = new PhysicalPoint(fontRegistry);
    registerHandler(physicalPoint);
    registerSourceHandler("osm", physicalPoint::processOsm);
    registerSourceHandler("ne", physicalPoint::processNe);

    var place = new Places(naturalEarthDb, fontRegistry);
    registerHandler(place);
    registerSourceHandler("osm", place::processOsm);
    registerSourceHandler("ne", place::processNe);

    var poi = new Pois(qrankDb, fontRegistry);
    registerHandler(poi);
    registerSourceHandler("osm", poi::processOsm);

    var roads = new Roads(fontRegistry);
    registerHandler(roads);
    registerSourceHandler("osm", roads::processOsm);

    var transit = new Transit(fontRegistry);
    registerHandler(transit);
    registerSourceHandler("osm", transit::processOsm);

    var water = new Water();
    registerHandler(water);
    registerSourceHandler("osm", water::processOsm);
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
    return "3.6.0";
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

  public static void main(String[] args) {
    run(Arguments.fromArgsOrConfigFile(args));
  }

  static void run(Arguments args) {
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
        "https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip")
      .addGeoPackageSource("landcover", sourcesDir.resolve("daylight-landcover.gpkg"),
        "https://r2-public.protomaps.com/datasets/daylight-landcover.gpkg");

    Downloader.create(planetiler.config()).add("ne", neUrl, nePath)
      // .add("pgf-encoding", "https://api.github.com/repos/wipfli/pgf-encoding/zipball/e9c03fb", sourcesDir.resolve("main.zip"))
      .add("qrank", "https://qrank.wmcloud.org/download/qrank.csv.gz", sourcesDir.resolve("qrank.csv.gz")).run();
    //      .add("qrank", "https://qrank.wmcloud.org/download/qrank.csv.gz", sourcesDir.resolve("qrank.csv.gz")).run();

    var tmpDir = nePath.resolveSibling(nePath.getFileName() + "-unzipped");
    var naturalEarthDb = NaturalEarthDb.fromSqlite(nePath, tmpDir);
    //    var qrankDb = QrankDb.fromCsv(sourcesDir.resolve("qrank.csv.gz"));
    var qrankDb = QrankDb.empty();

    String pgfEncodingRepoHash = "e9c03fb";
    FontRegistry fontRegistry = new FontRegistry(pgfEncodingRepoHash);
    fontRegistry.loadFontBundle("NotoSansDevanagari-Regular", "1", "Devanagari");

    planetiler.setProfile(new Basemap(naturalEarthDb, qrankDb, fontRegistry)).setOutput(Path.of(area + ".pmtiles"))
      .run();
  }
}
