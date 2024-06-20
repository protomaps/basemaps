// @ts-nocheck
declare const process: unknown;

import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import { labels_layers, nolabels_layers } from "./base_layers";
import themes, { type Theme } from "./themes";
import j from "jiti";

export default async function (
	source: string,
	keyOrPath: string
): Promise<LayerSpecification[]> {
	const jiti = j(
		/*
        Credits to https://flaviocopes.com/fix-dirname-not-defined-es-module-scope/
        */
		process.cwd()
	);
	const theme =
		keyOrPath in themes
			? themes[keyOrPath]
			: (
					(await jiti.import(keyOrPath, {})) as {
						default: Theme;
					}
			  ).default;
	return nolabels_layers(source, theme).concat(labels_layers(source, theme));
}

export function noLabels(source: string, key: string): LayerSpecification[] {
	const theme = themes[key];
	return nolabels_layers(source, theme);
}

export function labels(source: string, key: string): LayerSpecification[] {
	const theme = themes[key];
	return labels_layers(source, theme);
}

export function layersWithCustomTheme(
	source: string,
	theme: Theme
): LayerSpecification[] {
	return nolabels_layers(source, theme).concat(labels_layers(source, theme));
}

export function layersWithPartialCustomTheme(
	source: string,
	key: string,
	partialTheme: Partial<Theme>
): LayerSpecification[] {
	const mergedTheme = { ...themes[key], ...partialTheme };
	return nolabels_layers(source, mergedTheme).concat(
		labels_layers(source, mergedTheme)
	);
}

export function noLabelsWithCustomTheme(
	source: string,
	theme: Theme
): LayerSpecification[] {
	return nolabels_layers(source, theme);
}

export function labelsWithCustomTheme(
	source: string,
	theme: Theme
): LayerSpecification[] {
	return labels_layers(source, theme);
}

export { Theme };
