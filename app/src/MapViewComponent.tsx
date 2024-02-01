import maplibregl from "maplibre-gl";
import { MapGeoJSONFeature, StyleSpecification } from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import { Map as OpenLayersMap, View } from "ol";
import VectorTile from "ol/layer/VectorTile";
import "ol/ol.css";
import * as pmtiles from "pmtiles";
import {
  FormEvent,
  KeyboardEvent,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";
import { renderToString } from "react-dom/server";
import { useDropzone } from "react-dropzone";
import layers from "../../styles/src/index.ts";

import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";

import { stylefunction } from "ol-mapbox-style";
// @ts-ignore
import { PMTilesVectorSource } from "ol-pmtiles";
import { useGeographic } from "ol/proj";

import { FileAPISource, PMTiles, Protocol } from "pmtiles";
import { createHash, parseHash } from "./hash";

const GIT_SHA = (import.meta.env.VITE_GIT_SHA || "main").substr(0, 8);

const ATTRIBUTION =
  '<a href="https://github.com/protomaps/basemaps">Protomaps</a> © <a href="https://openstreetmap.org">OpenStreetMap</a>';

function getSourceLayer(l: LayerSpecification): string {
  if ("source-layer" in l && l["source-layer"]) {
    return l["source-layer"];
  }
  return "";
}

const FeaturesProperties = (props: { features: MapGeoJSONFeature[] }) => {
  return (
    <div className="features-properties">
      {props.features.map((f) => (
        <div key={f.id}>
          <span>
            <strong>{getSourceLayer(f.layer)}</strong>
            <span> ({f.geometry.type})</span>
          </span>
          <table>
            <tr key={0}>
              <td>id</td>
              <td>{f.id}</td>
            </tr>
            {Object.entries(f.properties).map(([key, value]) => (
              <tr key={key}>
                <td>{key}</td>
                <td>{value}</td>
              </tr>
            ))}
          </table>
        </div>
      ))}
    </div>
  );
};

export const isValidTiles = (tiles?: string): boolean => {
  if (!tiles) return false;
  if (!tiles.startsWith("http") && tiles.endsWith(".pmtiles")) return true;
  if (tiles.startsWith("http") && new URL(tiles).pathname.endsWith(".pmtiles"))
    return true;
  return false;
};

function getMaplibreStyle(
  theme: string,
  tiles?: string,
  npmLayers?: LayerSpecification[],
  droppedArchive?: PMTiles,
  minZoom?: number,
  maxZoom?: number,
): StyleSpecification {
  let tilesWithProtocol = tiles;
  if (isValidTiles(tiles)) {
    tilesWithProtocol = `pmtiles://${tiles}`;
  }
  const style = {
    version: 8 as unknown,
    sources: {},
    layers: [],
  } as StyleSpecification;
  if (!tilesWithProtocol) return style;
  style.layers = [];
  style.glyphs =
    "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf";

  if (droppedArchive) {
    style.sources = {
      protomaps: {
        type: "vector",
        attribution: ATTRIBUTION,
        tiles: [`pmtiles://${droppedArchive.source.getKey()}/{z}/{x}/{y}`],
        minzoom: minZoom,
        maxzoom: maxZoom,
      },
    };
  } else {
    style.sources = {
      protomaps: {
        type: "vector",
        attribution: ATTRIBUTION,
        url: tilesWithProtocol,
      },
    };
  }

  if (npmLayers && npmLayers.length > 0) {
    style.layers = style.layers.concat(npmLayers);
  } else {
    style.layers = style.layers.concat(layers("protomaps", theme));
  }

  return style;
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
        type="button"
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

function MapLibreView(props: {
  theme: string;
  tiles?: string;
  npmLayers: LayerSpecification[];
  droppedArchive?: PMTiles;
}) {
  const mapRef = useRef<maplibregl.Map>();
  const protocolRef = useRef<Protocol>();

  useEffect(() => {
    if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
      maplibregl.setRTLTextPlugin(
        "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
        () => {},
        true,
      );
    }

    const protocol = new pmtiles.Protocol();
    protocolRef.current = protocol;
    maplibregl.addProtocol("pmtiles", protocol.tile);

    const map = new maplibregl.Map({
      hash: "map",
      container: "map",
      style: getMaplibreStyle(props.theme, props.tiles, props.npmLayers),
    });

    map.addControl(new maplibregl.NavigationControl());
    map.addControl(new maplibregl.ScaleControl({}));
    map.addControl(
      new maplibregl.GeolocateControl({
        positionOptions: {
          enableHighAccuracy: true,
        },
        trackUserLocation: true,
        fitBoundsOptions: {
          animate: false,
        },
      }),
    );

    const popup = new maplibregl.Popup({
      closeButton: true,
      closeOnClick: false,
      maxWidth: "none",
    });

    map.on("mousedown", (e) => {
      const features = map.queryRenderedFeatures(e.point);
      if (features.length) {
        const content = renderToString(
          <FeaturesProperties features={features} />,
        );
        popup.setHTML(content);
        popup.setLngLat(e.lngLat);
        popup.addTo(map);
      } else {
        popup.remove();
      }
    });

    mapRef.current = map;

    return () => {
      protocolRef.current = undefined;
      maplibregl.removeProtocol("pmtiles");
      map.remove();
    };
  }, [props.npmLayers, props.theme, props.tiles]);

  useEffect(() => {
    if (protocolRef.current) {
      const archive = props.droppedArchive;
      if (archive) {
        protocolRef.current.add(archive);
        (async () => {
          const header = await archive.getHeader();
          mapRef.current?.fitBounds(
            [
              [header.minLon, header.minLat],
              [header.maxLon, header.maxLat],
            ],
            { animate: false },
          );
        })();
      }
    }
  }, [props.droppedArchive]);

  useEffect(() => {
    (async () => {
      if (mapRef.current) {
        let minZoom: number | undefined;
        let maxZoom: number | undefined;
        if (props.droppedArchive) {
          const header = await props.droppedArchive.getHeader();
          minZoom = header.minZoom;
          maxZoom = header.maxZoom;
        }
        mapRef.current.setStyle(
          getMaplibreStyle(
            props.theme,
            props.tiles,
            props.npmLayers,
            props.droppedArchive,
            minZoom,
            maxZoom,
          ),
        );
      }
    })();
  }, [props.tiles, props.theme, props.npmLayers, props.droppedArchive]);

  return <div id="map" />;
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
  }, [props.theme]);

  return <div id="map" />;
}

