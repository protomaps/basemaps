# Plan for Adding Overture Maps Support to Basemap.java

This document outlines how to expand Basemap.java to accept Overture Maps data from Geoparquet files as an alternative to OSM input.

## Implementation Status

**Phase 1 Complete (2025-12-29):** Basic infrastructure with land and water layers
- Added `--overture` CLI flag accepting single Parquet file path
- Implemented mutual exclusivity between `--overture` and `--area`
- Conditional data source loading (Overture Parquet vs OSM+GeoPackage)
- Natural Earth retained for low-zoom (0-5) rendering in both modes
- Implemented Water.java::processOverture() for theme=base/type=water
- Implemented Earth.java::processOverture() for theme=base/type=land
- Output filename derived from input Parquet file basename
- Successfully tested with lake-merritt-slice-overture.parquet

**Remaining Work:**
- Implement processOverture() methods for other layers (Buildings, Places, Roads, Transit, Boundaries, Pois)
- Handle nested JSON fields properly (currently warnings for `names.primary` access)
- Support for additional Overture themes beyond base theme

## Overview

Add `--overture` argument to accept Overture Maps Geoparquet data as an alternative to `--area` (OSM data). These options are mutually exclusive.

**Implementation Note:** The `--overture` argument accepts a **single Parquet file path**, not a directory. This differs from the original plan but matches the actual usage pattern in the Makefile.

## Overture Data Structure

Overture Maps data is organized in Geoparquet files, sometimes Hive-partitioned but not always:
```
theme=buildings/type=building/*.parquet
theme=buildings/type=building_part/*.parquet
theme=places/type=place/*.parquet
theme=transportation/type=segment/*.parquet
theme=transportation/type=connector/*.parquet
theme=base/type=water/*.parquet
theme=base/type=land/*.parquet
theme=base/type=land_use/*.parquet
theme=base/type=land_cover/*.parquet
theme=base/type=infrastructure/*.parquet
theme=base/type=bathymetry/*.parquet
theme=divisions/type=division/*.parquet
theme=divisions/type=division_area/*.parquet
theme=divisions/type=division_boundary/*.parquet
theme=addresses/type=address/*.parquet
```

**Note:** The base theme includes `water`, `land`, and `land_cover` types which replace the three GeoPackage sources (`osm_water`, `osm_land`, `landcover`) used in OSM mode.

## Code Changes Required

### 1. Command-line Argument Handling

**Location:** `Basemap.java:213-223` **IMPLEMENTED**

Added `--overture` argument accepting a **single Parquet file path** (not directory).

```java
String area = args.getString("area", "Geofabrik area name to download, or filename in data/sources/", "");
String overtureFile = args.getString("overture", "Path to Overture Maps Parquet file", "");

// Validate mutual exclusivity
if (!area.isEmpty() && !overtureFile.isEmpty()) {
  LOGGER.error("Error: Cannot specify both --area and --overture");
  System.exit(1);
}
if (area.isEmpty() && overtureFile.isEmpty()) {
  area = "monaco"; // default
}
```

**Help text updated** at `Basemap.java:176` to document `--overture=<path>` option.

### 2. Data Source Configuration

**Location:** `Basemap.java:225-246` **IMPLEMENTED**

Added conditional `.addParquetSource()` call for single Overture file when `--overture` is specified. Natural Earth source is **always added** for low-zoom (0-5) rendering.

```java
var planetiler = Planetiler.create(args)
  .addNaturalEarthSource("ne", nePath, neUrl);  // ALWAYS added for low zooms

if (!overtureFile.isEmpty()) {
  // Add Overture Parquet source
  planetiler.addParquetSource("overture",
    List.of(Path.of(overtureFile)),
    true, // enable Hive partitioning
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );
} else {
  // Add OSM and GeoPackage sources
  planetiler
    .addOsmSource("osm", Path.of("data", "sources", area + ".osm.pbf"), "geofabrik:" + area)
    .addShapefileSource("osm_water", sourcesDir.resolve("water-polygons-split-3857.zip"),
      "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip")
    .addShapefileSource("osm_land", sourcesDir.resolve("land-polygons-split-3857.zip"),
      "https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip")
    .addGeoPackageSource("landcover", sourcesDir.resolve("daylight-landcover.gpkg"),
      "https://r2-public.protomaps.com/datasets/daylight-landcover.gpkg");
}
```

