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
import shapely.wkb
import shapely.geometry


DISTANCE_THRESHOLD_KM = 2.0
DISTANCE_THRESHOLD_METERS = DISTANCE_THRESHOLD_KM * 1000

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
    parser.add_argument(
        '--10x',
        dest='max_results',
        action='store_const',
        const=30,
        default=3,
        help='10x the result count'
    )
    return parser.parse_args()


def find_osm_features(pbf_path: str, layer_name: str, tags: list[str], name_filter: str | None = None) -> list[dict]:
    """
    Query OSM PBF file for features matching any of the given tags.

    Returns list of dicts with: osm_id, layer, name, matched_tag, matched_value, geometry
    """
    features = []

    try:
        # Load layer into GeoPandas
        gdf = geopandas.read_file(pbf_path, layer=layer_name)
    except Exception as e:
        logging.debug(f"Could not read layer {layer_name} from {pbf_path}: {e}")
        return features

    if gdf.empty:
        return features

    # Apply name filter first if provided
    if name_filter:
        if 'name' in gdf.columns:
            gdf = gdf[gdf['name'].notna() & gdf['name'].str.contains(name_filter, case=False, na=False)]
        else:
            return features

    if gdf.empty:
        return features

    # Check which OSM tag columns are available
    available_tag_cols = [col for col in OSM_TAG_COLUMNS if col in gdf.columns]

    # For each row, check if any of the tag columns matches any of our search tags
    for idx, row in gdf.iterrows():
        matched_tag = None
        matched_value = None

        # Check each available tag column
        for col in available_tag_cols:
            val = row[col]
            if val and (val in tags or any(f'"{tag}"' in val for tag in tags)):
                matched_tag = col
                matched_value = val
                break

        if not matched_tag:
            continue

        # Get OSM ID
        osm_id = row.get('osm_id')
        if not osm_id:
            osm_id = row.get('osm_way_id')

        # Get geometry
        geom = row['geometry']
        if geom is None or geom.is_empty:
            continue

        features.append({
            'osm_id': osm_id,
            'layer': layer_name,
            'name': row.get('name'),
            'matched_tag': matched_tag,
            'matched_value': matched_value,
            'geometry': geom,
            'other_tags': row.get('other_tags')
        })

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
        category_conditions.append(f"subtype = '{tag}'")
        category_conditions.append(f"class = '{tag}'")

    where_clause = ' OR '.join(category_conditions)

    if name_filter:
        where_clause = f"({where_clause}) AND (names.primary LIKE '%{name_filter}%')"

    query = f"""
        SELECT
            id,
            names.primary as name,
            theme,
            type,
            subtype,
            class,
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
            overture_id, name, theme, type_, subtype, class_, basic_cat, cat_primary, geom_blob, confidence = row

            # Convert geometry blob to shapely
            if geom_blob:
                shapely_geom = shapely.wkb.loads(bytes(geom_blob))

                features.append({
                    'id': overture_id,
                    'name': name,
                    'theme': theme,
                    'type': type_,
                    'subtype': subtype,
                    'class': class_,
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
            'theme': f['theme'],
            'type': f['type'],
            'subtype': f['subtype'],
            'class': f['class'],
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
        output_lines.append(f"    Theme: {ov['theme']}")
        output_lines.append(f"    Type: {ov['type']}")
        output_lines.append(f"    Subtype: {ov['subtype']}")
        output_lines.append(f"    Class: {ov['class']}")
        output_lines.append(f"    Basic Category: {ov['basic_category']}")
        output_lines.append(f"    Primary Category: {ov['categories_primary']}")
        if ov['confidence'] is not None:
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
        osm_args = [
          (str(osm_file), layer_name, args.tags, args.name)
          for osm_file in osm_files
          for layer_name in ['points', 'lines', 'multipolygons']
        ]
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

    # Select up to args.max_results with weighted random selection (prefer closer matches)
    if len(matches) > args.max_results:
        # Calculate weights as inverse of distance (closer = higher weight)
        weights = [(ov.get('confidence') or 1.0) / (dist_m + 1) for osm, ov, dist_m in matches]
        try:
          weights = [
            (min(len(osm['name']), len(ov['name'])) / max(len(osm['name']), len(ov['name'])))
            * weight for weight in weights
          ]
        except:
          pass
        matches = random.choices(matches, weights=weights, k=args.max_results)

    # Display results
    print(format_output(matches))


if __name__ == '__main__':
    main()
