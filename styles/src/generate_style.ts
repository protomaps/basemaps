#!/usr/bin/env tsx

import fs from "fs";
import path from "path";
import { type StyleSpecification } from "@maplibre/maplibre-gl-style-spec";
import { writeFile } from "fs/promises";
import { type Flavor, layers, namedFlavor } from "./index";

if (process.argv.length < 5) {
  console.error(
    "usage: generate-style OUTPUT TILEJSON_URL [FLAVOR.js|FLAVOR.ts|FLAVOR.json|FLAVOR_NAME] LANG",
  );
  process.exit(1);
}

const args = process.argv.slice(2);
const out = args[0];
const tileJson = args[1];
const flavorArg = args[2];
let spriteValue: string | undefined;

let flavor: Flavor;
if (flavorArg.endsWith(".json")) {
  flavor = JSON.parse(fs.readFileSync(flavorArg, "utf-8"));
} else if (flavorArg.endsWith(".js") || flavorArg.endsWith(".ts")) {
  flavor = (await import(path.resolve(flavorArg))).default;
} else {
  if (flavorArg === "light" || flavorArg === "dark") {
    spriteValue = flavorArg;
  }
  flavor = namedFlavor(flavorArg);
}
const lang = args[3];

const style: StyleSpecification = {
  version: 8,
  sources: {
    protomaps: {
      type: "vector",
      attribution:
        '<a href="https://github.com/protomaps/basemaps">Protomaps</a> © <a href="https://osm.org/copyright">OpenStreetMap</a>',
      url: tileJson,
    },
  },
  layers: layers("protomaps", flavor, { lang: lang }),
  glyphs:
    "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
};

if (spriteValue) {
  style.sprite = `https://protomaps.github.io/basemaps-assets/sprites/v4/${flavorArg}`;
}

await writeFile(out, JSON.stringify(style, null, 2));
