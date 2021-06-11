import layers from './layers'

export const json_style = options => {
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
        layers:layers()
    }
}

export const raw_style = options => {
  return {
    layers:layers()
  }
}