**Important Changes from Original Plan:**
- Natural Earth is **retained** in Overture mode for low-zoom rendering (zooms 0-5)
- Single Parquet file path instead of directory with glob pattern
- The three GeoPackage sources (`osm_water`, `osm_land`, `landcover`) are NOT added in Overture mode
- Overture's base theme `water` and `land` types provide zoom 6+ data
- Natural Earth + Overture together provide complete zoom 0-15 coverage

**Key points:**
- Source name: `"overture"`
- Enable Hive partitioning: `true`
- ID extraction: `fields -> fields.get("id")`
- Layer extraction: `fields -> fields.get("type")` (gets "water", "land", etc. from Hive partition)
- Single file path wrapped in `List.of()`

### 3. Source Handler Registration

**Location:** `Basemap.java:88-104` (constructor)

Add `registerSourceHandler("overture", ...)` calls for each layer.

**IMPLEMENTED for Water and Earth layers:**

```java
// Water - Natural Earth (zooms 0-5) + Overture base/water (zooms 6+)
if (layer.isEmpty() || layer.equals(Water.LAYER_NAME)) {
  var water = new Water();
  registerHandler(water);
  registerSourceHandler("osm", water::processOsm);
  registerSourceHandler("osm_water", water::processPreparedOsm); // OSM GeoPackage
  registerSourceHandler("ne", water::processNe);                 // Low-zoom (0-5)
  registerSourceHandler("overture", water::processOverture);     // IMPLEMENTED
}

// Earth - Natural Earth (zooms 0-5) + Overture base/land (zooms 6+)
if (layer.isEmpty() || layer.equals(Earth.LAYER_NAME)) {
  var earth = new Earth();
  registerHandler(earth);
  registerSourceHandler("osm", earth::processOsm);
  registerSourceHandler("osm_land", earth::processPreparedOsm);  // OSM GeoPackage
  registerSourceHandler("ne", earth::processNe);                 // Low-zoom (0-5)
  registerSourceHandler("overture", earth::processOverture);     // IMPLEMENTED
}
```

**TODO for remaining layers:**

```java
if (layer.isEmpty() || layer.equals(Buildings.LAYER_NAME)) {
  var buildings = new Buildings();
  registerHandler(buildings);
  registerSourceHandler("osm", buildings::processOsm);
  registerSourceHandler("overture", buildings::processOverture); // TODO
}

if (layer.isEmpty() || layer.equals(Places.LAYER_NAME)) {
  var place = new Places(countryCoder);
  registerHandler(place);
  registerSourceHandler("osm", place::processOsm);
  registerSourceHandler("overture", place::processOverture); // TODO
}

if (layer.isEmpty() || layer.equals(Roads.LAYER_NAME)) {
  var roads = new Roads(countryCoder);
  registerHandler(roads);
  registerSourceHandler("osm", roads::processOsm);
  registerSourceHandler("overture", roads::processOverture); // TODO
}

// ... and others (Transit, Landuse, Landcover, Boundaries, Pois)
```

**Note:** The existing handlers for `osm_water`, `osm_land`, and `landcover` are kept because they're only called when those GeoPackage sources are added (OSM mode). When using `--overture`, those sources aren't added, so only the Natural Earth and Overture handlers are called.

### 4. Layer Processing Methods

**Location:** Individual layer files

Add `processOverture()` methods in each layer class. Filter by `feature.getSourceLayer()` which comes from the Hive partition `type=` value.

**IMPLEMENTED - Water.java::processOverture() (`Water.java:434-456`)**

```java
public void processOverture(SourceFeature sf, FeatureCollector features) {
  String sourceLayer = sf.getSourceLayer();

  // Filter by source layer - Overture base theme water
  if (!"water".equals(sourceLayer)) {
    return;
  }

  // Read Overture water attributes
  String subtype = sf.getString("subtype"); // e.g., "lake", "river", "ocean"
  String primaryName = sf.getString("names", "primary");

  if (sf.canBePolygon()) {
    features.polygon(LAYER_NAME)
      .setAttr("kind", subtype != null ? subtype : "water")
      .setAttr("name", primaryName)
      .setAttr("sort_rank", 200)
      .setPixelTolerance(Earth.PIXEL_TOLERANCE)
      .setMinZoom(6)
      .setMinPixelSize(1.0)
      .setBufferPixels(8);
  }
}
```

**IMPLEMENTED - Earth.java::processOverture() (`Earth.java:78-91`)**

