import assert from "node:assert";
import { test } from "node:test";
import { validateStyleMin } from "@maplibre/maplibre-gl-style-spec";
import {
  labelsWithCustomTheme,
  layersWithPartialCustomTheme,
} from "../src/index";
import themes from "../src/themes";

const STUB = {
  version: 8,
  glyphs: "https://example.com/{fontstack}/{range}.pbf",
  sources: {
    sourcename: {
      type: "vector",
    },
  },
};

test("validate custom themes", () => {
  const customTheme = themes["dark"];
  STUB.layers = labelsWithCustomTheme("sourcename", customTheme);
  const errors = validateStyleMin(STUB);
  assert.deepStrictEqual([], errors);
});

test("validate layers with partial custom theme overrides", () => {
  const customBackgroundColor = "#fff";
  const partialTheme = { background: customBackgroundColor };
  STUB.layers = layersWithPartialCustomTheme(
    "sourcename",
    "dark",
    partialTheme
  );
  const errors = validateStyleMin(STUB);
  assert.deepStrictEqual([], errors);
  assert.deepStrictEqual(
    STUB.layers.find((l) => l.id == "background")["paint"],
    { "background-color": customBackgroundColor }
  );
});
