#!/bin/bash
set -euo pipefail

mapfile -t input_files < <(find tests/ -type f -name "input.osm.pbf")

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
    --output=../render-tests/pmtiles/monaco-roads.pmtiles \
    --force \
    --download \
    --keep_unzipped \
    --nodemap-type=sparsearray
