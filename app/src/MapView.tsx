/* @refresh reload */
import { render } from "solid-js/web";
import "./index.css";
import MaplibreInspect from "@maplibre/maplibre-gl-inspect";
import "@maplibre/maplibre-gl-inspect/dist/maplibre-gl-inspect.css";
import maplibregl from "maplibre-gl";
import type { MapGeoJSONFeature, StyleSpecification } from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";
import type { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import { FileSource, PMTiles, Protocol } from "pmtiles";
import {
  For,
  type JSX,
  Show,
  createEffect,
  createMemo,
  createSignal,
  onMount,
} from "solid-js";
import layers from "../../styles/src/index.ts";
import { language_script_pairs } from "../../styles/src/language.ts";
import Nav from "./Nav";
import {
  createHash,
  isValidPMTiles,
  layersForVersion,
  parseHash,
} from "./utils";

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
    <div class="features-properties">
      <For each={props.features}>
        {(f) => (
          <div>
            <span>
              <strong>{getSourceLayer(f.layer)}</strong>
              <span> ({f.geometry.type})</span>
            </span>
            <table>
              <tbody>
                <tr>
                  <td>id</td>
                  <td>{f.id}</td>
                </tr>
                <For each={Object.entries(f.properties)}>
                  {([key, value]) => (
                    <tr>
                      <td>{key}</td>
                      <td>{value}</td>
                    </tr>
                  )}
                </For>
              </tbody>
            </table>
          </div>
        )}
      </For>
    </div>
  );
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
    style.sprite = `https://protomaps.github.io/basemaps-assets/sprites/v4/${theme}`;
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
    style.layers = style.layers.concat(layers("protomaps", theme, lang));
  }
  return style;
}

function StyleJsonPane(props: { theme: string; lang: string }) {
  const stringified = createMemo(() => {
    return JSON.stringify(
      getMaplibreStyle(
        props.theme,
        props.lang,
        false,
        "https://example.com/tiles.json",
      ),
      null,
      4,
    );
  });

  return (
    <div class="w-1/2 overflow-x-scroll overflow-y-scroll p-2">
      <button
        type="button"
        class="btn-primary"
        onClick={() => {
          navigator.clipboard.writeText(stringified());
        }}
      >
        Copy to clipboard
      </button>
      <textarea readonly class="text-xs mt-4 h-full w-full">
        {stringified()}
      </textarea>
    </div>
  );
}

