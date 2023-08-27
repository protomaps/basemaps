package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.util.FileUtils;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NaturalEarthDb {

  private static final Logger LOGGER = LoggerFactory.getLogger(NaturalEarthDb.class);

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("sqlite JDBC driver not found");
    }
  }

  private Map<String, NePopulatedPlace> placesByWikidataId;

  public record NeAdmin0Country(String name, String iso_a2, String wikidataId, double minLabel, double maxLabel) {};
  public record NeAdmin1StateProvince(String name, String iso3166_2, String wikidataId, double minLabel,
    double maxLabel) {};
  public record NePopulatedPlace(String name, String wikidataId, double minZoom, int rankMax) {};

  private NaturalEarthDb(List<NeAdmin0Country> countries, List<NeAdmin1StateProvince> statesProvinces,
    List<NePopulatedPlace> populatedPlaces) {
    this.placesByWikidataId = populatedPlaces.stream().filter(place -> place.wikidataId != null)
      .collect(Collectors.toMap(place -> place.wikidataId, place -> place, (s, a) -> s)); // TODO conflicts
  }

  public NePopulatedPlace getPopulatedPlaceByWikidataId(String wikidataId) {
    return this.placesByWikidataId.get(wikidataId);
  }

  public NeAdmin1StateProvince getAdmin1ByIsoCode(String isoCode) {
    return null;
  }

  public NeAdmin1StateProvince getAdmin1byWikidataId(String wikidataId) {
    return null;
  }

  public NeAdmin0Country getAdmin0ByIsoCode(String isoCode) {
    return null;
  }

  public NeAdmin0Country getAdmin0ByWikidataId(String isoCode) {
    return null;
  }

  public static NaturalEarthDb fromSqlite(Path path, Path unzippedDir) {
    boolean keepUnzipped = true;

    List<NeAdmin0Country> countries = new ArrayList<>();
    List<NeAdmin1StateProvince> statesProvinces = new ArrayList<>();
    List<NePopulatedPlace> populatedPlaces = new ArrayList<>();

    try {
      Path extracted;
      String uri = "jdbc:sqlite:" + path.toAbsolutePath();
      if (FileUtils.hasExtension(path, "zip")) {
        try (var zipFs = FileSystems.newFileSystem(path)) {
          var zipEntry = FileUtils.walkFileSystem(zipFs)
            .filter(Files::isRegularFile)
            .filter(entry -> FileUtils.hasExtension(entry, "sqlite"))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No .sqlite file found inside " + path));
          extracted = unzippedDir.resolve(URLEncoder.encode(zipEntry.toString(), StandardCharsets.UTF_8));
          FileUtils.createParentDirectories(extracted);
          if (!keepUnzipped || FileUtils.isNewer(path, extracted)) {
            LOGGER.info("unzipping {} to {}", path.toAbsolutePath(), extracted);
            Files.copy(Files.newInputStream(zipEntry), extracted, StandardCopyOption.REPLACE_EXISTING);
          }
          if (!keepUnzipped) {
            extracted.toFile().deleteOnExit();
          }
        }
        uri = "jdbc:sqlite:" + extracted.toAbsolutePath();
        var conn = DriverManager.getConnection(uri);

        try (Statement statement = conn.createStatement()) {
          ResultSet rs =
            statement
              .executeQuery("SELECT name, iso_a2, wikidataid, min_label, max_label FROM ne_10m_admin_0_countries;");
          while (rs.next()) {
            countries.add(new NeAdmin0Country(rs.getString("name"), rs.getString("iso_a2"), rs.getString("wikidataid"),
              rs.getDouble("min_label"), rs.getDouble("max_label")));
          }
        }

        try (Statement statement = conn.createStatement()) {
          ResultSet rs =
            statement.executeQuery(
              "SELECT name, iso_3166_2, wikidataid, min_label, max_label FROM ne_10m_admin_1_states_provinces;");
          while (rs.next()) {
            statesProvinces.add(new NeAdmin1StateProvince(rs.getString("name"), rs.getString("iso_3166_2"),
              rs.getString("wikidataid"), rs.getDouble("min_label"),
              rs.getDouble("max_label")));
          }
        }

        try (Statement statement = conn.createStatement()) {
          ResultSet rs =
            statement.executeQuery("SELECT name, wikidataid, min_zoom, rank_max FROM ne_10m_populated_places;");
          while (rs.next()) {
            populatedPlaces.add(new NePopulatedPlace(rs.getString("name"), rs.getString("wikidataid"),
              rs.getDouble("min_zoom"), rs.getInt("rank_max")));
          }
        }

        conn.close();
      }
    } catch (IOException | SQLException e) {
      throw new IllegalArgumentException(e);
    }
    return new NaturalEarthDb(countries, statesProvinces, populatedPlaces);
  }

  public static NaturalEarthDb fromList(List<NeAdmin0Country> countries, List<NeAdmin1StateProvince> statesProvinces,
    List<NePopulatedPlace> populatedPlaces) {
    return new NaturalEarthDb(countries, statesProvinces, populatedPlaces);
  }
}
