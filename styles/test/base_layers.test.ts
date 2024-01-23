import assert from "node:assert";
import { test } from "node:test";
import { labels_layers, nolabels_layers } from "../src/base_layers";

test("the labels layer only has labels", () => {
  for (const layer of labels_layers("dummy", "light")) {
    assert.equal("dummy", layer.source);
    assert(layer.type === "symbol" || layer.type === "circle");
  }
});

test("the nolayers label has no labels", () => {
  for (const layer of nolabels_layers("dummy", "light")) {
    if (layer.type === "background") continue;
    assert.equal("dummy", layer.source);
    assert.notEqual("symbol", layer.type);
  }
});
