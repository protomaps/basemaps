#!/usr/bin/env bash
set -euo pipefail

STAC_CATALOG="https://stac.overturemaps.org/catalog.json"
DEFAULT_BBOX="7.408446,43.722901,7.4405,43.752481"
DEFAULT_OUT="data/sources/monaco.parquet"

usage() {
  cat <<EOF
Usage: $0 [RELEASE] [BBOX] [OUTPUT]

Downloads Overture Maps data for a bounding box as a Parquet file.

Arguments:
  RELEASE  Overture release version         (default: latest, from ${STAC_CATALOG})
  BBOX     xmin,ymin,xmax,ymax in WGS84     (default: ${DEFAULT_BBOX})
  OUTPUT   Output file path                 (default: ${DEFAULT_OUT})

Example:
  $0 2026-06-17.0 -122.52,37.70,-122.35,37.83 data/sources/sf.parquet
EOF
}

latest_release() {
  # A network failure yields empty output rather than aborting under `set -e`,
  # so the caller can report it.
  duckdb -noheader -list -c \
    "SELECT latest FROM read_json('${STAC_CATALOG}')" 2>/dev/null || true
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

REL="${1:-$(latest_release)}"
if [[ -z "$REL" ]]; then
  echo "Could not determine the latest release from ${STAC_CATALOG}; pass one explicitly." >&2
  exit 1
fi
BBOX="${2:-$DEFAULT_BBOX}"
OUT="${3:-$DEFAULT_OUT}"

IFS=',' read -r XMIN YMIN XMAX YMAX <<< "$BBOX"

echo "Downloading release $REL: $BBOX to $OUT"

duckdb -c "
COPY (
  SELECT *
  FROM read_parquet(
    's3://overturemaps-us-west-2/release/${REL}/**/*.parquet',
    hive_partitioning=1, filename=1, union_by_name=1
  )
  WHERE theme IN ('transportation','places','base','buildings','divisions')
    AND bbox.xmin <= ${XMAX}
    AND bbox.xmax >= ${XMIN}
    AND bbox.ymin <= ${YMAX}
    AND bbox.ymax >= ${YMIN}
) TO '${OUT}' (FORMAT PARQUET);"

echo "Wrote ${OUT}"