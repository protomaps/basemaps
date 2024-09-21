# download dependencies and compile the JAR
# generally do this after each code change
clean:
	mvn clean package

clean-fast:
	mvn clean package -DskipTests

# is a build only partially finishes, the PMTiles can be corrupted
clean-pmtiles:
	rm -rf *.pmtiles

# Run linting to apply code formatting for clean PR merges in CI
lint:
	mvn spotless:apply

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

# Download and generate us/california.pmtiles in the current directory, clipped to the SF bay area
# Note: the slash confuses some downstream processes, try a symlink to california
sf:
	java -jar target/*-with-deps.jar --download --force --area=us/california --bounds=-122.9913,37.2377,-121.5265,38.2519

# Medium
#
# Download and generate switzerland.pmtiles in the current directory:
switzerland:
	java -jar target/*-with-deps.jar --download --force --area=switzerland

# Download and generate washington.pmtiles in the current directory:
washington:
	java -jar target/*-with-deps.jar --download --force --area=washington

# Download and generate kansas.pmtiles in the current directory (with USA label):
kansas:
	java -jar target/*-with-deps.jar --download --force --area=kansas

# Large
#
# Download and generate us/california.pmtiles in the current directory:
# Note: the slash confuses some downstream processes, try a symlink to california
california:
	java -jar target/*-with-deps.jar --download --force --area=us/california

# Download and generate us/new-york.pmtiles in the current directory:
# Note: the slash confuses some downstream processes, try a symlink to new-york
ny:
	java -jar target/*-with-deps.jar --download --force --area=us/new-york

# Download and generate us/new-york.pmtiles in the current directory,
# clipped to the NYC metro area (including New Jersey)
# Note: the slash confuses some downstream processes, try a symlink to northeast
nyc:
	java -jar target/*-with-deps.jar --download --force --area=us/northeast --bounds=-75.896703,39.530403,-73.260767,41.217394

# Download and generate japan.pmtiles in the current directory:
japan:
	java -jar target/*-with-deps.jar --download --force --area=japan --maxzoom=10 --render_maxzoom=10

us-lowzoom:
	java -jar target/*-with-deps.jar --download --force --area=us/california --bounds=-129.525228,18.870954,-59.212729,50.664092 --maxzoom=7 --render_maxzoom=7

eu-lowzoom:
	java -jar target/*-with-deps.jar --download --force --area=us/california --bounds=-32.318197,31.705746,53.287269,72.937699 --maxzoom=7 --render_maxzoom=7

# Planetiler docs say it'll download planet.pbf for you from S3, but that doesn't work in Protomaps/basemap

# very fast
data/sources/planet-latest.osm.pbf:
	aws s3 cp --no-sign-request s3://osm-pds/planet-latest.osm.pbf data/sources/

planet: data/sources/planet-latest.osm.pbf
	java -Xmx24g \
  	-jar target/*-with-deps.jar \
  	--download --bounds=planet \
  	--osm-path=data/sources/planet-latest.osm.pbf \
		--output=planet.pmtiles \
		--nodemap-type=sparsearray --nodemap-storage=mmap 2>&1 | tee logs.txt

planet-xl: data/sources/planet-latest.osm.pbf
	java -Xmx384g \
    `# return unused heap memory to the OS` \
    -XX:MaxHeapFreeRatio=40 \
		-jar target/*-with-deps.jar \
  	--download --bounds=planet \
  	--osm-path=data/sources/planet-latest.osm.pbf \
		--output=planet.pmtiles \
		--nodemap-type=array --storage=ram 2>&1 | tee logs.txt

.PHONY: clean clean-fast clean-pmtiles lint serve monaco sf switzerland washington california ny ny-metro japan us-lowzoom eu-lowzoom planet planet-xl
