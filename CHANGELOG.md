Tiles 4.13.7
------
- Add `platform_edge` to ignored railway types by @pietervdvn [#547]

Tiles 4.13.6
------
- Translate POI min_zoom= assignments to MultiExpression rules [#539]

Tiles 4.13.5
------
- Translate POI kind= assignments to MultiExpression rules [#537]

Tiles 4.13.4
------
- remove kind=protected_area which made historical city centers appear as parks [#531]

Tiles 4.13.3
------
- sanitize commas in building height tags by @xDefcon [#517]

Tiles 4.13.2
------
- prioritize aeroway=aerodrome over landuse=industrial [#526]

Tiles 4.13.1
------
- add in British Columbia label ranking to places.csv [#521]

Tiles 4.13.0, Styles 5.7.0
------
- addition of more sprites for POIs
- certain POI `kinds` are boosted to lower zoom levels using QRank data

Tiles 4.12.0, Styles 5.6.0
------
- addition of generic, US:I and NL:S-road highway shields [#238]
- add NL and US CartographicLocales
- add getLocale to CountryCoder
- proper `shield_text` and `network` values in road features
- limit `shield_text` display to 5 characters

Styles 5.5.0
------
- Add oneway arrows to roads [#499]

Tiles 4.11.9
------
- Remove extra large buffer at z15 POIs to reduce tile size [#493]

Tiles 4.11.8
------
- Fix aeroway polygons [#496]

Tiles 4.11.7
------
- Render access=no,private roads only at zoom 15 [#491]

Styles 5.4.1
------
- Show macrohoods [#487]

Styles 5.4.0
------
- add `generate_style` to CLI bin path
- `generate_style` can take a `.ts` or `.js` ESM Flavor, or a `.json` Flavor

Tiles 4.11.6
------
- Fix disputed tag on boundaries with labels only on OSM ways [#475]

Tiles 4.11.5
------
- Bump to planetiler v0.9.0

Tiles 4.11.4
------
- More code quality refactorings [#468, #464]

Tiles 4.11.3
------
- Code quality refactorings [#458, #459, #460, #461]

Tiles 4.11.2, Styles 5.3.0
------
- Move curated generalized places from natural earth DB to embedded CSV [#452, #445]
- text-variable-anchor for place points [#451]
- type-only imports of maplibre-style-spec [#448]

Tiles 4.11.1
------
- Use country min and maxzooms from tilezen [#445]

Tiles 4.11.0
------
- Tune place label density [#437]

Tiles 4.10.0, Styles 5.2.0
------
- Restrict region labels to United States, Canada, Brazil, India, China, and Australia [#441]

Tiles 4.9.0, Styles 5.1.0
------
- Use sort_rank to order places correctly across tile boundaries [#436, #434]

Tiles 4.8.2
------
- port places to use Matchers [#434]

Tiles 4.8.1, Styles 5.0.2
------
- change earth pixel minimum size to 1px [#428]
- export localization helper functions in style via @russss [#431]

Styles 5.0.1
------
- fix passing of regular font via @Shane98c [#427]

Tiles 4.8.0
------
- add island names [#294]

Tiles 4.7.1
------
- adjust vector tile feature IDs to start at id=1 [#420]

Tiles 4.7.0
------
- port water, earth and landcover to use Matchers [#416]

Tiles 4.6.1
------
- Refactor of roads layer to use Matchers [#407]

Tiles 4.6.0
------
- Symbolize kind=dam in landuse layer instead of waterway layer [#404]
- Fix kind=grassland [#405]

Tiles 4.5.0
------
- Refactor of landuse to use Matchers [#399]
- Fix roads trunk links minzoom [#403]

Tiles 4.4.1
------
- Make FeatureID=0 consistent for preprocessed OSM land/water [#394]

Tiles 4.4.0
------
- Improve the appearance of broken roads at zooms 0-6 by using relations [#386]

Tiles 4.3.1
------
- Capture `kind=address` in `buildings` from building centroids via @pietervdvn [#387]

Styles v5.0.0
------
- This is a breaking major version.
- NPM package renamed from `protomaps-themes-base` to `@protomaps/basemaps`
- script-includes script name renamed from `protomaps-themes-base.js` to `basemaps.js`
- `Theme` type renamed to `Flavor`
- migrate `CONTRAST` flavor to external repo
- Precomputed style JSON is no longer published to NPM
- All layer generation functions unified into a single `layers` method. To migrate:

Before:

```js
default("example","light","en")
labels("example","light","en")
layersWithCustomTheme("example",theme,"en")
layersWithPartialCustomTheme("example",theme,"en")
noLabelsWithCustomTheme("example",theme)
labelsWithPartialCustomTheme("example",theme,"en")
```

After:

```js
layers("example",namedFlavor("light"),{lang:"en"})
layers("example",namedFlavor("light"),{lang:"en",labelsOnly:true})
layers("example",flavor,{lang:"en"})
layers("example",{...flavor,buildings:"red"},{lang:"en"})
layers("example",flavor)
layers("example",{...flavor,buildings:"red"},{lang:"en",labelsOnly:true})
```

Tiles v4.3.0, Styles v4.5.0
------
- Add housenumbers via @SiarheiFedartsou [#380]

Tiles v4.2.0
------
- add `--clip` option to tile generation which clips the entire tileset by a polygon or multipolygon. [#51]

Styles v4.4.0
------
- Improve boundary appearances at low zooms
- give all text halos for contrast improvements, remove halo-blur
- fix lake small font sizes

Tiles v4.1.0
------
- bump planetiler to 0.8.4-SNAPSHOT, via @wipfli [#365]

Tiles v4.0.4
------
- fix antarctica landcover not extending to bottom with Natural Earth, via @wipfli [#337]

Styles v4.3.1
------
* Fix `$type` in water filters to be MapLibre v5.0 compatible.

Tiles v4.0.3
------
- fix water `kind=ocean` via @dericke [#329]

Styles v4.3.0
------
* Add landcover styling [#154]
* Adjust appearance of landuse layers to fade in after landcover

Styles v4.2.0
------
* add icons for a few lapis and slategray POIs [#238, #163]
* adjust light theme earth color
* add fonts and landcover as optional properties of `Theme` interface

Tiles v4.0.2
------
- fix buildings `min_height` via @JfrAziz [#323]

Styles v4.1.0
------
- add icon sprites for six green kinds [#238]
- point v4 styles at v4 sprites assets URL
- change light theme water color to blue instead of white for contrast
- consistent halo size across layers
- add landuse `kind=runway`

Tiles v4.0.1
------
- fix `roads` `kind=ferry`, remove ferry `kind_detail` [#312]

Styles v4.0.0 + Tiles v4.0.0
------
- Remove `medium_road`
- Move all current `transit` features into `roads`
- `pier` is now `kind=path` `kind_detail=pier`
- remove `level` and `layer` keys
- change `link=1` to `is_link=true`
- change `level=-1` and `level=1` to `is_bridge=true` and `is_tunnel=true`
- Remove `transit_pier` theme property
- `kind_detail` in boundaries is the min admin level
- add placeholder `sort_rank` to relevant layers
- rename peak `ele` to `elevation`


Styles v4.0.0-alpha.1
------
- remove all `pmap:` prefixes from style (breaking change) (#282)
- refactor layers as described in tiles v4.0.0-alpha.3

Tiles v4.0.0-alpha.3
------
- remove all `pmap:` prefixes (breaking change) [#282]
- remove `physical_point`, `natural` and `physical_line` layers.
	- move into `landuse`, `water`, `pois` and `earth` layers to align with Tilezen. 
	- Some layers are now mixed geometry types.

Styles v4.0.0-alpha.0
------
- Add lang and script parameters to TypeScript style generation [#275]

Tiles v4.0.0-alpha.3
------
- Replace Natural Earth places at low zooms with OSM [#289]

Tiles v4.0.0-alpha.2
------
- Segment name by script via @wipfli [#273]

Tiles v3.7.1
------
- change fountains in `water` layer to `kind=fountain` [#279]

Styles v3.1.0
------
- change to `tsup` for building ESM/CJS/IIFE outputs via @iwpnd [#231]

Tiles v3.7.0
------
- Use a positioned glyph font encoding for Devanagari via @wipfli [#265]

Tiles v3.6.0
------
- More places kinds (port from Tilezen), refine zoom levels via @nvkelso [#259]
- Add `pmap:script` for non-latin names via @wipfli [#254]

Tiles v3.5.2
------
- Exclude buildings from `transit` layer. via @pietervdvn [#249]

Styles v3.0.0
------
- introduce dependency on a spritesheet:
		- example for `light` theme https://protomaps.github.io/basemaps-assets/sprites/v3/light`
		- Style deployments need to depend on the spritesheet assets in addition to fonts.
- Migrate town labels to single "symbol" style layer to specify linked "icon" and "text", using the new spritesheet to source the icon's image. This resolves a bug where townspot icons in the old places_locality_circle "circle" style layer were often orphaned (still displayed) even if the related text in the places_locality "symbol" style layer couldn't be placed. The circle style layer has been removed and consolidated into the symbol style layer.

Styles v2.0.0
------
- Standardize JS package.json on ES6 modules

Tiles v3.5.1
------
- Order landcover by kind consistently [#154]

Tiles v3.5.0
------
- Add Daylight Landcover from zooms 0-7. [#154]

Tiles v3.4.1
------
- Improve boundaries generalization [#200]

Tiles v3.4.0, Styles v2.0.0-alpha.5
------
- Add `village_green`, `landuse`, `allotments` to POI layer via @lenalebt [#204]
- Add to styles via `landuse_urban_green` layer

Tiles v3.3.0
------
- Improve water generalization detail by doing area filtering post-merge [#198]

Tiles v3.2.0
------
- Add `village_green` and `allotments` to landuse layer via @lenalebt [#206]
- Remove non-deterministic ordering ID from POIs
- stricter parsing of building height values [#205]

Tiles v3.1.0
------
- Boundaries admin_level 3 and 5 are included along with 4 and 6, respectively [#189]

Tiles v3.0.1
------
- Fix pedestrian bridge areas

Styles v2.0.0-alpha.4
------
- Fix ordering issues related to pedestrian bridges.

Tiles v3.0.0-pre4
------
- Fix appearance of NE boundaries at low zooms.
- Add `pmap:kind`=`bus_stop` to POIs. via [@eikes](https://github.com/eikes)

Tiles v3.0.0-pre3
------
- Make `disputed` tag on boundaries consistent [#190]

v3.0.0-pre1
------
- **Release date:** 2023-07-10.
- **Credits:** [@nvkelso](https://github.com/nvkelso), [@bdon](https://github.com/bdon), [@jamesscottbrown](https://github.com/jamesscottbrown), [@tyrauber](https://github.com/tyrauber), [@HeikoGr](https://github.com/HeikoGr), and [@Edefritz](https://github.com/Edefritz)

#### BREAKING CHANGES

- **landuse** layer: Move `national_park`, `protected_area`, and `nature_reserve` to landuse layer from natural layer as they are not natural but cultural.  [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: OSM features with `leisure` tag are now mapped to individual kind values instead of all erroneously to `park`. [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer:  Features with `amenity` tag are mapped to individual kind values instead of all erroneously to `school` when not `hospital`. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural** layer: Moved `national_park`, `protected_area`, and `nature_reserve` to **landuse** layer. [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: OSM `state` (province) is now `region` (preparing for v4 Tilezen changes) [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: OSM `city` is now `locality` (preparing for v4 Tilezen changes) [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: OSM `town` and `village` are now `locality` ("city") instead of `neighbourhood` (fixing v2 series bug and preparing for v4 Tilezen changes) [#37](https://github.com/protomaps/basemaps/pull/37)

#### ENHANCEMENTS

- **Significant performance improvements** to reduce p99 file sizes globally (-XX%) at all zooms to under 200 kb. Changes include:  ...
- **Core Tilezen schema properties added:**:
	* `pmap:kind` is present on every feature now in every layer
	* `pmap:kind_detail` is optionally present on some features in some layers
	* **DEPRECATION WARNING**: in v4, the `pmap:` prefix will be removed (so `pmap:kind` will become simply `kind`)
- **Core OSM tags for different kinds of places have been augmented, but...**
	* **DEPRECATION WARNING**: These OSM tags are marked for deprecation in v4 schema, do not use these for styling. They aren't needed and take up extra file size.
	* If an explicate value is needed it should be a kind, or included in kind_detail. If there is a gap, please file an issue so it can be addressed.
- **Less names, now with label placements:**
	* Names have been removed from most features at early and mid-zooms (to also reduce file size)
	* Names have been kept on some features at early- and mid-zooms when they are known to be used for map display
	* When features do have names, a pmap:min_zoom is added to achieve more predictable label collisions client side.
		* **DEPRECATION WARNING**: in v4, the `pmap:` prefix will be removed (so `pmap:min_zoom` will become simply `min_zoom`)
- **Zoom ranges** of most features have been adjusted to remove details (and reduce file size) at early and mid-zooms. [#47](https://github.com/protomaps/basemaps/pull/47)
- **boundaries** layer: Add boolean indication of `disputed` lines   [#37](https://github.com/protomaps/basemaps/pull/37)
- **boundaries** layer: Use Natural Earth for low zoom boundary lines, including disputed status   [#47](https://github.com/protomaps/basemaps/pull/47)
- **boundaries** layer: Don't export **admin_level** `1`, `3`, `5`, and `7` – those generally aren't styled and are taking up a lot of file size even at very low zooms   [#47](https://github.com/protomaps/basemaps/pull/47)
- **boundaries** layer: Adjust zoom range of `county` lines to show starting at zoom 8 instead of 10. [#47](https://github.com/protomaps/basemaps/pull/47)
- **buildings** layer: Adjust zoom ranges, push `building_part` kind features to later zooms (to reduce file size)   [#47](https://github.com/protomaps/basemaps/pull/47)
- **buildings** layer: Quantize `height` tag at mid-zooms so more buildings merge in post-processing (to reduce file size)   [#47](https://github.com/protomaps/basemaps/pull/47)
- **buildings** layer: Add optional `min_height` property to enable 2.5D and 3D visualizations  [#47](https://github.com/protomaps/basemaps/pull/47)
- **earth** layer: Add 8 px buffer for Natural Earth sourced features at low zooms. [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: Add new `kind` values for: `beach`, `pier`, `zoo`, `military`, `naval_base`, `airfield`, `cemetery`, `recreation_ground`, `winter_sports`, `quarry`, `park`, and `forest`.   [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: Add new `national_park` kind (versus `park`), looking at `operator` tag to derive this from OSM to emphasize US National Park Service in United States of America and elsewhere [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: Improve detection of `forest` kind (versus `wood`), looking at `operator` tag to derive this from OSM to emphasize National Forests in United States of America and elsewhere [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: Remove names to reduce tile sizes (see the **pois** layer for calculated label points) [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: Reduce small polygons to improve figure-ground contrast and reduce file size. [#47](https://github.com/protomaps/basemaps/pull/47)
- **landuse** layer: Add `boundary`, `landuse`, `leisure`, and `natural` properties from OSM tags (though don't use them to be v4 safe) [#47 ](https://github.com/protomaps/basemaps/pull/47 )
- **natural** layer: Add new `kind` value for `grass` [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural** layer: Update to the same `pmap:kind` coallesce as in the **landuse** layer [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural** layer: Remove `boundary` and `leisure` properties (they moved to **landuse** layer along with the relevant featurse) [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural** layer: Remove small polygons to improve figure-ground contrast and reduce file size. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural** layer: Merge polygons with similar attributes to reduce file size. [#37](https://github.com/protomaps/basemaps/pull/37)
- **natural_lines** layer: Show `river` lines 3 zooms earlier at zoom 9. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_lines** layer: For linear water features like rivers – add tunnel / bridge indicator with `pmap:level` (same as **roads** layer) [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_lines** layer: Add `bridge`, `tunnel`, and `layer` properties from OSM. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_lines** layer: Add  `intermittent` boolean indicators. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_points** layer: Show `ocean` and `sea` label points much earlier. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_points** layer: Add calculated label positions for water features (like `lake`, `reservoir`, `swimming_pool` and other terrestial water features; and `bay`, `strait`, `fjord` marine featuers) [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_points** layer: Add  `alkaline`, `intermittent`, and `reservoir` boolean indicators. [#47](https://github.com/protomaps/basemaps/pull/47)
- **natural_points** layer: Add  `natural`, `landuse`, `leisure`, `water`, and `waterway` properties from OSM  (though don't use them to be v4 safe) [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Use Natural Earth for low-zoom `locality` features (to reduce file size) [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Add server-side label collisions with a label grid to reduce number of places in tiles, especially at mid-zooms. [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Remove `country` and `region` labels from mid- and high-zooms (still present at low-zooms) [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Add `pmap:kind_detail` for original OSM "place" tag values (including "city" instead of "locality") [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Add `pmap:population_rank` for a quantized and backfilled population approximation. [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Curate custom `min_zooms` for `country` and `region` (state/province) labels to removes many labels from early zooms when they couldn't reasonably be labeled anyhow (to reduce file size) [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add new `national_park` kind (versus `park`), looking at `operator` tag to derive this from OSM to emphasize US National Park Service in United States of America and elsewhere [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Improve detection of `forest` kind (versus `wood`), looking at `operator` tag to derive this from OSM to emphasize National Forests in United States of America and elsewhere [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add allow listed OSM features from natural (`beach`) and landuse (`cemetery`, `recreation_ground`, `winter_sports`, `quarry`, `park`, `forest`, `military`) tags. [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add `amenity`, `attraction`, `boundary` (select), `craft`, `historic`, `landuse` (select), `natural` (select), `shop`, `railway` (select), and `tourism` features and exported OSM tag to schema property (though don't use them to be v4 safe) [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add additional pasthru unrestricted OSM values from `attraction`, `craft`, `historic`, `landuse`, `leisure`, and `natural` tags. This augments `amenity`, `railway`, `shop`, and `tourism`. [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add `cuisine`, `religion` tags (though use `pmap:kind_detail` instead to be v4 safe) [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add `iata` property on `airport` kind features to indicate if they have international service. [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Derive label centroids from OSM ways and relations features to hugely increasing the number of included features [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add server-side label collisions with a label grid to reduce number of features in tiles at mid-zooms (all features still included at `max_zoom`) [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Add smattering of higher priority (even within a kind) features at earlier zoom levels (eg based on feature area and/or height). This primarily effects `aerodrome`, `airfield`, `cemetery`, `college`, `forest`, `golf_course`, `grocery`, `hospital`, `library`, `marina`, `military`, `national_park`, `nature_reserve`, `naval_base`, `park`, `post_office`, `protected_area`, `stadium`, `supermarket`, `townhall`, `university`, and `zoo`, or very large building area derived labels, or very tall height building area derived labels. [#47](https://github.com/protomaps/basemaps/pull/47)
- **pois** layer: Indicate with `pmap:min_zoom` property when a feature first became eligible to be in tiles, and mark `max_zoom` features to hide until later display zooms. Use this `pmap:min_zoom` property to setup client-side labe collisions. NOTE: In v4 it'll be renamed to just `min_zoom`. [#47](https://github.com/protomaps/basemaps/pull/47)
- **roads** layer: Modify `ref` tag values to remove some prefix values and remove whitespace values (for easier construction into client side shields with narrower graphics). [#37](https://github.com/protomaps/basemaps/pull/37)
- **roads** layer: Add `shield_text_length` for the integer lenth of the `ref` tag (with transformations) to more quickly converge towards Tilezen syntax. Can be paired with new `network` property to display client-side road shields. [#37](https://github.com/protomaps/basemaps/pull/37)
- **roads** layer: Add `network` with values of `US:I`, `US:US` or `other`. Can be paired with new `shield_text_length` and `ref` properties to display client-side road shields. [#37](https://github.com/protomaps/basemaps/pull/37)
- **roads** layer: Add `pmap:kind_detail` for values of `service` tag for `other` kind roads (eg for `parking_aisle` features) [#47](https://github.com/protomaps/basemaps/pull/47)
- **roads** layer: Remove name and ref tags from low- and mid-zooms selectively by road class to improve merging and reduce file size. [#47 ](https://github.com/protomaps/basemaps/pull/47 )
- **roads** layer: Add `pier` kind lines. See also new `pier` areas in the **landuse** layer. [#47](https://github.com/protomaps/basemaps/pull/47)
- **roads** layer: Remove small lines at low- and mid-zooms to improve figure-ground contrast and reduce file size. [#37](https://github.com/protomaps/basemaps/pull/37)
- **transit** layer: Show `runway` and `taxiway` kinds earlier (zoom 9 and 10), show `pier`, `yard`, `siding`, and `crossover` kinds later (zoom 13).  [#47 ](https://github.com/protomaps/basemaps/pull/47 )
- **transit** layer: Show certain kinds of railway (like `light_rail` and `disused`) later. [#47](https://github.com/protomaps/basemaps/pull/47)
- **transit** layer: Indicate with `pmap:kind_detail` the value of the relevant `service`, `ferry`, or `aerialway` tags. [#47](https://github.com/protomaps/basemaps/pull/47)
- **transit** layer: Add tunnel / bridge indicators with `pmap:level` (same as roads layer in v2), and `layer` property. [#47](https://github.com/protomaps/basemaps/pull/47)
- **transit** layer: Add `network`, `ref`, `route`, and `service` properties. [#47](https://github.com/protomaps/basemaps/pull/47)
- **transit** layer: Add `aerialway`, `aeroway`, `highspeed`, `man_made`, and `railway` properties  (though don't use them to be v4 safe) [#47](https://github.com/protomaps/basemaps/pull/47)
- **transit** layer: Remove small lines at low- and mid-zooms to improve figure-ground contrast and reduce file size. [#37](https://github.com/protomaps/basemaps/pull/37)
- **water** layer: Use Natural Earth for low-zoom water polygons [#47](https://github.com/protomaps/basemaps/pull/47)
- **water** layer: Use better `kind` values to match new label points in physical points layer [#47](https://github.com/protomaps/basemaps/pull/47)
- **water** layer: Add tunnel / bridge indicator with `pmap:level` (same as roads layer in v2) and `alkaline`, `intermittent`, and `reservoir` boolean indicators [#47](https://github.com/protomaps/basemaps/pull/47)
- **water** layer: Reduce small polygons to improve figure-ground contrast and reduce file size. [#47](https://github.com/protomaps/basemaps/pull/47)

#### BUG FIXES

- See also the "Breaking Changes" section above...
- **buildings** layer: Drop all names from buildings. [#47](https://github.com/protomaps/basemaps/pull/47)
- **buildings** layer: Exclude "no" features from `building` and `building_part`. [#47](https://github.com/protomaps/basemaps/pull/47)
- **places** layer: Fix parsing of OSM `population` values to be comma safe. [#38](https://github.com/protomaps/basemaps/pull/38)
- **places** layer: Fix parsing of OSM `population` values to be null safe. [#22](https://github.com/protomaps/basemaps/pull/22)
- **roads** layer: Remove OSM highway features that have been "abandoned", "razed", "demolished", or "removed". [#35](https://github.com/protomaps/basemaps/pull/35)
- **transit** layer: Remove OSM transit features that have been "razed", "demolished", "removed", or "proposed". [#35](https://github.com/protomaps/basemaps/pull/35)
- Fix project path in README.md [#24](https://github.com/protomaps/basemaps/pull/40)
- Fix Markdown in link formatting [#24](https://github.com/protomaps/basemaps/pull/24)

#### INTERNAL CHANGES

- Update default Protomaps style in MapLibre JS so it shows off the new features (and any impactful changes). [#37](https://github.com/protomaps/basemaps/pull/37) and [#47](https://github.com/protomaps/basemaps/pull/47)
- Update default Protomaps style in MapLibre JS so road casing layers are below other road layers [#42](https://github.com/protomaps/basemaps/pull/42)
- Update default Protomaps style in MapLibre JS so country boundary lines are solid [#45](https://github.com/protomaps/basemaps/pull/45)
- Update default Protomaps style in MapLibre JS with new "basic" color theme [#46](https://github.com/protomaps/basemaps/pull/46)
- Update OpenLayers plugin example to use official olpmtiles library instead of custom library [#49](https://github.com/protomaps/basemaps/pull/49)
- **landuse** layer: Allow fallback coallese of allow-listed OSM tags into `pmap:kind`. [#47](https://github.com/protomaps/basemaps/pull/47)
- **roads** layer: Significant refactor of the kind and other property logic.
- Add Usage section to README: [#20](https://github.com/protomaps/basemaps/pull/20)
- Update Usage section to README for building tiles and applying code linting (formatting): [#37](https://github.com/protomaps/basemaps/pull/37)
- Add Makefile with common build commands for easier development  [#47](https://github.com/protomaps/basemaps/pull/47)
- Add ability to build GeoFabrik named `area` regions (eg "monaco") in the CLI. [#20](https://github.com/protomaps/basemaps/pull/20)
- Improve consistency of internal private variable names in Planetiler profile   [#47](https://github.com/protomaps/basemaps/pull/47)
- Add SonarCloud linting... [#31](https://github.com/protomaps/basemaps/pull/31)
- Add Spotless [#25](https://github.com/protomaps/basemaps/pull/25)
- Don't version track target, YARN, DStore, or PMTiles artifacts: [#20](https://github.com/protomaps/basemaps/pull/20)


v2.1.0
------
- **Release date:** 2023-04-26.
- **Credits:** [@bdon](https://github.com/bdon)

- Initial open source release as a reimplementation of the Protomaps Basemap vector tile schema as a [Planetiler](https://github.com/onthegomap/planetiler/) schema in Java.
- Add openlayers basemap example
- Publish new PMTiles artifact:
    - https://r2-public.protomaps.com/protomaps-sample-datasets/protomaps-basemap-opensource-20230408.pmtiles


v2.0.0
------
- **Release date:** 2020-10-26
- **Credits:** [@bdon](https://github.com/bdon)

- Last closed source version of the Protomaps Basemap vector tile schema
- Add build script


v1.0.0
------
- **Release date:** 2020-03-05.
- **Credits:** [@bdon](https://github.com/bdon)

- Initiial closed source version of the Protomaps Basemap vector tile schema


NOTE: Release numbers follow [Semantic Versioning](SEMANTIC-VERSIONING.md). See also current project [VERSION](VERSION), the release notes here are for tagged releases; pre-release development changes are often not summarized until a tagged release.