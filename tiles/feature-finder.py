#!/usr/bin/env python3
"""
Feature finder script to match OSM and Overture features based on tags and spatial proximity.

Usage examples:
    feature-finder.py aerodrome airport --name OAK
    feature-finder.py national_park --name Alcatraz
"""
from __future__ import annotations

import argparse
import sys
import random
import pathlib
import logging
import multiprocessing
import math
import duckdb
import geopandas
import osgeo.ogr
import shapely.wkb
import shapely.geometry


DISTANCE_THRESHOLD_KM = 2.0
DISTANCE_THRESHOLD_METERS = DISTANCE_THRESHOLD_KM * 1000
MAX_RESULTS = 5

# Common OSM tag columns to check
OSM_TAG_COLUMNS = [
    'aeroway', 'amenity', 'leisure', 'tourism', 'landuse', 'natural',
    'shop', 'historic', 'building', 'highway', 'railway', 'waterway',
    'boundary', 'place', 'man_made', 'craft', 'office', 'sport'
]


def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description='Find matching features between OSM and Overture data'
    )
    parser.add_argument(
        'tags',
        nargs='+',
        help='Tags to search for (e.g., aerodrome airport national_park)'
    )
    parser.add_argument(
        '--name',
        help='Optional name filter (case-insensitive substring match)'
    )
    return parser.parse_args()


def find_osm_features(pbf_path: str, tags: list[str], name_filter: str | None = None) -> list[dict]:
    """
    Query OSM PBF file for features matching any of the given tags.

    Returns list of dicts with: osm_id, layer, name, matched_tag, matched_value, geometry
    """
    features = []
    datasource = osgeo.ogr.Open(pbf_path)

    if not datasource:
        return features

    # Search across all three geometry layers using SQL
    for layer_name in ['points', 'lines', 'multipolygons']:
        # First, get available columns for this layer
        temp_layer = datasource.GetLayerByName(layer_name)
        if not temp_layer:
            continue

        layer_defn = temp_layer.GetLayerDefn()
        available_columns = set()
        for i in range(layer_defn.GetFieldCount()):
            field_defn = layer_defn.GetFieldDefn(i)
            available_columns.add(field_defn.GetName())

        # Build WHERE clause for SQL
        conditions = []
        for tag in tags:
            for col in OSM_TAG_COLUMNS:
                if col in available_columns:
                    conditions.append(f"{col} = '{tag}'")

        if not conditions:
            continue

        where_clause = ' OR '.join(conditions)
        if name_filter:
            where_clause = f"({where_clause}) AND (name LIKE '%{name_filter}%')"

        sql = f"SELECT * FROM {layer_name} WHERE {where_clause}"
        result_layer = datasource.ExecuteSQL(sql)
        if not result_layer:
            continue

        # Process results
        for feature in result_layer:
            name = feature.GetField('name')

            # Check which tag matched
            matched_tag = None
            matched_value = None

            for col in OSM_TAG_COLUMNS:
                if col not in available_columns:
                    continue
                val = feature.GetField(col)
                if val and val in tags:
                    matched_tag = col
                    matched_value = val
                    break

            if not matched_tag:
                continue

            # Get geometry
            geom = feature.GetGeometryRef()
            if not geom:
                continue

            # Convert geometry to shapely
            geom_wkb = geom.ExportToWkb()
            shapely_geom = shapely.wkb.loads(geom_wkb)

            # Get OSM ID - try osm_id first, fallback to osm_way_id for multipolygons
            osm_id = feature.GetField('osm_id')
            if not osm_id and 'osm_way_id' in available_columns:
                osm_id = feature.GetField('osm_way_id')

            features.append({
                'osm_id': osm_id,
                'layer': layer_name,
                'name': name,
                'matched_tag': matched_tag,
                'matched_value': matched_value,
                'geometry': shapely_geom,
                'other_tags': feature.GetField('other_tags') if 'other_tags' in available_columns else None
            })

        datasource.ReleaseResultSet(result_layer)

    return features


