#!/bin/bash -ex

# Generate wikidata-website-qid.csv.gz -- a mapping from website domain to Wikidata QID.
#
# Fetches the complete Wikidata P856 (official website) table from QLever,
# extracts root domains, disambiguates multiple QIDs per domain by taking the
# lowest Q-number, and writes a gzipped two-column CSV.
#
# Output: data/sources/wikidata-website-qid-YYYY-MM.csv.gz
# Usage: ./generate-wikidata-website-qid.sh

DATE=$(date +%Y-%m)
OUTPUT="data/sources/wikidata-website-qid-${DATE}.csv.gz"
TSV_TMP=$(mktemp /tmp/wikidata-p856-XXXXXX) && mv "$TSV_TMP" "${TSV_TMP}.tsv" && TSV_TMP="${TSV_TMP}.tsv"

echo "Fetching Wikidata P856 (official website) from QLever..."
curl \
  -H "Accept: text/tab-separated-values" \
  --data-urlencode "query=PREFIX wdt: <http://www.wikidata.org/prop/direct/> SELECT ?item ?website WHERE { ?item wdt:P856 ?website }" \
  --data-urlencode "send=2400000" \
  "https://qlever.dev/api/wikidata" \
  -o "$TSV_TMP"

echo "Building domain -> QID mapping..."
duckdb -c "
COPY (
  SELECT
    regexp_extract(lower(\"?website\"), 'https?://(?:www\\.)?([^/>\?]+)', 1) AS domain,
    arg_min(
      regexp_extract(\"?item\", 'entity/(Q[0-9]+)', 1),
      CAST(regexp_extract(\"?item\", 'Q([0-9]+)', 1) AS INTEGER)
    ) AS qid
  FROM read_csv('${TSV_TMP}', delim='\t', header=true, ignore_errors=true)
  WHERE regexp_extract(\"?item\", 'entity/(Q[0-9]+)', 1) != ''
    AND regexp_extract(lower(\"?website\"), 'https?://(?:www\\.)?([^/>\?]+)', 1) != ''
  GROUP BY domain
  ORDER BY domain
) TO '/dev/stdout' (FORMAT CSV, HEADER true)
" | gzip > "$OUTPUT"

rm "$TSV_TMP"

echo "Done: ${OUTPUT} ($(du -sh "$OUTPUT" | cut -f1))"
