import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import { labels_layers, nolabels_layers } from "./base_layers";
import { language_script_pairs } from "./language";
import themes, { Theme } from "./themes";

export { language_script_pairs };

export default function (
  source: string,
  key: string,
  lang: string,
  script?: string,
): LayerSpecification[] {
  const theme = themes[key];
  return nolabels_layers(source, theme).concat(
    labels_layers(source, theme, lang, script),
  );
}

export function noLabels(source: string, key: string): LayerSpecification[] {
  const theme = themes[key];
  return nolabels_layers(source, theme);
}

export function labels(
  source: string,
  key: string,
  lang: string,
  script?: string,
): LayerSpecification[] {
  const theme = themes[key];
  return labels_layers(source, theme, lang, script);
}

export function layersWithCustomTheme(
  source: string,
  theme: Theme,
  lang: string,
  script?: string,
): LayerSpecification[] {
  return nolabels_layers(source, theme).concat(
    labels_layers(source, theme, lang, script),
  );
}

export function layersWithPartialCustomTheme(
  source: string,
  key: string,
  partialTheme: Partial<Theme>,
  lang: string,
  script?: string,
): LayerSpecification[] {
  const mergedTheme = { ...themes[key], ...partialTheme };
  return nolabels_layers(source, mergedTheme).concat(
    labels_layers(source, mergedTheme, lang, script),
  );
}

export function noLabelsWithCustomTheme(
  source: string,
  theme: Theme,
): LayerSpecification[] {
  return nolabels_layers(source, theme);
}

export function labelsWithCustomTheme(
  source: string,
  theme: Theme,
  lang: string,
  script?: string,
): LayerSpecification[] {
  return labels_layers(source, theme, lang, script);
}
