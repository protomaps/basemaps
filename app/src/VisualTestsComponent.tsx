import { useRef, useEffect, useState } from "react";
import rawExamples from "./examples.json";
import maplibregl from "maplibre-gl";
import * as pmtiles from "pmtiles";
import layers from "../../styles/src/index.ts";

// @ts-ignore
import pixelmatch from "pixelmatch";
import "maplibre-gl/dist/maplibre-gl.css";

const drawToCanvas = (ctx: CanvasRenderingContext2D, data: string) => {
  const img = new Image();
  const promise = new Promise<void>((resolve) => {
    img.onload = function () {
      ctx.drawImage(img, 0, 0);
      resolve();
    };
  });
  img.src = data;
  return promise;
};

const moveAndRenderMap = (
  map: maplibregl.Map,
  center: [number, number],
  zoom: number,
): Promise<string> => {
  return new Promise<string>((resolve) => {
    map.on("idle", () => {
      const canvas = map.getCanvas();
      resolve(canvas.toDataURL());
    });
    map.jumpTo({
      center: center,
      zoom: zoom,
    });
  });
};

const createMap = (
  container: HTMLElement,
  url: string,
  center: [number, number],
  zoom: number,
) => {
  return new maplibregl.Map({
    container: container,
    interactive: false,
    style: {
      version: 8,
      center: center,
      zoom: zoom,
      glyphs: "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
      sources: {
        protomaps: {
          type: "vector",
          url: "pmtiles://" + url,
          attribution: "",
        },
      },
      layers: layers("protomaps", "light"),
    },
  });
};

interface RenderState {
  leftMap: maplibregl.Map;
  rightMap: maplibregl.Map;
  leftCtx: CanvasRenderingContext2D;
  rightCtx: CanvasRenderingContext2D;
  diffCtx: CanvasRenderingContext2D;
  diffCanvas: HTMLCanvasElement;
}

interface RenderingResult {
  pixelsDifferent: number;
  leftData: string;
  rightData: string;
  diffData: string;
}

interface Example {
  name: string;
  description: string;
  tags: string[];
  center: [number, number];
  zoom: number;
}

interface ExampleResult {
  example: Example;
  rendered: RenderingResult;
}

const QUERY_PARAMS = new URLSearchParams(location.search);
// const SHOW_DIFFERENCES_ONLY = QUERY_PARAMS.get("showDifferencesOnly");
const DIM = 500 * window.devicePixelRatio;

const runExample = async (
  state: RenderState,
  example: Example,
): Promise<RenderingResult> => {
  const [left_data, right_data] = await Promise.all([
    moveAndRenderMap(state.leftMap, example.center, example.zoom),
    moveAndRenderMap(state.rightMap, example.center, example.zoom),
  ]);

  await drawToCanvas(state.leftCtx, left_data);
  await drawToCanvas(state.rightCtx, right_data);

  const diff = state.diffCtx.createImageData(DIM, DIM);
  const pixelsDifferent = pixelmatch(
    state.leftCtx.getImageData(0, 0, DIM, DIM).data,
    state.rightCtx.getImageData(0, 0, DIM, DIM).data,
    diff.data,
    DIM,
    DIM,
    { threshold: 0.1 },
  );
  state.diffCtx.putImageData(diff, 0, 0);

  return {
    pixelsDifferent: pixelsDifferent,
    leftData: left_data,
    rightData: right_data,
    diffData: state.diffCanvas.toDataURL(),
  };
};

const linkTo = (props: { name?: string; tag?: string }) => {
  const q = new URLSearchParams(QUERY_PARAMS);
  q.delete("name");
  q.delete("tag");
  if (props.name) {
    q.set("name", props.name);
  }
  if (props.tag) {
    q.set("tag", props.tag);
  }
  return "/visualtests/?" + q.toString();
};

