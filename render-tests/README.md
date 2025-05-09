# Render Tests

## Usage

Use the node version in the nvmrc file with `nvm use`.

First build the styles package locally:

```
cd ../styles
npm ci
npm run build
```

Then install the dependencies in the render-test folder with:

```
npm i
```

Then build the render test runner:

```
npm run build
```

Then run the render tests with:

```
npm run test
```

This will create a `results.html` file. Have a look at it with something like

```
npx serve .
```

## Updating Tests

When `expected.png` images should change, you can tell the render test runner to copy the `actual.png` to the `expected.png` images with:

```
UPDATE=true npm run test
```

## License

The code in this folder has been derived from the render tests of MapLibre GL, a fork of Mapbox GL. The license therefore is based on MapLibre's license (BSD), see [LICENSE.md](./LICENSE.md).
