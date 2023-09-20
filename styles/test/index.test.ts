import { test } from "node:test";
import assert from "node:assert";
import { validateStyleMin } from "@maplibre/maplibre-gl-style-spec";
import themes from "../src/themes";
import layers from "../src/index";

import "./themes.test";

let STUB = {
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
    let errors = validateStyleMin(STUB);
    assert.deepStrictEqual([], errors);
  }
});
