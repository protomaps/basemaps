# Sprites

You will need a Rust compiler.

`cargo build --release` will create the `spritegen` binary in `target/release`

To generate spritesheets for the light theme:

`./target/release/spritegen refill.svg flavors/light.json dist/light`

Creates `dist/light.json`, `dist/light.png`, `dist/light@2x.json`, `dist/light@2x.png`

In the basemaps viewer (run `npm run dev` in `/app`), check the "local sprites" box to load sprites from `/sprites/dist`

## All Themes

```
make all
```
