# Set the name of the area
$AREA_NAME="my_beloved_home_country"

# Run a Docker container and mount the output directory,
# then download the source data and build the planetiler profile.
# The .pmtiles archive will be saved in the basemaps/output directory.
docker run -v ${pwd}/output:/basemaps/output -it basemaps bash -c "\
  java -jar /basemaps/tiles/target/*-with-deps.jar --download --force --area=$AREA_NAME && \
  mv /basemaps/tiles/$AREA_NAME.pmtiles /basemaps/output/$AREA_NAME.pmtiles "

# Move the generated pmtiles file to the basemaps/tiles directory
mv "./output/$AREA_NAME.pmtiles" "./tiles/$AREA_NAME.pmtiles"

# Remove the output directory
rm ./output