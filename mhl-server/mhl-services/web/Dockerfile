FROM ubuntu:18.04

#Install base packages
RUN apt-get update && apt-get install -y curl openjdk-8-jdk-headless 
RUN apt-get -y install python3.8 python3-pip python3-dev

#RUN apt-get -y install apache2

RUN pip3 install kafka-python
RUN pip3 install django

COPY mhl-web /mhl-web

RUN cd mhl-web; bash setup.sh

CMD cd mhl-web; bash run.sh