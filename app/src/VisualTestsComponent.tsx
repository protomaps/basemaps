import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import maplibregl from "maplibre-gl";
import * as pmtiles from "pmtiles";
import { useEffect, useRef, useState } from "react";
import layers from "../../styles/src/index.ts";
import rawExamples from "./examples.json";

import "maplibre-gl/dist/maplibre-gl.css";
// @ts-ignore
import pixelmatch from "pixelmatch";

const drawToCanvas = (ctx: CanvasRenderingContext2D, data: string) => {
  const img = new Image();
  const promise = new Promise<void>((resolve) => {
    img.onload = () => {
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
  layers: LayerSpecification[],
) => {
  return new maplibregl.Map({
    container: container,
    interactive: false,
    style: {
      version: 8,
      center: center,
      zoom: zoom,
      glyphs:
        "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
      sources: {
        protomaps: {
          type: "vector",
          url: `pmtiles://${url}`,
          attribution: "",
        },
      },
      layers: layers,
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
  return `/visualtests/?${q.toString()}`;
};

const layersForVersion = async (version: string) => {
  const resp = await fetch(
    `https://unpkg.com/protomaps-themes-base@${version}/dist/layers/light.json`,
  );
  return await resp.json();
};

const latestVersion = async () => {
  const resp = await fetch("https://registry.npmjs.org/protomaps-themes-base", {
    headers: { Accept: "application/vnd.npm.install-v1+json" },
  });
  return (await resp.json())["dist-tags"].latest;
};

function ExampleComponent(props: { result: ExampleResult }) {
  const leftRef = useRef<HTMLImageElement | null>(null);
  const rightRef = useRef<HTMLImageElement | null>(null);
  const diffRef = useRef<HTMLImageElement | null>(null);

  useEffect(() => {
    if (!leftRef.current || !rightRef.current || !diffRef.current) {
      console.error("DOM element not initialized");
      return;
    }

    leftRef.current.src = props.result.rendered.leftData;
    rightRef.current.src = props.result.rendered.rightData;
    diffRef.current.src = props.result.rendered.diffData;
  }, [props.result]);

  const example = props.result.example;

  return (
    <div className="example">
      <div>
        <img alt="left" ref={leftRef} />
        <img alt="right" ref={rightRef} />
        <img alt="diff" ref={diffRef} />
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
  const mapLeftRef = useRef<maplibregl.Map | null>(null);
  const mapRightRef = useRef<maplibregl.Map | null>(null);
  const canvasLeftRef = useRef<HTMLCanvasElement | null>(null);
  const canvasRightRef = useRef<HTMLCanvasElement | null>(null);
  const canvasDiffRef = useRef<HTMLCanvasElement | null>(null);
  const [results, setResults] = useState<ExampleResult[]>([]);
  const [displayInfo, setDisplayInfo] = useState<string[]>([
    "-",
    "-",
    "-",
    "-",
  ]);

  useEffect(() => {
    const runExamples = async () => {
      if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
        maplibregl.setRTLTextPlugin(
          "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
          false, // we need to pre-load this, otherwise diffs will be flaky
        );
      }

      const protocol = new pmtiles.Protocol();
      maplibregl.addProtocol("pmtiles", protocol.tile);

      // the tileset defaults to the latest daily build
      const builds = await (
        await fetch("https://build-metadata.protomaps.dev/builds.json")
      ).json();
      const last_build = `https://build.protomaps.com/${
        builds[builds.length - 1].key
      }`;
      const leftTiles = QUERY_PARAMS.get("leftTiles") || last_build;
      const rightTiles = QUERY_PARAMS.get("rightTiles") || last_build;

      // the left style defaults to the latest published NPM version
      // the right style is the main branch (GitHub Pages) or local development
      const leftLayersStr =
        QUERY_PARAMS.get("leftStyle") || (await latestVersion());
      const rightLayersStr = QUERY_PARAMS.get("rightStyle");

      const leftLayers = await layersForVersion(leftLayersStr);
      const rightLayers = rightLayersStr
        ? await layersForVersion(rightLayersStr)
        : layers("protomaps", "light", "en", "Latin");

      setDisplayInfo([
        leftTiles,
        rightTiles,
        leftLayersStr || "npm@latest",
        rightLayersStr || "local/main",
      ]);

      // filter the visual tests
      const name = QUERY_PARAMS.get("name");
      const tag = QUERY_PARAMS.get("tag");

      // get all JSONs first - we don't want to initialize the map without a starting position
      let examples: Example[] = rawExamples as Example[];
      if (name !== null) {
        examples = examples.filter((e) => e.name === name);
      } else if (tag !== null) {
        examples = examples.filter((e) => e.tags.indexOf(tag) >= 0);
      }
      const example = examples[0];

      if (
        !mapLeftContainerRef.current ||
        !mapRightContainerRef.current ||
        !canvasLeftRef.current ||
        !canvasRightRef.current ||
        !canvasDiffRef.current
      ) {
        console.error("Page failed to initialize");
        return;
      }

      // create two map instances:
      // one for each version, so we don't have to re-initialize the map on changing view.
      mapLeftRef.current = createMap(
        mapLeftContainerRef.current,
        leftTiles,
        example.center,
        example.zoom,
        leftLayers,
      );

      mapRightRef.current = createMap(
        mapRightContainerRef.current,
        rightTiles,
        example.center,
        example.zoom,
        rightLayers,
      );

      const leftCanvas = canvasLeftRef.current;
      leftCanvas.width = DIM;
      leftCanvas.height = DIM;
      const rightCanvas = canvasRightRef.current;
      rightCanvas.width = DIM;
      rightCanvas.height = DIM;
      const diffCanvas = canvasDiffRef.current;
      diffCanvas.width = DIM;
      diffCanvas.height = DIM;

      const leftCtx = leftCanvas.getContext("2d", { willReadFrequently: true });
      const rightCtx = rightCanvas.getContext("2d", {
        willReadFrequently: true,
      });
      const diffCtx = diffCanvas.getContext("2d", { willReadFrequently: true });

      if (!leftCtx || !rightCtx || !diffCtx) {
        console.error("Canvas failed to initialize");
        return;
      }

      const renderState: RenderState = {
        leftMap: mapLeftRef.current,
        rightMap: mapRightRef.current,
        leftCtx: leftCtx,
        rightCtx: rightCtx,
        diffCtx: diffCtx,
        diffCanvas: diffCanvas,
      };

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

    runExamples();

    return () => {
      maplibregl.removeProtocol("pmtiles");
      if (mapLeftRef.current) {
        mapLeftRef.current.remove();
      }
      if (mapRightRef.current) {
        mapRightRef.current.remove();
      }
    };
  }, []);

  return (
    <div className="visual-tests">
      <div style={{ position: "absolute", opacity: 0.0, zIndex: -1 }}>
        <div ref={mapLeftContainerRef} className="map" />
        <div ref={mapRightContainerRef} className="map" />
        <canvas ref={canvasLeftRef} />
        <canvas ref={canvasRightRef} />
        <canvas ref={canvasDiffRef} />
      </div>
      <div style={{ width: "500px", display: "inline-block" }}>
        {displayInfo[0]}
        <br />
        {displayInfo[2]}
      </div>
      <div style={{ width: "500px", display: "inline-block" }}>
        {displayInfo[1]}
        <br />
        {displayInfo[3]}
      </div>
      {results.map((result: ExampleResult) => (
        <ExampleComponent key={result.example.name} result={result} />
      ))}
    </div>
  );
}
