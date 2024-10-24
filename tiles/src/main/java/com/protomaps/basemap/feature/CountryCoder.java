package com.protomaps.basemap.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.onthegomap.planetiler.geo.GeoUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

  public static CountryCoder fromJsonString(String s) throws IOException {
    STRtree tree = new STRtree();

    ObjectMapper mapper = new ObjectMapper();
    JsonNode featureCollection = mapper.readTree(s);
    ArrayNode features = (ArrayNode) featureCollection.get("features");
    for (var feature : features) {
      JsonNode properties = feature.get("properties");

      if (!feature.has("geometry") || feature.get("geometry").isNull() || !properties.has("country")) {
        continue;
      }
      List<Polygon> polygons = new ArrayList<>();
      for (var polygon : feature.get("geometry").get("coordinates")) {
        ArrayNode outerRingNode = (ArrayNode) polygon.get(0);
        Coordinate[] outerRingCoordinates = parseCoordinates(outerRingNode);
        LinearRing outerRing = GeoUtils.JTS_FACTORY.createLinearRing(outerRingCoordinates);

        LinearRing[] innerRings = new LinearRing[polygon.size() - 1];
        for (int j = 1; j < polygon.size(); j++) {
          ArrayNode innerRingNode = (ArrayNode) polygon.get(j);
          Coordinate[] innerRingCoordinates = parseCoordinates(innerRingNode);
          innerRings[j - 1] = GeoUtils.JTS_FACTORY.createLinearRing(innerRingCoordinates);
        }
        polygons.add(GeoUtils.JTS_FACTORY.createPolygon(outerRing, innerRings));
      }

      MultiPolygon multiPolygon = GeoUtils.createMultiPolygon(polygons);
      multiPolygon.getEnvelopeInternal();
      tree.insert(multiPolygon.getEnvelopeInternal(),
        new Record(properties.get("country").asText(), properties.get("nameEn").asText(), multiPolygon));
    }
    return new CountryCoder(tree);
  }

  private static Coordinate[] parseCoordinates(ArrayNode coordinateArray) {
    Coordinate[] coordinates = new Coordinate[coordinateArray.size()];
    for (int i = 0; i < coordinateArray.size(); i++) {
      ArrayNode coordinate = (ArrayNode) coordinateArray.get(i);
      double x = coordinate.get(0).asDouble();
      double y = coordinate.get(1).asDouble();
      coordinates[i] = new Coordinate(x, y);
    }
    return coordinates;
  }

  public Optional<String> getCountryCode(Point point) {
    List<Record> results = tree.query(point.getEnvelopeInternal());
    if (results.isEmpty())
      return Optional.empty();
    return Optional.of(results.getFirst().country);
  }
}
