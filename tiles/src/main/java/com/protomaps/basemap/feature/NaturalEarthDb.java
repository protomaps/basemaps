package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.SourceFeature;
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

/**
 * An in-memory representation of a subset of Natural Earth tables, used for generalizing OpenStreetMap data.
 * <p>
 * Themes: admin_0_countries, admin_1_states_provinces, populated_places at the 10m map scale. Queried via multiple
 * convenience methods or matching to OpenStreetMap tags. * Query hardcoded information about countries. *
 * <p>
 * * Retrieve embedded hardcoded data about countries, addressable by names that should match OSM and NE. *
 * </p>
 * ** * Query hardcoded information about sub-national regions. *
 * <p>
 * * Embedded hand-curated data on sub-national regions of significant extents, to assist in labeling. Includes US
 * states, * AU states and territories, CA provinces and territories, and other significant regions globally *
 * </p>
 */
public class NaturalEarthDb {

  private static final Logger LOGGER = LoggerFactory.getLogger(NaturalEarthDb.class);

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("sqlite JDBC driver not found");
    }
  }

  private final Map<String, NePopulatedPlace> placesByWikidataId;
  private final Map<String, NeAdmin0Country> admin0sByIsoA2;
  private final Map<String, NeAdmin0Country> admin0sByWikidata;
  private final Map<String, NeAdmin1StateProvince> admin1sByIso31662;
  private final Map<String, NeAdmin1StateProvince> admin1sByWikidata;

  public record NeAdmin0Country(String name, String name_en, String iso_a2, String wikidataId, double minLabel,
    double maxLabel) {}
  public record NeAdmin1StateProvince(String name, String iso3166_2, String wikidataId, double minLabel,
    double maxLabel) {}
  public record NePopulatedPlace(String name, String wikidataId, double minZoom, int rankMax) {}

  private NaturalEarthDb(List<NeAdmin0Country> countries, List<NeAdmin1StateProvince> statesProvinces,
    List<NePopulatedPlace> populatedPlaces) {

    // resolve wikidata conflicts by choosing the larger rankMax
    this.placesByWikidataId = populatedPlaces.stream().filter(p -> p.wikidataId != null)
      .collect(Collectors.toMap(p -> p.wikidataId, p -> p, (p1, p2) -> p1.rankMax() > p2.rankMax() ? p1 : p2));

    this.admin0sByIsoA2 = countries.stream().filter(c -> !c.iso_a2.equals("-99"))
      .collect(Collectors.toMap(c -> c.iso_a2, c -> c));

    // resolve wikidata conflicts like Q27561 by choosing the smaller min_label
    this.admin0sByWikidata = countries.stream().filter(c -> c.wikidataId != null)
      .collect(Collectors.toMap(c -> c.wikidataId, c -> c, (c1, c2) -> c1.minLabel() < c2.minLabel() ? c1 : c2));

    this.admin1sByIso31662 = statesProvinces.stream().collect(Collectors.toMap(s -> s.iso3166_2, s -> s));

    this.admin1sByWikidata = statesProvinces.stream().filter(s -> s.wikidataId != null)
      .collect(Collectors.toMap(s -> s.wikidataId, s -> s));
  }

  public NePopulatedPlace getPopulatedPlaceByWikidata(String wikidataId) {
    return this.placesByWikidataId.get(wikidataId);
  }

  public NeAdmin1StateProvince getAdmin1ByIso(String isoCode) {
    return this.admin1sByIso31662.get(isoCode);
  }

  public NeAdmin1StateProvince getAdmin1ByWikidata(String wikidataId) {
    return this.admin1sByWikidata.get(wikidataId);
  }

  private static String coalesceTag(SourceFeature sf, String... tags) {
    for (String tag : tags) {
      String value = sf.getString(tag);
      if (value != null)
        return value;
    }
    return null;
  }

  /*
   * Convenience method for matching an OSM feature.
   * <p>
   * 51% of country nodes have country_code_iso3166_1_alpha_2
   * 50% of country nodes have ISO3166-1:alpha2
   * 28% of country nodes have ISO3166-1
   */
  public NeAdmin0Country getAdmin0ByIso(SourceFeature sf) {
    String isoCode = NaturalEarthDb.coalesceTag(sf, "ISO3166-1:alpha2", "country_code_iso3166_1_alpha_2", "ISO3166-1");
    return this.getAdmin0ByIso(isoCode);
  }

  public NeAdmin0Country getAdmin0ByIso(String isoCode) {
    return this.admin0sByIsoA2.get(isoCode);
  }

  // 100% of country nodes have wikidata
  public NeAdmin0Country getAdmin0ByWikidata(String wikidataId) {
    return this.admin0sByWikidata.get(wikidataId);
  }

  public static NaturalEarthDb fromSqlite(Path path, Path unzippedDir) {
    List<NeAdmin0Country> countries = new ArrayList<>();
    List<NeAdmin1StateProvince> statesProvinces = new ArrayList<>();
    List<NePopulatedPlace> populatedPlaces = new ArrayList<>();

    try {
      Path extracted;
      String uri;
      try (var zipFs = FileSystems.newFileSystem(path)) {
        var zipEntry = FileUtils.walkFileSystem(zipFs)
          .filter(Files::isRegularFile)
          .filter(entry -> FileUtils.hasExtension(entry, "sqlite"))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("No .sqlite file found inside " + path));
        extracted = unzippedDir.resolve(URLEncoder.encode(zipEntry.toString(), StandardCharsets.UTF_8));
        FileUtils.createParentDirectories(extracted);
        if (FileUtils.isNewer(path, extracted)) {
          LOGGER.info("unzipping {} to {}", path.toAbsolutePath(), extracted);
          Files.copy(Files.newInputStream(zipEntry), extracted, StandardCopyOption.REPLACE_EXISTING);
        }
      }
      uri = "jdbc:sqlite:" + extracted.toAbsolutePath();

      var conn = DriverManager.getConnection(uri);

      try (Statement statement = conn.createStatement()) {
        ResultSet rs =
          statement
            .executeQuery(
              "SELECT name, name_en, iso_a2, wikidataid, min_label, max_label FROM ne_10m_admin_0_countries_tlc;");
        while (rs.next()) {
          countries.add(new NeAdmin0Country(rs.getString("name"), rs.getString("name_en"), rs.getString("iso_a2"),
            rs.getString("wikidataid"),
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
