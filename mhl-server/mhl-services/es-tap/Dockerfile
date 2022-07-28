FROM ubuntu:18.04

#Install base packages
RUN apt-get update && apt-get install -y curl openjdk-8-jdk-headless 

#Set ESTAP root
ENV MHL_ESTAP_ROOT=/estap

#Install ESTAP
RUN mkdir $MHL_ESTAP_ROOT
COPY mhl-estap.jar $MHL_ESTAP_ROOT

RUN ls -la $MHL_ESTAP_ROOT

#Run ESTAP
CMD ["java", "-jar", "/estap/mhl-estap.jar", "mhl-broker0:9092", "data-message,system-message,analytics-message", "domino", "1000","5000"]
