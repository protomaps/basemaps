# Semantic Versioning

When a new version of the Protomaps basemap schema is released, developers should be able to tell from the
version increment how much effort it will take them to integrate the new tiles with their map. We use semantic
versioning to communicate this.

### What is Semantic Versioning?

Semantic versioning (or [SemVer](http://semver.org/)) is a formalized way of making promises with an X.Y.Z version indicator.

#### Version components

- `MAJOR`.`MINOR`.`PATCH`, example: `1.0.0` (default)
- `MAJOR`.`MINOR`.`PATCH`-`POSTFIX`, example: `1.0.0-pre1` (optional)

#### Version parts:

1. **MAJOR** version **X** for incompatible API changes.
2. **MINOR** version **Y** when adding functionality in a backwards-compatible manner, and
3. **PATCH** version **Z** when fixing backwards-compatible bugs
4. **pre-release** version **-POSTFIX** when releasing developer previews

**NOTE:** The above applies _after_ software has reached as version `1.0.0`, no promises are made for earlier versions.

#### Developer level of effort:

- Major version X: **high** – significant integration challenges, read the changelog closely
- Minor version Y: **low** – some integration challenges, read the changelog
- Bug fixes Z: **none** – simply use the new tiles, skim or ignore the changelog
- Pre-release POSTFIX: **low to high** – some to significant integration challenges, read the changelog

## Versioning the Protomaps Basemap vector tile schema

Upon our version `1.0.0` release Protomaps Basemap makes the following promises based on the Tilezen schema:

##### Definition of terms

* **`common`** - These `layer`s, `property`s, and `kind`s are generally available across all features in a Tilezen response.
    - Establishes basic selection of features and their arrangement into specific named layers.
    - Core properties needed for display and labeling of features:
        - Special bits that make vector tile content **interoperably Tilezen**, including `kind`, `kind_detail`, `landuse_kind`, `kind_tile_rank`, `min_zoom`, `max_zoom`, `is_landuse_aoi`, `sort_rank`, `boundary`, and `maritime_boundary`.
        - Fundamental properties like `name` (including localized names), `id`, and `source` included on most every feature.
* **`common-optional`** - These are meant to be part of a common set, but may not be present because they aren't relevant or because we don't have the data (primarily feature `property`s, but could also be `layer`s).
    - Used to refine feature selection.
    - Lightly transformed **interoperable Tilezen** properties based on original data values. Examples include: `country_capital`, `region_capital`, `bicycle_network`, `is_bridge`, `is_link`, `is_tunnel`, `is_bicycle_related`, `is_bus_route`, `walking_network`, `area`, left & right names and localized `name:*` values on lines, and left & right `id` values on lines.
    - Fundamental properties like `ref`, `colour`, `population`, `elevation`, `cuisine`, `operator`, `protect_class`, and `sport`.
* **`optional`** - These are the properties of a specific, less important `kind`, or generally present across `kind`s but only in exceptional cases.
    - Often used to decorate features already selected for display.
    - Additional properties like `capacity` and `covered`.

#### MAJOR version increments:

1. **Remove** `common layer`
1. **Change** `common layer` **name**
1. **Remove** `common-optional layer`
1. **Change** `common-optional layer` **name**
1. **Remove** `common` feature **property** completely from all zooms
1. **Change** `common` feature **property name**
1. **Remove** `common-optional` feature **property** at zoom 14 or more
1. **Change** `common-optional` feature **property name**
1. **Remove** `optional` feature **property** at zoom 14 or more
1. **Change** `kind` **value name**
1. **Remove** `kind` **value** completely from all zooms
1. **Move** `kind` from one layer to another
1. **Additional simplification** across `kind` **values** in zooms 14, 15, or 16 (or max zoom) by removing `common`, `common-optional`, and/or `optional` **properties** by merging or other method
1. **Simplification** within `kind` **values** at zooms 16 (or max zoom) by removing `common`, `common-optional`, and `optional` **properties** by merging or other method
1. **Change** of <= -3 (earlier) to default `min_zoom` or `max_zoom` **values** to determine when `kind` is included
1. **Change** of >= +2 (later) to default `min_zoom` or `max_zoom` **values** to determine when `kind` is included
1. **Change** of version of a positioned glyph font for an already included script

#### MINOR version increments:

1. **Add** `common` layer
1. **Add** `common-optional` layer
1. **Add** `common` feature **property**
1. **Add** `common-optional` feature **property**
1. **Add** `optional` feature **property**
1. **Add** new `kind` value
1. **Additional simplification** across `kind` **values** at zooms 13 or less by removing `common`, `common-optional`, and `optional` **properties** by merging or other method
1. **Additional simplification** within `kind` **values** at zooms 13, 14, or 15 by removing `common`, `common-optional`, and `optional` **properties** by merging or other method
1. **Reassign** 50% or more of existing `kind` **value** into a new `kind` **value**, when kind has 10,000 or more features
1. **Change** of <= -2 (earlier) to `min_zoom` or `max_zoom` **values** to determine when `kind` is included
1. **Change** of >= +1 (later) to default `min_zoom` or `max_zoom` **values** to determine when `kind` is included
1. **Change** the maximum Tilezen zoom (currently zoom 16).
1. **Add** new positioned glyph font script

#### PATCH version increments

1. **Add** `optional` layer
1. **Change** `optional` layer **name**
1. **Remove** `optional` layer
1. **Additional simplification** within `kind` **values** at zooms 12 or less by removing `common`, `common-optional`, and `optional` **properties** by merging or other method
1. **Reassign** an existing `kind` **value** to an another existing `kind` value when they are equivalent (e.g. fixing the spelling of a value coming from an upstream data source)
1. **Reassign** less than 50% of existing `kind` **value** into a new `kind` value, when kind has 10,000 or more features
1. **Reassign** more than 50% of existing `kind` **value** into a new `kind` value, when kind has less than 10,000 features
1. **Change** of -1 to default `min_zoom` or `max_zoom` **values** to determine when `kind` is included
1. **Adjustments** to the overall map balance (proportion of features in one layer or another, proportion of `kind`s in a single layer) by adjusting boosting of `min_zoom` values over the `kind`'s default, limiting the number of individual `kind` features in a given tile coordinate, and other means.
1. **Add** unpublicized `kind` **value**
1. **Remove** unpublicized `kind` **value**
1. **Correct** a regression in the API (to the last good version)
1. **Correct** a newly added feature **property name**
1. **Correct** a newly added `kind` **value**

## Versioning Data

We do not version data features, but we do attempt to indicate the data source and the feature ID as assigned by that source so customers can investigate upstream changes.


### Frequency of data updates

Protomaps basemaps have 3 primary sources:

- **Natural Earth** (used for zooms 0-8 for most everything) updates infrequently (often annually)
- **OpenStreetMap** (used for zooms 9+ for most everything, sometimes earlier) updates frequently (at least daily)
- **OpenStreetMapData** (used for zooms 9+ in the earth and water layers only) updates infrequently (optimistically monthly)


### INDIVIDUAL FEATURES are not versioned

#### Examples:

1. **Add** new feature
1. **Removal** of existing feature
1. **Change** feature **name**
1. **Change** feature **geometry**
1. **Change** feature **property** values (including property removal if removed from original source)
1. **Change** feature `kind` **value** (when upstream data source reclassifies them).
1. **Change** feature `min_zoom` &/or `max_zoom` **values** (when area or other signal changes upstream).

**NOTE:** It is possible to query the version of individual features by looking at a feature's `source` and `id` properties and performing a lookup via the source service, but that is beyond the scope of Tilezen. Because of simplification, `id` properties are not always available due to feature merging.

### LANGUAGES are not versioned

In addition to the `common` **name** locals call a place, the following `common` and `common-optional` languages are generally available:

#### Common languages:

1. `name:ar` **العربية (Arabic)** _common_
1. `name:bn` **বাংলা (Bengali)** – _common-optional_
1. `name:de` **Deutsch (German)** – _common-optional_
1. `name:en` **English** – _common_
1. `name:es` **español (Spanish)** – _common_
1. `name:fr` **français (French)** – _common_
1. `name:el` **ελληνικά (Greek)** – _common-optional_
1. `name:hi` **हिन्दी (Hindi)** – _common-optional_
1. `name:id` **Bahasa Indonesia (Indonesian)** – _common-optional_
1. `name:it` **italiano (Italian)** – _common-optional_
1. `name:ja` **日本語 (Japanese)** – _common-optional_
1. `name:ko` **한국어 (Korean)** – _common-optional_
1. `name:nl` **Nederlands (Dutch)** – _common-optional_
1. `name:pl` **Polski (Polish)** – _common-optional_
1. `name:pt` **Português (Portuguese)** – _common-optional_
1. `name:ru` **Русский (Russian)** – _common_
1. `name:sv` **Svenska (Swedish)** – _common-optional_
1. `name:tr` **Türkçe (Turkish)** – _common-optional_
1. `name:vi` **Tiếng Việt (Vietnamese)** – _common-optional_
1. `name:zh` **中文 (Chinese)**: primarily simplified but sometimes traditional – _common_ – _deprecated_
1. `name:zh-Hans` **中文 (Chinese)**: primarily simplified but sometimes traditional – _common_
1. `name:zh-Hant` **中文 (Chinese)**: primarily traditional but sometimes simplified – _common_

Arabic, Chinese, English, French, Russian and Spanish are used by the United Nations for meetings and official documents. The other languages listed are either proposed as official language of the United Nations (Bengali, Hindi, Portugese, and Turkish) or frequently used in OpenStreetMap, Who's On First, or Wikipedia.

Additional localized names are available as `common-optional` and `optional`, but their actual use is the data is not widespread.


### POLITICAL GEOGRAPHY is not versioned

#### Examples:

1. **Major** additions, deletions to country names, borders, disputed territories, and capitals are possible and may be advertised but do not bump the Tilezen API version.
1. **Minor** corrections to country names, borders, disputed territories, capitals, and other administrative geography are always possible and will not be tracked or advertised.

## Versioning the Tilezen Service

#### MAJOR version increments:

1. **Rollup** of MAJOR Tilezen API changes
1. **Change** the maximum Tilezen zoom at which tiles are generated by default (currently zoom 16)
1. **Remove** a file format
1. Backwards incompatible change to a file format
1. Tilezen software dependency has a breaking change to the tile response

#### MINOR version increments:

1. **Rollup** of MINOR Tilezen API changes
1. **Add** a file format

#### PATCH version increments:

1. **Rollup** of PATCH Tilezen API changes
1. **Infrequent update** of static data sources like Natural Earth or OpenStreetMapData
1. Backwards compatible change to a file format
1. Software dependency has a backwards compatible change or bug fix with no change to the tile response

## See also

- [VERSION](VERSION)
- http://semver.org
- https://github.com/tilezen/vector-datasource/edit/master/docs/SEMANTIC-VERSIONING.md
- https://github.com/nvkelso/natural-earth-vector/blob/master/README.md
- https://github.com/whosonfirst/whosonfirst-placetypes#roles
