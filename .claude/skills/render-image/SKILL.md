---
name: render-image
description: >
  Render a static map image (PNG) from a local .pmtiles archive at a given location and zoom.
  Use when asked to render an image, screenshot the map, or produce a static map picture. NOT
  YET IMPLEMENTED — a native renderer is planned; this skill is a stub.
---

# Render image (STUB — not yet implemented)

**This skill is a placeholder.** The intended implementation is a **native renderer**, not
in-browser screenshotting. Until that lands, do not improvise a substitute — say it's not
implemented yet and offer `/view-tiles <name>`, which opens the same archive in a live map.

## Intended interface (when implemented)

```
/render-image <archive> [--center=<lon,lat>] [--zoom=<z>] [--size=<w>x<h>] [--flavor=<name>]
```

Reading `tiles/data/<archive>.pmtiles` and writing a PNG.

## What already exists (context for whoever implements this)

- **`render-tests/`** — a fixture-comparison harness (puppeteer + maplibre-gl + pixelmatch),
  not a general renderer. It drives per-test `style.json` fixtures with a `metadata.test`
  block (`width`, `height`, `flavor`, `lang`) plus `center`/`zoom`, serves pmtiles over
  express on port 2900, and diffs against `expected.png`. Useful as a reference for how a
  style + archive + camera turn into pixels; wrong shape to reuse as a one-shot CLI.
- **`app/`** — the live MapView. `vite.config.ts` serves `../tiles` for any `.pmtiles` path;
  `MapView.tsx` takes `tiles=`, `flavorName=`, `lang=`, `local_sprites=` from the location
  hash and passes `hash: "map"` to MapLibre, so `#map=<z>/<lat>/<lon>` aims the camera. See
  `/view-tiles` for the details.
- **`styles/`** — the `@protomaps/basemaps` style package (`namedFlavor`, `layers`) that both
  of the above build their style from. A native renderer needs the same style JSON.

## Notes

- Flavors are `light`, `dark`, `white`, `grayscale`, `black`.
- Sprites: published CDN sprites are the default; `sprites/dist` holds locally-generated ones
  and is currently stale. See `/view-tiles`.
- When implementing, prefer reading the archive directly over standing up an HTTP server —
  the whole point of the native path is skipping the browser.
