/* @refresh reload */
import { render } from "solid-js/web";
import "./index.css";
import type { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import maplibregl from "maplibre-gl";
import * as pmtiles from "pmtiles";
import { For, createEffect, createSignal, onMount } from "solid-js";
import layers from "../../styles/src/index.ts";
import Nav from "./Nav";
import rawExamples from "./examples.json";
import { layersForVersion } from "./utils";
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
      sprite: "https://protomaps.github.io/basemaps-assets/sprites/v4/light",
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

interface DisplayInfo {
  leftTiles: string;
  rightTiles: string;
  leftLayersStr: string;
  rightLayersStr: string;
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

const latestVersion = async () => {
  const resp = await fetch("https://registry.npmjs.org/protomaps-themes-base", {
    headers: { Accept: "application/vnd.npm.install-v1+json" },
  });
  return (await resp.json())["dist-tags"].latest;
};

function ExampleComponent(props: { result: ExampleResult }) {
  let leftRef: HTMLImageElement | undefined;
  let rightRef: HTMLImageElement | undefined;
  let diffRef: HTMLImageElement | undefined;

  createEffect(() => {
    if (!leftRef || !rightRef || !diffRef) {
      console.error("DOM element not initialized");
      return;
    }

    leftRef.src = props.result.rendered.leftData;
    rightRef.src = props.result.rendered.rightData;
    diffRef.src = props.result.rendered.diffData;
  });

  const example = props.result.example;

  return (
    <div class="mt-8">
      <div>
        <img
          alt="left"
          ref={leftRef}
          class="inline-block w-[500px] h-[500px]"
        />
        <img
          alt="right"
          ref={rightRef}
          class="inline-block w-[500px] h-[500px]"
        />
        <img
          alt="diff"
          ref={diffRef}
          class="inline-block w-[500px] h-[500px]"
        />
      </div>
      <div class="space-x-4">
        <a class="underline" href={linkTo({ name: example.name })}>
          {example.name}
        </a>
        <a
          class="underline"
          href={`/map/#map=${example.zoom}/${example.center[1]}/${example.center[0]}`}
          target="_blank"
          rel="noreferrer"
        >
          (map)
        </a>
        <span>{example.description}</span>
        <For each={example.tags}>
          {(tag: string) => (
            <a class="underline" href={linkTo({ tag: tag })}>
              {tag}
            </a>
          )}
        </For>
      </div>
    </div>
  );
}

function VisualTests() {
  let mapLeftContainerRef: HTMLDivElement | undefined;
  let mapRightContainerRef: HTMLDivElement | undefined;
  let mapLeftRef: maplibregl.Map | undefined;
  let mapRightRef: maplibregl.Map | undefined;
  let canvasLeftRef: HTMLCanvasElement | undefined;
  let canvasRightRef: HTMLCanvasElement | undefined;
  let canvasDiffRef: HTMLCanvasElement | undefined;
  const [results, setResults] = createSignal<ExampleResult[]>([]);
  const [displayInfo, setDisplayInfo] = createSignal<DisplayInfo>({
    leftTiles: "",
    rightTiles: "",
    leftLayersStr: "",
    rightLayersStr: "",
  });

  onMount(() => {
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

      setDisplayInfo({
        leftTiles,
        rightTiles,
        leftLayersStr: leftLayersStr || "npm@latest",
        rightLayersStr: rightLayersStr || "local/main",
      });

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
        !mapLeftContainerRef ||
        !mapRightContainerRef ||
        !canvasLeftRef ||
        !canvasRightRef ||
        !canvasDiffRef
      ) {
        console.error("Page failed to initialize");
        return;
      }

      // create two map instances:
      // one for each version, so we don't have to re-initialize the map on changing view.
      mapLeftRef = createMap(
        mapLeftContainerRef,
        leftTiles,
        example.center,
        example.zoom,
        leftLayers,
      );

      mapRightRef = createMap(
        mapRightContainerRef,
        rightTiles,
        example.center,
        example.zoom,
        rightLayers,
      );

      const leftCanvas = canvasLeftRef;
      leftCanvas.width = DIM;
      leftCanvas.height = DIM;
      const rightCanvas = canvasRightRef;
      rightCanvas.width = DIM;
      rightCanvas.height = DIM;
      const diffCanvas = canvasDiffRef;
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
        leftMap: mapLeftRef,
        rightMap: mapRightRef,
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
      if (mapLeftRef) {
        mapLeftRef.remove();
      }
      if (mapRightRef) {
        mapRightRef.remove();
      }
    };
  });

  return (
    <div class="flex flex-col h-screen w-full">
      <Nav page={2} />
      <div class="w-[1500px] mx-auto">
        <h1 class="my-8 text-4xl">Visual Tests</h1>
        <div class="inline-block w-[500px] font-mono text-xs">
          leftTiles={displayInfo().leftTiles}
          <br />
          leftStyle={displayInfo().leftLayersStr}
        </div>
        <div class="inline-block w-[500px] font-mono text-xs">
          rightTiles={displayInfo().rightTiles}
          <br />
          rightStyle={displayInfo().rightLayersStr}
        </div>
        <For each={results()}>
          {(result: ExampleResult) => <ExampleComponent result={result} />}
        </For>
        <div class="h-0 overflow-hidden">
          <div ref={mapLeftContainerRef} class="w-[500px] h-[500px]" />
          <div ref={mapRightContainerRef} class="w-[500px] h-[500px]" />
          <canvas ref={canvasLeftRef} class="w-[500px] h-[500px]" />
          <canvas ref={canvasRightRef} class="w-[500px] h-[500px]" />
          <canvas ref={canvasDiffRef} class="w-[500px] h-[500px]" />
        </div>
      </div>
    </div>
  );
}

const root = document.getElementById("root");

if (root) {
  render(() => <VisualTests />, root);
}
