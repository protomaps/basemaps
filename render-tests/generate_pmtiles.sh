#!/bin/bash
set -euo pipefail

target_subdir="${1:-}"

if [[ -n "$target_subdir" ]]; then
  echo "Processing only subfolder: $target_subdir"
  input_files=()
  while IFS= read -r -d '' file; do
    input_files+=("$file")
  done < <(find "$target_subdir" -type f -name "input.osm.pbf" -print0)
else
  echo "Processing all subfolders in tests/"
  input_files=()
  while IFS= read -r -d '' file; do
    input_files+=("$file")
  done < <(find tests/ -type f -name "input.osm.pbf" -print0)
fi

sorted_files=()

for input in "${input_files[@]}"; do
  dir=$(dirname "$input")
  sorted="$dir/input-sorted.osm.pbf"
  echo "Sorting $input..."
  osmium sort --overwrite "$input" -o "$sorted"
  sorted_files+=("$sorted")
done

echo "Merging..."
osmium merge --overwrite "${sorted_files[@]}" -o merged.osm.pbf

mkdir -p pmtiles
cd ../tiles
mvn clean package -DskipTests
java -Xmx2g -jar target/protomaps-basemap-HEAD-with-deps.jar \
    --osm_path=../render-tests/merged.osm.pbf \
    --osm_water_path=../render-tests/fixtures/empty-water-polygons-split-3857.zip \
    --osm_land_path=../render-tests/fixtures/empty-land-polygons-split-3857.zip \
    --output=../render-tests/pmtiles/tiles.pmtiles \
    --force \
    --download \
    --keep_unzipped \
    --nodemap-type=sparsearray