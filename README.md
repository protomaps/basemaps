# Protomaps Basemaps

This repository has two core parts:

* `tiles/`: A [Planetiler](https://github.com/onthegomap/planetiler) build profile that generates `planet.pmtiles` from OpenStreetMap and Natural Earth in 2-3 hours on a modest computer.
* `base/`: A TypeScript package that generates [MapLibre GL](http://github.com/maplibre) styles, in multiple color themes, that can be used via `npm` or exported as JSON.

# Usage

You will need [Java 17+](https://github.com/onthegomap/planetiler/blob/main/CONTRIBUTING.md) and [Maven](https://maven.apache.org/install.html) installed, which is available in most package managers. 

Generate and inspect a basemap PMTiles of any named area:

1. Clone this repository.

```shell
git clone git@github.com:protomaps/basemaps.git
```
2. change to the `tiles` directory, download dependencies and compile the JAR:

```shell
cd basemaps/tiles
mvn clean package
```
3. Download and generate `monaco.pmtiles` in the current directory:

```shell
java -jar target/*-with-deps.jar --download --force --area=monaco
```

4. Switch to the `compare/` directory to run the map compare tool:

```shell
cd compare
npm run serve
```

5. Linting to apply code formatting

```shell
mvn spotless:apply
```

## License

[BSD 3-clause](/LICENSE.md). The organization of layers and features used by these map styles, as well as the "look and feel" of the resulting maps, are licensed [CC0](https://creativecommons.org/publicdomain/zero/1.0/). However, maps using the [Protomaps web map service](https://protomaps.com) or another OpenStreetMap-based service will be subject to the terms of the [Open Database License](https://www.openstreetmap.org/copyright).
