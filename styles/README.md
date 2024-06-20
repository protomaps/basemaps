## Generate JSON layers

This only generates the `layers` key of a style; you'll assemble `source`, `glyphs`, `sprite` yourself.

`generate-layers SOURCE_NAME THEME_OR_PATH_TO_THEME`

```
npm run generate-layers protomaps light
```

You can also generate layers by calling the protomaps-base-themes npm package directly, with a path to a theme in your local directory:

```bash
npx --silent generate-layers protomaps ./my-custom-theme.ts > ./dist/my-custom-layers.json
```

with

```ts
// ./my-custom-theme.ts
import { Theme } from "protomaps-base-themes";
const theme: Theme = {
	background: "#cccccc",
	earth: "#e0e0e0",
	park_a: "#cfddd5",
	park_b: "#9cd3b4",
	hospital: "#e4dad9",
	// ...
	// For reference: https://github.com/protomaps/basemaps/blob/424ba2de06d96bf93089e7751adcf75883a25b37/styles/src/themes.ts#L529-L615
};
export default theme;
```

This will generate the custom layers in the file `./dist/my-custom-layers.json`.
