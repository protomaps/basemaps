---
name: view-tiles
description: >
  Run the local MapView web app in app/ and open a named .pmtiles archive from the tiles data
  folder in it. Use when asked to view/inspect/look at a tileset, open a pmtiles in the map,
  preview a build, or check how a change renders. Takes the archive name as its argument.
---

# View tiles (run the app, then open an archive in it)

Does two things: **start the `app/` dev server** and **hand back a URL that loads the named
archive**. Run from the `app/` directory at the repo root.

This skill does not build anything. If the archive doesn't exist yet, or the user wants it to
reflect current working-tree Java changes, that's `/build-tiles <area>` first.

## 0. Resolve the archive (the argument)

The archive is user-defined and required — it's the skill's argument (e.g. `/view-tiles
new-york`), naming `tiles/data/<name>.pmtiles`.

- List what's available with `ls ../tiles/data/*.pmtiles`.
- If no archive was given, list them and ask which one.
- If the name doesn't match exactly but one archive is an obvious near-match, say which one
  you picked and why rather than silently substituting.
- If nothing matches, stop and offer `/build-tiles <area>` — don't start the server on a
  path that will 404.

## 1. Start the dev server

The dev server is long-running and must stay up while the user looks at the map, so start it
**detached** — `Bash` with `run_in_background: true`. A foreground `npm run dev` blocks until
timeout and hangs the session.

```
npm run dev
```

Vite serves on **http://localhost:5173** by default; it silently picks 5174, 5175, … if the
port is taken, so read the actual `Local:` line out of the startup output rather than
assuming. If a server is already up on 5173 serving this app, reuse it instead of starting a
second one.

## 2. Build the URL

`vite.config.ts` mounts `../tiles` for any request path ending in `.pmtiles`, so
`tiles/data/<name>.pmtiles` is served at `/data/<name>.pmtiles`. `MapView.tsx` reads its
config from the location hash, and `tiles=` accepts that server-relative path:

```
http://localhost:5173/#tiles=/data/<name>.pmtiles
```

Verify the archive actually serves *before* opening the browser — a range request is what the
map will make:

```
curl -s -o /dev/null -w "%{http_code}\n" -r 0-99 "http://localhost:5173/data/<name>.pmtiles"
```

`206` is success. `404` means the path is wrong (check the name against `ls`); `200` with a
full body means the middleware didn't match.

## 3. Open it

Once the range request returns `206`, open the URL in the default browser — this is the
point of the skill, so don't ask first:

```
open "http://localhost:5173/#tiles=/data/<name>.pmtiles"
```

Quote the URL: the `#` starts a shell comment unquoted, and everything after it — the whole
`tiles=` fragment — is silently dropped, so the app loads the hosted demo bucket instead of
the local build and the map looks plausible but wrong.

Print the URL in the reply too, so the user can reopen or hand-edit the hash.

## Useful hash parameters

`MapView.tsx` reads these from the hash; combine with `&`. Values must not contain `=` — the
parser in `src/utils.ts` splits naively on it.

- `tiles=/data/<name>.pmtiles` — the archive to load. Omitted, it falls back to the hosted
  `DEFAULT_TILES` demo bucket, which is *not* the local build.
- `flavorName=light|dark|white|grayscale|black` — style flavor.
- `local_sprites=true` — serve sprites from `sprites/dist` instead of the published CDN
  sprites. **Off by default — do not add it unless the user asks.** `sprites/dist` is
  generated and currently stale, so turning it on silently swaps in months-old icons. Revisit
  when the sprite pipeline is being worked on.
- `lang=en` — label language.
- `show_boxes=true` — draw label collision boxes.
- `npm_version=<v>` — compare against a published style version.
- `#map=<z>/<lat>/<lon>` — MapLibre's own hash; append to jump somewhere specific.

## Notes

- Default flavor sprites come from the published CDN, so icons reflect what's released, not
  the working tree. That's the intended default for now. If someone does opt into
  `local_sprites=true`, `sprites/dist` is generated and needs `cd sprites && make` (after
  `cargo build --release` builds `spritegen`) to pick up a changed SVG — suspect a stale
  `dist` before suspecting the style.
- The app also serves a drag-and-drop path ("Drag .pmtiles here to view") for archives outside
  `tiles/`; the `tiles=` hash is the scriptable equivalent and the one to prefer.
- The dev server keeps running after the skill finishes — that's intended. Mention that it's
  still up, and stop it with `pkill -f vite` when the user is done.
- Vite hot-reloads style/app edits, but **not** the .pmtiles — a rebuilt archive needs a
  browser reload (hard-reload, since range responses cache).
