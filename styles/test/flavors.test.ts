import assert from "node:assert";
import { test } from "node:test";
import { BLACK, DARK, GRAYSCALE, LIGHT, WHITE } from "../src/flavors";

function isOrderedSubset(subset: string[], array: string[]): boolean {
  return (
    subset.reduce((i, val) => {
      const idx = array.indexOf(val, i);
      return idx === -1 ? -1 : idx + 1;
    }, 0) !== -1
  );
}

test("key ordering in a flavor", () => {
  const ordered = Object.keys(LIGHT);
  for (const f of [DARK, WHITE, GRAYSCALE, BLACK]) {
    assert(isOrderedSubset(Object.keys(f), ordered));
  }
});

test("lower case hex color codes like fresco", () => {
  const hexPattern = /^#[0-9a-f]{6}$/;
  for (const f of [LIGHT, DARK, WHITE, GRAYSCALE, BLACK]) {
    for (const val of Object.values(f)) {
      if (typeof val === "string") {
        // TODO work for poi and landcover
        assert(hexPattern.test(val), val);
      }
    }
  }
});
