package com.protomaps.basemap.feature;

import com.onthegomap.planetiler.reader.geojson.GeoJson;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.index.strtree.STRtree;

public class CountryCoder {

  public record Record(String country, String nameEn, MultiPolygon multiPolygon) {}

  private STRtree tree;

  public CountryCoder(STRtree tree) {
    this.tree = tree;
  }

  public static CountryCoder fromJarResource() throws IOException {
    InputStream inputStream = CountryCoder.class.getResourceAsStream("/borders.json");

    String jsonContent = new String(inputStream.readAllBytes());
    return fromJsonString(jsonContent);
  }

  public static CountryCoder fromJsonString(String s) {
    STRtree tree = new STRtree();

    var g = GeoJson.from(s);

    for (var feature : g) {

      if (feature.geometry().getNumGeometries() == 0) {
        continue;
      }

      MultiPolygon mp = (MultiPolygon) feature.geometry();

      var properties = feature.tags();

      String country;
      if (properties.containsKey("iso1A2")) {
        country = properties.get("iso1A2").toString();
      } else if (properties.containsKey("country")) {
        country = properties.get("country").toString();
      } else {
        continue;
      }

      tree.insert(mp.getEnvelopeInternal(),
        new Record(country, properties.get("nameEn").toString(), mp));
    }
    return new CountryCoder(tree);
  }

  public Optional<String> getCountryCode(Geometry geom) {
    List<Record> results = tree.query(geom.getEnvelopeInternal());
    if (results.isEmpty())
      return Optional.empty();
    var filtered = results.stream().filter(rec -> rec.multiPolygon.contains(geom)
    ).toList();
    if (filtered.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(filtered.getFirst().country);
  }
}
