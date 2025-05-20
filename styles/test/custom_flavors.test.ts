import assert from "node:assert";
import { test } from "node:test";
import {
  type StyleSpecification,
  validateStyleMin,
} from "@maplibre/maplibre-gl-style-spec";
import { DARK } from "../src/flavors";
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

test("validate passed flavor", () => {
  STUB.layers = layers("sourcename", DARK);
  const errors = validateStyleMin(STUB);
  assert.deepStrictEqual([], errors);
});

test("validate layers with partial custom theme overrides", () => {
  STUB.layers = layers("sourcename", { ...DARK, background: "#fff" });
  const errors = validateStyleMin(STUB);
  assert.deepStrictEqual([], errors);
  const bgLayer = STUB.layers.find((l) => l.id === "background");
  assert.deepStrictEqual(bgLayer?.paint, {
    "background-color": "#fff",
  });
});
