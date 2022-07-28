FROM ubuntu:18.04

#Install base packages
RUN apt-get update && apt-get install -y curl openjdk-8-jdk-headless 

#Set kafka root
ENV MHL_KAFKA_ROOT=/kafka

#Install kafka
RUN mkdir $MHL_KAFKA_ROOT
RUN curl "https://archive.apache.org/dist/kafka/0.10.1.0/kafka_2.11-0.10.1.0.tgz" -o $MHL_KAFKA_ROOT/kafka.tgz
RUN cd $MHL_KAFKA_ROOT
RUN ls -la /kafka
RUN tar -xvzf $MHL_KAFKA_ROOT/kafka.tgz -C /kafka --strip 1
RUN ls -la /kafka

#Copy configurations
COPY config_desktop/zookeeper.properties $MHL_KAFKA_ROOT/zookeeper.properties

#Run kafka
CMD ["/kafka/bin/zookeeper-server-start.sh", "/kafka/zookeeper.properties"]
