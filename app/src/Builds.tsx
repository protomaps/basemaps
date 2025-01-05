/* @refresh reload */
import { render } from "solid-js/web";
import "./index.css";
import Nav from "./Nav";

import { For, createResource, createSignal } from "solid-js";

interface Build {
  uploaded: string;
  key: string;
  size: number;
  version: string;
}

function isMonday(dateStr: string): boolean {
  const year = Number.parseInt(dateStr.substring(0, 4), 10);
  const month = Number.parseInt(dateStr.substring(4, 6), 10) - 1; // Subtract 1 because months are 0-indexed in JavaScript dates
  const day = Number.parseInt(dateStr.substring(6, 8), 10);
  const date = new Date(year, month, day);
  return date.getDay() === 1;
}

function formatBytes(bytes: number, decimals = 2) {
  if (!+bytes) return "0 Bytes";
  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = [
    "Bytes",
    "KiB",
    "MiB",
    "GiB",
    "TiB",
    "PiB",
    "EiB",
    "ZiB",
    "YiB",
  ];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${Number.parseFloat((bytes / k ** i).toFixed(dm))} ${sizes[i]}`;
}

function BuildComponent(props: {
  build: Build;
  idx: number;
  cmpA: number;
  cmpB: number;
  setCmpA: (i: number) => void;
  setCmpB: (i: number) => void;
}) {
  const build = props.build;
  const link = `https://build.protomaps.com/${build.key}`;
  const date = build.key.substr(0, 8);
  const statsLink = `https://build.protomaps.com/${date}.layerstats.parquet`;
  const idx = props.idx;

  const onChangeA = () => {
    props.setCmpA(idx);
  };

  const onChangeB = () => {
    props.setCmpB(idx);
  };

  return (
    <tr style={{ color: isMonday(date) ? "black" : "#aaa" }}>
      <td>
        <span style={{ display: "inline-block", width: "20px" }}>
          {idx > props.cmpB && (
            <input
              type="radio"
              onChange={onChangeA}
              checked={idx === props.cmpA}
            />
          )}
        </span>
        <span style={{ display: "inline-block", width: "20px" }}>
          {idx < props.cmpA && (
            <input
              type="radio"
              onChange={onChangeB}
              checked={idx === props.cmpB}
            />
          )}
        </span>
      </td>
      <td>{build.key}</td>
      <td>{build.version}</td>
      <td>{formatBytes(build.size)}</td>
      <td>{build.uploaded}</td>
      <td>
        <a class="underline" href={`/#tiles=${link}`}>
          map
        </a>
      </td>
      <td>
        <a
          class="underline"
          href={`https://protomaps.github.io/PMTiles/?url=${link}`}
        >
          xray
        </a>
      </td>
      <td>
        <a class="underline" href={link}>
          download
        </a>
      </td>
      <td>
        {date >= "20231228" ? (
          <a class="underline" href={statsLink}>
            stats
          </a>
        ) : null}
      </td>
    </tr>
  );
}

interface MaperturePayload {
  name: string;
  type: string;
  renderer: string;
  url: string;
  index: number;
}

function Builds() {
  const [builds] = createResource(async () => {
    const resp = await fetch(
      "https://build-metadata.protomaps.dev/builds.json",
    );
    const j = await resp.json();
    return j.sort((a: Build, b: Build) => {
      if (a.key < b.key) return 1;
      if (a.key > b.key) return -1;
      return 0;
    });
  });
  const [latestStyle] = createResource(async () => {
    const resp = await fetch(
      "https://registry.npmjs.org/protomaps-themes-base",
      {
        headers: { Accept: "application/vnd.npm.install-v1+json" },
      },
    );
    const j = await resp.json();
    const versions = Object.keys(j.versions).filter(
      (v) => +v.split(".")[0] >= 2,
    );
    return versions[versions.length - 1];
  });

  // the index is starting from the top (0)
  const [cmpA, setCmpA] = createSignal<number>(1);
  const [cmpB, setCmpB] = createSignal<number>(0);

  const theme = "light";

  const openVisualTests = () => {
    const left = `https://build.protomaps.com/${builds()[cmpA()].key}`;
    const right = `https://build.protomaps.com/${builds()[cmpB()].key}`;
    open(`/visualtests/?leftTiles=${left}&rightTiles=${right}`);
  };

  const openMaperture = () => {
    const leftKey = builds()[cmpA()].key.replace(".pmtiles", "");
    const rightKey = builds()[cmpB()].key.replace(".pmtiles", "");
    const left: MaperturePayload = {
      name: `${leftKey} ${latestStyle()} ${theme}`,
      type: "maplibre-gl",
      renderer: "maplibre-gl",
      index: 0,
      url: `https://build-metadata.protomaps.dev/style@${latestStyle()}+theme@${theme}+tiles@${leftKey}.json`,
    };
    const right: MaperturePayload = {
      name: `${rightKey} ${latestStyle()} ${theme}`,
      type: "maplibre-gl",
      renderer: "maplibre-gl",
      index: 0,
      url: `https://build-metadata.protomaps.dev/style@${latestStyle()}+theme@${theme}+tiles@${rightKey}.json`,
    };
    const payload = JSON.stringify([left, right]);
    open(
      `https://stamen.github.io/maperture/#maps=${encodeURIComponent(payload)}`,
    );
  };

  return (
    <div class="flex flex-col h-screen w-full">
      <Nav page={1} />
      <div class="max-w-[1500px] mx-auto">
        <h1 class="my-8 text-4xl">Builds</h1>
        <p>Only Monday builds (black) are kept indefinitely.</p>
        <div class="space-x-2 my-2">
          <button class="btn-primary" type="button" onClick={openVisualTests}>
            Compare visual tests
          </button>
          {latestStyle() ? (
            <button class="btn-primary" type="button" onClick={openMaperture}>
              Compare in Maperture (style {latestStyle()})
            </button>
          ) : null}
        </div>
        <table class="table-auto border-separate border-spacing-4 font-mono">
          <tbody>
            <For each={builds()}>
              {(build, idx) => (
                <BuildComponent
                  build={build}
                  idx={idx()}
                  cmpA={cmpA()}
                  cmpB={cmpB()}
                  setCmpA={setCmpA}
                  setCmpB={setCmpB}
                />
              )}
            </For>
          </tbody>
        </table>
      </div>
    </div>
  );
}

const root = document.getElementById("root");

if (root) {
  render(() => <Builds />, root);
}
