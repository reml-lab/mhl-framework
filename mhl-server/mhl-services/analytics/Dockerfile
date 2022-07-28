FROM ubuntu:18.04

#Install base packages
RUN apt-get update && apt-get install -y curl openjdk-8-jdk-headless 

RUN apt-get -y install python3.8 python3-pip python3-dev
RUN pip3 install --upgrade pip

RUN pip3 install jupyter
RUN pip3 install numpy scipy pandas
RUN pip3 install kafka-python
RUN pip3 install elasticsearch==7.13.1 elasticsearch-dsl==7.4.0

RUN mkdir /notebooks

CMD cd /notebooks; jupyter notebook --notebook-dir=/notebooks --allow-root --ip=0.0.0.0 --no-browser --NotebookApp.token='' --NotebookApp.password=''