// if no tiles are passed, loads the latest daily build.
export default function MapViewComponent() {
  const hash = parseHash(location.hash);
  const [theme, setTheme] = useState<string>(hash.theme || "light");
  const [tiles, setTiles] = useState<string | undefined>(hash.tiles);
  const [renderer, setRenderer] = useState<string>(
    hash.renderer || "maplibregl",
  );
  const [showStyleJson, setShowStyleJson] = useState<boolean>(false);
  const [publishedStyleVersion, setPublishedStyleVersion] = useState<
    string | undefined
  >(hash.npm_version);
  const [knownNpmVersions, setKnownNpmVersions] = useState<string[]>([]);
  const [npmLayers, setNpmLayers] = useState<LayerSpecification[]>([]);
  const [droppedArchive, setDroppedArchive] = useState<PMTiles>();

  useEffect(() => {
    const record = {
      theme: theme,
      tiles: tiles,
      renderer: renderer,
      npm_version: publishedStyleVersion,
    };
    location.hash = createHash(location.hash, record);
  });

  const onDrop = useCallback((acceptedFiles: File[]) => {
    setDroppedArchive(new PMTiles(new FileAPISource(acceptedFiles[0])));
  }, []);

  const { getRootProps } = useDropzone({ onDrop });

  // TODO: language tag selector

  useEffect(() => {
    if (!tiles) {
      fetch("https://build-metadata.protomaps.dev/builds.json")
        .then((r) => {
          return r.json();
        })
        .then((j) => {
          setTiles(`https://build.protomaps.com/${j[j.length - 1].key}`);
        });
    }
  }, [tiles]);

  const handleKeyPress = (event: KeyboardEvent<HTMLDivElement>) => {
    const c = event.charCode;
    if (c >= 49 && c <= 53) {
      setTheme(["light", "dark", "white", "grayscale", "black"][c - 49]);
    }
  };

  const loadTiles = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const formData = new FormData(event.target as HTMLFormElement);
    const tilesValue = formData.get("tiles");
    if (typeof tilesValue === "string") {
      setTiles(tilesValue);
    }
  };

  const loadVersionsFromNpm = async () => {
    const resp = await fetch(
      "https://registry.npmjs.org/protomaps-themes-base",
      {
        headers: { Accept: "application/vnd.npm.install-v1+json" },
      },
    );
    const j = await resp.json();
    setKnownNpmVersions(
      Object.keys(j.versions)
        .sort()
        .filter((v) => +v.split(".")[0] >= 2)
        .reverse(),
    );
  };

  useEffect(() => {
    (async () => {
      if (publishedStyleVersion === undefined) {
        setNpmLayers([]);
      } else {
        fetch(
          `https://unpkg.com/protomaps-themes-base@${publishedStyleVersion}/dist/layers/${theme}.json`,
        )
          .then((resp) => {
            return resp.json();
          })
          .then((j) => {
            setNpmLayers(j);
          });
      }
    })();
  }, [publishedStyleVersion, theme]);

  return (
    <div className="map-container">
      <nav>
        <form onSubmit={loadTiles}>
          <input
            type="text"
            name="tiles"
            defaultValue={tiles}
            style={{ width: "50%" }}
          />
          <button type="submit">load</button>
        </form>
        <span {...getRootProps()}>Drop Zone</span>
        <select onChange={(e) => setTheme(e.target.value)} value={theme}>
          <option value="light">light</option>
          <option value="dark">dark</option>
          <option value="white">data viz (white)</option>
          <option value="grayscale">data viz (grayscale)</option>
          <option value="black">data viz (black)</option>
        </select>
        <select onChange={(e) => setRenderer(e.target.value)} value={renderer}>
          <option value="maplibregl">maplibregl</option>
          <option value="openlayers">openlayers</option>
        </select>
        {knownNpmVersions.length === 0 ? (
          <button type="button" onClick={loadVersionsFromNpm}>
            npm version...
          </button>
        ) : (
          <select
            onChange={(e) => setPublishedStyleVersion(e.target.value)}
            value={publishedStyleVersion}
          >
            <option key="" value="">
              main
            </option>
            {knownNpmVersions.map((v: string) => (
              <option key={v} value={v}>
                {v}
              </option>
            ))}
          </select>
        )}
        <button type="button" onClick={() => setShowStyleJson(!showStyleJson)}>
          get style JSON
        </button>
        <a href="/visualtests/">visual tests</a>|
        <a
          target="_blank"
          rel="noreferrer"
          href="https://github.com/protomaps/basemaps"
        >
          {GIT_SHA}
        </a>
      </nav>
      <div className="split" onKeyPress={handleKeyPress}>
        {renderer === "maplibregl" ? (
          <MapLibreView
            tiles={tiles}
            theme={theme}
            npmLayers={npmLayers}
            droppedArchive={droppedArchive}
          />
        ) : (
          <OpenLayersView tiles={tiles} theme={theme} /> // TODO: we need to refactor ol-pmtiles to take PMTiles as argument
        )}
        {showStyleJson && <StyleJsonPane theme={theme} />}
      </div>
    </div>
  );
}
