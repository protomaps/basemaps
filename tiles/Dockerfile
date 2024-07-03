FROM maven:3-eclipse-temurin-22-alpine
WORKDIR /tiles
COPY src src
COPY pom.xml pom.xml
RUN mvn clean package
ENTRYPOINT ["java","-jar","/tiles/target/protomaps-basemap-HEAD-with-deps.jar"]