# Plan for Adding Overture Maps Support to Basemap.java

This document outlines how to expand Basemap.java to accept Overture Maps data from Geoparquet files as an alternative to OSM input.

## Overview

Add `--overture` argument to accept Overture Maps Geoparquet data as an alternative to `--area` (OSM data). These options are mutually exclusive.

## Overture Data Structure

Overture Maps data is organized in Hive-partitioned Geoparquet files:
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

**Location:** `Basemap.java:163-216`

Add `--overture` argument accepting a directory path containing Hive-partitioned Overture data.

```java
String area = args.getString("area", "Geofabrik area name to download, or filename in data/sources/", "");
String overtureBase = args.getString("overture", "Path to Overture Maps base directory", "");

// Validate mutual exclusivity
if (!area.isEmpty() && !overtureBase.isEmpty()) {
  LOGGER.error("Error: Cannot specify both --area and --overture");
  System.exit(1);
}
if (area.isEmpty() && overtureBase.isEmpty()) {
  area = "monaco"; // default
}
```

**Update help text** to document `--overture=<directory>` option.

### 2. Data Source Configuration

**Location:** `Basemap.java:214-222`

Add multiple `.addParquetSource()` calls for different Overture themes when `--overture` is specified.

```java
if (!overtureBase.isEmpty()) {
  Path base = Path.of(overtureBase);

  // Buildings
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=buildings", "type=building", "*.parquet").find(),
    true, // hive-partitioning
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Building parts
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=buildings", "type=building_part", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Places
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=places", "type=place", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Transportation segments (roads)
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=transportation", "type=segment", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Transportation connectors
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=transportation", "type=connector", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Water
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=base", "type=water", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Land
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=base", "type=land", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Land use
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=base", "type=land_use", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

  // Divisions (boundaries)
  planetiler.addParquetSource("overture",
    Glob.of(base).resolve("theme=divisions", "type=division", "*.parquet").find(),
    true,
    fields -> fields.get("id"),
    fields -> fields.get("type")
  );

} else {
  // Add OSM source (existing code)
  planetiler.addOsmSource("osm", Path.of("data", "sources", area + ".osm.pbf"), "geofabrik:" + area);

  // Add GeoPackage sources (existing code) - these are OSM-specific
  planetiler.addGeoPackageSource("osm_water", sourcesDir.resolve("water-polygons-split-4326.gpkg"),
    "https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip");
  planetiler.addGeoPackageSource("osm_land", sourcesDir.resolve("land-polygons-split-4326.gpkg"),
    "https://osmdata.openstreetmap.de/download/land-polygons-split-3857.zip");
  planetiler.addGeoPackageSource("landcover", sourcesDir.resolve("daylight-landcover.gpkg"),
    "https://r2-public.protomaps.com/datasets/daylight-landcover.gpkg");
}
```

**Important:** When using `--overture`, the three GeoPackage sources (`osm_water`, `osm_land`, `landcover`) are NOT added because Overture's base theme already includes `water`, `land`, and `land_cover` types that serve the same purpose. The handler registration for these layers will remain unchanged but they will receive data from Overture's base theme instead of the GeoPackage files.

**Key points:**
- All sources use name `"overture"` (routing is by source layer, not source name)
- Enable Hive partitioning with `true` flag
- ID extraction: `fields -> fields.get("id")`
- Layer extraction: `fields -> fields.get("type")`
- Use `Glob.of().resolve().find()` to get `List<Path>` of partitioned files

### 3. Source Handler Registration

**Location:** `Basemap.java:36-103` (constructor)

Add `registerSourceHandler("overture", ...)` calls for each layer:

