import {
  DataDrivenPropertyValueSpecification,
  ExpressionSpecification,
  LayerSpecification,
} from "@maplibre/maplibre-gl-style-spec";
import { get_country_name, get_multiline_name } from "./language";
import { Theme } from "./themes";

export function nolabels_layers(
  source: string,
  t: Theme,
): LayerSpecification[] {
  return [
    {
      id: "background",
      type: "background",
      paint: {
        "background-color": t.background,
      },
    },
    {
      id: "earth",
      type: "fill",
      filter: ["==", ["geometry-type"], "Polygon"],
      source: source,
      "source-layer": "earth",
      paint: {
        "fill-color": t.earth,
      },
    },
    {
      id: "landuse_park",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: [
        "in",
        "kind",
        "national_park",
        "park",
        "cemetery",
        "protected_area",
        "nature_reserve",
        "forest",
        "golf_course",
      ],
      paint: {
        "fill-color": [
          "interpolate",
          ["linear"],
          ["zoom"],
          0,
          t.park_a,
          12,
          t.park_b,
        ],
      },
    },
    {
      id: "landuse_urban_green",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "allotments", "village_green", "playground"],
      paint: {
        "fill-color": t.park_b,
        "fill-opacity": 0.7,
      },
    },
    {
      id: "landuse_hospital",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["==", "kind", "hospital"],
      paint: {
        "fill-color": t.hospital,
      },
    },
    {
      id: "landuse_industrial",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["==", "kind", "industrial"],
      paint: {
        "fill-color": t.industrial,
      },
    },
    {
      id: "landuse_school",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "school", "university", "college"],
      paint: {
        "fill-color": t.school,
      },
    },
    {
      id: "landuse_beach",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "beach"],
      paint: {
        "fill-color": t.beach,
      },
    },
    {
      id: "landuse_zoo",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "zoo"],
      paint: {
        "fill-color": t.zoo,
      },
    },
    {
      id: "landuse_military",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "military", "naval_base", "airfield"],
      paint: {
        "fill-color": t.zoo,
      },
    },
    {
      id: "landuse_wood",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "wood", "nature_reserve", "forest"],
      paint: {
        "fill-color": [
          "interpolate",
          ["linear"],
          ["zoom"],
          0,
          t.wood_a,
          12,
          t.wood_b,
        ],
      },
    },
    {
      id: "landuse_scrub",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "scrub", "grassland", "grass"],
      paint: {
        "fill-color": [
          "interpolate",
          ["linear"],
          ["zoom"],
          0,
          t.scrub_a,
          12,
          t.scrub_b,
        ],
      },
    },
    {
      id: "landuse_glacier",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["==", "kind", "glacier"],
      paint: {
        "fill-color": t.glacier,
      },
    },
    {
      id: "landuse_sand",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["==", "kind", "sand"],
      paint: {
        "fill-color": t.sand,
      },
    },
    {
      id: "landuse_aerodrome",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["in", "kind", "aerodrome"],
      paint: {
        "fill-color": t.aerodrome,
      },
    },
    {
      id: "roads_runway",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["==", "kind_detail", "runway"],
      paint: {
        "line-color": t.runway,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          10,
          0,
          12,
          4,
          18,
          30,
        ],
      },
    },
    {
      id: "roads_taxiway",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 13,
      filter: ["==", "kind_detail", "taxiway"],
      paint: {
        "line-color": t.runway,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          15,
          6,
        ],
      },
    },
    {
      id: "landuse_runway",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["any", ["in", "kind", "runway", "taxiway"]],
      paint: {
        "fill-color": t.runway,
      },
    },
    {
      id: "water",
      type: "fill",
      filter: ["==", ["geometry-type"], "Polygon"],
      source: source,
      "source-layer": "water",
      paint: {
        "fill-color": t.water,
      },
    },
    {
      id: "water_stream",
      type: "line",
      source: source,
      "source-layer": "water",
      minzoom: 14,
      filter: ["in", "kind", "stream"],
      paint: {
        "line-color": t.water,
        "line-width": 0.5,
      },
    },
    {
      id: "water_river",
      type: "line",
      source: source,
      "source-layer": "water",
      minzoom: 9,
      filter: ["in", "kind", "river"],
      paint: {
        "line-color": t.water,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          9,
          0,
          9.5,
          1.0,
          18,
          12,
        ],
      },
    },
    {
      id: "landuse_pedestrian",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["==", "kind", "pedestrian"],
      paint: {
        "fill-color": t.pedestrian,
      },
    },
    {
      id: "landuse_pier",
      type: "fill",
      source: source,
      "source-layer": "landuse",
      filter: ["==", "kind", "pier"],
      paint: {
        "fill-color": t.pier,
      },
    },
    {
      id: "roads_tunnels_other_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["in", "kind", "other", "path"]],
      paint: {
        "line-color": t.tunnel_other_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          14,
          0,
          20,
          7,
        ],
      },
    },
    {
      id: "roads_tunnels_minor_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["==", "kind", "minor_road"]],
      paint: {
        "line-color": t.tunnel_minor_casing,
        "line-dasharray": [3, 2],
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          0,
          12.5,
          0.5,
          15,
          2,
          18,
          11,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          12,
          0,
          12.5,
          1,
        ],
      },
    },
    {
      id: "roads_tunnels_link_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["has", "is_link"]],
      paint: {
        "line-color": t.tunnel_link_casing,
        "line-dasharray": [3, 2],
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          18,
          11,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          12,
          0,
          12.5,
          1,
        ],
      },
    },
    {
      id: "roads_tunnels_major_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "major_road"],
      ],
      paint: {
        "line-color": t.tunnel_major_casing,
        "line-dasharray": [3, 2],
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          0.5,
          18,
          13,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          9,
          0,
          9.5,
          1,
        ],
      },
    },
    {
      id: "roads_tunnels_highway_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "highway"],
        ["!has", "is_link"],
      ],
      paint: {
        "line-color": t.tunnel_highway_casing,
        "line-dasharray": [6, 0.5],
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          3.5,
          0.5,
          18,
          15,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          1,
          20,
          15,
        ],
      },
    },
    {
      id: "roads_tunnels_other",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["in", "kind", "other", "path"]],
      paint: {
        "line-color": t.tunnel_other,
        "line-dasharray": [4.5, 0.5],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          14,
          0,
          20,
          7,
        ],
      },
    },
    {
      id: "roads_tunnels_minor",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["==", "kind", "minor_road"]],
      paint: {
        "line-color": t.tunnel_minor,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          0,
          12.5,
          0.5,
          15,
          2,
          18,
          11,
        ],
      },
    },
    {
      id: "roads_tunnels_link",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["has", "is_link"]],
      paint: {
        "line-color": t.tunnel_minor,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          18,
          11,
        ],
      },
    },
    {
      id: "roads_tunnels_major",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["all", ["has", "is_tunnel"], ["==", "kind", "major_road"]],
      paint: {
        "line-color": t.tunnel_major,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          6,
          0,
          12,
          1.6,
          15,
          3,
          18,
          13,
        ],
      },
    },
    {
      id: "roads_tunnels_highway",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["has", "is_tunnel"],
        ["==", ["get", "kind"], "highway"],
        ["!", ["has", "is_link"]],
      ],
      paint: {
        "line-color": t.tunnel_highway,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          6,
          1.1,
          12,
          1.6,
          15,
          5,
          18,
          15,
        ],
      },
    },
    {
      id: "buildings",
      type: "fill",
      source: source,
      "source-layer": "buildings",
      paint: {
        "fill-color": t.buildings,
        "fill-opacity": 0.5,
      },
    },
    {
      id: "roads_pier",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["==", "kind_detail", "pier"],
      paint: {
        "line-color": t.pier,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          12,
          0,
          12.5,
          0.5,
          20,
          16,
        ],
      },
    },
    {
      id: "roads_minor_service_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 13,
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "minor_road"],
        ["==", "kind_detail", "service"],
      ],
      paint: {
        "line-color": t.minor_service_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          18,
          8,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          0.8,
        ],
      },
    },
    {
      id: "roads_minor_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "minor_road"],
        ["!=", "kind_detail", "service"],
      ],
      paint: {
        "line-color": t.minor_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          0,
          12.5,
          0.5,
          15,
          2,
          18,
          11,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          12,
          0,
          12.5,
          1,
        ],
      },
    },
    {
      id: "roads_link_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 13,
      filter: ["has", "is_link"],
      paint: {
        "line-color": t.minor_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          18,
          11,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1.5,
        ],
      },
    },
    {
      id: "roads_major_casing_late",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "major_road"],
      ],
      paint: {
        "line-color": t.major_casing_late,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          6,
          0,
          12,
          1.6,
          15,
          3,
          18,
          13,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          9,
          0,
          9.5,
          1,
        ],
      },
    },
    {
      id: "roads_highway_casing_late",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "highway"],
        ["!has", "is_link"],
      ],
      paint: {
        "line-color": t.highway_casing_late,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          3.5,
          0.5,
          18,
          15,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          1,
          20,
          15,
        ],
      },
    },
    {
      id: "roads_other",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["in", "kind", "other", "path"],
        ["!=", "kind_detail", "pier"],
      ],
      paint: {
        "line-color": t.other,
        "line-dasharray": [3, 1],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          14,
          0,
          20,
          7,
        ],
      },
    },
    {
      id: "roads_link",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["has", "is_link"],
      paint: {
        "line-color": t.link,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          18,
          11,
        ],
      },
    },
    {
      id: "roads_minor_service",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "minor_road"],
        ["==", "kind_detail", "service"],
      ],
      paint: {
        "line-color": t.minor_service,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          18,
          8,
        ],
      },
    },
    {
      id: "roads_minor",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "minor_road"],
        ["!=", "kind_detail", "service"],
      ],
      paint: {
        "line-color": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          t.minor_a,
          16,
          t.minor_b,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          0,
          12.5,
          0.5,
          15,
          2,
          18,
          11,
        ],
      },
    },
    {
      id: "roads_major_casing_early",
      type: "line",
      source: source,
      "source-layer": "roads",
      maxzoom: 12,
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "major_road"],
      ],
      paint: {
        "line-color": t.major_casing_early,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          0.5,
          18,
          13,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          9,
          0,
          9.5,
          1,
        ],
      },
    },
    {
      id: "roads_major",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "major_road"],
      ],
      paint: {
        "line-color": t.major,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          6,
          0,
          12,
          1.6,
          15,
          3,
          18,
          13,
        ],
      },
    },
    {
      id: "roads_highway_casing_early",
      type: "line",
      source: source,
      "source-layer": "roads",
      maxzoom: 12,
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "highway"],
        ["!has", "is_link"],
      ],
      paint: {
        "line-color": t.highway_casing_early,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          3.5,
          0.5,
          18,
          15,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          1,
        ],
      },
    },
    {
      id: "roads_highway",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["!has", "is_tunnel"],
        ["!has", "is_bridge"],
        ["==", "kind", "highway"],
        ["!has", "is_link"],
      ],
      paint: {
        "line-color": t.highway,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          6,
          1.1,
          12,
          1.6,
          15,
          5,
          18,
          15,
        ],
      },
    },
    {
      id: "roads_rail",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: ["==", "kind", "rail"],
      paint: {
        "line-dasharray": [0.3, 0.75],
        "line-opacity": 0.5,
        "line-color": t.railway,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          6,
          0.15,
          18,
          9,
        ],
      },
    },
    {
      id: "boundaries_country",
      type: "line",
      source: source,
      "source-layer": "boundaries",
      filter: ["<=", "kind_detail", 2],
      paint: {
        "line-color": t.boundaries,
        "line-width": 1,
        "line-dasharray": [3, 2],
      },
    },
    {
      id: "boundaries",
      type: "line",
      source: source,
      "source-layer": "boundaries",
      filter: [">", "kind_detail", 2],
      paint: {
        "line-color": t.boundaries,
        "line-width": 0.5,
        "line-dasharray": [3, 2],
      },
    },
    {
      id: "roads_bridges_other_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["in", "kind", "other", "path"]],
      paint: {
        "line-color": t.bridges_other_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          14,
          0,
          20,
          7,
        ],
      },
    },
    {
      id: "roads_bridges_link_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["has", "is_link"]],
      paint: {
        "line-color": t.bridges_minor_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          18,
          11,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          12,
          0,
          12.5,
          1.5,
        ],
      },
    },
    {
      id: "roads_bridges_minor_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["==", "kind", "minor_road"]],
      paint: {
        "line-color": t.bridges_minor_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          0,
          12.5,
          0.5,
          15,
          2,
          18,
          11,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          0.8,
        ],
      },
    },
    {
      id: "roads_bridges_major_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["==", "kind", "major_road"]],
      paint: {
        "line-color": t.bridges_major_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          0.5,
          18,
          10,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          9,
          0,
          9.5,
          1.5,
        ],
      },
    },
    {
      id: "roads_bridges_other",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["in", "kind", "other", "path"]],
      paint: {
        "line-color": t.bridges_other,
        "line-dasharray": [2, 1],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          14,
          0,
          20,
          7,
        ],
      },
    },
    {
      id: "roads_bridges_minor",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["==", "kind", "minor_road"]],
      paint: {
        "line-color": t.bridges_minor,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          11,
          0,
          12.5,
          0.5,
          15,
          2,
          18,
          11,
        ],
      },
    },
    {
      id: "roads_bridges_link",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["has", "is_link"]],
      paint: {
        "line-color": t.bridges_minor,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          13,
          0,
          13.5,
          1,
          18,
          11,
        ],
      },
    },
    {
      id: "roads_bridges_major",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: ["all", ["has", "is_bridge"], ["==", "kind", "major_road"]],
      paint: {
        "line-color": t.bridges_major,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          6,
          0,
          12,
          1.6,
          15,
          3,
          18,
          13,
        ],
      },
    },
    {
      id: "roads_bridges_highway_casing",
      type: "line",
      source: source,
      "source-layer": "roads",
      minzoom: 12,
      filter: [
        "all",
        ["has", "is_bridge"],
        ["==", "kind", "highway"],
        ["!has", "is_link"],
      ],
      paint: {
        "line-color": t.bridges_highway_casing,
        "line-gap-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          3.5,
          0.5,
          18,
          15,
        ],
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          7,
          0,
          7.5,
          1,
          20,
          15,
        ],
      },
    },
    {
      id: "roads_bridges_highway",
      type: "line",
      source: source,
      "source-layer": "roads",
      filter: [
        "all",
        ["has", "is_bridge"],
        ["==", "kind", "highway"],
        ["!has", "is_link"],
      ],
      paint: {
        "line-color": t.bridges_highway,
        "line-width": [
          "interpolate",
          ["exponential", 1.6],
          ["zoom"],
          3,
          0,
          6,
          1.1,
          12,
          1.6,
          15,
          5,
          18,
          15,
        ],
      },
    },
  ];
}

