---
name: download-osm
description: >
  Cut an OSM extract for a named place using the SliceOSM API and save it into
  tiles/data/sources/ as a .osm.pbf ready for /build-tiles. Use when asked to download/fetch/cut
  an OSM extract, get the data for a place, or prepare sources before a build. Takes the place
  name as its argument.
---

# Download OSM (SliceOSM extract for a named place)

Turns a **place name** into a `.osm.pbf` in `tiles/data/sources/`. Does not build anything —
`/build-tiles <name>` picks it up from there.

Do **not** use planetiler's `--download` or Geofabrik for this. SliceOSM cuts an arbitrary
bbox from a live OSM Express database, so it isn't limited to Geofabrik's fixed regions and
the snapshot is current.

API: **`https://slice.openstreetmap.us/api/`** (docs: https://github.com/SliceOSM/sliceosm-api)

## 0. Place name → bbox (do this yourself, no geocoder)

The place is the skill's argument (e.g. `/download-osm minneapolis`). **Derive the bounding
box from your own world knowledge — do not call a geocoding service.**

Aim for a box that covers the place's built-up extent plus a little margin. Rough is fine and
expected; a slightly generous box costs extract time, a too-tight one silently clips the map
at the edges, so err generous.

State the bbox you chose and roughly what it covers before submitting — it's a guess, and the
user is the one who knows if it's wrong. Ask first only when the name is genuinely ambiguous
(Portland OR vs ME; Cambridge UK vs MA) or the box is large enough to risk the node limit.

## 1. Submit the task

`POST /` with a JSON body. **The bbox order is `[min_lat, min_lon, max_lat, max_lon]` — latitude
first.** This is the single biggest trap here: it matches neither GeoJSON (lon,lat) nor
planetiler's `--bounds=w,s,e,n` (lon first). Swapping them either errors or silently cuts a
region on the other side of the world.

```
curl -s -X POST https://slice.openstreetmap.us/api/ \
  -d '{"Name":"<slug>","RegionType":"bbox","RegionData":[<min_lat>,<min_lon>,<max_lat>,<max_lon>]}'
```

The response is a bare **UUID** on success, or an error message — it is not JSON, so check it
looks like a UUID before polling. `Name` is just a human label; use the slug.

For a non-rectangular cut, `"RegionType":"geojson"` takes a Polygon/MultiPolygon geometry in
`RegionData` (normal GeoJSON lon,lat order, unlike the bbox form). There's an unspecified
vertex limit.

## 2. Poll until complete

`GET /{uuid}` returns progress; the fields are strings:

```
{"Timestamp":"","CellsProg":"","CellsTotal":"","NodesProg":"","NodesTotal":"",
 "ElemsProg":"","ElemsTotal":"","SizeBytes":"","Elapsed":"","Complete":""}
```

Poll with `Monitor` (a deferred tool: load it with `ToolSearch` query `select:Monitor`) so
progress streams and the watch ends when the job does:

```
Monitor(
  description = "sliceosm <slug>",
  command = "for i in $(seq 1 120); do r=$(curl -s https://slice.openstreetmap.us/api/<uuid> || true); echo \"$r\" | grep -q '\"Complete\":\"\\?true' && { echo \"COMPLETE $r\"; break; }; echo \"$r\"; sleep 5; done",
  timeout_ms = 600000,
)
```

Emit the raw progress line each tick and break on `Complete` — a loop that only prints on
success is silent through a stuck job, which is indistinguishable from a slow one. The task
URL is valid for 24 hours.

Relay progress as nodes/elements percentages, not raw JSON dumps.

## 3. Save with the naming convention

Download to `data/sources/` — **never** to the repo root or `tiles/`:

```
curl -s -o data/sources/<slug>-<YYMMDD>.osm.pbf https://slice.openstreetmap.us/api/<uuid>.osm.pbf
```

**Naming convention: `<place-slug>-<YYMMDD>.osm.pbf`**, where:

- `<place-slug>` is the place, lowercased and hyphenated (`new-york`, `sf-bay`).
- `<YYMMDD>` is the **OSM snapshot date** from the task's `Timestamp` field (or the API root's
  `Timestamp`) — *not* today's date, and not the download date.

This matches the existing `new-york-260503.osm.pbf` (a 2026-05-03 snapshot). The date is the
point: extracts of the same place from different days are different data, and `pmtiles show`
on a built archive reports `planetiler:osm:osmosisreplicationtime`, so a dated filename is
what lets you line an archive up with its source. Undated names like `nyc.osm.pbf` are older
files that predate this convention — don't add more.

The slug becomes the `/build-tiles` argument, since planetiler resolves `--area=<name>` to
`data/sources/<name>.osm.pbf`. So this saves as `new-york-260503.osm.pbf` and builds with
`/build-tiles new-york-260503`.

Verify before reporting success:

```
ls -lh data/sources/<slug>-<YYMMDD>.osm.pbf
```

A 0-byte or few-hundred-byte file means the download raced ahead of completion or returned an
error body — check it's really a pbf, delete it if not. A corrupt pbf fails confusingly later,
deep inside a build. `GET /{uuid}.osm.pbf` 404s until the task completes, so an early grab
saves an error page under a `.osm.pbf` name.

Two cheap confirmations:

```
file data/sources/<slug>-<YYMMDD>.osm.pbf                  # "OpenStreetMap Protocolbuffer Binary Format"
osmium fileinfo -e data/sources/<slug>-<YYMMDD>.osm.pbf    # bbox, counts, replication timestamp
```

`file` needs nothing extra. `osmium` is a separate install (`osmium-tool`; `brew install
osmium-tool` on macOS) — `render-tests/generate_pmtiles.sh` depends on it too, so a full
checkout usually has it, but skip this check rather than failing the skill if it's absent.

`osmium fileinfo -e` reports `osmosis_replication_timestamp` — the authoritative source for
the `<YYMMDD>` in the filename, and the same value `pmtiles show` will later report on any
archive built from this extract.

**The extract's actual bbox is wider than the one you asked for.** SliceOSM keeps ways and
relations whole when they cross the boundary, so long features (highways, boundary relations)
drag geometry well past the box — a San Diego request of `-117.35,-116.85` came back spanning
to `-116.11`, over half a degree beyond. This is normal and not a bug. If you need a hard
edge, clip at *build* time with planetiler's `--bounds=<w,s,e,n>` (lon-first there, unlike
here); don't try to fix it by shrinking the request.

## Notes

- **Node limit is 100,000,000** (`NodesLimit` at `GET /`). A bbox over it is rejected. For
  scale: the New York state extract is ~56M nodes, so metro-area boxes are comfortable and a
  whole-country box is not. If rejected, cut a smaller box rather than retrying.
- `GET /` also reports `QueueSize` — if a submit seems to hang, check whether the server is
  backed up rather than assuming the job failed.
- `GET /{uuid}_region.json` returns the submitted geometry immediately, which is a quick way
  to confirm the bbox landed where you meant before waiting on the extract.
- Check `ls data/sources/*.osm.pbf` first — if a recent extract for the place is already
  there, offer it instead of re-cutting.
- SliceOSM has no fixed-region concept, so there's no `us/` path prefix and none of the
  slash-in-the-name problems the Geofabrik path had.
