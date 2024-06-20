import assert from "node:assert";
import { test } from "node:test";
import { validateStyleMin } from "@maplibre/maplibre-gl-style-spec";
import layers from "../src/index";
import themes from "../src/themes";

import "./base_layers.test";
import "./custom_themes.test";
import "./themes.test";

const STUB = {
	version: 8,
	glyphs: "https://example.com/{fontstack}/{range}.pbf",
	sources: {
		sourcename: {
			type: "vector",
		},
	},
};

test("validate all final themes", async () => {
	for (const i in themes) {
		STUB.layers = await layers("sourcename", i);
		const errors = validateStyleMin(STUB);
		assert.deepStrictEqual([], errors);
	}
});

test("validate custom theme", async () => {
	STUB.layers = await layers("sourcename", "./test/protomapsStyles.ts");
	const errors = validateStyleMin(STUB);
	assert.deepStrictEqual([], errors);
});
