# Part 1: Build the app using Maven
FROM maven:3.9.1-amazoncorretto-19 AS build

WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
## download dependencie
###ADD . /
###RUN mvn verify clean
## build after dependencies are down so it wont redownload unless the POM chans
###ADD . /
RUN mvn clean package -DskipTests

FROM openjdk:19-jdk-alpine
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar .
# Set the command to run the application
CMD ["java", "-jar","demo-0.0.1-SNAPSHOT.jar"]
