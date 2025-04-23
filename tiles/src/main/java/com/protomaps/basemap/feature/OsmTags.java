package com.protomaps.basemap.feature;

public final class OsmTags {
  public static final String HIGHWAY = "highway";
  public static final class HighwayValues {
    public static final String MOTORWAY = "motorway";
    public static final String MOTORWAY_LINK = "motorway_link";
    public static final String TRUNK = "trunk";
    public static final String TRUNK_LINK = "trunk_link";
    public static final String PRIMARY = "primary";
    public static final String PRIMARY_LINK = "primary_link";
    public static final String SECONDARY = "secondary";
    public static final String SECONDARY_LINK = "secondary_link";
    public static final String TERTIARY = "tertiary";
    public static final String TERTIARY_LINK = "tertiary_link";
    public static final String SERVICE = "service";
    public static final String RESIDENTIAL = "residential";
    public static final String UNCLASSIFIED = "unclassified";
    public static final String ROAD = "road";
    public static final String RACEWAY = "raceway";
    public static final String PEDESTRIAN = "pedestrian";
    public static final String TRACK = "track";
    public static final String CORRIDOR = "corridor";
    public static final String PATH = "path";
    public static final String CYCLEWAY = "cycleway";
    public static final String BRIDLEWAY = "bridleway";
    public static final String FOOTWAY = "footway";
    public static final String STEPS = "steps";
    private HighwayValues() {}
  }

  public static final String SERVICE = "service";

  public static final String FOOTWAY = "footway";
  public static final class FootwayValues {
    public static final String SIDEWALK = "sidewalk";
    public static final String CROSSING = "crossing";
    private FootwayValues() {}
  }

  private OsmTags() {}
}
