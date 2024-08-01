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

## Generate JSON style

To generate a full style.json file, run:

```
npm run --silent generate-style protomaps light https://example.com/your-tilejson-url.json > style.json
```