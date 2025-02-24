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
    String renderOnly) {

    if (renderOnly.isEmpty() || renderOnly.equals("Boundaries")) {
      var admin = new Boundaries();
      registerHandler(admin);
      registerSourceHandler("osm", admin::processOsm);
      registerSourceHandler("ne", admin::processNe);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Buildings")) {
      var buildings = new Buildings();
      registerHandler(buildings);
      registerSourceHandler("osm", buildings::processOsm);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Landuse")) {
      var landuse = new Landuse();
      registerHandler(landuse);
      registerSourceHandler("osm", landuse::processOsm);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Landcover")) {
      var landcover = new Landcover();
      registerHandler(landcover);
      registerSourceHandler("landcover", landcover::processLandcover);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Places")) {
      var place = new Places(naturalEarthDb);
      registerHandler(place);
      registerSourceHandler("osm", place::processOsm);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Pois")) {
      var poi = new Pois(qrankDb);
      registerHandler(poi);
      registerSourceHandler("osm", poi::processOsm);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Roads")) {
      var roads = new Roads(countryCoder);
      registerHandler(roads);
      registerSourceHandler("osm", roads::processOsm);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Transit")) {
      var transit = new Transit();
      registerHandler(transit);
      registerSourceHandler("osm", transit::processOsm);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Water")) {
      var water = new Water();
      registerHandler(water);
      registerSourceHandler("osm", water::processOsm);
      registerSourceHandler("osm_water", water::processPreparedOsm);
      registerSourceHandler("ne", water::processNe);
    }

    if (renderOnly.isEmpty() || renderOnly.equals("Earth")) {
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
    return "4.3.1";
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

    List<String> renderOnlyOptions = List.of(
      "Boundaries",
      "Buildings",
      "Landuse",
      "Landcover",
      "Places",
      "Pois",
      "Roads",
      "Transit",
      "Water",
      "Earth"
    );
    String renderOnly = args.getString("render_only",
      "Render only a single layer. Possible values are: " + String.join(", ", renderOnlyOptions), "");
    if (!(renderOnly.isEmpty() || renderOnlyOptions.contains(renderOnly))) {
      System.err.println("Error: --render_only=\"" + renderOnly + "\" is not a valid option. Possible values are: " +
        String.join(", ", renderOnlyOptions));
      System.exit(1);
    }

    fontRegistry.loadFontBundle("NotoSansDevanagari-Regular", "1", "Devanagari");

    planetiler.setProfile(new Basemap(naturalEarthDb, qrankDb, countryCoder, clip, renderOnly))
      .setOutput(Path.of(area + ".pmtiles"))
      .run();
  }
}
