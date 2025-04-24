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
    public static final String PROPOSED = "proposed";
    public static final String ABANDONED = "abandoned";
    public static final String RAZED = "razed";
    public static final String DEMOLISHED = "demolished";
    public static final String REMOVED = "removed";
    public static final String CONSTRUCTION = "construction";
    public static final String ELEVATOR = "elevator";

    private HighwayValues() {}
  }

  public static final String SERVICE = "service";

  public static final class ServiceValues {
    public static final String YARD = "yard";
    public static final String SIDING = "siding";
    public static final String CROSSOVER = "crossover";
  }

  public static final String FOOTWAY = "footway";

  public static final class FootwayValues {
    public static final String SIDEWALK = "sidewalk";
    public static final String CROSSING = "crossing";

    private FootwayValues() {}
  }

  public static final String RAILWAY = "railway";

  public static final class RailwayValues {
    public static final String SERVICE = "service";
    public static final String FUNICULAR = "funicular";
    public static final String LIGHT_RAIL = "light_rail";
    public static final String MINIATURE = "miniature";
    public static final String MONORAIL = "monorail";
    public static final String NARROW_GAUGE = "narrow_gauge";
    public static final String PRESERVED = "preserved";
    public static final String SUBWAY = "subway";
    public static final String TRAM = "tram";
    public static final String DISUSED = "disused";
    public static final String ABANDONED = "abandoned";
    public static final String RAZED = "razed";
    public static final String DEMOLISHED = "demolished";
    public static final String REMOVED = "removed";
    public static final String CONSTRUCTION = "construction";
    public static final String PLATFORM = "platform";
    public static final String PROPOSED = "proposed";

    private RailwayValues() {}
  }

  public static final String AERIALWAY = "aerialway";

  public static final class AerialwayValues {
    public static final String CABLE_CAR = "cable_car";

    private AerialwayValues() {}
  }

  public static final String MAN_MADE = "man_made";

  public static final class ManMadeValues {
    public static final String PIER = "pier";

    private ManMadeValues() {}
  }

  public static final String ROUTE = "route";

  public static final class RouteValues {
    public static final String FERRY = "ferry";

    private RouteValues() {}
  }

  public static final String AEROWAY = "aeroway";

  public static final class AerowayValues {
    public static final String TAXIWAY = "taxiway";
    public static final String RUNWAY = "runway";

    private AerowayValues() {}
  }

  public static final String BRIDGE = "bridge";

  public static final class BridgeValues {
    public static final String NO = "no";

    private BridgeValues() {}
  }

  public static final String TUNNEL = "tunnel";

  public static final class TunnelValues {
    public static final String NO = "no";

    private TunnelValues() {}
  }

  public static final String BUILDING = "building";

  private OsmTags() {}
}
