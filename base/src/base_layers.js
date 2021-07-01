export default layers = c => {
  return [
    {
      "id": "background",
      "type": "background",
      "paint": {
        "background-color": c.background
      }
    },
    {
      "id": "earth",
      "type":"fill",
      "source":"protomaps",
      "source-layer":"earth",
      "paint": {
        "fill-color": c.earth
      }
    },
    {
      "id": "landuse_park",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "any",
        [
          "==",
          "pmap:kind",
          "park"
        ],
        [
          "==",
          "landuse",
          "cemetery"
        ]
      ],
      "paint": {
        "fill-color": c.park
      }
    },
    {
      "id": "landuse_hospital",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "any",
        [
          "==",
          "pmap:kind",
          "hospital"
        ]
      ],
      "paint": {
        "fill-color": c.hospital
      }
    },
    {
      "id": "landuse_industrial",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "any",
        [
          "==",
          "pmap:kind",
          "industrial"
        ]
      ],
      "paint": {
        "fill-color": c.industrial
      }
    },
    {
      "id": "landuse_school",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "any",
        [
          "==",
          "pmap:kind",
          "school"
        ]
      ],
      "paint": {
        "fill-color": c.school
      }
    },
    {
      "id": "natural_wood",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "natural",
      "filter": [
        "any",
        [
          "==",
          "natural",
          "wood"
        ],
        [
          "==",
          "leisure",
          "nature_reserve"
        ],
        [
          "==",
          "landuse",
          "forest"
        ]
      ],
      "paint": {
        "fill-color": c.wood
      }
    },
    {
      "id": "landuse_pedestrian",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "any",
        [
          "==",
          "highway",
          "footway"
        ]
      ],
      "paint": {
        "fill-color": c.pedestrian
      }
    },
    {
      "id": "natural_scrub",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "natural",
      "filter": [
        "in",
        "natural",
        "scrub",
        "grassland"
      ],
      "paint": {
        "fill-color": c.scrub
      }
    },
    {
      "id": "natural_glacier",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "natural",
      "filter": [
        "==",
        "natural",
        "glacier"
      ],
      "paint": {
        "fill-color": c.glacier
      }
    },
    {
      "id": "natural_sand",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "natural",
      "filter": [
        "==",
        "natural",
        "sand"
      ],
      "paint": {
        "fill-color": c.sand
      }
    },
    {
      "id": "landuse_aerodrome",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "==",
        "aeroway",
        "aerodrome"
      ],
      "paint": {
        "fill-color": c.aerodrome
      }
    },
    {
      "id": "transit_runway",
      "type": "line",
      "source": "protomaps",
      "source-layer": "transit",
      "filter": [
        "has",
        "aeroway"
      ],
      "paint": {
        "line-color": c.runway,
        "line-width": 6
      }
    },
    {
      "id": "landuse_runway",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "landuse",
      "filter": [
        "any",
        [
          "==",
          "aeroway",
          "runway"
        ],
        [
          "==",
          "area:aeroway",
          "runway"
        ],
        [
          "==",
          "area:aeroway",
          "taxiway"
        ]
      ],
      "paint": {
        "fill-color": c.runway
      }
    },
    {
      "id": "water",
      "type": "fill",
      "source": "protomaps",
      "source-layer": "water",
      "paint": {
        "fill-color": c.water
      }
    },
    {
      "id": "roads_tunnels_other_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "other"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_other_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          14,
          0,
          14.5,
          0.5,
          20,
          12
        ]
      }
    },
    {
      "id": "roads_tunnels_other",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "other"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_other,
        "line-dasharray": [
          1,
          1
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          14,
          0,
          14.5,
          0.5,
          20,
          12
        ]
      }
    },
    {
      "id": "roads_tunnels_minor_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "minor_road"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_minor_casing,
        "line-dasharray": [
          3,
          2
        ],
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          0.5,
          20,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          1
        ]
      }
    },
    {
      "id": "roads_tunnels_minor",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "minor_road"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_minor,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          0.5,
          20,
          32
        ]
      }
    },
    {
      "id": "roads_tunnels_medium_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "medium_road"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_medium_casing,
        "line-dasharray": [
          3,
          2
        ],
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          20,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          10,
          0,
          10.5,
          1
        ]
      }
    },
    {
      "id": "roads_tunnels_medium",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "medium_road"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_medium,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          20,
          32
        ]
      }
    },
    {
      "id": "roads_tunnels_major_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "major_road"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_major_casing,
        "line-dasharray": [
          3,
          2
        ],
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          19,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          9,
          0,
          9.5,
          1
        ]
      }
    },
    {
      "id": "roads_tunnels_major",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "major_road"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_major,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          19,
          32
        ]
      }
    },
    {
      "id": "roads_tunnels_highway_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "highway"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_highway_casing,
        "line-dasharray": [
          3,
          2
        ],
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          3,
          0,
          3.5,
          0.5,
          18,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          1
        ]
      }
    },
    {
      "id": "roads_tunnels_highway",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "<",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "highway"
        ]
      ],
      "paint": {
        "line-color": c.tunnel_highway,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          3,
          0,
          3.5,
          0.5,
          18,
          32
        ]
      }
    },
    {
      "id": "physical_line_waterway",
      "type": "line",
      "source": "protomaps",
      "source-layer": "physical_line",
      "filter": [
        "==",
        [
          "get",
          "pmap:kind"
        ],
        "waterway"
      ],
      "paint": {
        "line-color": c.water,
        "line-width": 0.5
      }
    },
    {
      "id": "buildings",
      "type": "fill-extrusion",
      "source": "protomaps",
      "source-layer": "buildings",
      "paint": {
        "fill-extrusion-color": c.buildings,
        "fill-extrusion-height": [
          "to-number",
          [
            "get",
            "height"
          ]
        ],
        "fill-extrusion-opacity": 0.5
      }
    },
    {
      "id": "roads_other",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "other"
        ]
      ],
      "paint": {
        "line-color": c.other,
        "line-dasharray": [
          2,
          1
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          14,
          0,
          14.5,
          0.5,
          20,
          12
        ]
      }
    },
    {
      "id": "roads_minor_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "minor_road"
        ]
      ],
      "paint": {
        "line-color": c.minor_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          0.5,
          20,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          1
        ]
      }
    },
    {
      "id": "roads_minor",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "minor_road"
        ]
      ],
      "paint": {
        "line-color": c.minor,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          0.5,
          20,
          32
        ]
      }
    },
    {
      "id": "roads_medium_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "medium_road"
        ]
      ],
      "paint": {
        "line-color": c.medium_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          20,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          10,
          0,
          10.5,
          1
        ]
      }
    },
    {
      "id": "roads_medium",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "medium_road"
        ]
      ],
      "paint": {
        "line-color": c.medium,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          20,
          32
        ]
      }
    },
    {
      "id": "roads_major_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "major_road"
        ]
      ],
      "paint": {
        "line-color": c.major_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          19,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          9,
          0,
          9.5,
          1
        ]
      }
    },
    {
      "id": "roads_major",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "major_road"
        ]
      ],
      "paint": {
        "line-color": c.major,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          19,
          32
        ]
      }
    },
    {
      "id": "roads_highway_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "highway"
        ]
      ],
      "paint": {
        "line-color": c.highway_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          3,
          0,
          3.5,
          0.5,
          18,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          1
        ]
      }
    },
    {
      "id": "roads_highway",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          "==",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "highway"
        ]
      ],
      "paint": {
        "line-color": c.highway,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          3,
          0,
          3.5,
          0.5,
          18,
          32
        ]
      }
    },
    {
      "id": "transit_railway",
      "type": "line",
      "source": "protomaps",
      "source-layer": "transit",
      "filter": [
        "all",
        [
          "==",
          "pmap:kind",
          "railway"
        ]
      ],
      "paint": {
        "line-color": c.railway,
        "line-width": 2
      }
    },
    {
      "id": "transit_railway_tracks",
      "type": "line",
      "source": "protomaps",
      "source-layer": "transit",
      "filter": [
        "all",
        [
          "==",
          "pmap:kind",
          "railway"
        ]
      ],
      "paint": {
        "line-color": c.railway_tracks,
        "line-width": 0.8,
        "line-dasharray": [
          6,
          10
        ]
      }
    },
    {
      "id": "boundaries",
      "type": "line",
      "source": "protomaps",
      "source-layer": "boundaries",
      "paint": {
        "line-color": c.boundaries,
        "line-width": 0.5,
        "line-dasharray": [
          3,
          2
        ]
      }
    },
    {
      "id": "physical_line_waterway_label",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "physical_line",
      "minzoom": 14,
      "layout": {
        "symbol-placement": "line",
        "text-font": [
          "NotoSans-Regular"
        ],
        "text-field": [
          "get",
          "name"
        ],
        "text-size": 14,
        "text-letter-spacing": 0.3
      },
      "paint": {
        "text-color": c.waterway_label,
        "text-halo-color": c.waterway_label_halo,
        "text-halo-width": 2
      }
    },
    {
      "id": "roads_bridges_other_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "other"
        ]
      ],
      "paint": {
        "line-color": c.bridges_other_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          14,
          0,
          14.5,
          0.5,
          20,
          12
        ]
      }
    },
    {
      "id": "roads_bridges_other",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "other"
        ]
      ],
      "paint": {
        "line-color": c.bridges_other,
        "line-dasharray": [
          2,
          1
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          14,
          0,
          14.5,
          0.5,
          20,
          12
        ]
      }
    },
    {
      "id": "roads_bridges_minor_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "minor_road"
        ]
      ],
      "paint": {
        "line-color": c.bridges_minor_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          0.5,
          20,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          1
        ]
      }
    },
    {
      "id": "roads_bridges_minor",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "minor_road"
        ]
      ],
      "paint": {
        "line-color": c.bridges_minor,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          12,
          0,
          12.5,
          0.5,
          20,
          32
        ]
      }
    },
    {
      "id": "roads_bridges_medium_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "medium_road"
        ]
      ],
      "paint": {
        "line-color": c.bridges_medium_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          20,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          10,
          0,
          10.5,
          1
        ]
      }
    },
    {
      "id": "roads_bridges_medium",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "medium_road"
        ]
      ],
      "paint": {
        "line-color": c.bridges_medium,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          20,
          32
        ]
      }
    },
    {
      "id": "roads_bridges_major_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "major_road"
        ]
      ],
      "paint": {
        "line-color": c.bridges_major_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          19,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          9,
          0,
          9.5,
          1
        ]
      }
    },
    {
      "id": "roads_bridges_major",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "major_road"
        ]
      ],
      "paint": {
        "line-color": c.bridges_major,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          0.5,
          19,
          32
        ]
      }
    },
    {
      "id": "roads_bridges_highway_casing",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "highway"
        ]
      ],
      "paint": {
        "line-color": c.bridges_highway_casing,
        "line-gap-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          3,
          0,
          3.5,
          0.5,
          18,
          32
        ],
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          7,
          0,
          7.5,
          1
        ]
      }
    },
    {
      "id": "roads_bridges_highway",
      "type": "line",
      "source": "protomaps",
      "source-layer": "roads",
      "filter": [
        "all",
        [
          ">",
          "pmap:level",
          0
        ],
        [
          "==",
          "pmap:kind",
          "highway"
        ]
      ],
      "paint": {
        "line-color": c.bridges_highway,
        "line-width": [
          "interpolate",
          [
            "exponential",
            1.6
          ],
          [
            "zoom"
          ],
          3,
          0,
          3.5,
          0.5,
          18,
          32
        ]
      }
    },
    {
      "id": "roads_labels",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "roads",
      "layout": {
        "symbol-placement": "line",
        "text-font": [
          "NotoSans-Regular"
        ],
        "text-field": [
          "get",
          "name"
        ],
        "text-size": 14
      },
      "paint": {
        "text-color": c.roads_label,
        "text-halo-color": c.roads_label_halo,
        "text-halo-width": 2
      }
    },
    {
      "id":"mask",
      "type":"fill",
      "source":"protomaps",
      "source-layer":"mask",
      "paint":{
        "fill-color": c.background
      }
    },
    {
      "id": "physical_point_ocean",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "physical_point",
      "filter": [
        "any",
        [
          "==",
          "place",
          "sea"
        ],
        [
          "==",
          "place",
          "ocean"
        ]
      ],
      "layout": {
        "text-font": [
          "NotoSans-Regular"
        ],
        "text-field": [
          "get",
          "name"
        ],
        "text-size": 14,
        "text-letter-spacing": 0.15
      },
      "paint": {
        "text-color": c.ocean_label,
        "text-halo-color": c.ocean_label_halo,
        "text-halo-width": 1
      }
    },
    {
      "id": "physical_point_peak",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "physical_point",
      "filter": [
        "any",
        [
          "==",
          "natural",
          "peak"
        ]
      ],
      "layout": {
        "text-font": [
          "NotoSans-Regular"
        ],
        "text-field": [
          "get",
          "name"
        ],
        "text-size": 14
      },
      "paint": {
        "text-color": c.peak_label,
        "text-halo-color": c.peak_label_halo,
        "text-halo-width": 1.5
      }
    },
    {
      "id": "places_subplace",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "places",
      "filter": [
        "==",
        "pmap:kind",
        "neighbourhood"
      ],
      "layout": {
        "text-field": "{name}",
        "text-font": [
          "NotoSans-Regular"
        ],
        "text-size": {
          "base": 1.2,
          "stops": [
            [
              11,
              10
            ],
            [
              14,
              12
            ]
          ]
        },
        "text-transform":"uppercase"
      },
      "paint": {
        "text-color": c.subplace_label,
        "text-halo-color": c.subplace_label_halo,
        "text-halo-width": 0.5
      }
    },
    {
      "id": "places_city_circle",
      "type": "circle",
      "source": "protomaps",
      "source-layer": "places",
      "filter": [
        "==",
        "pmap:kind",
        "city"
      ],
      "paint": {
        "circle-radius": 2,
        "circle-stroke-width": 2,
        "circle-stroke-color": c.city_circle_stroke,
        "circle-color": c.city_circle
      },
      "maxzoom": 8
    },
    {
      "id": "places_city",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "places",
      "filter": [
        "==",
        "pmap:kind",
        "city"
      ],
      "layout": {
        "text-field": "{name}",
        "text-font": [
          "NotoSans-Bold"
        ],
        "text-size": [
          "step",
          [
            "get",
            "pmap:rank"
          ],
          0,
          1,
          16,
          2,
          14
        ],
        "text-variable-anchor": [
          "bottom-left"
        ],
        "text-radial-offset": 0.2
      },
      "paint": {
        "text-color": c.city_label,
        "text-halo-color": c.city_label_halo,
        "text-halo-width": 1
      }
    },
    {
      "id": "places_state",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "places",
      "filter": [
        "==",
        "pmap:kind",
        "state"
      ],
      "layout": {
        "text-field": "{name}",
        "text-font": [
          "NotoSans-Regular"
        ],
        "text-size": 12,
        "text-radial-offset": 0.2,
        "text-anchor": "center",
        "text-transform": "uppercase"
      },
      "paint": {
        "text-color": c.state_label,
        "text-halo-color": c.state_label_halo,
        "text-halo-width": 0.5
      }
    },
    {
      "id": "places_country",
      "type": "symbol",
      "source": "protomaps",
      "source-layer": "places",
      "filter": [
        "==",
        "place",
        "country"
      ],
      "layout": {
        "text-field": "{name}",
        "text-font": [
          "NotoSans-Bold"
        ],
        "text-size": {
          "base": 1.2,
          "stops": [
            [
              2,
              14
            ],
            [
              6,
              14
            ],
            [
              8,
              20
            ]
          ]
        },
        "text-transform": "uppercase"
      },
      "paint": {
        "text-color": c.country_label,
        "text-halo-color": c.country_label_halo,
        "text-halo-width": 1.0
      }
    }
  ]
}
