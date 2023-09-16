import { useEffect, useState, useRef } from "react";
import layers from "../../styles/src/index.ts";
import maplibregl from "maplibre-gl";
import * as pmtiles from "pmtiles";
import "maplibre-gl/dist/maplibre-gl.css";
import "ol/ol.css";
import { Map, View } from "ol";
import VectorTile from "ol/layer/VectorTile";

// @ts-ignore
import { PMTilesVectorSource } from "ol-pmtiles";
import { useGeographic } from 'ol/proj';
import { stylefunction } from 'ol-mapbox-style';

function getMapLibreStyle(tiles: string, theme: string):any {
  return {
    version: 8 as any,
    glyphs: "https://cdn.protomaps.com/fonts/pbf/{fontstack}/{range}.pbf",
    sources: {
      protomaps: {
        type: "vector",
        url: tiles,
        attribution:
          '© <a href="https://openstreetmap.org">OpenStreetMap</a>',
      },
    },
    layers: layers("protomaps", theme)
  } 
}

function StyleJsonPane(props: {theme: string}) {

  // TODO: not working for OpenLayers
  const stringified = JSON.stringify(getMapLibreStyle("https://example.com/tiles.json", props.theme),null,4);

  return <div className="stylePane">
    { stringified }
  </div>
}

function MapLibreView(props: { tiles: string; theme: string }) {
  let mapRef = useRef<maplibregl.Map>()

  useEffect(() => {
    if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
      maplibregl.setRTLTextPlugin(
        "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
        () => {},
        true,
      );
    }

    let protocol = new pmtiles.Protocol();
    maplibregl.addProtocol("pmtiles", protocol.tile);

    const map = new maplibregl.Map({
      hash: true,
      container: "map",
      style: getMapLibreStyle(props.tiles, props.theme)
    });

    map.on("mousedown", function (e) {
      map.queryRenderedFeatures(e.point).map((feat) => {
        console.log(feat);
      });
    });

    mapRef.current = map

    return () => {
      maplibregl.removeProtocol("pmtiles");
      map.remove();
    };
  }, []);

  useEffect(() => {
    if (mapRef.current) {
      mapRef.current.setStyle(getMapLibreStyle(props.tiles, props.theme))
    }
  },[props.theme])

  return <div id="map"></div>;
}

// TODO: does not sync map hash state
function OpenLayersView(props: {tiles: string, theme: string}) {
  console.log(props)


  useEffect(() => {
    useGeographic();

    const layer = new VectorTile({
      declutter: true,
      source: new PMTilesVectorSource({
        url: "https://r2-public.protomaps.com/protomaps-sample-datasets/protomaps-basemap-opensource-20230408.pmtiles",
        attributions: ["© OpenStreetMap"]
      }), 
      style: null
    });

    stylefunction(layer, {
      version: "8",
      layers:layers("protomaps",props.theme),
      sources: {protomaps: {type: "vector"}}
    }, 'protomaps');

    new Map({
      target: "map",
      layers: [layer],
      view: new View({
        center: [0,0],
        zoom: 0,
      }),
    });
  }, []) 

  return <div id="map"></div>
}

export default function MapView() {
  let DEFAULT_TILES =
    "pmtiles://https://r2-public.protomaps.com/protomaps-sample-datasets/protomaps-basemap-opensource-20230408.pmtiles";

  const params = new URLSearchParams(location.search);
  const [theme, setTheme] = useState<string>(params.get("theme") || "light");
  const [tiles, setTiles] = useState<string>(params.get("tiles") || DEFAULT_TILES);
  const [renderer, setRenderer] = useState<string>(params.get("renderer") || "maplibregl");
  const [showStyleJson, setShowStyleJson] = useState<boolean>(false);

  console.log(setTiles);
  // TODO: dynamic import of https://unpkg.com/protomaps-themes-base@1.3.1/dist/index.js etc
  // TODO: language tag selector

  return (
    <div className="map-container">
      <nav>
        <select onChange={e => setTheme(e.target.value)} value={theme}>
          <option value="light">base light</option>
          <option value="dark">base dark</option>
          <option value="white">data light</option>
          <option value="grayscale">data grayscale</option>
          <option value="black">data dark</option>
        </select>
        <select onChange={e => setRenderer(e.target.value)} value={renderer}>
          <option value="maplibregl">maplibregl</option>
          <option value="openlayers">openlayers</option>
        </select>
        <button onClick={() => setShowStyleJson(!showStyleJson)}>get style JSON</button>
      </nav>
      <div className="split">
        { renderer == "maplibregl" ? 
          <MapLibreView tiles={tiles} theme={theme}/> : 
          <OpenLayersView tiles={tiles} theme={theme}/>
        }
        { showStyleJson && <StyleJsonPane theme={theme}/> }
      </div>
    </div>
  );
}