```java
if (layer.isEmpty() || layer.equals(Buildings.LAYER_NAME)) {
  var buildings = new Buildings();
  registerHandler(buildings);
  registerSourceHandler("osm", buildings::processOsm);
  registerSourceHandler("overture", buildings::processOverture); // NEW
}

if (layer.isEmpty() || layer.equals(Places.LAYER_NAME)) {
  var place = new Places(countryCoder);
  registerHandler(place);
  registerSourceHandler("osm", place::processOsm);
  registerSourceHandler("overture", place::processOverture); // NEW
}

if (layer.isEmpty() || layer.equals(Roads.LAYER_NAME)) {
  var roads = new Roads(countryCoder);
  registerHandler(roads);
  registerSourceHandler("osm", roads::processOsm);
  registerSourceHandler("overture", roads::processOverture); // NEW
}

// Water - existing registrations remain, but data source changes
if (layer.isEmpty() || layer.equals(Water.LAYER_NAME)) {
  var water = new Water();
  registerHandler(water);
  registerSourceHandler("osm", water::processOsm);
  registerSourceHandler("osm_water", water::processPreparedOsm); // OSM GeoPackage
  registerSourceHandler("overture", water::processOverture); // NEW - from base theme
}

// Earth - existing registrations remain, but data source changes
if (layer.isEmpty() || layer.equals(Earth.LAYER_NAME)) {
  var earth = new Earth();
  registerHandler(earth);
  registerSourceHandler("osm", earth::processOsm);
  registerSourceHandler("osm_land", earth::processPreparedOsm); // OSM GeoPackage
  registerSourceHandler("overture", earth::processOverture); // NEW - from base theme
}

// Landcover - existing registrations remain, but data source changes
if (layer.isEmpty() || layer.equals(Landcover.LAYER_NAME)) {
  var landcover = new Landcover();
  registerHandler(landcover);
  registerSourceHandler("landcover", landcover::processLandcover); // OSM GeoPackage
  registerSourceHandler("overture", landcover::processOverture); // NEW - from base theme
}

// Repeat for all other layers...
```

**Note:** The existing handlers for `osm_water`, `osm_land`, and `landcover` are kept because they're only called when those GeoPackage sources are added (OSM mode). When using `--overture`, those sources aren't added, so only the new `overture` handlers will be called.

### 4. Layer Processing Methods

**Location:** Individual layer files (Buildings.java, Places.java, Roads.java, etc.)

Add `processOverture()` methods in each layer class. Filter by `feature.getSourceLayer()` which comes from the Hive partition `type=` value.

**Example for Buildings.java:**

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

**Example for Landcover.java:**

```java
public void processOverture(SourceFeature feature, FeatureCollector features) {
  String sourceLayer = feature.getSourceLayer();

  // Filter by source layer - Overture base theme land_cover
  if (!"land_cover".equals(sourceLayer)) {
    return;
  }

  // Read Overture land cover attributes
  String subtype = feature.getString("subtype"); // e.g., "forest", "grass", "wetland"

  features.polygon(LAYER_NAME)
    .setAttr("kind", subtype)
    .setZoomRange(calculateMinZoom(subtype), 15);
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
- `names` object with `primary`, `common`, etc. subfields
- `addresses` object for structured address data
- `categories` object with `primary` and `alternate` fields
- `subtype` field for classification (instead of OSM tags)
- Organized by theme/type in Hive partitions

**Mapping examples:**
- OSM `building=yes` → Overture `theme=buildings` + `type=building`
- OSM `amenity=restaurant` → Overture `theme=places` + `type=place` + `categories.primary=restaurant`
- OSM `highway=primary` → Overture `theme=transportation` + `type=segment` + `subtype=road`

### 6. Output Filename

**Location:** `Basemap.java:267`

```java
String outputName;
if (!overtureBase.isEmpty()) {
  outputName = "overture-basemap";
} else {
  outputName = area;
}
planetiler.setOutput(Path.of(outputName + ".pmtiles"));
```

## Summary of Required Files

| File | Changes |
|------|---------|
| `Basemap.java` | Add `--overture` argument, validation, conditional `.addParquetSource()` calls, source handler registration, output filename |
| `Buildings.java` | Add `processOverture()` method |
| `Places.java` | Add `processOverture()` method |
| `Roads.java` | Add `processOverture()` method |
| `Transit.java` | Add `processOverture()` method |
| `Water.java` | Add `processOverture()` method |
| `Earth.java` | Add `processOverture()` method |
| `Landuse.java` | Add `processOverture()` method |
| `Landcover.java` | Add `processOverture()` method |
| `Boundaries.java` | Add `processOverture()` method |
| `Pois.java` | Add `processOverture()` method |

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
