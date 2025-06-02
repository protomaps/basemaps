## Background

There is no one-size-fits-all map. The **Protomaps Basemaps project is designed to offer an 80/20 solution**: a default cartographic product that's appropriate for most general-purpose mapping use cases. The remaining use cases can be addressed by forks or more specialized map tile systems.

The tileset defined in `tiles` is designed to primarily work with the [MapLibre GL styles](https://maplibre.org/maplibre-style-spec/) in `styles`, and vice versa. All features included in the tileset ought to be visualized in the default styles, and the styles should not attempt to visualize any layers or features not included in the tiles. Visual customizations of the map, such as colors, are primarily controlled through [`Flavor` TypeScript objects](https://maps.protomaps.com/typedoc/interfaces/Flavor.html).

It is possible to build custom MapLibre GL styles (e.g. via the [Maputnik](http://maputnik.github.io) editor or hand-coded JSON) without using the `Flavor` interface, but **no special affordances or additions are made for custom styles.**

## Forks

Because basemap tilesets are delivered in the [PMTiles](https://docs.protomaps.com/pmtiles/) format, there is no special privilege assigned to the [api.protomaps.com tile service](https://protomaps.com/api) - it's a recent build from [maps.protomaps.com/builds](https://maps.protomaps.com/builds). Developers are **encouraged to fork and modify this project and then deploy their own tileset to cloud storage.**

## Contributions We Accept

1. **License-compatible derived works**.
  * Deriving visual appearance or logic from the [Mapzen (Linux Foundation)](https://www.mapzen.com/about/) open source projects, including [Bubble Wrap](https://github.com/tangrams/bubble-wrap), [Refill](https://tangrams.github.io/refill-style/) and other map styles.
  * Deriving from license-compatible projects such as [OpenStreetMap Carto](https://www.openstreetmap.org) and [Shortbread](https://shortbread-tiles.org).

2. **Data additions that are included in Tilezen**.
  * Include a reference to the Tilezen docs at https://tilezen.readthedocs.io/en/latest/layers/ or a link to a Tilezen legacy build at https://pmtiles.io/#url=https%3A%2F%2Fr2-public.protomaps.com%2Fprotomaps-sample-datasets%2Ftilezen.pmtiles

3. Visual optimization, generalization and efficiency improvements to the base tileset, base style layers or [5 default Flavors](https://github.com/protomaps/basemaps/blob/main/styles/src/flavors.ts).

## Contributions We Don't Accept

1. **Data or features additions not included in Tilezen**
  * There is a direct trade-off between the completeness of the map visually and the size of the tiles. The Protomaps Basemap is **not designed to represent 100% of OpenStreetMap data.** In general, the scope of features follows the [Tilezen project](https://tilezen.readthedocs.io/en/latest/layers/), an existing cartographic project that has proven sufficient for many commercial, general-purpose map applications.

2. **Significant changes to default flavors**
  * If you want to design a new Flavor, add it to the open-contribution [Basemaps Flavors](https://github.com/protomaps/basemaps-flavors) repository.

3. **License-incompatible derived maps**. 
  * Copying the visual appearance or logic from a proprietary map such as Google Maps, Apple Maps, Esri or Mapbox maps.
  * Deriving from other open source but license-incompatible projects like Stamen maps or OpenMapTiles, which use a [CC-BY license](https://creativecommons.org/licenses/by/4.0/).

4. **License-incompatible datasets**
  * CC-BY or proprietary datasets. In general, data sources should be [ODbL-compatible](https://wiki.openstreetmap.org/wiki/Import/ODbL_Compatibility).
  * Direct inclusion of Wikidata is discouraged because of the existence of [integrated datasets](https://www.wikidata.org/wiki/Wikidata:Licensing#Determining_the_copyright_of_a_dataset) - use a primary source with a single explicit license.

