# Part 1: Build the app using Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine
RUN apk add jq
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
RUN mvn clean package 

EXPOSE 8081
