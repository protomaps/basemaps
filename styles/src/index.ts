import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";
import { labels_layers, nolabels_layers } from "./base_layers";
import { language_script_pairs } from "./language";
import themes, {
  Theme,
  Pois,
  LIGHT,
  DARK,
  WHITE,
  GRAYSCALE,
  BLACK,
} from "./themes";

export { language_script_pairs };
export type { Theme, Pois };

export function namedTheme(name: string): Theme {
  switch (name) {
    case "light":
      return LIGHT;
      break;
    case "dark":
      return DARK;
      break;
    case "white":
      return WHITE;
      break;
    case "grayscale":
      return GRAYSCALE;
      break;
    case "black":
      return BLACK;
      break;
  }
  throw new Error("Theme not found");
}

export function layers(
  source: string,
  theme: Theme,
  options?: { labelsOnly?: boolean; lang?: string },
): LayerSpecification[] {
  let layers: LayerSpecification[] = [];
  if (!options?.labelsOnly) {
    layers = nolabels_layers(source, theme);
  }
  if (options?.lang) {
    layers = layers.concat(labels_layers(source, theme, options.lang));
  }
  return layers;
}

// TODO: deprecate me
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

// TODO: deprecate me
export function noLabels(source: string, key: string): LayerSpecification[] {
  const theme = themes[key];
  return nolabels_layers(source, theme);
}

// TODO: deprecate me
export function labels(
  source: string,
  key: string,
  lang: string,
  script?: string,
): LayerSpecification[] {
  const theme = themes[key];
  return labels_layers(source, theme, lang, script);
}

// TODO: deprecate me
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

// TODO: deprecate me
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

// TODO: deprecate me
export function noLabelsWithCustomTheme(
  source: string,
  theme: Theme,
): LayerSpecification[] {
  return nolabels_layers(source, theme);
}

// TODO: deprecate me
export function labelsWithCustomTheme(
  source: string,
  theme: Theme,
  lang: string,
  script?: string,
): LayerSpecification[] {
  return labels_layers(source, theme, lang, script);
}