export function labels_layers(
  source: string,
  t: Theme,
  lang: string,
  script?: string,
): LayerSpecification[] {
  return [
    {
      id: "water_waterway_label",
      type: "symbol",
      source: source,
      "source-layer": "water",
      minzoom: 13,
      filter: ["in", "kind", "river", "stream"],
      layout: {
        "symbol-placement": "line",
        "text-font": ["Noto Sans Italic"],
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-size": 12,
        "text-letter-spacing": 0.2,
      },
      paint: {
        "text-color": t.ocean_label,
      },
    },
    {
      id: "roads_labels_minor",
      type: "symbol",
      source: source,
      "source-layer": "roads",
      minzoom: 15,
      filter: ["in", "kind", "minor_road", "other", "path"],
      layout: {
        "symbol-sort-key": ["get", "min_zoom"],
        "symbol-placement": "line",
        "text-font": ["Noto Sans Regular"],
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-size": 12,
      },
      paint: {
        "text-color": t.roads_label_minor,
        "text-halo-color": t.roads_label_minor_halo,
        "text-halo-width": 1,
        "text-halo-blur": 1,
      },
    },
    {
      id: "water_label_ocean",
      type: "symbol",
      source: source,
      "source-layer": "water",
      filter: [
        "in",
        "kind",
        "sea",
        "ocean",
        "lake",
        "water",
        "bay",
        "strait",
        "fjord",
      ],
      layout: {
        "text-font": ["Noto Sans Italic"],
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-size": ["interpolate", ["linear"], ["zoom"], 3, 10, 10, 12],
        "text-letter-spacing": 0.1,
        "text-max-width": 9,
        "text-transform": "uppercase",
      },
      paint: {
        "text-color": t.ocean_label,
      },
    },
    {
      id: "water_label_lakes",
      type: "symbol",
      source: source,
      "source-layer": "water",
      filter: ["in", "kind", "lake", "water"],
      layout: {
        "text-font": ["Noto Sans Italic"],
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-size": ["interpolate", ["linear"], ["zoom"], 3, 0, 6, 12, 10, 12],
        "text-letter-spacing": 0.1,
        "text-max-width": 9,
      },
      paint: {
        "text-color": t.ocean_label,
      },
    },
    {
      id: "roads_labels_major",
      type: "symbol",
      source: source,
      "source-layer": "roads",
      minzoom: 11,
      filter: ["in", "kind", "highway", "major_road"],
      layout: {
        "symbol-sort-key": ["get", "min_zoom"],
        "symbol-placement": "line",
        "text-font": ["Noto Sans Regular"],
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-size": 12,
      },
      paint: {
        "text-color": t.roads_label_major,
        "text-halo-color": t.roads_label_major_halo,
        "text-halo-width": 2,
      },
    },
    {
      id: "places_subplace",
      type: "symbol",
      source: source,
      "source-layer": "places",
      filter: ["==", "kind", "neighbourhood"],
      layout: {
        "symbol-sort-key": ["get", "min_zoom"],
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-font": ["Noto Sans Regular"],
        "text-max-width": 7,
        "text-letter-spacing": 0.1,
        "text-padding": [
          "interpolate",
          ["linear"],
          ["zoom"],
          5,
          2,
          8,
          4,
          12,
          18,
          15,
          20,
        ],
        "text-size": [
          "interpolate",
          ["exponential", 1.2],
          ["zoom"],
          11,
          8,
          14,
          14,
          18,
          24,
        ],
        "text-transform": "uppercase",
      },
      paint: {
        "text-color": t.subplace_label,
        "text-halo-color": t.subplace_label_halo,
        "text-halo-width": 1,
        "text-halo-blur": 1,
      },
    },
    {
      id: "places_locality",
      type: "symbol",
      source: source,
      "source-layer": "places",
      filter: ["==", "kind", "locality"],
      layout: {
        "icon-image": ["step", ["zoom"], "townspot", 8, ""],
        "icon-size": 0.7,
        "text-field": get_multiline_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-font": [
          "case",
          ["<=", ["get", "min_zoom"], 5],
          ["literal", ["Noto Sans Medium"]],
          ["literal", ["Noto Sans Regular"]],
        ],
        "text-padding": [
          "interpolate",
          ["linear"],
          ["zoom"],
          5,
          3,
          8,
          7,
          12,
          11,
        ],
        "text-size": [
          "interpolate",
          ["linear"],
          ["zoom"],
          2,
          [
            "case",
            ["<", ["get", "population_rank"], 13],
            8,
            [">=", ["get", "population_rank"], 13],
            13,
            0,
          ],
          4,
          [
            "case",
            ["<", ["get", "population_rank"], 13],
            10,
            [">=", ["get", "population_rank"], 13],
            15,
            0,
          ],
          6,
          [
            "case",
            ["<", ["get", "population_rank"], 12],
            11,
            [">=", ["get", "population_rank"], 12],
            17,
            0,
          ],
          8,
          [
            "case",
            ["<", ["get", "population_rank"], 11],
            11,
            [">=", ["get", "population_rank"], 11],
            18,
            0,
          ],
          10,
          [
            "case",
            ["<", ["get", "population_rank"], 9],
            12,
            [">=", ["get", "population_rank"], 9],
            20,
            0,
          ],
          15,
          [
            "case",
            ["<", ["get", "population_rank"], 8],
            12,
            [">=", ["get", "population_rank"], 8],
            22,
            0,
          ],
        ],
        "icon-padding": [
          "interpolate",
          ["linear"],
          ["zoom"],
          0,
          0,
          8,
          4,
          10,
          8,
          12,
          6,
          22,
          2,
        ],
        "text-justify": "auto",
        "text-anchor": ["step", ["zoom"], "left", 8, "center"],
        "text-radial-offset": 0.4,
      },
      paint: {
        "text-color": t.city_label,
        "text-halo-color": t.city_label_halo,
        "text-halo-width": 1,
        "text-halo-blur": 1,
      },
    },
    {
      id: "places_region",
      type: "symbol",
      source: source,
      "source-layer": "places",
      filter: ["==", "kind", "region"],
      layout: {
        "symbol-sort-key": ["get", "min_zoom"],
        "text-field": [
          "step",
          ["zoom"],
          ["get", "name:short"],
          6,
          get_multiline_name(lang, script) as ExpressionSpecification,
        ],
        "text-font": ["Noto Sans Regular"],
        "text-size": ["interpolate", ["linear"], ["zoom"], 3, 11, 7, 16],
        "text-radial-offset": 0.2,
        "text-anchor": "center",
        "text-transform": "uppercase",
      },
      paint: {
        "text-color": t.state_label,
        "text-halo-color": t.state_label_halo,
        "text-halo-width": 1,
        "text-halo-blur": 1,
      },
    },
    {
      id: "places_country",
      type: "symbol",
      source: source,
      "source-layer": "places",
      filter: ["==", "kind", "country"],
      layout: {
        "symbol-sort-key": ["get", "min_zoom"],
        "text-field": get_country_name(
          lang,
          script,
        ) as DataDrivenPropertyValueSpecification<string>,
        "text-font": ["Noto Sans Medium"],
        "text-size": [
          "interpolate",
          ["linear"],
          ["zoom"],
          2,
          [
            "case",
            ["<", ["get", "population_rank"], 10],
            8,
            [">=", ["get", "population_rank"], 10],
            12,
            0,
          ],
          6,
          [
            "case",
            ["<", ["get", "population_rank"], 8],
            10,
            [">=", ["get", "population_rank"], 8],
            18,
            0,
          ],
          8,
          [
            "case",
            ["<", ["get", "population_rank"], 7],
            11,
            [">=", ["get", "population_rank"], 7],
            20,
            0,
          ],
        ],
        "icon-padding": [
          "interpolate",
          ["linear"],
          ["zoom"],
          0,
          2,
          14,
          2,
          16,
          20,
          17,
          2,
          22,
          2,
        ],
        "text-transform": "uppercase",
      },
      paint: {
        "text-color": t.country_label,
      },
    },
  ];
}
