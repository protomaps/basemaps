---
name: build-tiles
description: >
  Compile the current working-tree Java source into a fresh planetiler jar and build a
  pmtiles tileset for a user-specified source area, streaming live build progress into the
  chat. Use when asked to build tiles, (re)build the tileset, compile-and-run planetiler, or
  build/generate a pmtiles for an area/extract. Takes the source area as its argument.
---

# Build tiles (compile current source, then run + watch)

Always does three things, in order: **compile the current source**, **build the area**, and
**stream progress**. Run from the `tiles/` directory at the repo root.

## 0. Resolve the source area (the argument)

The area is user-defined and required — it's the skill's argument (e.g. `/build-tiles sfbay`).

- A local extract lives at `data/sources/<area>.osm.pbf`; pass `--area=<area>` and planetiler
  uses it directly (no download). List what's available with `ls data/sources/*.osm.pbf`.
- If no area was given, list the local extracts and ask which one.
- If the area isn't present locally but is a known Geofabrik region, add `--download` so
  planetiler fetches it (slower). Default the output to `data/<area>.pmtiles`.

## 1. Compile the current source (always)

Rebuild the fat jar so it reflects working-tree changes — this is the whole point of the
skill, so never skip it:

```
mvn -q package -DskipTests
```

Tests are skipped for speed; this only compiles + packages. Confirm
`target/*-with-deps.jar` exists and its mtime is fresh before continuing. If the build fails,
stop and report the compile error — do not run the old jar.

## 2. Build the area under a progress watch

`Monitor` is a deferred tool: load it first with `ToolSearch` query `select:Monitor`.

Launch planetiler *as* the Monitor command so the watch ends when the build exits. Pipe
through a **single unbuffered `awk` stage** that splits planetiler's carriage-return progress
bar into lines, keeps the per-stage milestones, and `fflush()`es each so events stream live
(a `tr | grep` pipeline block-buffers and dumps everything at the end — do not use it):

```
Monitor(
  description = "build-tiles <area>",
  command = "cd \"$(git rev-parse --show-toplevel)/tiles\" && java -jar target/*-with-deps.jar --area=<AREA> --output=data/<OUT>.pmtiles --force 2>&1 | awk 'BEGIN{RS=\"[\\r\\n]\"} /INF \\[|Finished in|Exception|ERROR/{print; fflush()}'",
  timeout_ms = <expected build seconds * 1000, generously; ~300000 for an NY-size extract>,
)
```

(`git rev-parse --show-toplevel` resolves the repo root wherever the checkout lives, so the
command works regardless of the session's cwd.)

You get one milestone line per ~10s tick — a sequence of progress snapshots, not a single
in-place bar (the chat renders each event as its own message and cannot rewrite a prior one).

## Reading the stream

- `[ne]` — Natural Earth prep (~13s).
- `[osm_pass1]` — reads nodes/ways/relations (fast).
- `[osm_pass2]` — the long phase; watch `ways: [ … NN% … ]` climb (e.g. 7→39→68→98%).
- `[osm_water]`, `[osm_land]`, `[landcover]`, `[sort]` — quick.
- `[archive]` — encodes/writes tiles z0…z15; `archive NNNMB` is the final size.
- `Finished in …` / `FINISHED!` — done.

Relay progress by stage and percentage, not raw line dumps.

## Notes

- `--force` is required when re-running to an existing `--output`; planetiler otherwise aborts
  immediately (`… already exists, use the --force argument …`). That abort exits before any
  `INF [stage]` line, which is why the filter also matches `Exception|ERROR` — otherwise a
  config crash looks like a silent hang.
- A full NY extract is ~2.5 min; sfbay ~1 min. Set `timeout_ms` generously above that.
- The harness re-invokes you when the build process exits, so you always get a completion
  signal even between progress ticks.
