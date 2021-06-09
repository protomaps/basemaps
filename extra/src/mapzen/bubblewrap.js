import { arr, exp, createPattern, Font, Sprites, LineSymbolizer, FillSymbolizer, TextSymbolizer, IconSymbolizer, PolygonLabelSymbolizer } from 'protomaps'
import icons from './bubblewrap-icons.html'

const BubbleWrap = variant => {
    let sprites = Sprites(icons)
    return {
        tasks:[
            sprites.load(),
            Font('Inter','https://cdn.protomaps.com/fonts/woff2/Inter.var.woff2','100 900')
        ],
        paint: [
            {
                dataLayer: "earth",
                symbolizer: new FillSymbolizer({
                    fill:"#e4e4e4"
                })
            },
            {
                dataLayer: "water",
                symbolizer: new FillSymbolizer({
                    fill: "#9dc3de"
                })
            },
            {
                dataLayer: "roads",
                symbolizer: new LineSymbolizer({
                    color: "white"
                })
            }
        ],
        label: [
        ],
        attribution:''
    }
}

export { BubbleWrap }