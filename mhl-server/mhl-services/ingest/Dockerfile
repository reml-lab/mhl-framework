FROM ubuntu:18.04

#Install base packages
RUN apt-get update && apt-get install -y curl openjdk-8-jdk-headless 

#Set ingest root
ENV MHL_ingest_ROOT=/ingest

#Install ingest
RUN mkdir $MHL_ingest_ROOT
COPY mhl-ingest.jar $MHL_ingest_ROOT
COPY log4j.properties $MHL_ingest_ROOT

RUN ls -la /ingest

CMD java -Djavax.net.ssl.keyStore=/run/secrets/mhl_ingest_serverkeys  -Djavax.net.ssl.keyStorePassword=`cat /run/secrets/mhl_ingest_keyStorePassword`  -DDlog4j.configuration=$MHL_ingest_ROOT/log4j.properties  -jar $MHL_ingest_ROOT/mhl-ingest.jar -port 9596 -kafka mhl-broker0:9092 