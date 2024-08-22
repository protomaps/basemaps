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

To generate style.json files for each language, run:

```
npm run generate-languages protomaps light https://example.com/your-tilejson-url.json
```

This will create files in the `languages/` folder like `languages/style-en.json`, `languages/style-de.json` etc...

You can inspect the different language styles by running a http server in the `/styles` folder and opening `index.html` in your web browser. Different languages can be chosen with a dropdown menu.
