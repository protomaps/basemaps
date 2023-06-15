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
# Example tile coord to test with: http://localhost:8080/monaco12/2133/1495.mvt
serve:
	pmtiles serve --cors=* .

#
# Testing areas
#

# Smallest
#
#Download and generate monaco.pmtiles in the current directory:
monaco:
	java -jar target/*-with-deps.jar --download --force --area=monaco

# Generate monaco.pmtiles in the current directory (using already downloaded asset)
monaco-fast:
	java -jar target/*-with-deps.jar --force --area=monaco

# Medium
#
#Download and generate switzerland.pmtiles in the current directory:
switzerland:
	java -jar target/*-with-deps.jar --download --force --area=switzerland

# Generate switzerland.pmtiles in the current directory (using already downloaded asset)
switzerland-fast:
	java -jar target/*-with-deps.jar --force --area=switzerland


# Large
#
# Download and generate ca/california.pmtiles in the current directory:
# Note: the slash confuses some downstream processes, try a symlink to california
california:
	java -jar target/*-with-deps.jar --download --force --area=us/california

# Generate ca/california.pmtiles in the current directory (using already downloaded asset)
# Note: the slash confuses some downstream processes, try a symlink to california
california-fast:
	java -jar target/*-with-deps.jar --force --area=us/california