# Basemaps Visual Test Suite

## Test Examples

The file `examples.json` is a JSON array of named examples. Each example consists of:

* a `center` lon,lat
* a `zoom` level
* a `name` that must be a simple slug e.g. `null-island`
* a `description` to explain the cartographic feature under test.
* an array of string `tags` that group examples e.g. `buildings`, `national-parks`

## Test Runner

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