import { useState, useEffect, useRef, useCallback } from "react";
import "tachyons/css/tachyons.min.css";
import { Toner } from "../../extra/src/stamen/toner";
import transit from "../../extra/src/layers/transit";
import L from "leaflet";
import { leafletLayer, light, paintRules } from "protomaps";
import "leaflet/dist/leaflet.css";
import "leaflet.sync/L.Map.Sync.js";
import "leaflet-hash/leaflet-hash.js";

const GIT_SHA = (import.meta.env.VITE_GIT_SHA || "").substr(0, 8);

const DEMO_KEY = "1003762824b9687f"; // only works on gh-pages, get your own at https://protomaps.com
const VECTOR_TILES_URL =
  "https://api.protomaps.com/tiles/v2/{z}/{x}/{y}.pbf?key=" + DEMO_KEY;

interface Option {
  name: string;
  type: string;
  fn?: () => L.Layer;
  url?: string;
  attribution?: string;
}

const OPTIONS: Option[] = [
  {
    name: "default",
    type: "canvas",
    fn: () => {
      return leafletLayer({ url: VECTOR_TILES_URL });
    },
  },
  {
    name: "transit",
    type: "canvas",
    fn: () => {
      let rules = paintRules(light, "").concat(transit());
      return leafletLayer({ url: VECTOR_TILES_URL, paint_rules: rules });
    },
  },
  {
    name: "toner",
    type: "canvas",
    fn: () => {
      let [tasks, paint_rules, label_rules, attribution] = Toner("");
      return leafletLayer({
        url: VECTOR_TILES_URL,
        tasks: tasks,
        paint_rules: paint_rules,
        label_rules: label_rules,
        attribution: attribution,
      });
    },
  },
  {
    name: "toner-lite",
    type: "canvas",
    fn: () => {
      let [tasks, paint_rules, label_rules, attribution] = Toner("");
      return leafletLayer({
        url: VECTOR_TILES_URL,
        tasks: tasks,
        paint_rules: paint_rules,
        label_rules: label_rules,
        attribution: attribution,
      });
    },
  },
  {
    name: "osm-carto",
    type: "raster",
    url: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    attribution:
      '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
  },
  {
    name: "toner-raster",
    type: "raster",
    url: "https://stamen-tiles.a.ssl.fastly.net/toner/{z}/{x}/{y}@2x.png",
    attribution:
      'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>. Data by <a href="http://openstreetmap.org">OpenStreetMap</a>, under <a href="http://www.openstreetmap.org/copyright">ODbL</a>.',
  },
];

let setLayer = (map: L.Map, optionIdx: number) => {
  map.eachLayer((layer) => {
    map.removeLayer(layer);
  });
  let opt = OPTIONS[optionIdx];
  if (opt.type === "raster") {
    L.tileLayer(opt.url!, {
      attribution: opt.attribution,
    }).addTo(map);
  } else if (opt.type === "canvas") {
    let layer = opt.fn!();
    layer.addTo(map);
  }
};

let initialSelection = 0;
let initialCompareSelection = 0;
const paramMap = new URLSearchParams(location.search).get("map");
const paramCompare = new URLSearchParams(location.search).get("compare");

if (paramMap) {
  let idx = OPTIONS.findIndex((o) => o.name === paramMap);
  if (idx >= 0) initialSelection = idx;
}

if (paramCompare) {
  let idx = OPTIONS.findIndex((o) => o.name === paramCompare);
  if (idx >= 0) {
    initialCompareSelection = idx;
  }
}

function App() {
  var leftMap = useRef<L.Map | null>(null);
  let leftMapElement = useRef<HTMLDivElement | null>(null);
  var rightMap = useRef<L.Map | null>(null);
  let rightMapElement = useRef<HTMLDivElement | null>(null);
  let [compare, setCompare] = useState<boolean>(false);
  let [selection, setSelection] = useState<number>(initialSelection);
  let [compareSelection, setCompareSelection] = useState<number>(
    initialCompareSelection
  );
  let onChangeLeft = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelection(+e.target.value);
  };

  let onChangeRight = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setCompareSelection(+e.target.value);
  };

  useEffect(() => {
    leftMap.current = L.map(leftMapElement.current!).setView(
      [40.7239, -73.9814],
      12
    );
    (leftMap.current as any).addHash();

    rightMap.current = L.map(rightMapElement.current!, {
      doubleClickZoom: false,
      touchZoom: false,
      scrollWheelZoom: false,
      zoomControl: false,
    }).setView([0, 0], 0);
    rightMap.current.dragging.disable();
    (leftMap.current as any).sync(rightMap.current);

    return () => {
      if (leftMap.current) {
        leftMap.current.remove();
        (leftMap.current as any).removeHash();
      }
      if (rightMap.current) {
        rightMap.current.remove();
      }
    };
  }, []);

  useEffect(() => {
    if (leftMap.current) setLayer(leftMap.current, selection);
  }, [selection]);

  useEffect(() => {
    if (rightMap.current) setLayer(rightMap.current, compareSelection);
  }, [compareSelection]);

  useEffect(() => {
    const url = new URL(window.location.href);
    url.searchParams.set("map", OPTIONS[selection].name);
    url.searchParams.set("compare", OPTIONS[compareSelection].name);
    history.pushState(null, "", url.toString());
  }, [compare, selection, compareSelection]);

  return (
    <div id="app" className="flex sans-serif">
      <div className="flex flex-column vh-100 w-50">
        <div className="flex flex-grow-0 fw6 justify-between h3 items-center ph3">
          <span>protomaps-themes {GIT_SHA}</span>
          <span>
            <select onChange={onChangeLeft} value={selection}>
              {OPTIONS.map((option, i) => (
                <option value={i} key={i}>
                  {option.name}
                </option>
              ))}
            </select>
          </span>
          <span></span>
        </div>
        <div className="flex-grow h-100 flex" ref={leftMapElement}></div>
      </div>
      <div className="flex flex-column vh-100 w-50 bg-moon-gray">
        <div className="flex flex-grow-0 justify-between h3 items-center ph3">
          <span>
            <select onChange={onChangeRight} value={compareSelection}>
              {OPTIONS.map((option, i) => (
                <option value={i} key={i}>
                  {option.name}
                </option>
              ))}
            </select>
          </span>
        </div>
        <div className="flex-grow h-100 flex" ref={rightMapElement}></div>
      </div>
    </div>
  );
}

export default App;
