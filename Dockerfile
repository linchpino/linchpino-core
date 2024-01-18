# Part 1: Build the app using Maven
#FROM maven:3.9.1-amazoncorretto-18 AS build
#FROM maven:3.8.7-openjdk-18-slim
FROM maven:18-jdk-alpine
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use an official OpenJDK image as the base image
 FROM openjdk:18-jdk-alpine
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar .
# Set the command to run the application
CMD ["java", "-jar","demo-0.0.1-SNAPSHOT.jar"]
