---
name: analyze-tiles
description: >
  Inspect a built .pmtiles archive — header/metadata, per-layer size breakdown, and the biggest
  tiles — using pmtiles show and planetiler's TileSizeStats plus duckdb. Use when asked to
  analyze/inspect/profile a tileset, find what's making tiles big, compare two archives' sizes,
  or check an archive's metadata. Takes the archive name as its argument.
---

# Analyze tiles (what's in the archive, and what's making it big)

Read-only inspection of `tiles/data/<name>.pmtiles`. Run from the `tiles/` directory at the
repo root.

**Requires two external tools** that this repo does not vendor: the `pmtiles` binary
([go-pmtiles](https://github.com/protomaps/go-pmtiles) releases) and
[`duckdb`](https://duckdb.org). Check with `command -v pmtiles duckdb` before relying on
them; if either is missing, say so and point at the install rather than improvising a
substitute. Note `tiles/Makefile` invokes `pmtiles` as `~/Downloads/pmtiles`, so a checkout
may have the binary without it being on PATH.

Two independent tools, for two different questions. Pick by what was asked — don't run both by
reflex:

- **"What is this archive?"** → `pmtiles show` (instant).
- **"Why is it big / which layer dominates?"** → `TileSizeStats` + duckdb (a minute on a large
  archive).

## 0. Resolve the archive (the argument)

Required — names `tiles/data/<name>.pmtiles` (e.g. `/analyze-tiles new-york`). List with
`ls -lh data/*.pmtiles`. If it doesn't exist, stop and offer `/build-tiles <area>`.

## 1. Header and metadata — `pmtiles show`

```
pmtiles show data/<name>.pmtiles
```

Gives bounds, min/max zoom, center, tile/entry/content counts, compression, clustering, and
the planetiler provenance keys. The useful ones to actually read back:

- `planetiler:osm:osmosisreplicationtime` — **the OSM snapshot date of the source extract.**
  This is how you tell whether an archive predates a data change you're looking for; a missing
  feature is often a stale extract, not a code bug.
- `addressed tiles count` vs `tile contents count` — the gap is deduplication (identical tiles
  stored once). A big gap is normal and healthy, especially for sparse extracts.
- `min zoom` / `max zoom` — confirms a `--maxzoom` actually took effect.

`vector_layers` is elided in the default output; add `--metadata` for the full JSON when the
question is about layer/attribute schema.

Related subcommands worth knowing: `pmtiles verify <input>` checks archive structure (use when
a build was interrupted and corruption is suspected — the Makefile has a `clean-pmtiles`
target for exactly this), and `pmtiles tile <path> <z> <x> <y>` dumps one tile.

## 2. Size breakdown — TileSizeStats + duckdb

Planetiler's stats tool runs standalone against an already-built archive:

```
java -cp target/*-with-deps.jar com.onthegomap.planetiler.util.TileSizeStats \
  --input=data/<name>.pmtiles \
  --output=/tmp/<name>-layerstats.tsv.gz
```

It logs the biggest tiles directly (with a demo-map link and the dominant layer per tile),
which is often the whole answer — read the `Biggest tiles (gzipped)` block before writing any
query.

It also writes a gzipped TSV, one row per tile *per layer*, with columns:

```
z  x  y  hilbert  archived_tile_bytes  layer  layer_bytes  layer_features
layer_geometries  layer_attr_bytes  layer_attr_keys  layer_attr_values
```

Query it with duckdb (reads the `.gz` directly, no decompression step):

```
duckdb -c "select layer, sum(layer_bytes)/1024/1024 as mb, sum(layer_features) as feats
           from read_csv('/tmp/<name>-layerstats.tsv.gz', delim='\t', header=true)
           group by layer order by mb desc;"
```

Other cuts that answer real questions:

- **Which zoom is expensive** — `group by z` instead of `layer`.
- **Worst individual tiles** — `select z,x,y,layer,layer_bytes … order by layer_bytes desc limit 20`.
- **Attribute bloat** — compare `layer_attr_bytes` against `layer_bytes`; a high ratio means
  the tags are the payload, not the geometry. Relevant when a change adds per-feature
  attributes (shield text, network names) rather than geometry.

To get layerstats at build time instead, planetiler takes `--output-layerstats`, which writes
`<output>.layerstats.tsv.gz` alongside the tileset — cheaper than a separate pass if you know
in advance you'll want it.

## Comparing two archives

The common real task ("did my change bloat the tiles?") is a diff, and the repo keeps prior
builds around (`ny-shields.pmtiles`, `ny-norefs.pmtiles`, …) for this. Run TileSizeStats over
both, then join:

```
duckdb -c "select a.layer, sum(a.layer_bytes)/1024/1024 as before_mb,
                  sum(b.layer_bytes)/1024/1024 as after_mb
           from read_csv('/tmp/before.tsv.gz', delim='\t', header=true) a
           full join read_csv('/tmp/after.tsv.gz', delim='\t', header=true) b
             on a.z=b.z and a.x=b.x and a.y=b.y and a.layer=b.layer
           group by a.layer order by after_mb - before_mb desc;"
```

Only compare archives built from the **same extract** — a different source area or snapshot
date makes the numbers meaningless, and `pmtiles show` tells you both.

## Notes

- TileSizeStats decompresses and re-parses every tile, so it's minutes on a large archive and
  seconds on a small one. Consider testing a query shape against a small archive
  (`data/output.pmtiles`) before running it on a 500MB one.
- Report layer sizes in MB with the dominant layer named, not raw duckdb tables — the answer
  is usually "pois is 40% of the archive", not a grid of numbers.
- These tools read the archive only; nothing here can corrupt a build.
