mkdir -p pmtiles
cd ../tiles
mvn clean package -DskipTests
java -Xmx2g -jar target/protomaps-basemap-HEAD-with-deps.jar \
    --osm_path=../render-tests/fixtures/monaco-2025-02-15.osm.pbf \
    --output=../render-tests/pmtiles/monaco-roads.pmtiles \
    --force \
    --download \
    --nodemap-type=sparsearray
