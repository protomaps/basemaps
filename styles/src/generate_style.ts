import fs from "fs";
import path from "path";
import { type StyleSpecification } from "@maplibre/maplibre-gl-style-spec";
import { type Flavor, layers, namedFlavor } from "./index";

export async function generateStyle(args: string[]) {
  if (args.length < 2) {
    throw new TypeError(
      "usage: generate-style OUTPUT TILEJSON_URL [FLAVOR.js|FLAVOR.ts|FLAVOR.json|FLAVOR_NAME] LANG [SPRITE_URL] [GLYPHS_URL]",
    );
  }

  const tileJson = args[0];
  const flavorArg = args[1];
  const customSprite = args[3];
  const customGlyphs = args[4];

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

  const lang = args[2];

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
      customGlyphs ||
      "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
  };

  if (customSprite) {
    style.sprite = customSprite;
  } else if (spriteValue) {
    style.sprite = `https://protomaps.github.io/basemaps-assets/sprites/v4/${flavorArg}`;
  }

  return JSON.stringify(style, null, 2);
}