```java
public void processOverture(SourceFeature sf, FeatureCollector features) {
  String sourceLayer = sf.getSourceLayer();

  // Filter by source layer - Overture base theme land
  if (!"land".equals(sourceLayer)) {
    return;
  }

  features.polygon(LAYER_NAME)
    .setAttr("kind", "earth")
    .setPixelTolerance(PIXEL_TOLERANCE)
    .setMinZoom(6)
    .setBufferPixels(8);
}
```

**TODO - Example for Buildings.java:**

```java
public void processOverture(SourceFeature feature, FeatureCollector features) {
  String sourceLayer = feature.getSourceLayer();

  // Filter by source layer (from Hive partition type=building or type=building_part)
  if (!"building".equals(sourceLayer) && !"building_part".equals(sourceLayer)) {
    return;
  }

  // Read Overture attributes
  Double height = feature.getDouble("height");
  String roofColor = feature.getString("roof_color");

  // Extract nested names (Overture uses structured names object)
  String primaryName = feature.getString("names", "primary");

  String kind = "building_part".equals(sourceLayer) ? "building_part" : "building";
  Integer minZoom = "building_part".equals(sourceLayer) ? 14 : 11;

  features.polygon(LAYER_NAME)
    .setId(FeatureId.create(feature))
    .setAttr("kind", kind)
    .setAttr("name", primaryName)
    .setAttr("height", height)
    .setAttr("roof_color", roofColor)
    .setAttr("sort_rank", 400)
    .setZoomRange(minZoom, 15);
}
```

**Example for Places.java:**

```java
public void processOverture(SourceFeature feature, FeatureCollector features) {
  String sourceLayer = feature.getSourceLayer();

  // Filter by source layer
  if (!"place".equals(sourceLayer)) {
    return;
  }

  // Read Overture structured data
  String primaryName = feature.getString("names", "primary");
  String primaryCategory = feature.getString("categories", "primary");

  // Map Overture categories to basemap place types
  String placeType = mapOvertureCategory(primaryCategory);

  features.point(LAYER_NAME)
    .setAttr("name", primaryName)
    .setAttr("place_type", placeType)
    .setMinZoom(calculateMinZoom(primaryCategory));
}
```

**Example for Water.java:**

```java
public void processOverture(SourceFeature feature, FeatureCollector features) {
  String sourceLayer = feature.getSourceLayer();

  // Filter by source layer - Overture base theme water
  if (!"water".equals(sourceLayer)) {
    return;
  }

  // Read Overture water attributes
  String subtype = feature.getString("subtype"); // e.g., "lake", "river", "ocean"
  String primaryName = feature.getString("names", "primary");

  features.polygon(LAYER_NAME)
    .setAttr("kind", subtype)
    .setAttr("name", primaryName)
    .setZoomRange(0, 15);
}
```

**Example for Earth.java (land polygons):**

```java
public void processOverture(SourceFeature feature, FeatureCollector features) {
  String sourceLayer = feature.getSourceLayer();

  // Filter by source layer - Overture base theme land
  if (!"land".equals(sourceLayer)) {
    return;
  }

  features.polygon(LAYER_NAME)
    .setAttr("kind", "land")
    .setZoomRange(0, 15);
}
```

**Key differences from OSM processing:**
- Use `feature.getSourceLayer()` for routing, NOT tag checking
- Source layer comes from Hive partition path (e.g., `type=building`)
- Access nested JSON properties: `feature.getString("names", "primary")`
- Overture uses `subtype` instead of OSM tag combinations
- No `hasTag()` checks needed - source layer determines feature type

### 5. Overture Schema Mapping

Overture Maps uses a different schema than OSM:

**Overture structure:**

Overture features are primarily organized by "theme" and "type", these are the most important things to know about them:
- "id" and "geometry" always exist
- "level_rules" designates rendering z-order sections
- "names"."primary" holds the most important name
- theme=transportation and type=segment includes highways, railways, and waterways
    - Highways have subtype=road, OSM-like "motorway", "residential", etc. class values, and "road_flags" to designate e.g. bridge or tunnel sections
    - Railways have subtype=rail, "standard_gauge", "subway", etc. class values, and "rail_flags" to designate e.g. bridge or tunnel sections
    - Waterways have subtype=water
- theme=base includes land, land_cover, and water
    - General land has type=land
    - Bodies of water have type=water, "subtype" with type of water body, and "class" with further description
    - Areas of land have type=land_cover and "subtype" with type of land cover
- theme=buildings includes representations of buildings and building parts
    - All buildings and building parts can have height in meters
    - Whole buildings have type=building, and optionally boolean "has_parts"
    - Parts of buildings have type=building_part and can show higher-detail parts (like towers vs. bases) instead a generic whole building

