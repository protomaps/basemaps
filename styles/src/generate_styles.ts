// @ts-nocheck
declare const process: unknown;

import fs from "fs";
import { writeFile } from "fs/promises";
import i from "./index";
import { language_script_pairs } from "./language";
import themes from "./themes";

if (process.argv.length < 2) {
  process.stdout.write("usage: generate-styles TILEJSON_URL");
  process.exit(1);
}

const args = process.argv.slice(2);
const tileJson = args[0];

for (const theme of ["light", "dark", "white", "grayscale", "black"]) {
  for (const { lang, full_name, script } of language_script_pairs) {
    const layers = i("protomaps", theme, lang, script);

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
      layers: layers,
      sprite: "https://protomaps.github.io/basemaps-assets/sprites/v3/light",
      glyphs:
        "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
    };

    const directory = `dist/styles/${theme}`;
    if (!fs.existsSync(directory)) {
      fs.mkdirSync(directory, { recursive: true });
      console.log(`Directory ${directory} created successfully!`);
    }

    try {
      await writeFile(
        `${directory}/${lang}.json`,
        JSON.stringify(style, null, 2),
      );
    } catch (err) {
      console.error("An error occurred while writing to the file:", err);
    }
  }
}
