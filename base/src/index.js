import debug_layers from "./debug_layers";
import base_layers from "./base_layers";
import colors from "./colors";

export const layers = (source, variant) => {
    if (variant == "debug") return debug_layers(source);
    else return base_layers(source, colors[variant]);
};
