#!/usr/bin/env bash
set -euo pipefail

REL="${1:-2026-02-18.0}"
BBOX="${2:-7.408446,43.722901,7.4405,43.752481}"
OUT="data/sources/${3:-monaco.parquet}"

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