/* @refresh reload */
import { render } from "solid-js/web";
import "./index.css";
import type { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import maplibregl from "maplibre-gl";
import * as pmtiles from "pmtiles";
import { For, createEffect, createSignal, onMount } from "solid-js";
import { layers, namedFlavor } from "../../styles/src/index.ts";
import Nav from "./Nav";
import rawExamples from "./examples.json";
import { layersForVersion } from "./utils";
import "maplibre-gl/dist/maplibre-gl.css";
// @ts-ignore
import pixelmatch from "pixelmatch";

if (maplibregl.getRTLTextPluginStatus() === "unavailable") {
  maplibregl.setRTLTextPlugin(
    "https://unpkg.com/@mapbox/mapbox-gl-rtl-text@0.2.3/mapbox-gl-rtl-text.min.js",
    false, // we need to pre-load this, otherwise diffs will be flaky
  );
}

// Jumps the map and waits for idle, capturing a Blob of the rendered frame.
const moveAndCaptureBlob = (
  map: maplibregl.Map,
  center: [number, number],
  zoom: number,
): Promise<Blob> => {
  return new Promise<Blob>((resolve) => {
    map.once("idle", () => {
      map.getCanvas().toBlob((blob) => resolve(blob!));
    });
    map.jumpTo({ center, zoom });
  });
};

const canvasToObjectURL = (canvas: HTMLCanvasElement): Promise<string> => {
  return new Promise<string>((resolve) => {
    canvas.toBlob((blob) => resolve(URL.createObjectURL(blob!)));
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
  diffImageData: ImageData;
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
  const resp = await fetch("https://registry.npmjs.org/@protomaps/basemaps", {
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
          href={`/#map=${example.zoom}/${example.center[1]}/${example.center[0]}`}
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
      const protocol = new pmtiles.Protocol();
      maplibregl.addProtocol("pmtiles", protocol.tile);

      const rightLayersStr = QUERY_PARAMS.get("rightStyle");
      const buildsPromise = fetch(
        "https://build-metadata.protomaps.dev/builds.json",
      ).then((r) => r.json());
      const leftLayersStrPromise = QUERY_PARAMS.get("leftStyle")
        ? Promise.resolve(QUERY_PARAMS.get("leftStyle")!)
        : latestVersion();
      const rightLayersPromise = rightLayersStr
        ? layersForVersion(rightLayersStr)
        : Promise.resolve(layers("protomaps", namedFlavor("light"), { lang: "en" }));

      const leftLayersStr = await leftLayersStrPromise;

      const [builds, leftLayers, rightLayers] = await Promise.all([
        buildsPromise,
        layersForVersion(leftLayersStr),
        rightLayersPromise,
      ]);

      // the tileset defaults to the latest daily build
      const last_build = `https://build.protomaps.com/${
        builds[builds.length - 1].key
      }`;
      const leftTiles = QUERY_PARAMS.get("leftTiles") || last_build;
      const rightTiles = QUERY_PARAMS.get("rightTiles") || last_build;

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

      // Pre-allocate diff ImageData once; reused across all examples
      const diffImageData = new ImageData(DIM, DIM);

      const renderState: RenderState = {
        leftMap: mapLeftRef,
        rightMap: mapRightRef,
        leftCtx: leftCtx,
        rightCtx: rightCtx,
        diffCtx: diffCtx,
        diffCanvas: diffCanvas,
        diffImageData: diffImageData,
      };

      // Kick off the first render before the loop. The maps are already
      // positioned at examples[0] from createMap, so this jumpTo is redundant
      // but harmless and keeps the loop uniform.
      let leftNext = moveAndCaptureBlob(
        renderState.leftMap,
        examples[0].center,
        examples[0].zoom,
      );
      let rightNext = moveAndCaptureBlob(
        renderState.rightMap,
        examples[0].center,
        examples[0].zoom,
      );

      for (let i = 0; i < examples.length; i++) {
        const ex = examples[i];
        const nextEx = examples[i + 1] ?? null;

        // Wait for both maps to have captured their frame blob.
        // The blobs were snapshotted synchronously inside the idle handler,
        // so they always contain the correct rendered frame.
        const [leftBlob, rightBlob] = await Promise.all([leftNext, rightNext]);

        const leftUrl = URL.createObjectURL(leftBlob);
        const rightUrl = URL.createObjectURL(rightBlob);

        if (nextEx) {
          leftNext = moveAndCaptureBlob(
            renderState.leftMap,
            nextEx.center,
            nextEx.zoom,
          );
          rightNext = moveAndCaptureBlob(
            renderState.rightMap,
            nextEx.center,
            nextEx.zoom,
          );
        }

        // Decode blobs to ImageBitmap and draw to scratch canvases for pixelmatch.
        const [leftBitmap, rightBitmap] = await Promise.all([
          createImageBitmap(leftBlob),
          createImageBitmap(rightBlob),
        ]);
        renderState.leftCtx.drawImage(leftBitmap, 0, 0);
        leftBitmap.close();
        renderState.rightCtx.drawImage(rightBitmap, 0, 0);
        rightBitmap.close();

        // Run diff
        const leftImageData = renderState.leftCtx.getImageData(0, 0, DIM, DIM);
        const rightImageData = renderState.rightCtx.getImageData(
          0,
          0,
          DIM,
          DIM,
        );
        renderState.diffImageData.data.fill(0);
        const pixelsDifferent = pixelmatch(
          leftImageData.data,
          rightImageData.data,
          renderState.diffImageData.data,
          DIM,
          DIM,
          { threshold: 0.1 },
        );
        renderState.diffCtx.putImageData(renderState.diffImageData, 0, 0);
        const diffUrl = await canvasToObjectURL(diffCanvas);

        setResults((currentResults: ExampleResult[]) => [
          ...currentResults,
          {
            example: ex,
            rendered: {
              pixelsDifferent,
              leftData: leftUrl,
              rightData: rightUrl,
              diffData: diffUrl,
            },
          },
        ]);
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
