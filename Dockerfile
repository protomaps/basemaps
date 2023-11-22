FROM maven:3-eclipse-temurin-21-alpine
WORKDIR /basemaps
COPY . /basemaps
WORKDIR /basemaps/tiles
RUN mvn clean package


