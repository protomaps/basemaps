## Protomaps Map Styles

Map styles that can be used with the [Protomaps web map API](https://protomaps.com), which serves [vector tiles](https://github.com/mapbox/vector-tile-spec).

These styles have a few unique properties:
* OpenStreetMap is the only data source for all geographic features. There are no discontinuities where different datasets are used at smaller scales.
* OpenStreetMap tags are included directly, instead of being transformed to a fixed schema. For ease of styling, there are helper tags included prefixed with `pmap:`. examples `pmap:level`, `pmap:area`.
* A version of each style is included for use with the [Tangram](https://github.com/tangrams/tangram) library as well as the [MapboxGL](https://github.com/mapbox/mapbox-gl-js) library.

Use TangramJS for:
* Custom web fonts or variable fonts.
* High-quality CJK and Indic script rendering.
* Integration with the rich LeafletJS ecosystem.

Use MapboxGL for:
* High performance on the web.
* True 3D maps that can be rotated (changing bearing and pitch).
* Shared custom styles with [Mapbox GL Native](https://github.com/mapbox/mapbox-gl-native) apps for iOS, Mac and Android.


To generate font files for MapboxGL, try [sdf-glyph-tool](https://github.com/protomaps/sdf-glyph-tool).

## License

[BSD 3-clause](/LICENSE.md). The organization of layers and features used by these map styles, as well as the "look and feel" of the resulting maps, are licensed [CC0](https://creativecommons.org/publicdomain/zero/1.0/). However, maps using the [Protomaps web map service](https://protomaps.com) or another OpenStreetMap-based service will be subject to the terms of the [Open Database License](https://www.openstreetmap.org/copyright).