def find_overture_features(parquet_path: str, tags: list[str], name_filter: str | None = None) -> list[dict]:
    """
    Query Overture parquet file for features matching any of the given tags.

    Returns list of dicts with: id, name, basic_category, categories_primary, geometry
    """
    conn = duckdb.connect(':memory:')

    # Build WHERE clause for categories
    category_conditions = []
    for tag in tags:
        category_conditions.append(f"categories.primary = '{tag}'")
        category_conditions.append(f"basic_category = '{tag}'")

    where_clause = ' OR '.join(category_conditions)

    if name_filter:
        where_clause = f"({where_clause}) AND (names.primary LIKE '%{name_filter}%')"

    query = f"""
        SELECT
            id,
            names.primary as name,
            basic_category,
            categories.primary as categories_primary,
            geometry,
            confidence
        FROM read_parquet('{parquet_path}')
        WHERE {where_clause}
    """

    try:
        result = conn.execute(query).fetchall()
        features = []

        for row in result:
            overture_id, name, basic_cat, cat_primary, geom_blob, confidence = row

            # Convert geometry blob to shapely
            if geom_blob:
                shapely_geom = shapely.wkb.loads(bytes(geom_blob))

                features.append({
                    'id': overture_id,
                    'name': name,
                    'basic_category': basic_cat,
                    'categories_primary': cat_primary,
                    'geometry': shapely_geom,
                    'confidence': confidence
                })

        return features

    except Exception as e:
        logging.error(f"Error querying {parquet_path}: {e}")
        return []
    finally:
        conn.close()


def get_utm_zone_epsg(longitude: float) -> str:
    """
    Calculate appropriate UTM zone EPSG code based on longitude.

    UTM zones are 6 degrees wide, numbered 1-60 starting at -180 degrees.
    Northern hemisphere uses EPSG:326xx, Southern hemisphere uses EPSG:327xx.
    For simplicity, assuming Northern hemisphere (add latitude check if needed).
    """
    zone_number = int((longitude + 180) / 6) + 1
    # Assuming Northern hemisphere
    epsg_code = 32600 + zone_number
    return f'EPSG:{epsg_code}'


def calculate_distance_meters(geom1, geom2) -> float:
    """Calculate distance between two geometries in meters using centroids and appropriate UTM projection."""
    # Create GeoDataFrames with geometries
    gdf1 = geopandas.GeoDataFrame([1], geometry=[geom1], crs='EPSG:4326')
    gdf2 = geopandas.GeoDataFrame([1], geometry=[geom2], crs='EPSG:4326')

    # Calculate average longitude to determine UTM zone
    centroid1_wgs84 = geom1.centroid
    centroid2_wgs84 = geom2.centroid
    avg_longitude = (centroid1_wgs84.x + centroid2_wgs84.x) / 2

    # Get appropriate UTM zone
    utm_crs = get_utm_zone_epsg(avg_longitude)

    # Project to UTM for accurate distance calculation
    gdf1_proj = gdf1.to_crs(utm_crs)
    gdf2_proj = gdf2.to_crs(utm_crs)

    # Get centroids and calculate distance
    centroid1 = gdf1_proj.geometry.iloc[0].centroid
    centroid2 = gdf2_proj.geometry.iloc[0].centroid

    return centroid1.distance(centroid2)


def find_matches(osm_features: list[dict], overture_features: list[dict]) -> list[tuple[dict, dict, float]]:
    """
    Find OSM-Overture pairs within distance threshold.

    Returns list of tuples: (osm_feature, overture_feature, distance_meters)
    """
    if not osm_features or not overture_features:
        return []

    # Create GeoDataFrames from the feature lists
    osm_gdf = geopandas.GeoDataFrame([
        {
            'osm_id': f['osm_id'],
            'layer': f['layer'],
            'name': f['name'],
            'matched_tag': f['matched_tag'],
            'matched_value': f['matched_value'],
            'other_tags': f['other_tags'],
            'geometry': f['geometry'],
            'index': i
        }
        for i, f in enumerate(osm_features)
    ], crs='EPSG:4326')

    overture_gdf = geopandas.GeoDataFrame([
        {
            'id': f['id'],
            'name': f['name'],
            'basic_category': f['basic_category'],
            'categories_primary': f['categories_primary'],
            'confidence': f['confidence'],
            'geometry': f['geometry'],
            'index': i
        }
        for i, f in enumerate(overture_features)
    ], crs='EPSG:4326')

    # Project both to EPSG:3857 (Web Mercator) for distance calculations
    osm_gdf_proj = osm_gdf.to_crs('EPSG:3857')
    overture_gdf_proj = overture_gdf.to_crs('EPSG:3857')

    # Use spatial join with a buffer to find potential matches
    # Buffer by 1.5x the threshold for a conservative search area
    buffer_distance = DISTANCE_THRESHOLD_METERS * 2
    osm_buffered = osm_gdf_proj.copy()
    osm_buffered['geometry'] = osm_buffered.geometry.buffer(buffer_distance)

    # Spatial join to find candidates within buffer distance
    joined = osm_buffered.sjoin(overture_gdf_proj, how='inner', predicate='intersects')

    # Now calculate precise distances only for candidates
    matches = []
    for _, row in joined.iterrows():
        osm_idx = row['index_left']
        ov_idx = row['index_right']

        # Get the original (unbuffered) geometries
        osm_geom = osm_gdf_proj.iloc[osm_idx].geometry
        ov_geom = overture_gdf_proj.iloc[ov_idx].geometry

        # Calculate distance between centroids in EPSG:3857
        distance_m = osm_geom.centroid.distance(ov_geom.centroid)

        if distance_m <= DISTANCE_THRESHOLD_METERS:
            # Reconstruct feature dicts from original lists
            osm_feat = osm_features[osm_idx]
            ov_feat = overture_features[ov_idx]
            matches.append((osm_feat, ov_feat, distance_m))

    return matches


