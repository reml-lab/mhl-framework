FROM ubuntu:18.04

#Install base packages
RUN apt-get update && apt-get install -y curl openjdk-8-jdk-headless 

#Set VS root
ENV MHL_VS_ROOT=/vs

#Install VS
RUN mkdir $MHL_VS_ROOT
COPY mhl-vs.jar $MHL_VS_ROOT
RUN ls -la $MHL_VS_ROOT

COPY bootstrap.servers .

RUN ls -la $MHL_VS_ROOT
RUN ls -la 

#Run VS server
CMD ["java", "-jar", "/vs/mhl-vs.jar", "8090"]