Complete Overture schema reference is at https://docs.overturemaps.org/schema/reference/

**Mapping examples:**
- OSM `building=yes` → Overture `theme=buildings` + `type=building`
- OSM `amenity=restaurant` → Overture `theme=places` + `type=place` + `categories.primary=restaurant`
- OSM `highway=primary` → Overture `theme=transportation` + `type=segment` + `subtype=road`

### 6. Output Filename

**Location:** `Basemap.java:292-304` **IMPLEMENTED**

Output filename is derived from the input Parquet file basename:

```java
String outputName;
if (!overtureFile.isEmpty()) {
  // Use base filename from input Parquet file
  String filename = Path.of(overtureFile).getFileName().toString();
  // Remove .parquet extension if present
  if (filename.endsWith(".parquet")) {
    outputName = filename.substring(0, filename.length() - ".parquet".length());
  } else {
    outputName = filename;
  }
} else {
  outputName = area;
}
planetiler.setOutput(Path.of(outputName + ".pmtiles"));
```

**Example:** Input `data/sources/lake-merritt-slice-overture.parquet` → Output `lake-merritt-slice-overture.pmtiles`

## Summary of Implementation Status

| File | Status | Changes |
|------|--------|---------|
| `Basemap.java` | **Complete** | Added `--overture` argument, validation, conditional `.addParquetSource()` calls, source handler registration for Water/Earth, output filename logic |
| `Water.java` | **Complete** | Added `processOverture()` method for theme=base/type=water |
| `Earth.java` | **Complete** | Added `processOverture()` method for theme=base/type=land |
| `Buildings.java` | **TODO** | Need to add `processOverture()` method for theme=buildings |
| `Places.java` | **TODO** | Need to add `processOverture()` method for theme=places |
| `Roads.java` | **TODO** | Need to add `processOverture()` method for theme=transportation/type=segment |
| `Transit.java` | **TODO** | Need to add `processOverture()` method for theme=transportation |
| `Landuse.java` | **Complete** | Added `processOverture()` method for theme=base/type=land_cover|land_use |
| `Landcover.java` | **TODO** | Need to add `processOverture()` method for theme=base/type=land_cover |
| `Boundaries.java` | **TODO** | Need to add `processOverture()` method for theme=divisions |
| `Pois.java` | **TODO** | Need to add `processOverture()` method for theme=places |

## Planetiler Library Support

Planetiler provides robust built-in support:

1. **ParquetReader** - handles file reading, Hive partitioning, WKB/WKT geometry parsing
2. **`Planetiler.addParquetSource()`** - adds Geoparquet to processing pipeline
3. **`SourceFeature` interface** - common abstraction for all source types
4. **Bounding box filtering** - built into ParquetReader for efficient spatial queries
5. **Multi-file support** - reads multiple partitioned files efficiently

## Implementation Phases

### Phase 1: Core Infrastructure
1. Add `--overture` argument and validation
2. Add conditional `.addParquetSource()` calls for all Overture themes
3. Update help text

### Phase 2: Layer Implementation
1. Implement `processOverture()` for Buildings layer (proof of concept)
2. Test with Overture buildings data
3. Implement `processOverture()` for remaining layers
4. Create Overture→basemap schema mapping for each layer

### Phase 3: Polish
1. Test with complete Overture dataset
2. Add integration tests
3. Profile performance and optimize if needed
4. Document Overture schema mappings

## Key Implementation Notes

1. **Routing mechanism:** All features from all Overture sources go to all registered handlers. Each handler filters by checking `feature.getSourceLayer()` value.

2. **Hive partitions:** The `type=` value in partition paths becomes the source layer name. Example: files in `theme=buildings/type=building/` have source layer `"building"`.

3. **Multiple files:** Each `.addParquetSource()` call processes many partitioned files via `Glob.of().resolve().find()`.

4. **Source name:** All Overture sources use the same name `"overture"` because routing is by source layer, not source name.

5. **No remote URL support:** Parquet sources don't support URLs directly. Users must download Overture data first or we must implement download logic separately.

## Resources

- [Overture Maps Schema Documentation](https://docs.overturemaps.org/schema/)
- [Overture Maps Data Downloads](https://overturemaps.org/download/)
- [Overture Maps GitHub](https://github.com/OvertureMaps)
- Planetiler example: `planetiler-examples/.../overture/OvertureBasemap.java`