// TODO: this only compares tilesets, make it compare styles
function ExampleComponent(props: { result: ExampleResult }) {
  const leftRef = useRef<HTMLImageElement | null>(null);
  const rightRef = useRef<HTMLImageElement | null>(null);
  const diffRef = useRef<HTMLImageElement | null>(null);

  useEffect(() => {
    leftRef.current!.src = props.result.rendered.leftData;
    rightRef.current!.src = props.result.rendered.rightData;
    diffRef.current!.src = props.result.rendered.diffData;
  }, []);

  const example = props.result.example;

  return (
    <div>
      <div>
        <img ref={leftRef} />
        <img ref={rightRef} />
        <img ref={diffRef} />
      </div>
      <a href={linkTo({ name: example.name })}>{example.name}</a>
      <span>{example.description}</span>
      {example.tags.map((tag: string) => (
        <a href={linkTo({ tag: tag })} key={tag}>
          {tag}
        </a>
      ))}
    </div>
  );
}

export default function VisualTestsComponent() {
  const mapLeftContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRightContainerRef = useRef<HTMLDivElement | null>(null);
  const canvasLeftRef = useRef<HTMLCanvasElement | null>(null);
  const canvasRightRef = useRef<HTMLCanvasElement | null>(null);
  const canvasDiffRef = useRef<HTMLCanvasElement | null>(null);
  const [results, setResults] = useState<ExampleResult[]>([]);

  const runExamples = async (renderState: RenderState, examples: Example[]) => {
    for (const example of examples) {
      const rendered = await runExample(renderState, example);

      setResults((currentResults: ExampleResult[]) => {
        return [
          ...currentResults,
          {
            example: example,
            rendered: rendered,
          },
        ];
      });
    }
  };

  useEffect(() => {
    if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
      maplibregl.setRTLTextPlugin(
        "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
        () => {},
        false, // we need to pre-load this, otherwise diffs will be flaky
      );
    }

    const protocol = new pmtiles.Protocol();
    maplibregl.addProtocol("pmtiles", protocol.tile);

    // const left = queryParams.get("left");
    // const right = queryParams.get("right");
    const name = QUERY_PARAMS.get("name");
    const tag = QUERY_PARAMS.get("tag");

    // get all JSONs first - we don't want to initialize the map without a starting position
    let examples: Example[] = rawExamples as any;
    if (name !== null) {
      examples = examples.filter((e) => e.name === name);
    } else if (tag !== null) {
      examples = examples.filter((e) => e.tags.indexOf(tag) >= 0);
    }
    const example = examples[0];

    // create two map instances:
    // one for each version, so we don't have to re-initialize the map on changing view.
    const leftMap = createMap(
      mapLeftContainerRef.current!,
      "https://build.protomaps.com/20230915.pmtiles",
      example.center,
      example.zoom,
    );

    const rightMap = createMap(
      mapRightContainerRef.current!,
      "https://build.protomaps.com/20230915.pmtiles",
      example.center,
      example.zoom,
    );

    const leftCanvas = canvasLeftRef.current!;
    leftCanvas.width = DIM;
    leftCanvas.height = DIM;
    const rightCanvas = canvasRightRef.current!;
    rightCanvas.width = DIM;
    rightCanvas.height = DIM;
    const diffCanvas = canvasDiffRef.current!;
    diffCanvas.width = DIM;
    diffCanvas.height = DIM;

    const renderState: RenderState = {
      leftMap: leftMap,
      rightMap: rightMap,
      leftCtx: leftCanvas.getContext("2d", { willReadFrequently: true })!,
      rightCtx: rightCanvas.getContext("2d", { willReadFrequently: true })!,
      diffCtx: diffCanvas.getContext("2d", { willReadFrequently: true })!,
      diffCanvas: diffCanvas,
    };

    runExamples(renderState, examples);

    return () => {
      maplibregl.removeProtocol("pmtiles");
      leftMap.remove();
      rightMap.remove();
    };
  }, []);

  return (
    <div className="visual-tests">
      <div style={{ position: "absolute", opacity: 0.0, zIndex: -1 }}>
        <div ref={mapLeftContainerRef} className="map"></div>
        <div ref={mapRightContainerRef} className="map"></div>
        <canvas ref={canvasLeftRef}></canvas>
        <canvas ref={canvasRightRef}></canvas>
        <canvas ref={canvasDiffRef}></canvas>
      </div>
      {results.map((result: ExampleResult) => (
        <ExampleComponent key={result.example.name} result={result} />
      ))}
    </div>
  );
}
