#!/bin/bash

CHANGED_FILES=$(git diff --name-only HEAD..main)
echo "$CHANGED_FILES"
TILES_SRC_CHANGED=$(echo "$CHANGED_FILES" | grep '^tiles/src/main' || true)
VERSION_CHANGED=$(echo "$CHANGED_FILES" | grep '^tiles/src/main/java/com/protomaps/basemap/Basemap.java$' || true)
if [[ -n "$TILES_SRC_CHANGED" && -z "$VERSION_CHANGED" ]]; then
  echo "::error ::You modified files in tiles/src/main, but did not update Basemap.java."
  exit 1
fi