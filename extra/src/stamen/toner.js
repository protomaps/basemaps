import { arr, exp, createPattern, Font, Sprites, LineLabelSymbolizer, GroupSymbolizer, CircleSymbolizer, LineSymbolizer, PolygonSymbolizer, TextSymbolizer, IconSymbolizer, PolygonLabelSymbolizer } from 'protomaps'
import icons from './toner-icons.html'

// https://github.com/stamen/toner-carto/blob/master/map.mss

const Toner = variant => {
    let halftone = createPattern(4,4, c => {
        var ctx = c.getContext("2d");
        ctx.beginPath();
        ctx.rect(0,0,1,1)
        ctx.rect(2,2,1,1)
        ctx.fillStyle = "black"
        ctx.fill()
    })

    var background = "black" 
    if (variant === 'lite') {
        background = "#d9d9d9"
    }

    let lang = ["name:en","name"]

    let sprites = Sprites(icons)
    return {
        tasks:[
            sprites.load(),
            Font('Inter','https://cdn.protomaps.com/fonts/woff2/Inter.var.woff2','100 900')
        ],
        paint: [
            {
                dataLayer: "earth",
                symbolizer: new PolygonSymbolizer({
                    fill:"white"
                })
            },
            {
                dataLayer: "landuse",
                symbolizer: new PolygonSymbolizer({
                    pattern:halftone
                }),
                filter: f => { return f.leisure == "park" }
            },
            {
                dataLayer: "water",
                symbolizer: new PolygonSymbolizer({
                    fill: background
                })
            },
            {
                dataLayer: "roads",
                symbolizer: new PolygonSymbolizer({
                    color:"white",
                    width: arr(16,[7,9,17,20])
                }),
                filter: f => { return f["pmap:kind"] === "medium_road" }
            },
            {
                dataLayer: "roads",
                symbolizer: new LineSymbolizer({
                    color:"#cccccc",
                    width: arr(10,[0.2,0.2,0.2,0.4,0.8,1.5,4,7,13,16])
                }),
                filter: f => { return f["pmap:kind"] === "medium_road" }
            },
            {
                dataLayer: "roads",
                symbolizer: new LineSymbolizer({
                    color:"white",
                    width: arr(11,[1.25,5,5,5,8,11,18,22,30])
                }),
                filter: f => { return f["pmap:kind"] === "major_road" }
            },
            {
                dataLayer: "roads",
                symbolizer: new LineSymbolizer({
                    color:"black",
                    width: arr(9,[0.15,0.5,0.7,1,1.5,1.9,5,7,12,18,26])
                }),
                filter: f => { return f["pmap:kind"] === "major_road" }
            },
            {
                dataLayer: "roads",
                symbolizer: new LineSymbolizer({
                    color:"white",
                    width: arr(7,[2.25,3.25,4.25,5,6,7,8,9,11,14,24,42,49])
                }),
                filter: f => { return f["pmap:kind"] === "highway" }
            },
            {
                dataLayer: "roads",
                symbolizer: new LineSymbolizer({
                    color:"black",
                    width: arr(6,[0.1,1.5,1.5,1.5,2,2.5,3,3,4,6,9,15,28,35])
                }),
                filter: f => { return f["pmap:kind"] === "highway" }
            },
            {
                dataLayer: "transit",
                symbolizer: new LineSymbolizer({
                    color:"#888888",
                    dashColor:"#888888",
                    dash:[1,4],
                    dashWidth:3
                }),
                filter: f => { return f["pmap:kind"] === "railway" },
                minzoom:14
            },
            {
                dataLayer: "buildings",
                symbolizer: new LineSymbolizer({
                    color:"#888888",
                    width: 0.5
                })
            },
            {
                dataLayer: "boundaries",
                symbolizer: new LineSymbolizer({
                    color:"black",
                    width:1
                }),
                maxzoom:6
            },
            {
                dataLayer: "boundaries",
                symbolizer: new LineSymbolizer({
                    color:"white",
                    width:2.5,
                    dash:[3,1],
                    dashWidth:0.3,
                    dashColor:"black"
                }),
                minzoom:7
            },
        ],
        label: [
            {
                dataLayer: "places",
                symbolizer: new TextSymbolizer({
                    properties:lang,
                    fill:"black",
                    stroke:"white",
                    width:3,
                    fontFamily:"Inter",
                    fontWeight:300,
                    fontSize: 15,
                    align:"center"
                }),
                filter: f => { return f["pmap:kind"] == "country" }
            },
            {
                dataLayer: "places",
                symbolizer: new TextSymbolizer({
                    properties:lang,
                    fill:"black",
                    stroke:"white",
                    width:3,
                    fontFamily:"Inter",
                    fontWeight:300,
                    fontSize: 12,
                    align:"center"
                }),
                filter: f => { return f["pmap:kind"] == "state" }
            },
            {
                dataLayer: "places",
                symbolizer: new GroupSymbolizer([
                    new CircleSymbolizer({
                        radius:2,
                        fill:"black",
                        stroke:"white",
                        width:2
                    }),
                    new TextSymbolizer({
                        properties:lang,
                        offset:3,
                        fill:"black",
                        stroke:"white",
                        width:3,
                        fontFamily:"Inter",
                        fontWeight:600,
                        fontSize: (z,p) => {
                            if (p["pmap:rank"] == 1) return 15
                            return 13
                        }
                    })
                ]),
                sort: (a,b) => { return a["pmap:rank"] - b["pmap:rank"] },
                filter: f => { return f["pmap:kind"] == "city" },
                maxzoom:8
            },
            {
                dataLayer: "places",
                symbolizer: new TextSymbolizer({
                    properties:lang,
                    align:"center",
                    fill:"black",
                    stroke:"white",
                    width:3,
                    fontFamily:"Inter",
                    fontWeight:600,
                    fontSize: (z,p) => {
                        if (p["pmap:rank"] == 1) return 15
                        return 13
                    }
                }),
                sort: (a,b) => { return a["pmap:rank"] - b["pmap:rank"] },
                filter: f => { return f["pmap:kind"] == "city" },
                minzoom:9
            },
            {
                dataLayer: "water",
                symbolizer: new PolygonLabelSymbolizer({
                    properties:lang,
                    fill:"white",
                    stroke:"black",
                    width:3,
                    font:"italic 400 12px Inter"
                })
            },
            {
                dataLayer: "landuse",
                symbolizer: new PolygonLabelSymbolizer({
                    fill:"black",
                    stroke:"white",
                    width:4,
                    font:"italic 400 12px Inter"
                })
            },
            {
                dataLayer: "pois",
                symbolizer: new IconSymbolizer({
                    sprites:sprites,
                    name:"airplane"
                }),
                filter: f => { return f.railway == 'station' }
            },
            {
                dataLayer: "physical_point",
                symbolizer: new TextSymbolizer({
                    properties:lang,
                    fill:"white",
                    stroke:"black",
                    width:3,
                    font:"italic 600 12px Inter",
                    textTransform:"uppercase",
                    align:"center"
                }),
                filter: f => { return ["ocean","sea"].includes(f.place) }
            },
            // {
            //     dataLayer: "roads",
            //     symbolizer: new LineLabelSymbolizer({
            //         fill: "black"
            //     })
            // }
        ],
        attribution:'Map tiles by <a href="http://stamen.com">Stamen Design</a>, under <a href="http://creativecommons.org/licenses/by/3.0">CC BY 3.0</a>.'
    }
}

export { Toner }