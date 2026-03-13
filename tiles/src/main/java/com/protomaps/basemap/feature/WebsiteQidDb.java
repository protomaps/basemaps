package com.protomaps.basemap.feature;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * An in-memory mapping from website domain to Wikidata Q-ID, used to enrich Overture POIs (which lack native wikidata
 * fields) for QRank-based zoom assignment.
 * <p>
 * Parses a gzipped CSV with columns {@code domain,qid} into a HashMap for efficient lookup.
 **/
public final class WebsiteQidDb {

  private final Map<String, Long> db;

  public WebsiteQidDb(Map<String, Long> db) {
    this.db = db;
  }

  /**
   * Extracts the root domain from a URL and looks up the corresponding Wikidata Q-ID.
   *
   * @param url a full URL such as "https://www.iflyoak.com/flights"
   * @return a Wikidata Q-ID string like "Q1165584", or null if not found
   */
  public String getQid(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }
    String domain = url;
    // Strip protocol
    if (domain.startsWith("https://")) {
      domain = domain.substring("https://".length());
    } else if (domain.startsWith("http://")) {
      domain = domain.substring("http://".length());
    }
    // Strip www. prefix
    if (domain.startsWith("www.")) {
      domain = domain.substring("www.".length());
    }
    // Take portion up to first /
    int slash = domain.indexOf('/');
    if (slash >= 0) {
      domain = domain.substring(0, slash);
    }
    Long id = db.get(domain);
    return id != null ? "Q" + id : null;
  }

  public static WebsiteQidDb fromCsv(Path csvPath) throws IOException {
    GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(csvPath.toFile()));
    try (BufferedReader br = new BufferedReader(new InputStreamReader(gzip))) {
      String content;
      Map<String, Long> db = new HashMap<>();
      String header = br.readLine(); // header
      assert (header.equals("domain,qid"));
      while ((content = br.readLine()) != null) {
        int lastComma = content.lastIndexOf(',');
        if (lastComma < 0) {
          continue;
        }
        String domain = content.substring(0, lastComma);
        String qid = content.substring(lastComma + 1);
        if (qid.startsWith("Q")) {
          qid = qid.substring(1);
        }
        try {
          db.put(domain, Long.parseLong(qid));
        } catch (NumberFormatException e) {
          // skip malformed rows
        }
      }
      return new WebsiteQidDb(db);
    }
  }
}
