import layers from './layers'
import colors from './colors'


export const json_style = (variant,options) => {
    return {
        version: 8,
        glyphs: options.glyphs,
        name: "Light",
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
        layers:layers(colors[variant])
    }
}

export const raw_style =  variant => {
  return {
    layers:layers(colors[variant])
  }
}