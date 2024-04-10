# Part 1: Build the app using Maven
FROM maven:3.8.6-eclipse-temurin-18-alpine
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
RUN mvn clean package

EXPOSE 8081
