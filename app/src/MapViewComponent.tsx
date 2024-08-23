import maplibregl from "maplibre-gl";
import { MapGeoJSONFeature, StyleSpecification } from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
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
import { language_script_pairs } from "../../styles/src/language.ts";

import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";

import { FileSource, PMTiles, Protocol } from "pmtiles";
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

export const isValidPMTiles = (tiles?: string): boolean => {
  if (!tiles) return false;
  if (!tiles.startsWith("http") && tiles.endsWith(".pmtiles")) return true;
  if (tiles.startsWith("http") && new URL(tiles).pathname.endsWith(".pmtiles"))
    return true;
  return false;
};

function getMaplibreStyle(
  theme: string,
  lang: string,
  localSprites: boolean,
  tiles?: string,
  npmLayers?: LayerSpecification[],
  droppedArchive?: PMTiles,
  minZoom?: number,
  maxZoom?: number,
): StyleSpecification {
  const style = {
    version: 8 as unknown,
    sources: {},
    layers: [],
  } as StyleSpecification;
  if (!tiles) return style;
  let tilesWithProtocol: string;
  if (isValidPMTiles(tiles)) {
    tilesWithProtocol = `pmtiles://${tiles}`;
  } else {
    tilesWithProtocol = tiles;
  }
  style.layers = [];

  if (localSprites) {
    style.sprite = `${location.protocol}//${location.host}/${theme}`;
  } else {
    style.sprite = `https://protomaps.github.io/basemaps-assets/sprites/v3/${theme}`;
  }

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
    const pair = language_script_pairs.find((d) => d.lang === lang);
    const script = pair === undefined ? "Latin" : pair.script;
    style.layers = style.layers.concat(
      layers("protomaps", theme, lang, script),
    );
  }
  return style;
}

function StyleJsonPane(props: { theme: string; lang: string }) {
  const stringified = JSON.stringify(
    getMaplibreStyle(
      props.theme,
      props.lang,
      false,
      "https://example.com/tiles.json",
    ),
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
  lang: string;
  localSprites: boolean;
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
        true,
      );
    }

    const protocol = new pmtiles.Protocol();
    protocolRef.current = protocol;
    maplibregl.addProtocol("pmtiles", protocol.tile);

    const map = new maplibregl.Map({
      hash: "map",
      container: "map",
      style: getMaplibreStyle("", "de", false),
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

    map.on("contextmenu", (e) => {
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
  }, []);

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
            props.lang,
            props.localSprites,
            props.tiles,
            props.npmLayers,
            props.droppedArchive,
            minZoom,
            maxZoom,
          ),
        );
      }
    })();
  }, [
    props.tiles,
    props.theme,
    props.lang,
    props.localSprites,
    props.npmLayers,
    props.droppedArchive,
  ]);

  return <div id="map" />;
}

// if no tiles are passed, loads the latest daily build.
export default function MapViewComponent() {
  const hash = parseHash(location.hash);
  const [theme, setTheme] = useState<string>(hash.theme || "light");
  const [lang, setLang] = useState<string>(hash.lang || "en");
  const [tiles, setTiles] = useState<string | undefined>(hash.tiles);
  const [localSprites, setLocalSprites] = useState<boolean>(
    hash.local_sprites === "true",
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
      lang: lang,
      tiles: tiles,
      local_sprites: localSprites ? "true" : undefined,
      npm_version: publishedStyleVersion,
    };
    location.hash = createHash(location.hash, record);
  });

  const onDrop = useCallback((acceptedFiles: File[]) => {
    setDroppedArchive(new PMTiles(new FileSource(acceptedFiles[0])));
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

  language_script_pairs.sort((a, b) => a.full_name.localeCompare(b.full_name));

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
        <select onChange={(e) => setLang(e.target.value)} value={lang}>
          {language_script_pairs.map((pair) => (
            <option key={pair.lang} value={pair.lang}>
              {pair.full_name}
            </option>
          ))}
        </select>
        <input
          id="localSprites"
          type="checkbox"
          checked={localSprites}
          onChange={(e) => setLocalSprites(e.currentTarget.checked)}
        />
        <label htmlFor="localSprites">local sprites</label>
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
        <MapLibreView
          tiles={tiles}
          localSprites={localSprites}
          theme={theme}
          lang={lang}
          npmLayers={npmLayers}
          droppedArchive={droppedArchive}
        />
        {showStyleJson && <StyleJsonPane theme={theme} lang={lang} />}
      </div>
    </div>
  );
}
