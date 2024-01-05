FROM maven:3.9.6-eclipse-temurin-21-jammy as build

WORKDIR /usr/src/app

#Copy the pom file in and pull its dependencies first since its less likely to change
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY ./src/ ./src/
RUN mvn install -DskipTests

## Build the final image using artifacts from the build step
FROM openjdk:21-slim

COPY --from=build /usr/src/app/target/NoiseBean*.jar /app/NoiseBean.jar

WORKDIR /app

RUN mkdir data

ENV PORT 8443
EXPOSE $PORT
CMD ["java", "-jar", "NoiseBean.jar"]
