## Usage

Prepare your environment with:

```
nvm use
npm ci
```

## Generate JSON layers

This only generates the `layers` key of a style; you'll assemble `source`, `glyphs`, `sprite` yourself.

`generate-layers SOURCE_NAME THEME LANG SCRIPT`

For example, if you want to create the layers for a protomaps basemap in the `light` theme where you have a source called `protomaps` in your style.json file, and you want to localize the map to English `en` which uses the `Latin` script, then run this command:

```
npm run generate-layers --silent protomaps light en Latin
```

This will output the layers to your console.

## Generate JSON styles in all themes and languages

To generate style.json files in all themes and supported languages, run:

```
npm run generate-styles https://example.com/your-tilejson-url.json
```

Note that you have to replace the tilejson url with your own.

This will create files in the `dist/styles/` folder like this:

```
dist/
  styles/
    black/
      style-ar.json
      style-bg.json
      ...
    contrast/
      style-ar.json
      style.bg.json
      ...
    ...
```