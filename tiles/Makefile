# download dependencies and compile the JAR
# generally do this after each code change
clean:
	mvn clean package

# is a build only partially finishes, the PMTiles can be corrupted
clean-pmtiles:
	rm -rf *.pmtiles

# This is optimized for local dev serving PMTiles out of directory local to this Makefile
# Don't use this for production (instead set --cors=ORIGIN)
# The default port is: 8080 (use --port to override)
# Assumes go-pmtiles has been installed locally (eg prebuilt go binary download) & the path is added to your shell env
# https://github.com/protomaps/go-pmtiles
# Example tile coord to test with: http://localhost:8080/monaco/12/2133/1495.mvt
serve:
	~/Downloads/pmtiles serve --cors=* .

#
# Testing areas
#

# Smallest
#
# Download and generate monaco.pmtiles in the current directory:
monaco:
	java -jar target/*-with-deps.jar --download --force --area=monaco

# Download and generate california.pmtiles in the current directory, clipped to SF bay area
sf:
	java -jar target/*-with-deps.jar --download --force --area=us/california --bounds=-122.9913,37.2377,-121.5265,38.2519

# Medium
#
# Download and generate switzerland.pmtiles in the current directory:
switzerland:
	java -jar target/*-with-deps.jar --download --force --area=switzerland

# Download and generate switzerland.pmtiles in the current directory:
washington:
	java -jar target/*-with-deps.jar --download --force --area=washington

# Large
#
# Download and generate ca/california.pmtiles in the current directory:
# Note: the slash confuses some downstream processes, try a symlink to california
california:
	java -jar target/*-with-deps.jar --download --force --area=us/california

# Download and generate ca/new-york.pmtiles in the current directory:
# Note: the slash confuses some downstream processes, try a symlink to california
ny:
	java -jar target/*-with-deps.jar --download --force --area=us/new-york

planet:
    java -Xmx24g \
      -jar target/*-with-deps.jar \
      `# Download the latest planet.osm.pbf from s3://osm-pds bucket` \
      --area=planet --bounds=world --download \
      `# Accelerate the download by fetching the 10 1GB chunks at a time in parallel` \
      --download-threads=10 --download-chunk-size-mb=1000 \
      `# Also download name translations from wikidata` \
      --fetch-wikidata \
      --output=output.\pbtiles \
      --nodemap-type=sparsearray --nodemap-storage=ram 2>&1 | tee logs.txt

.PHONY: clean clean-pmtiles serve monaco sf switzerland washington california planet