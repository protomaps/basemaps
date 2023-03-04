# Protomaps Basemaps

This repository has two core parts:

* `tiles/`: A [Planetiler](https://github.com/onthegomap/planetiler) build profile that generates `planet.pmtiles` from OpenStreetMap and Natural Earth in 2-3 hours on a modest computer.
* `base/`: A TypeScript package that generates [MapLibre GL](http://github.com/maplibre) styles, in multiple color themes, that can be used via `npm` or exported as JSON.

## License

[BSD 3-clause](/LICENSE.md). The organization of layers and features used by these map styles, as well as the "look and feel" of the resulting maps, are licensed [CC0](https://creativecommons.org/publicdomain/zero/1.0/). However, maps using the [Protomaps web map service](https://protomaps.com) or another OpenStreetMap-based service will be subject to the terms of the [Open Database License](https://www.openstreetmap.org/copyright).
