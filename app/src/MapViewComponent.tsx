import { useEffect, useState, useRef } from "react";
import layers from "../../styles/src/index.ts";
import maplibregl from "maplibre-gl";
import * as pmtiles from "pmtiles";
import "maplibre-gl/dist/maplibre-gl.css";
import "ol/ol.css";
import { Map as OpenLayersMap, View } from "ol";
import VectorTile from "ol/layer/VectorTile";

// @ts-ignore
import { PMTilesVectorSource } from "ol-pmtiles";
import { useGeographic } from "ol/proj";
import { stylefunction } from "ol-mapbox-style";

function getMaplibreStyle(theme: string, tiles?: string): any {
  if (tiles && tiles.endsWith(".pmtiles")) {
    tiles = "pmtiles://" + tiles;
  }
  if (!tiles) {
    return {
      version: 8 as any,
      sources: {},
      layers: [],
    };
  }
  return {
    version: 8 as any,
    glyphs: "https://cdn.protomaps.com/fonts/pbf/{fontstack}/{range}.pbf",
    sources: {
      protomaps: {
        type: "vector",
        url: tiles,
        attribution: '© <a href="https://openstreetmap.org">OpenStreetMap</a>',
      },
    },
    layers: layers("protomaps", theme),
  };
}

function StyleJsonPane(props: { theme: string }) {
  // TODO: wrong structure for OpenLayers
  const stringified = JSON.stringify(
    getMaplibreStyle(props.theme, "https://example.com/tiles.json"),
    null,
    4,
  );

  return (
    <div>
      <button
        onClick={() => {
          navigator.clipboard.writeText(stringified);
        }}
      >
        Copy to clipboard
      </button>
      <pre className="stylePane">{stringified}</pre>
    </div>
  );
}

function MapLibreView(props: { theme: string; tiles?: string }) {
  const mapRef = useRef<maplibregl.Map>();

  useEffect(() => {
    if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
      maplibregl.setRTLTextPlugin(
        "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
        () => {},
        true,
      );
    }

    const protocol = new pmtiles.Protocol();
    maplibregl.addProtocol("pmtiles", protocol.tile);

    const map = new maplibregl.Map({
      hash: true,
      container: "map",
      style: getMaplibreStyle(props.theme, props.tiles),
    });

    map.addControl(new maplibregl.NavigationControl());
    map.addControl(new maplibregl.ScaleControl({}));
    map.addControl(
      new maplibregl.GeolocateControl({
        positionOptions: {
          enableHighAccuracy: true,
        },
        trackUserLocation: true,
      }),
    );

    map.on("mousedown", function (e) {
      map.queryRenderedFeatures(e.point).map((feat) => {
        console.log(feat);
      });
    });

    mapRef.current = map;

    return () => {
      maplibregl.removeProtocol("pmtiles");
      map.remove();
    };
  }, []);

  useEffect(() => {
    if (mapRef.current) {
      mapRef.current.setStyle(getMaplibreStyle(props.theme, props.tiles));
    }
  }, [props.tiles, props.theme]);

  return <div id="map"></div>;
}

// TODO: does not sync map hash state
function OpenLayersView(props: { theme: string; tiles?: string }) {
  useEffect(() => {
    useGeographic();

    const layer = new VectorTile({
      declutter: true,
      source: new PMTilesVectorSource({
        url: "https://r2-public.protomaps.com/protomaps-sample-datasets/protomaps-basemap-opensource-20230408.pmtiles",
        attributions: ["© OpenStreetMap"],
      }),
      style: null,
    });

    stylefunction(
      layer,
      {
        version: "8",
        layers: layers("protomaps", props.theme),
        sources: { protomaps: { type: "vector" } },
      },
      "protomaps",
    );

    new OpenLayersMap({
      target: "map",
      layers: [layer],
      view: new View({
        center: [0, 0],
        zoom: 0,
      }),
    });
  }, []);

  return <div id="map"></div>;
}

// if no tiles are passed, loads the latest daily build.
export default function MapViewComponent() {
  const params = new URLSearchParams(location.search);
  const [theme, setTheme] = useState<string>(params.get("theme") || "light");

  let tilesParam = params.get("tiles") || undefined;

  const [tiles, setTiles] = useState<string | undefined>(tilesParam);
  const [renderer, setRenderer] = useState<string>(
    params.get("renderer") || "maplibregl",
  );
  const [showStyleJson, setShowStyleJson] = useState<boolean>(false);

  // TODO: dynamic import of https://unpkg.com/protomaps-themes-base@1.3.1/dist/light.json etc
  // TODO: language tag selector

  useEffect(() => {
    if (!tiles) {
      console.log("fetching");
      fetch("https://build-metadata.protomaps.dev/builds.json")
        .then((r) => {
          return r.json();
        })
        .then((j) => {
          setTiles("https://build.protomaps.com/" + j[j.length - 1].key);
        });
    }
  }, [tiles]);

  return (
    <div className="map-container">
      <nav>
        <input defaultValue={tiles} style={{ width: "50%" }} />
        <button>load</button>
        <select onChange={(e) => setTheme(e.target.value)} value={theme}>
          <option value="light">base light</option>
          <option value="dark">base dark</option>
          <option value="white">data light</option>
          <option value="grayscale">data grayscale</option>
          <option value="black">data dark</option>
          <option value="contrast">contrast</option>
        </select>
        <select onChange={(e) => setRenderer(e.target.value)} value={renderer}>
          <option value="maplibregl">maplibregl</option>
          <option value="openlayers">openlayers</option>
        </select>
        <button onClick={() => setShowStyleJson(!showStyleJson)}>
          get style JSON
        </button>
        <a href="/visualtests/">visual tests</a>
      </nav>
      <div className="split">
        {renderer == "maplibregl" ? (
          <MapLibreView tiles={tiles} theme={theme} />
        ) : (
          <OpenLayersView tiles={tiles} theme={theme} />
        )}
        {showStyleJson && <StyleJsonPane theme={theme} />}
      </div>
    </div>
  );
}
