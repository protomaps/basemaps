import { test } from "node:test";
import assert from "node:assert";
import themes from "../src/themes";

test("theme ordering", () => {
  let ordered;
  for (var i in themes) {
    if (!ordered) {
      ordered = Object.keys(themes[i]);
    } else {
      assert.deepStrictEqual(ordered, Object.keys(themes[i]));
    }
  }
});

test("lower case hex color codes like fresco", () => {
  const hexPattern = /^#[0-9a-f]{6}$/;
  for (var i in themes) {
    for (var j in themes[i]) {
      const color = themes[i][j];
      assert(hexPattern.test(color), color);
    }
  }
});
