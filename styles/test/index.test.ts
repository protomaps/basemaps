import assert from "node:assert";
import { test } from "node:test";
import { validateStyleMin } from "@maplibre/maplibre-gl-style-spec";
import layers from "../src/index";
import themes from "../src/themes";

import "./base_layers.test";
import "./themes.test";

const STUB = {
  version: 8,
  glyphs: "https://example.com/{fontstack}/{range}.pbf",
  sources: {
    sourcename: {
      type: "vector",
    },
  },
};

test("validate all final themes", () => {
  for (var i in themes) {
    STUB.layers = layers("sourcename", i);
    const errors = validateStyleMin(STUB);
    assert.deepStrictEqual([], errors);
  }
});
