// @ts-nocheck
declare const process: unknown;

import fs from "fs";
import path from "path";
import { writeFile } from "fs/promises";
import { layersWithCustomTheme } from "./index";

const args = process.argv.slice(2);
const tileJson = args[0];
const theme = await import(path.resolve(process.cwd(), args[1]));
const lang = args[2];
const output = args[3];

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
  layers: layersWithCustomTheme("protomaps", themeObj, lang),
  sprite: "https://protomaps.github.io/basemaps-assets/sprites/v3/light",
  glyphs:
    "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
};

await writeFile(output, JSON.stringify(style, null, 2));
