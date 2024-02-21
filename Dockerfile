FROM openjdk:21.0.1-jdk
MAINTAINER lonchpino
VOLUME /tmp
ARG JAR_FILE=target/linchpino.jar
COPY ${JAR_FILE} linchpino.jar
EXPOSE 8088 9951
ENTRYPOINT ["java","-jar","linchpino.jar"]