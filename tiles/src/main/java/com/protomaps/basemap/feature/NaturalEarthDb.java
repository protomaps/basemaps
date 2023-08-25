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

  public record NePopulatedPlace(String name, String wikidataId, String minZoom, String populationRank) {};

  private NaturalEarthDb(List<NePopulatedPlace> places) {
    this.placesByWikidataId = places.stream().filter(place -> place.wikidataId != null)
      .collect(Collectors.toMap(place -> place.wikidataId, place -> place, (s, a) -> s)); // TODO conflicts
  }

  public NePopulatedPlace getByWikidataId(String wikidataId) {
    return this.placesByWikidataId.get(wikidataId);
  }

  public static NaturalEarthDb fromSqlite(Path path, Path unzippedDir) {
    boolean keepUnzipped = true;
    List<NePopulatedPlace> results = new ArrayList<>();
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
            statement.executeQuery("SELECT wikidataid, min_zoom, name, rank_max FROM ne_10m_populated_places;");
          while (rs.next()) {
            results.add(new NePopulatedPlace(rs.getString("name"), rs.getString("wikidataid"), rs.getString("min_zoom"),
              rs.getString("rank_max")));
          }
        }

        conn.close();
      }
    } catch (IOException | SQLException e) {
      throw new IllegalArgumentException(e);
    }
    return new NaturalEarthDb(results);
  }

  public static NaturalEarthDb fromList(List<NePopulatedPlace> places) {
    return new NaturalEarthDb(places);
  }
}