def format_output(matches: list[tuple[dict, dict, float]]) -> str:
    """Format matched features for display."""
    if not matches:
        return "No matches found."

    output_lines = []
    output_lines.append(f"Found {len(matches)} match(es) within {DISTANCE_THRESHOLD_KM} km:\n")

    for i, (osm, ov, dist_m) in enumerate(matches, 1):
        dist_km = dist_m / 1000
        output_lines.append(f"Match {i}:")
        output_lines.append(f"  OSM:")
        output_lines.append(f"    ID: {osm['osm_id']}")
        output_lines.append(f"    Layer: {osm['layer']}")
        output_lines.append(f"    Name: {osm['name']}")
        output_lines.append(f"    Tag: {osm['matched_tag']}={osm['matched_value']}")

        output_lines.append(f"  Overture:")
        output_lines.append(f"    ID: {ov['id']}")
        output_lines.append(f"    Name: {ov['name']}")
        output_lines.append(f"    Basic Category: {ov['basic_category']}")
        output_lines.append(f"    Primary Category: {ov['categories_primary']}")
        output_lines.append(f"    Confidence: {ov['confidence']:.2f}")

        output_lines.append(f"  Distance: {dist_km:.2f} km")
        output_lines.append("")

    return '\n'.join(output_lines)


def main():
    # Setup logging
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )

    args = parse_args()

    # Find all transect files
    data_dir = pathlib.Path('data/sources')
    osm_files = list(data_dir.glob('*-Transect.osm.pbf'))
    parquet_files = list(data_dir.glob('*-Transect.parquet'))

    if not osm_files or not parquet_files:
        logging.error("No transect files found in data/sources/")
        sys.exit(1)

    # Collect all features from all transect files in parallel
    all_osm_features = []
    all_overture_features = []

    # Process OSM files in parallel using multiprocessing
    with multiprocessing.Pool() as pool:
        osm_args = [(str(osm_file), args.tags, args.name) for osm_file in osm_files]
        osm_results = pool.starmap(find_osm_features, osm_args)
        for osm_features in osm_results:
            all_osm_features.extend(osm_features)

    # Process Overture files in parallel using multiprocessing
    with multiprocessing.Pool() as pool:
        ov_args = [(str(parquet_file), args.tags, args.name) for parquet_file in parquet_files]
        ov_results = pool.starmap(find_overture_features, ov_args)
        for ov_features in ov_results:
            all_overture_features.extend(ov_features)

    logging.info(f"Found {len(all_osm_features)} OSM features and {len(all_overture_features)} Overture features")

    # Find matches
    matches = find_matches(all_osm_features, all_overture_features)

    # Select up to MAX_RESULTS with weighted random selection (prefer closer matches)
    if len(matches) > MAX_RESULTS:
        # Calculate weights as inverse of log distance (closer = higher weight)
        weights = [
          (min(len(osm['name']), len(ov['name'])) / max(len(osm['name']), len(ov['name'])))
          * ov['confidence']
          / math.log(dist_m + 2.0)
          for osm, ov, dist_m in matches
        ]
        matches = random.choices(matches, weights=weights, k=MAX_RESULTS)

    # Display results
    print(format_output(matches))


if __name__ == '__main__':
    main()
