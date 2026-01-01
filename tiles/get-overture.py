#!/usr/bin/env python3
"""
Download Overture Maps data for a given GeoJSON bounding box.
Uses DuckDB to efficiently query S3-hosted Parquet files with spatial partitioning.
"""

import argparse
import json
import sys
import duckdb


def get_bbox_from_geojson(geojson_path):
    """Extract bounding box from GeoJSON file."""
    with open(geojson_path, 'r') as f:
        data = json.load(f)

    # Collect all coordinates
    coords = []
    for feature in data.get('features', []):
        geom = feature.get('geometry', {})
        geom_type = geom.get('type')
        geom_coords = geom.get('coordinates', [])

        if geom_type == 'Polygon':
            # Flatten polygon coordinates
            for ring in geom_coords:
                coords.extend(ring)
        elif geom_type == 'MultiPolygon':
            for polygon in geom_coords:
                for ring in polygon:
                    coords.extend(ring)
        elif geom_type == 'Point':
            coords.append(geom_coords)
        elif geom_type == 'LineString':
            coords.extend(geom_coords)

    if not coords:
        raise ValueError("No coordinates found in GeoJSON")

    # Calculate bounding box
    lons = [c[0] for c in coords]
    lats = [c[1] for c in coords]

    return {
        'xmin': min(lons),
        'ymin': min(lats),
        'xmax': max(lons),
        'ymax': max(lats)
    }


def query_overture_data(bbox, output_path):
    """
    Query Overture Maps data using DuckDB with spatial partition filtering.
    """
    print(f"Bounding box: {bbox}")
    print("Connecting to DuckDB and configuring S3 access...")

    # Create DuckDB connection
    con = duckdb.connect()

    # Install and load spatial extension
    con.execute("INSTALL spatial;")
    con.execute("LOAD spatial;")

    # Install and load httpfs for S3 access
    con.execute("INSTALL httpfs;")
    con.execute("LOAD httpfs;")

    # Configure for anonymous S3 access
    con.execute("SET s3_region='us-west-2';")
    con.execute("SET s3_url_style='path';")

    # Overture base path - all themes, will filter by theme in WHERE clause
    base_path = "s3://overturemaps-us-west-2/release/2025-12-17.0"

    print("\nQuerying Overture transportation and places data with bbox filtering...")
    print("Using Hive partitioning to filter themes efficiently.")

    # Query with bbox and theme filtering
    # The theme is a Hive partition, so filtering on it should be efficient
    query = f"""
    COPY (
        SELECT *
        FROM read_parquet('{base_path}/**/*.parquet',
                         hive_partitioning=1,
                         filename=1,
                         union_by_name=1)
        WHERE theme IN ('transportation', 'places', 'base', 'buildings', 'divisions')
          AND bbox.xmin <= {bbox['xmax']}
          AND bbox.xmax >= {bbox['xmin']}
          AND bbox.ymin <= {bbox['ymax']}
          AND bbox.ymax >= {bbox['ymin']}
    ) TO '{output_path}' (FORMAT PARQUET);
    """

    print("\nExecuting query...")
    print("(This may take a few minutes depending on partition overlap)")

    try:
        con.execute(query)
        print(f"\n✓ Successfully wrote data to: {output_path}")

        # Get some stats
        result = con.execute(f"SELECT COUNT(*) as count FROM read_parquet('{output_path}')").fetchone()
        print(f"✓ Total features retrieved: {result[0]:,}")

    except Exception as e:
        print(f"\n✗ Error during query: {e}", file=sys.stderr)
        raise
    finally:
        con.close()


def main():
    parser = argparse.ArgumentParser(
        description='Download Overture Maps data for a GeoJSON bounding box'
    )
    parser.add_argument('geojson', help='Input GeoJSON file defining the area')
    parser.add_argument('output', help='Output Parquet file path')

    args = parser.parse_args()

    try:
        # Extract bounding box from GeoJSON
        print(f"Reading GeoJSON from: {args.geojson}")
        bbox = get_bbox_from_geojson(args.geojson)

        # Query Overture data
        query_overture_data(bbox, args.output)

    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == '__main__':
    main()
