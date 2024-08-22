## Usage

Prepare your environment with:

```
nvm use
npm ci
```

## Generate JSON layers

This only generates the `layers` key of a style; you'll assemble `source`, `glyphs`, `sprite` yourself.

`generate-layers SOURCE_NAME THEME`

```
npm run generate-layers protomaps light
```