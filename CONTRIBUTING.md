## Background

There is no one-size-fits-all map. The Protomaps Basemaps project is designed to offer an 80/20 solution: Defaults that are appropriate for 80% of mapmaking use cases. The remaining 20% can be addressed by forks or more specialized map tile systems.

The tileset defined in `tiles` is designed to only work with the MapLibre GL styles in `styles`, and vice versa. Most visual customizations of the map, such as colors, is controlled through `Flavor` TypeScript objects.

## Forks

Because the basemap tilesets are delivered in the PMTiles format, there is no special privilege assigned to the api.protomaps.com tile service - it's simply a recent build from maps.protomaps.com/builds. Users that need customizations are encouraged to fork and modify this project and then deploy their own tileset to cloud storage.

## Contributions We Accept

1. **License-compatible derived works**.
  * Deriving visual appearance or logic from the Tilezen / Mapzen (Linux Foundation) open source projects.
  * Deriving from license-compatible projects such as OpenStreetMap Carto and Shortbread.

2. **Data additions that are included in Tilezen**.
  * Include a reference to the Tilezen docs at https://tilezen.readthedocs.io/en/latest/layers/ or a link to a Tilezen legacy build at https://pmtiles.io/#url=https%3A%2F%2Fr2-public.protomaps.com%2Fprotomaps-sample-datasets%2Ftilezen.pmtiles

3. Visual optimization, generalization and efficiency improvements to the base tileset, base style layers or default 5 Flavors.

## Contributions We Don't Accept

1. **Data or features additions not included in Tilezen**
  * There is a direct trade-off between the completeness of the map visually and the size of the tiles. The Protomaps Basemap is not designed to represent 100% of OpenStreetMap data. In general, the scope of features follows the Tilezen project, an existing cartographic project that has been proven to be sufficient for many commercial and general-purpose map applications.

2. **Significant changes to default flavors**
  * If you want to design a new Flavor, add it to the open-contribution Basemaps Flavors repository.

3. **License-incompatible derived maps**. 
  * Copying the visual appearance or logic from a proprietary map such as Google Maps, Apple Maps, Esri or Mapbox maps.
  * Deriving from other open source but license-incompatible projects like Stamen maps, OpenMapTiles (CC-BY).

4. **License-incompatible datasets**
  * CC-BY or proprietary datasets.
  * Direct inclusion of Wikidata is problematic because the CC0 license is not enforced (?)

