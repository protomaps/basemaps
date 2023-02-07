import debug_layers from "./debug_layers";
import { nolabels_layers, labels_layers } from "./base_layers";
import colors from "./colors";

export default function (source: string, variant: string) {
    if (variant == "debug") return debug_layers(source);
    let theme = (colors as any)[variant];
    return nolabels_layers(source, theme).concat(labels_layers(source, theme));
}

export function noLabels(source: string, variant: string) {
    let theme = (colors as any)[variant];
    return nolabels_layers(source, theme);
}

export function labels(source: string, variant: string) {
    let theme = (colors as any)[variant];
    return labels_layers(source, theme);
}
