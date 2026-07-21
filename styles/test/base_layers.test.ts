import assert from "node:assert";
import { test } from "node:test";
import { labels_layers, nolabels_layers } from "../src/base_layers";
import { LIGHT } from "../src/flavors";

test("the labels layer only has labels", () => {
  for (const layer of labels_layers("dummy", LIGHT, "en")) {
    if ("source" in layer) {
      assert.equal("dummy", layer.source);
    }
    assert(layer.type === "symbol" || layer.type === "circle");
  }
});

test("the nolayers label has no labels", () => {
  for (const layer of nolabels_layers("dummy", LIGHT)) {
    if ("source" in layer) {
      assert.equal("dummy", layer.source);
    }
    assert.notEqual("symbol", layer.type);
  }
});

test("country boundary styling includes disputed level three boundaries", () => {
  const boundaryLayers = nolabels_layers("dummy", LIGHT);
  const country = boundaryLayers.find(
    (layer) => layer.id === "boundaries_country",
  );
  const other = boundaryLayers.find((layer) => layer.id === "boundaries");

  assert.deepEqual(country?.filter, [
    "any",
    ["<=", "kind_detail", 2],
    ["all", ["==", "kind_detail", 3], ["==", "disputed", true]],
  ]);
  assert.deepEqual(other?.filter, [
    "all",
    [">", "kind_detail", 2],
    ["none", ["all", ["==", "kind_detail", 3], ["==", "disputed", true]]],
  ]);
});
