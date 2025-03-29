package com.protomaps.basemap;

import com.onthegomap.planetiler.ForwardingProfile;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.util.Downloader;
import com.protomaps.basemap.feature.CountryCoder;
import com.protomaps.basemap.feature.NaturalEarthDb;
import com.protomaps.basemap.feature.QrankDb;
import com.protomaps.basemap.layers.Boundaries;
import com.protomaps.basemap.layers.Buildings;
import com.protomaps.basemap.layers.Earth;
import com.protomaps.basemap.layers.Landcover;
import com.protomaps.basemap.layers.Landuse;
import com.protomaps.basemap.layers.Places;
import com.protomaps.basemap.layers.Pois;
import com.protomaps.basemap.layers.Roads;
import com.protomaps.basemap.layers.Transit;
import com.protomaps.basemap.layers.Water;
import com.protomaps.basemap.postprocess.Clip;
import com.protomaps.basemap.text.FontRegistry;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Basemap extends ForwardingProfile {

  public Basemap(NaturalEarthDb naturalEarthDb, QrankDb qrankDb, CountryCoder countryCoder, Clip clip,
    String layer) {

    if (layer.isEmpty() || layer.equals(Boundaries.LAYER_NAME)) {
      var admin = new Boundaries();
      registerHandler(admin);
      registerSourceHandler("osm", admin::processOsm);
      registerSourceHandler("ne", admin::processNe);
    }

    if (layer.isEmpty() || layer.equals(Buildings.LAYER_NAME)) {
      var buildings = new Buildings();
      registerHandler(buildings);
      registerSourceHandler("osm", buildings::processOsm);
    }

    if (layer.isEmpty() || layer.equals(Landuse.LAYER_NAME)) {
      var landuse = new Landuse();
      registerHandler(landuse);
      registerSourceHandler("osm", landuse::processOsm);
    }

    if (layer.isEmpty() || layer.equals(Landcover.LAYER_NAME)) {
      var landcover = new Landcover();
      registerHandler(landcover);
      registerSourceHandler("landcover", landcover::processLandcover);
      registerSourceHandler("ne", landcover::processNe);
    }

    if (layer.isEmpty() || layer.equals(Places.LAYER_NAME)) {
      var place = new Places(naturalEarthDb);
      registerHandler(place);
      registerSourceHandler("osm", place::processOsm);
    }

    if (layer.isEmpty() || layer.equals(Pois.LAYER_NAME)) {
      var poi = new Pois(qrankDb);
      registerHandler(poi);
      registerSourceHandler("osm", poi::processOsm);
    }

    if (layer.isEmpty() || layer.equals(Roads.LAYER_NAME)) {
      var roads = new Roads(countryCoder);
      registerHandler(roads);
      registerSourceHandler("osm", roads::processOsm);
    }

    if (layer.isEmpty() || layer.equals(Transit.LAYER_NAME)) {
      var transit = new Transit();
      registerHandler(transit);
      registerSourceHandler("osm", transit::processOsm);
    }

    if (layer.isEmpty() || layer.equals(Water.LAYER_NAME)) {
      var water = new Water();
      registerHandler(water);
      registerSourceHandler("osm", water::processOsm);
      registerSourceHandler("osm_water", water::processPreparedOsm);
      registerSourceHandler("ne", water::processNe);
    }

    if (layer.isEmpty() || layer.equals(Earth.LAYER_NAME)) {
      var earth = new Earth();
      registerHandler(earth);

      registerSourceHandler("osm", earth::processOsm);
      registerSourceHandler("osm_land", earth::processPreparedOsm);
      registerSourceHandler("ne", earth::processNe);
    }

    if (clip != null) {
      registerHandler(clip);
    }
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
    return "4.9.0";
  }

  @Override
  public boolean isOverlay() {
    return false;
  }

  @Override
  public String attribution() {
    return """
      <a href="https://www.openstreetmap.org/copyright" target="_blank">&copy; OpenStreetMap</a>
      """.trim();
  }

  @Override
  public Map<String, String> extraArchiveMetadata() {
    Map<String, String> result = new HashMap<>();

    FontRegistry fontRegistry = FontRegistry.getInstance();
    List<String> scripts = fontRegistry.getScripts();

    for (String script : scripts) {
      result.put("pgf:" + script.toLowerCase() + ":name", fontRegistry.getName(script));
      result.put("pgf:" + script.toLowerCase() + ":version", fontRegistry.getVersion(script));
    }

    return result;
  }

  public static void main(String[] args) throws IOException {
    run(Arguments.fromArgsOrConfigFile(args));
  }

  static void run(Arguments args) throws IOException {
    args = args.orElse(Arguments.of("maxzoom", 15));

    Path dataDir = Path.of("data");
    Path sourcesDir = dataDir.resolve("sources");

    Path nePath = sourcesDir.resolve("natural_earth_vector.sqlite.zip");
    String neUrl = "https://naciscdn.org/naturalearth/packages/natural_earth_vector.sqlite.zip";

    var countryCoder = CountryCoder.fromJarResource();

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

    Path pgfEncodingZip = sourcesDir.resolve("pgf-encoding.zip");
    Downloader.create(planetiler.config()).add("ne", neUrl, nePath)
      .add("pgf-encoding", "https://wipfli.github.io/pgf-encoding/pgf-encoding.zip", pgfEncodingZip)
      .run();
    //      .add("qrank", "https://qrank.wmcloud.org/download/qrank.csv.gz", sourcesDir.resolve("qrank.csv.gz")).run();

    var tmpDir = nePath.resolveSibling(nePath.getFileName() + "-unzipped");
    var naturalEarthDb = NaturalEarthDb.fromSqlite(nePath, tmpDir);
    //    var qrankDb = QrankDb.fromCsv(sourcesDir.resolve("qrank.csv.gz"));
    var qrankDb = QrankDb.empty();

    FontRegistry fontRegistry = FontRegistry.getInstance();
    fontRegistry.setZipFilePath(pgfEncodingZip.toString());

    Clip clip = null;
    var clipArg = args.getString("clip", "File path to GeoJSON Polygon or MultiPolygon geometry to clip tileset.", "");
    if (!clipArg.isEmpty()) {
      clip =
        Clip.fromGeoJSONFile(args.getStats(), planetiler.config().minzoom(), planetiler.config().maxzoom(), true,
          Paths.get(clipArg));
    }

    List<String> availableLayers = List.of(
      Boundaries.LAYER_NAME,
      Buildings.LAYER_NAME,
      Landuse.LAYER_NAME,
      Landcover.LAYER_NAME,
      Places.LAYER_NAME,
      Pois.LAYER_NAME,
      Roads.LAYER_NAME,
      Transit.LAYER_NAME,
      Water.LAYER_NAME,
      Earth.LAYER_NAME
    );

    String layer = args.getString("layer",
      "Process only a single layer. Possible values are: " + String.join(", ", availableLayers), "");
    if (!(layer.isEmpty() || availableLayers.contains(layer))) {
      System.err.println("Error: --layer=" + layer + " is not a valid option. Possible values are: " +
        String.join(", ", availableLayers));
      System.exit(1);
    }

    fontRegistry.loadFontBundle("NotoSansDevanagari-Regular", "1", "Devanagari");

    planetiler.setProfile(new Basemap(naturalEarthDb, qrankDb, countryCoder, clip, layer))
      .setOutput(Path.of(area + ".pmtiles"))
      .run();
  }
}
