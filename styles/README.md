## Usage

Prepare your environment with:

```
nvm use
npm ci
```

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
      ar.json
      bg.json
      ...
    contrast/
      ar.json
      bg.json
      ...
    ...
```