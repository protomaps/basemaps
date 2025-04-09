# Data Licenses & Attribution

The license and attributions requirements of protomaps/basemaps output depends on the input data.

By default, data sources include:

### OpenStreetMap

[OpenStreetMap](https://www.openstreetmap.org/) is licensed under the [ODbL](https://opendatacommons.org/licenses/odbl/), a [share-alike](https://en.wikipedia.org/wiki/Share-alike) license which requires attribution. Example:

```
© OpenStreetMap
```

### osmdata.openstreetmap.de

OSM-derived water and earth polygons are sourced from [osmdata.openstreetmap.de](https://osmdata.openstreetmap.de), which is also ODbL licensed. This data is produced by the [osmcoastline](https://github.com/osmcode/osmcoastline) program written by [Jochen Topf](https://github.com/joto) and contributors.

### Natural Earth

Natural Earth is a vector map dataset released to the public domain. The details of its license:

```
All versions of Natural Earth raster + vector map data found on this
website are in the public domain. You may use the maps in any manner,
including modifying the content and design, electronic dissemination,
and offset printing. The primary authors, Tom Patterson and Nathaniel
Vaughn Kelso, and all other contributors renounce all financial claim
to the maps and invites you to use them for personal, educational, and
commercial purposes.

No permission is needed to use Natural Earth. Crediting the authors is
unnecessary.
```

More details can be found on the Natural Earth [Terms of Use](http://www.naturalearthdata.com/about/terms-of-use/) page.


### Daylight Landcover

If displaying the `landcover` basemap layer, landcover is derived from the [ESA WorldCover dataset](https://esa-worldcover.org/en/data-access), available under [CC-BY 4.0 DEED](https://creativecommons.org/licenses/by/4.0/).

See the [Overture Maps Attribution Guidelines](https://docs.overturemaps.org/attribution/).

### Mapzen

If displaying the default Light and Dark styles, POI icons are derived from the [tangrams/icons](https://github.com/tangrams/icons) icon set, which is MIT licensed.

```
The MIT License (MIT)

Copyright (c) 2017 Mapzen, Linux Foundation

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

### Minzoom Configuration
The minzoom configuration in `tiles/src/resources/places.csv` is derived from Tilezen for `kind=country` features and from Natural Earth for `kind=region,locality` features.
