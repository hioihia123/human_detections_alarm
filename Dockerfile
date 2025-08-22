#Stage 1: Builder stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

#WORKDIR
#cd/app
WORKDIR /app

#Copy all the files in the current directory into the container
COPY . . 

#Compiles code & packages it into a single executable .jar file
RUN mvn clean package -DskipTests

# ---------------------------------------------------- #
#Stage 2: Final Shipping Stage
#openjdk:17-jdk-slim, lightweight, official image that contains only the JDK 17 run time
#No maven or any other build tools => much smaller
FROM openjdk:21-jdk-slim

#Set a working directory for the new stage
WORKDIR  /app

#--from=build tells Docker to copy a file from "build" stage (stage 1)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

#specifies the command that will
#be executed automatically when a container is STARTED from this image.
ENTRYPOINT  ["java", "-jar", "app.jar"]
