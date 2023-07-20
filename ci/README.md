# Basemaps Visual Test Suite

## Artifacts

Recent git commits on `main` and pull requests each have a set of artifacts.

The test suite depends on the existence of a public HTTP endpoint with these paths:

* `ci-storage.protomaps.com/smalltestregion.osm.pbf`
* `ci-storage.protomaps.com/artifacts/ARTIFACT_SHA/smalltestregion_vector.pmtiles`
* `ci-storage.protomaps.com/artifacts/ARTIFACT_SHA/light.json`

`smalltestregion_vector.pmtiles` is the java tiler output at SHA run on `smalltestregion.osm.pbf`.

```sh
./osmx extract planet.osmx smalltestregion.osm.pbf --region smalltestregion.geojson
```

`light.json` is the generated `layers` of the GL JSON (not the full style).

*Later we will add more than just light.json*

## Test Examples

The file `examples.json` is a JSON array of named examples. Each example consists of:

* a `center` lon,lat
* a `zoom` level
* a `name` that must be a simple slug e.g. `null-island`
* a `description` to explain the cartographic feature under test.
* an array of string `tags` that group examples e.g. `buildings`, `national-parks`

## Test Runner

`index.html` is the single-file test runner, there is no build step. It takes query parameters:

Required query parameters:

* `?left=abc123&right=61`: The Artifact SHA or PR# to display on each side of the comparison.

Optional query parameters:

* `?name=null-island`: run only the named example.
* `?tag=national-parks`: run only one tag.
* `?showDifferencesOnly`: run the tests, but only display where the pixels don't match.

## Versions

The tile archive and named style layers are the only versioned artifacts. Non-versioned parts that affect the test run:

* The current `examples.json`
* The `smalltestregion.osm.pbf` covered areas and snapshot date from OSM.
* The `maplibre-gl-js` version.
* The font glyphs and sprite assets used by the style.