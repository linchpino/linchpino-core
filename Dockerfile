FROM openjdk:21.0.1-jdk
MAINTAINER portfolio
VOLUME /tmp
ARG JAR_FILE=target/linchpin.jar
COPY ${JAR_FILE} linchpin.jar
EXPOSE 9950 9951
ENTRYPOINT ["java","-jar","linchpin.jar"]