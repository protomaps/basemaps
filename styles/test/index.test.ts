import assert from "node:assert";
import { test } from "node:test";
import {
  type StyleSpecification,
  validateStyleMin,
} from "@maplibre/maplibre-gl-style-spec";
import { BLACK, DARK, GRAYSCALE, LIGHT, WHITE } from "../src/flavors";
import { layers } from "../src/index";

const STUB: StyleSpecification = {
  version: 8,
  glyphs: "https://example.com/{fontstack}/{range}.pbf",
  sources: {
    sourcename: {
      type: "vector",
    },
  },
  layers: [],
};

test("validate all final themes", () => {
  for (const f of [LIGHT, DARK, WHITE, GRAYSCALE, BLACK]) {
    STUB.layers = layers("sourcename", f, { lang: "en" });
    const errors = validateStyleMin(STUB);
    assert.deepStrictEqual([], errors);
  }
});
