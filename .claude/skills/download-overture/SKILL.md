---
name: download-overture
description: >
  Download Overture Maps GeoParquet data for a bounding box into tiles/data/overture/ using the
  overturemaps CLI, for building tiles from the pm:overture source instead of OSM. Use when
  asked to download/fetch Overture data, get Overture parquet, or build tiles from Overture.
  Takes the Overture theme type as its argument.
---

# Download Overture (GeoParquet, for the `pm:overture` source)

Downloads Overture GeoParquet and stops. Run from the `tiles/` directory at the repo root.

Overture is a **parallel source to OSM, not an addition to it**. `Basemap.java` rejects
`--area` and `--overture` together — a build is one or the other. Each layer registers a
separate `pm:overture` handler (`buildings::processOverture`, `roads::processOverture`, …), so
Overture data flows through different code than the OSM path.

## 0. Resolve the type and bbox (the arguments)

The **type** is required — it's the skill's argument (e.g. `/download-overture building`).
Valid values, from `overturemaps download --help`:

```
address, bathymetry, building, building_part, division, division_area,
division_boundary, place, segment, connector, infrastructure, land,
land_cover, land_use, water
```

Note these are Overture's names, not this repo's layer names — roads come from `segment`
(plus `connector`), not a type called `roads`.

A **bbox** is effectively required too: without `--bbox` the CLI pulls the type globally,
which is enormous. Ask for one if the user didn't give a region, and pass it as
`--bbox=west,south,east,north`.

## 1. Download

The CLI is the `overturemaps` Python package. Don't assume it's on PATH — a checkout may keep
it in a repo-local venv at `tiles/venv/`, so check both:

```
command -v overturemaps || ls ./venv/bin/overturemaps
```

Use whichever resolves (`./venv/bin/overturemaps` below; drop the prefix if it's on PATH):

```
./venv/bin/overturemaps download \
  --bbox=<w,s,e,n> \
  -f geoparquet \
  -t <type> \
  -o data/overture/<type>.parquet
```

`-f geoparquet` is what the build consumes. The CLI also offers `geojson`/`geojsonseq`, which
are fine for eyeballing data but **cannot** be fed to `--overture`.

Add `-r <release>` to pin a release; it defaults to the latest, so an unpinned download is not
reproducible over time. Pin it if the user cares about matching a previous run.

Downloads are network-bound and can be slow — stream with `Monitor` (a deferred tool: load via
`ToolSearch` query `select:Monitor`) rather than blocking, and confirm with
`ls -lh data/overture/`.

## 2. Build from it (what the download is for)

Single file, or a hive-partitioned directory:

```
java -jar target/*-with-deps.jar --overture=data/overture/<type>.parquet --output=data/<name>.pmtiles --force
```

`Basemap.java` branches on the path's extension:

- **Ends in `.parquet`** — that exact file is the only input, no hive partitioning.
- **Anything else** — treated as a base directory, globbed as `**/*.parquet` with hive
  partitioning **on**. This is the multi-type case: download several types under
  `data/overture/` and point `--overture` at the directory to build them together.

Bounds are read automatically from the GeoParquet metadata when `--bounds` isn't given, so a
bbox-limited download builds only its own region without you restating the extent. If bounds
come out wrong, check that metadata before suspecting the build.

## Notes

- If neither `overturemaps` nor `./venv/bin/overturemaps` resolves, it isn't installed:
  `python3 -m venv venv && ./venv/bin/pip install overturemaps` from `tiles/`. Prefer the venv
  over a global install.
- Overture releases are versioned `YYYY-MM-DD.N`. Record which release was used when
  reporting results; "latest" drifts and makes two runs incomparable for no visible reason.
- Overture parquet is large per unit area compared to an OSM extract. Start with a small bbox
  when validating that a `processOverture` path works at all, then widen.
- Sanity-check a download without a full build, if [duckdb](https://duckdb.org) is available:
  `duckdb -c "select count(*) from 'data/overture/<type>.parquet';"`
- The `pm:overture` source layer is keyed on the parquet `type` field, and feature ids on
  `id` (see the `addParquetSource` call in `Basemap.java`). A type whose rows lack those
  fields won't map onto the layer handlers.
