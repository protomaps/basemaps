import assert from "node:assert";
import { test } from "node:test";
import { generateStyle } from "../src/generate_style";

test("generating a style too few args", async () => {
  await assert.rejects(generateStyle(["http://example.com"]));
});

test("generating a style default flavor", async () => {
  assert.ok(JSON.parse(await generateStyle(["http://example.com", "light"])));
});

test("generating a style default flavor", async () => {
  assert.ok(JSON.parse(await generateStyle(["http://example.com", "light"])));
});
