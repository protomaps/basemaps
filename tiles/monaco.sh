java -Xmx2g -jar target/protomaps-basemap-HEAD-with-deps.jar \
    --osm_url=https://protomaps.dev/~wipfli/fixtures/monaco-2025-02-15.osm.pbf \
    --nodemap-type=sparsearray \
    --layer=roads \
    --log_jts_exceptions \
    --download