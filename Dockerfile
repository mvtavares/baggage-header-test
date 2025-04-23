# FROM openjdk:8u322
FROM openjdk:21-jdk-bullseye

RUN apt-get update

RUN apt-get install -y nano

RUN apt-get install -y wget

RUN apt-get install -y unzip

ADD data/sample-mongo-app /home/sample-mongo-app

WORKDIR /home/sample-mongo-app

RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer

RUN ./mvnw -DskipTests=true package

ENV DD_SERVICE "sample-springboot-app"
ENV DD_VERSION 1.0.2
ENV DD_ENV dev

LABEL com.datadoghq.tags.service="sample-springboot-app"
LABEL com.datadoghq.tags.version="1.0.2"

CMD java -javaagent:dd-java-agent.jar -Ddd.trace.debug=true -Ddd.profiling.ddprof.enabled=true -Ddd.profiling.enabled=true -jar target/sample-mongo-app-0.0.1-SNAPSHOT.jar

