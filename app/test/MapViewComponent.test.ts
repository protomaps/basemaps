import { isValidTiles } from "src/MapViewComponent";
import { expect, test } from "vitest";

test("checks valid tiles value", () => {
  expect(isValidTiles("local.pmtiles")).toBe(true);
  expect(isValidTiles("http://example.com/remote.pmtiles")).toBe(true);
  expect(isValidTiles("http://example.com/remote.pmtiles?abc=def")).toBe(true);
  expect(isValidTiles("invalid")).toBe(false);
  expect(isValidTiles("invalid.pmtiles?abc=def")).toBe(false);
});
