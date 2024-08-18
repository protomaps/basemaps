// @ts-nocheck
declare const process: unknown;

import i from "./index";

if (process.argv.length < 3) {
  process.stdout.write("usage: generate-style SOURCE_NAME THEME TILES_URL");
  process.exit(1);
}
const args = process.argv.slice(2);

const lang = 'hi';
const script = 'Devanagari';

const layers = i(args[0], args[1], lang, script);

const style = {
  "version": 8,
  "sources": {
      "protomaps": {
          "type": "vector",
          "attribution": "<a href=\"https://github.com/protomaps/basemaps\">Protomaps</a> © <a href=\"https://openstreetmap.org\">OpenStreetMap</a>",
          "url": args[2]
      }
  },
  "layers": layers,
  "sprite": "https://protomaps.github.io/basemaps-assets/sprites/v3/light",
  "glyphs": "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf"
}

process.stdout.write(JSON.stringify(style, null, 2));
