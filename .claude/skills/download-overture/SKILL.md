---
name: download-overture
description: >
  Download Overture Maps GeoParquet data for a bounding box using tiles/download-overture.sh,
  which queries the public S3 release with DuckDB, for building tiles from the pm:overture
  source instead of OSM. Use when asked to download/fetch Overture data, get Overture parquet,
  or build tiles from Overture. Takes the area (place name or bbox) as its argument.
---

# Download Overture (GeoParquet, for the `pm:overture` source)

Downloads Overture GeoParquet and stops. Run from the `tiles/` directory at the repo root.

Overture is a **parallel source to OSM, not an addition to it**. `Basemap.java` rejects
`--area` and `--overture` together — a build is one or the other. Each layer registers a
separate `pm:overture` handler (`buildings::processOverture`, `roads::processOverture`, …), so
Overture data flows through different code than the OSM path.

## 0. Resolve the bbox (the argument)

The skill's argument is the **area**. `download-overture.sh` takes a bbox as
`xmin,ymin,xmax,ymax` in WGS84 — if the user named a place instead, work out the bbox for it
and state the numbers you used before downloading.

There is no per-type argument: the script pulls the `transportation`, `places`, `base`,
`buildings` and `divisions` themes in one query and writes one Parquet file. Bbox is what
bounds the download, so a loose bbox is the expensive mistake here.

## 1. Download

```
./download-overture.sh [RELEASE] [BBOX] [OUTPUT]
```

All three are positional and optional, but in practice pass at least the first two, since
`BBOX` can't be given without `RELEASE`:

- `RELEASE` — Overture release, e.g. `2026-06-17.0`. Defaults to the latest, resolved by
  DuckDB from `https://stac.overturemaps.org/catalog.json`. Pass one explicitly when the run
  needs to be reproducible.
- `BBOX` — `xmin,ymin,xmax,ymax`. Defaults to Monaco.
- `OUTPUT` — output file path. Defaults to `data/sources/monaco.parquet`.

```
./download-overture.sh 2026-06-17.0 -122.52,37.70,-122.35,37.83 data/sources/sf.parquet
```

`./download-overture.sh --help` prints the same usage and the current defaults.

The script shells out to `duckdb`, which reads the release directly from
`s3://overturemaps-us-west-2/release/<REL>/` with hive partitioning and filters rows on the
`bbox` struct. Requirements:

- `duckdb` on PATH (`brew install duckdb`). No Python, no venv, no `overturemaps` package.
- Network access to both the STAC catalog (for release resolution) and the S3 bucket. If the
  catalog is unreachable the script exits telling you to pass a release explicitly — that
  failure is about release resolution, not the download itself.

Downloads are network-bound and can be slow — stream with `Monitor` (a deferred tool: load via
`ToolSearch` query `select:Monitor`) rather than blocking, and confirm with
`ls -lh data/sources/`.

## 2. Build from it (what the download is for)

```
java -jar target/*-with-deps.jar --overture=data/sources/<name>.parquet --output=data/<name>.pmtiles --force
```

`Basemap.java` branches on the path's extension:

- **Ends in `.parquet`** — that exact file is the only input, no hive partitioning. This is
  what the script produces.
- **Anything else** — treated as a base directory, globbed as `**/*.parquet` with hive
  partitioning **on**, for a manually assembled multi-file layout.

Bounds are read automatically from the GeoParquet metadata when `--bounds` isn't given, so a
bbox-limited download builds only its own region without you restating the extent. If bounds
come out wrong, check that metadata before suspecting the build.

## Notes

- Overture releases are versioned `YYYY-MM-DD.N`. Record which release was used when
  reporting results; "latest" drifts and makes two runs incomparable for no visible reason.
- Overture parquet is large per unit area compared to an OSM extract. Start with a small bbox
  when validating that a `processOverture` path works at all, then widen.
- Sanity-check a download without a full build:
  `duckdb -c "select theme, count(*) from 'data/sources/<name>.parquet' group by 1;"`
- The `pm:overture` source layer is keyed on the parquet `type` field, and feature ids on
  `id` (see the `addParquetSource` call in `Basemap.java`). Rows lacking those fields won't
  map onto the layer handlers.
