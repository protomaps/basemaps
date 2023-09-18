import { nolabels_layers, labels_layers } from "./base_layers";
import themes from "./themes";
import { LayerSpecification } from "@maplibre/maplibre-gl-style-spec";

export default function (source: string, key: string): LayerSpecification[] {
    let theme = themes[key];
    return nolabels_layers(source, theme).concat(labels_layers(source, theme));
}

export function noLabels(source: string, key: string): LayerSpecification[] {
    let theme = themes[key];
    return nolabels_layers(source, theme);
}

export function labels(source: string, key: string): LayerSpecification[] {
    let theme = themes[key];
    return labels_layers(source, theme);
}
