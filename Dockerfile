# Part 1: Build the app using Maven
FROM maven:3.8.6-eclipse-temurin-18-alpine AS build
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
RUN mvn clean package 
#RUN mvn clean package
# Part 2: Use an official OpenJDK image as the base image
 FROM openjdk:23-jdk
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
#COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar .
COPY --from=build /app/target/core-0.0.1-SNAPSHOT.jar .
# Set the command to run the application
CMD ["java", "-jar","core-0.0.1-SNAPSHOT.jar"]

#FROM openjdk:21.0.1-jdk
#MAINTAINER lonchpino
#VOLUME /tmp
#ARG JAR_FILE=target/linchpino.jar
#COPY ${JAR_FILE} linchpino.jar
#EXPOSE 8088 9951
#ENTRYPOINT ["java","-jar","linchpino.jar"]
