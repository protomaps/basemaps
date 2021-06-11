import debug_layers from './debug_layers'
import base_layers from './base_layers'
import colors from './colors'

export const layers = variant => {
  if (variant == 'debug') return debug_layers
  else return base_layers(colors[variant])
}

export const json_style = (variant,options) => {
    return {
        version: 8,
        glyphs: options.glyphs,
        name: "protomaps-themes-base/" + variant,
        sources: {
            protomaps: {
                type: "vector",
                tiles: [
                   options.tiles
                ],
                minzoom: 0,
                maxzoom: 14,
                attribution: "<a href=\"https://protomaps.com\" target=\"_blank\">Protomaps</a> © <a href=\"https://www.openstreetmap.org\" target=\"_blank\"> OpenStreetMap</a>"
            },
        },
        layers:layers(variant)
    }
}