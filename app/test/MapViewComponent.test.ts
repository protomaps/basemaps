import { isValidPMTiles } from "src/MapViewComponent";
import { expect, test } from "vitest";

test("checks valid tiles value", () => {
  expect(isValidPMTiles("local.pmtiles")).toBe(true);
  expect(isValidPMTiles("http://example.com/remote.pmtiles")).toBe(true);
  expect(isValidPMTiles("http://example.com/remote.pmtiles?abc=def")).toBe(
    true,
  );
  expect(isValidPMTiles("invalid")).toBe(false);
  expect(isValidPMTiles("invalid.pmtiles?abc=def")).toBe(false);
});
