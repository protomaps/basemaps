// @ts-nocheck
declare const process: unknown;

import fs from "fs";
import { writeFile } from "fs/promises";
import { layers, namedFlavor } from "./index";

if (process.argv.length < 5) {
  process.stdout.write(
    "usage: generate-style OUTPUT TILEJSON_URL FLAVOR_NAME LANG",
  );
  process.exit(1);
}

const args = process.argv.slice(2);
const out = args[0];
const tileJson = args[1];
const flavorName = args[2];
const flavor = namedFlavor(flavorName);
const lang = args[3];

const style = {
  version: 8,
  sources: {
    protomaps: {
      type: "vector",
      attribution:
        '<a href="https://github.com/protomaps/basemaps">Protomaps</a> © <a href="https://openstreetmap.org">OpenStreetMap</a>',
      url: tileJson,
    },
  },
  layers: layers("protomaps", flavor, { lang: lang }),
  sprite: `https://wipfli.github.io/basemaps-assets/sprites/v4/${flavorName}`,
  glyphs:
    "https://wipfli.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
};

await writeFile(out, JSON.stringify(style, null, 2));
