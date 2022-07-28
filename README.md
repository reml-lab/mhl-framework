# mhl-framework

## 0. Overview
This repository contains the mHealth Lab streaming data collection framework. The framework includes reference implementations of both data collection server components and an Android client application. 

The server is fully containerized using Docker and consists of a combination of open source and customized components. The server framework uses Apache Kafka to provide a fast and scalable message passing bus. Data storage and retrieval is provided by ElasticSearch. Kibana and Jupyter notebook are used as off-the-shelf dashboarding and data analytics platforms. 

The server uses a custom data ingestion front end implemented in Java for receiving streaming data from clients connected over the internet and publishing these data to the Kafka bus. This component can also pass messages back to connected clients that are published to the Kafka bus. A second custom component implemented in Java receives messages from the Kafka bus and send them to ElasticSearch for indexing and long term storage. A third custom Java component allows for real-time, low latency visualization of streaming data. Finally, a custom, light-weight Python-based Django web application is used to provide a basic web portal with participant management capabilities.

## 1. Installation

The server components will run on a relatively recent laptop or desktop computer. They have been tested on Intel and M1 Macbook Pros as well as Ubuntu 16 and 18 Linux servers. The client app will run on Android 9 and above.

### 1.1 Installing the server components

To install the server components, follow these steps.

1. Install [Docker for your system](https://docs.docker.com/get-docker/)
2. From a terminal, clone this repository: git clone https://github.com/reml-lab/mhl-framework.git
3. Change to the directory mhl-server
4. Run docker-compose build

### 1.2 Installing the Android client app

From an Android device, click this link to download and install the client app: https://github.com/reml-lab/mhl-framework/blob/main/mhl-android-apps/mhl-demo.apk?raw=true. This client streams a univariate synthetic data stream to the server.

## 2. Running the Framework

Basic usage instructions for streaming and inspecting data are provided below. Additional instructions for more advanced use of server components follows.

1. To run the server framework, navigate to the mhl-framework root directory and run: docker-compose up
2. Find the IP address of the server. 
3. Navigate to the MHL Demo app on the Android device and open it.
4. Enter the IP address of the server. This IP address must be reachable from the Android device.
5. Enter a valid research token. The server ships with the research token 12345678 pre-configured.
6. Press the "Start Streaming" button to connect to the server and start streaming data. 
7. Open a web browser and go to <server_ip>:9801 to connect to the web portal. If running the server on the system where the broweser is running, simply click this link: https://localhost:9801.
8. From the web portal, you can view the streaming data visualization by clicking on the graph icon in the Actions column of the user listing. You can also connect to the Kibana server and the Jupyter Notebook server (more information is provided below).
9. When finished, click "Stop Streaming" on the Android phone to stop streaming data and run: "docker-compose stop" on the server to stop the server components.

## 3. Usage Instructions

### 3.1 Using Kibana

Kibana can be accessed from the web portal or directly at <server_ip>:9803. If you are viewing this page from a machine running the server, you can access it using this link: https://localhost:9803. Users who are familiar with Kibana can set an index pattern and use the provided data discovery and dashboarding tools to inspect the data stored in elastic search.

A base configuration file is also provided at this link: https://raw.githubusercontent.com/reml-lab/mhl-framework/main/mhl-server/mhl-services/kibana/export.ndjson. The file can also be found in the cloned repo in mhl-server/mhl-services/kibana/export.ndjson. To load the configuration, follow these steps:

1. Open the Kibana Saved Objects page by going to <server_ip>:9801/app/management/kibana/objects. If you are viewing this page from a machine running the server, you can access it using this link: https://localhost:9803/app/management/kibana/objects.
2. Click the Import link at the top right of the page.
3. Drag and drop a copy of the export.ndjson file onto the file import area.
4. Click the Import button.

Once these step are compelte, you will have access to a basic dashboard for displaying data streamed from the reference Android application. 