function MapLibreView(props: {
  theme: string;
  lang: string;
  localSprites: boolean;
  showBoxes: boolean;
  tiles?: string;
  npmLayers: LayerSpecification[];
  droppedArchive?: PMTiles;
}) {
  let mapContainer: HTMLDivElement | undefined;
  let mapRef: maplibregl.Map | undefined;
  let protocolRef: Protocol | undefined;
  let hiddenRef: HTMLDivElement | undefined;

  onMount(() => {
    if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
      maplibregl.setRTLTextPlugin(
        "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
        true,
      );
    }

    if (!mapContainer) {
      console.error("Could not mount map element");
      return;
    }

    const protocol = new Protocol({ metadata: true });
    protocolRef = protocol;
    maplibregl.addProtocol("pmtiles", protocol.tile);

    const map = new maplibregl.Map({
      hash: "map",
      container: mapContainer,
      style: getMaplibreStyle("", "en", false),
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

    map.addControl(
      new MaplibreInspect({
        popup: new maplibregl.Popup({
          closeButton: false,
          closeOnClick: false,
        }),
      }),
    );

    const popup = new maplibregl.Popup({
      closeButton: true,
      closeOnClick: false,
      maxWidth: "none",
    });

    map.on("contextmenu", (e) => {
      const features = map.queryRenderedFeatures(e.point);
      if (hiddenRef && features.length) {
        render(() => <FeaturesProperties features={features} />, hiddenRef);
        popup.setHTML(hiddenRef.innerHTML);
        popup.setLngLat(e.lngLat);
        popup.addTo(map);
      } else {
        popup.remove();
      }
    });

    mapRef = map;

    return () => {
      protocolRef = undefined;
      maplibregl.removeProtocol("pmtiles");
      map.remove();
    };
  });

  createEffect(() => {
    if (protocolRef) {
      const archive = props.droppedArchive;
      if (archive) {
        protocolRef.add(archive);
        (async () => {
          const header = await archive.getHeader();
          mapRef?.fitBounds(
            [
              [header.minLon, header.minLat],
              [header.maxLon, header.maxLat],
            ],
            { animate: false },
          );
        })();
      }
    }
  });

  createEffect(() => {
    if (mapRef) {
      mapRef.showTileBoundaries = props.showBoxes;
      mapRef.showCollisionBoxes = props.showBoxes;
    }
  });

  createEffect(() => {
    (async () => {
      if (mapRef) {
        let minZoom: number | undefined;
        let maxZoom: number | undefined;
        if (props.droppedArchive) {
          const header = await props.droppedArchive.getHeader();
          minZoom = header.minZoom;
          maxZoom = header.maxZoom;
        }
        mapRef.setStyle(
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
  });

  return (
    <>
      <div class="hidden" ref={hiddenRef} />
      <div ref={mapContainer} class="h-100 w-full flex" />
    </>
  );
}

function MapView() {
  const hash = parseHash(location.hash);
  const [theme, setTheme] = createSignal<string>(hash.theme || "light");
  const [lang, setLang] = createSignal<string>(hash.lang || "en");
  const [tiles, setTiles] = createSignal<string>(
    hash.tiles || "https://demo-bucket.protomaps.com/v4.pmtiles",
  );
  const [localSprites, setLocalSprites] = createSignal<boolean>(
    hash.local_sprites === "true",
  );
  const [showBoxes, setShowBoxes] = createSignal<boolean>(
    hash.show_boxes === "true",
  );
  const [showStyleJson, setShowStyleJson] = createSignal<boolean>(false);
  const [publishedStyleVersion, setPublishedStyleVersion] = createSignal<
    string | undefined
  >(hash.npm_version);
  const [knownNpmVersions, setKnownNpmVersions] = createSignal<string[]>([]);
  const [npmLayers, setNpmLayers] = createSignal<LayerSpecification[]>([]);
  const [droppedArchive, setDroppedArchive] = createSignal<PMTiles>();

  createEffect(() => {
    const record = {
      theme: theme(),
      lang: lang(),
      tiles: tiles(),
      local_sprites: localSprites() ? "true" : undefined,
      show_boxes: showBoxes() ? "true" : undefined,
      npm_version: publishedStyleVersion(),
    };
    location.hash = createHash(location.hash, record);
  });

  const drop: JSX.EventHandler<HTMLDivElement, DragEvent> = (event) => {
    if (event.dataTransfer) {
      setDroppedArchive(
        new PMTiles(new FileSource(event.dataTransfer.files[0])),
      );
    }
  };

  const dragover: JSX.EventHandler<HTMLDivElement, Event> = (event) => {
    event.preventDefault();
    return false;
  };

  const handleKeyPress: JSX.EventHandler<HTMLDivElement, KeyboardEvent> = (
    event,
  ) => {
    const c = event.charCode;
    if (c >= 49 && c <= 53) {
      setTheme(["light", "dark", "white", "grayscale", "black"][c - 49]);
    }
  };

  const loadTiles: JSX.EventHandler<HTMLFormElement, Event> = (event) => {
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

  createEffect(() => {
    (async () => {
      const psv = publishedStyleVersion();
      if (psv === undefined) {
        setNpmLayers([]);
      } else {
        setNpmLayers(await layersForVersion(psv, theme()));
      }
    })();
  });

  language_script_pairs.sort((a, b) => a.full_name.localeCompare(b.full_name));

  return (
    <div class="flex flex-col h-screen w-full">
      <Nav page={0} />
      <div class="max-w-[1500px] mx-auto">
        <form onSubmit={loadTiles} class="flex">
          <input
            class="border-2 border-gray p-1 flex-1 mr-2"
            type="text"
            name="tiles"
            value={tiles()}
            style={{ width: "50%" }}
          />
          <button class="btn-primary" type="submit">
            load
          </button>
        </form>
        <div class="my-2 space-x-2">
          <select onChange={(e) => setTheme(e.target.value)} value={theme()}>
            <option value="light">light</option>
            <option value="dark">dark</option>
            <option value="white">data viz (white)</option>
            <option value="grayscale">data viz (grayscale)</option>
            <option value="black">data viz (black)</option>
          </select>
          <select onChange={(e) => setLang(e.target.value)} value={lang()}>
            <For each={language_script_pairs}>
              {(pair) => (
                <option value={pair.lang}>
                  {pair.lang} ({pair.full_name})
                </option>
              )}
            </For>
          </select>
          <div class="hidden lg:inline">
            <input
              id="localSprites"
              type="checkbox"
              checked={localSprites()}
              onChange={(e) => setLocalSprites(e.currentTarget.checked)}
            />
            <label for="localSprites" class="text-xs ml-1">
              local sprites
            </label>
          </div>
          <div class="hidden lg:inline">
            <input
              id="showBoxes"
              type="checkbox"
              checked={showBoxes()}
              onChange={(e) => setShowBoxes(e.currentTarget.checked)}
            />
            <label for="showBoxes" class="text-xs ml-1">
              bboxes
            </label>
          </div>
          <Show
            when={knownNpmVersions().length > 0}
            fallback={
              <button
                class="btn-primary"
                type="button"
                onClick={loadVersionsFromNpm}
              >
                style version
              </button>
            }
          >
            <select
              onChange={(e) => setPublishedStyleVersion(e.target.value)}
              value={publishedStyleVersion()}
            >
              <option value="">main</option>
              <For each={knownNpmVersions()}>
                {(v: string) => <option value={v}>{v}</option>}
              </For>
            </select>
          </Show>
          <button
            type="button"
            class="btn-primary hidden lg:inline"
            onClick={() => setShowStyleJson(!showStyleJson())}
          >
            get style JSON
          </button>
        </div>
      </div>
      <div
        class="h-full flex"
        onKeyPress={handleKeyPress}
        ondragover={dragover}
        ondrop={drop}
      >
        <MapLibreView
          tiles={tiles()}
          localSprites={localSprites()}
          showBoxes={showBoxes()}
          theme={theme()}
          lang={lang()}
          npmLayers={npmLayers()}
          droppedArchive={droppedArchive()}
        />
        <Show when={showStyleJson()}>
          <StyleJsonPane theme={theme()} lang={lang()} />
        </Show>
      </div>
    </div>
  );
}

const root = document.getElementById("root");

if (root) {
  render(() => <MapView />, root);
}
