FROM maven:3.8.1-jdk-11-slim as build

WORKDIR /usr/src/app

#Copy the pom file in and pull its dependencies first since its less likely to change
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY ./src/ ./src/
RUN mvn install -DskipTests

#ENV PORT 8443
#EXPOSE $PORT
#CMD ["java", "-jar", "/usr/src/app/target/NoiseBean-1.3.4.jar"]


## Build the final image using artifacts from the build step
FROM openjdk:11-jre-slim

COPY --from=build /usr/src/app/target/NoiseBean-1.3.4.jar /app/NoiseBean-1.3.4.jar

WORKDIR /app

RUN mkdir data

ENV PORT 8443
EXPOSE $PORT
CMD ["java", "-jar", "NoiseBean-1.3.4.jar"]
