Styles v2.0.0-alpha.4
------
- Fix ordering issues related to pedestrian bridges.

Tiles v3.0.0-pre4
------
- Fix appearance of NE boundaries at low zooms.
- Add `pmap:kind`=`bus_stop` to POIs. via [@eikes](https://github.com/eikes)

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