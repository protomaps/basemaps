# Protomaps Basemaps

This repository has two core parts:

* `tiles/`: A [Planetiler](https://github.com/onthegomap/planetiler) build profile that generates `planet.pmtiles` from OpenStreetMap and Natural Earth in 2-3 hours on a modest computer.
* `base/`: A TypeScript package that generates [MapLibre GL](http://github.com/maplibre) styles, in multiple color themes, that can be used via `npm` or exported as JSON.

# Usage

How to build protomaps-theme compatabile pmtiles with [Planetiler](https://github.com/onthegomap/planetiler):

1) Clone the [Planetiler](https://github.com/onthegomap/planetiler)
`$ git clone git@github.com:onthegomap/planetiler.git`
2) Clone [protomaps/basemaps](https://github.com/protomaps/basemaps) as a directory in the planetiler repo
`$ git clone git@github.com:protomaps/basemaps.git`
3) Install [Planetiler dependencies](https://github.com/onthegomap/planetiler/tree/main/planetiler-examples), specifially maven
4) Build Planetiler with the protomaps/basemaps build profile
`$ mvn clean package --file basemaps/tiles/pom.xml`
5) Download and process an osm area with the protomaps/basemaps build profile
`$ java -cp basemaps/tiles/target/*-with-deps.jar com.protomaps.basemap.Basemap --download --area=monaco`
6) Install [go-pmtiles](https://github.com/protomaps/go-pmtiles)
7) Convert the mbtiles to pmtiles
`$ pmtiles convert INPUT.mbtiles OUTPUT.pmtiles`


## License

[BSD 3-clause](/LICENSE.md). The organization of layers and features used by these map styles, as well as the "look and feel" of the resulting maps, are licensed [CC0](https://creativecommons.org/publicdomain/zero/1.0/). However, maps using the [Protomaps web map service](https://protomaps.com) or another OpenStreetMap-based service will be subject to the terms of the [Open Database License](https://www.openstreetmap.org/copyright).
