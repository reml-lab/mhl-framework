# mhl-framework

## Overview
This repository contains the mHealth Lab streaming data collection framework. The framework includes reference implementations of both data collection server components and an Android client application. 

The server is fully containerized using Docker and consists of a combination of open source and customized components. The server framework uses Apache Kafka to provide a fast and scalable message passing bus. Data storage and retrieval is provided by ElasticSearch. Kibana and Jupyter Notebook are used as off-the-shelf dashboarding and data analytics platforms. 

The server uses a custom data ingestion front end implemented in Java for receiving streaming data from clients connected over the internet and publishing these data to the Kafka bus. This component can also pass messages back to connected clients that are published to the Kafka bus. A second custom component implemented in Java receives messages from the Kafka bus and send them to ElasticSearch for indexing and long term storage. A third custom Java component allows for real-time, low latency visualization of streaming data. Finally, a custom, light-weight Python-based Django web application is used to provide a basic web portal with participant management capabilities.

![MHL Framework Overview](https://raw.githubusercontent.com/reml-lab/mhl-framework/main/mhl-images/framework-overview.png)

## 1. Quick Start Guide

The server components will run on a relatively recent laptop or desktop computer. They have been tested on Intel and M1 Macbook Pros as well as Ubuntu 16 and 18 Linux servers. The client app will run on Android 9 and above. The machine where the server is running must be reachable from the network the Android phone is on in order for phone to be able to connect to the server.

Please note that the instructions below are suitable for quick setup and testing using a secured machine on a secured network. These instructions should not be followed for deployment on an open network. 

### 1.1 Installing the server components

To install the server components, follow these steps.

1. Install Docker for your system: https://docs.docker.com/get-docker/
2. From a terminal, clone this repository: git clone https://github.com/reml-lab/mhl-framework.git
3. Change to the directory mhl-server
4. Run docker-compose build

### 1.2 Installing the Android client app

From an Android device, click this link to download and install the client app: https://github.com/reml-lab/mhl-framework/blob/main/mhl-android-apps/mhl-demo.apk?raw=true. This client streams a univariate synthetic data stream to the server.

### 1.3 Running and Testing the Framework

Basic usage instructions for testing streaming and inspecting data are provided below. Additional instructions for more advanced use of server components follows.

1. To run the server framework, navigate to the mhl-framework/mhl-server root directory and run: docker-compose up
2. Find the IP address of the server. 
3. Navigate to the MHL Demo app on the Android device and open it.
4. Enter the IP address of the server. This IP address must be reachable from the Android device.
5. Enter a valid research token. The server ships with the research token 12345678 pre-configured.
6. Press the "Start Streaming" button to connect to the server and start streaming data. 
7. Open a web browser and go to http://<server_ip>:9801 to connect to the web portal. If running the server on the system where the browser is running, simply click this link: http://localhost:9801.
8. From the web portal, you can view the streaming data visualization by clicking on the graph icon in the Actions column of the user listing. You can also connect to the Kibana server and the Jupyter Notebook server (more information is provided below).
9. When finished, click "Stop Streaming" on the Android phone to stop streaming data and run: "docker-compose stop" from the mhl-framework/mhl-server directory on the server to stop the server components.

## 2. Usage Instructions

### 2.1 Using the Participant Management Portal

The participant management portal includes basic functionality for managing participant records. The participant management portal can be accessed from the web portal at http://<server_ip>:9801. If you are viewing this page from a machine running the server, you can access it using this link: http://localhost:9801.

The existing structure includes several field types. The basic data model includes a participant facing research token and an internal identifier (the Badge ID). All stored streaming data records are associated with the Badge ID. 

The portal allows for editing participant records, deleting participant records, adding new participant records, and viewing currently streaming data for each participant (to view streaming data from a currently connected participant, click the chart icon in the Actions column of the participant list). This portal is implemented using Django and can easily be extended by adding more fields to the Django data model.

### 2.2 Using Kibana

[Kibana](https://www.elastic.co/kibana/) can be accessed from the web portal or directly at http://<server_ip>:9803. If you are viewing this page from a machine running the server, you can access it using this link: http://localhost:9803. Users who are familiar with Kibana can set an index pattern and use the provided data discovery and dashboarding tools to inspect the data stored in elastic search.

A base configuration file is also provided at this link: https://raw.githubusercontent.com/reml-lab/mhl-framework/main/mhl-server/mhl-services/kibana/export.ndjson. The file can also be found in the cloned repo in mhl-server/mhl-services/kibana/export.ndjson. To load the base configuration, follow these steps:

1. Open the Kibana Saved Objects page by going to http://<server_ip>:9801/app/management/kibana/objects. If you are viewing this page from a machine running the server, you can access it using this link: http://localhost:9803/app/management/kibana/objects.
2. Click the Import link at the top right of the page.
3. Drag and drop a copy of the export.ndjson configuration file onto the file import area.
4. Click the Import button.

Once these step are complete, you will have access to a basic dashboard for displaying data streamed from the reference Android application and index patterns enabling the use of Kibana data discovery features.

### 2.3 Using Jupyter

The [Jupyter](https://jupyter.org/) server provides a notebook interface for interacting with ElasticSearch and Kafka data using Python. An example notebook is included in ElasticSearch.ipynb that queries the ElasticSearch data store for collected data streamed from the reference Android app. The included Python ElasticSearch library is https://elasticsearch-py.readthedocs.io/en/v7.13.1/. The included Python Kafka library is https://kafka-python.readthedocs.io/en/2.0.1/. 

## 3. Modifying the Framework

The reference framework components are distributed with the intent that users will modify, extend and experiment with them. This section provides a basic guide to modifying the system.

### 3.1 Server Java Applications

The custom server applications written in Java use the Apache Maven build system. To work with them, first install Maven by following the instructions here: https://maven.apache.org/users/index.html. Mac users may find it easier to install Maven using a package manager such as Homebrew (e.g., https://formulae.brew.sh/formula/maven).

The source files for the three applications is included in mhl-framework/mhl-server-app-src directory. The three applications are mhl-ingest (the data ingestion front end), mhl-estap (the Kafka to ElasticSearch bridge application), and mhl-vs (the streaming data visualization server). Each application can be re-compiled using maven by running: "mvn package" in its root folder. 

To deploy updates, the compiled jar file need to be copied back to the corresponding service folder under mhl-framework/mhl-server/mhl-services/<service> and then the corresponding container needs to be rebuilt using "docker-compose build <service_name>". The service can then be re-deployed using docker-compose up <service_name>.

#### 3.1.1 mhl-ingest

The mhl-ingest server application is responsible for connecting clients to the mhl-server infrastructure. It executes a handshake operation with connected clients to ensure that the client's research token and badge ID correspond to a known user in the user management database. This component also pushes a study configuration to the client on a first connection. The class UserManager provides functions for querying the user management database that is maintained by the Django user management server component. The class MHLIngest implements the handshake operation and is responsible for starting data ingestion threads. The class MHLIngestThread receives data from a connected client and publishes to the Kafka bus. 

Note that modifications to the Django data model for the user management system require updates to the mhl-ingest code in order to add additional items to the study configuration pushed to the Android application. They also require updates to the Android application to receive and store the updated configuration information.

#### 3.1.2 mhl-estap

This server application listens for messages on the Kafka bus and selectively sends them for indexing by ElasticSearch. The primary class is ESTap. The function receive(String message) implements the logic for what messages are passed to ElasticSearch. This function will require modification to index custom message types. It can also implement real-time modifications to messages received from the Kafka bus prior to indexing.

#### 3.1.3 mhl-vs

This server application implements a custom streaming data  service using web sockets. The corresponding data are visualized using Javascript and plotly. The visualization components are included in the mhl-web application in the the file user_vis.html.

### 3.2 Python Server Applications

The Python-based server application use Python 3.8. It is recommended to install a Python 3.8 development environment outside of the server containers for development purposes. The source code for the server applications is in the mhl-framework/mhl-server-app-src folder. 

#### 3.2.1 mhl-web

This component implements basic participant management functions using a Python/Django web service. Additional fields can be added to the participant records by updating mhl-web/userapp/models.py. To display added fields in the participant listing, add them to mhl-web/userapp/templates/user_list.html. 

To deploy changes, copy the contents of the mhl-framework/mhl-server-app-src/mhl-web to mhl-framework/mhl-services/web folder over top of the existing mhl-web folder. Then, rebuild the container using "docker-compose build mhl-web" and redeploy it using "docker-compose up mhl-web." 

Note that the database structure defined in the models.py file is also accessed by the mhl-ingest server component as part of the participant authentication and configuration push process. The mhl-ingest Java application and corresponding Android applications may also require modification to reflect changes to the participant management data model.

#### 3.2.2 Adding Custom Python-Based Data Analytic Services

Adding additional Python-based data analytic services to the mhl-server Docker infrastructure and connecting them to existing data resources is a relatively easy process. To begin, add a new service directory to mhl-framework/mhl-server/mhl-services. A good starting point for the corresponding Dockerfile is the mhl-analytics service. 

This Dockerfile sets up a container including the needed library Kafka and ElasticSearch library versions. The ElasticSearch.ipynb example notebook gives a basic example of how to query data from ElasticSearch data store. This is a suitable approach for analytic tasks that need to run periodically on a window of data. The Kafka-python documentation provides a starting point for developing applications for interacting directly with streaming data via the creation of Kafka consumers and producers. See https://kafka-python.readthedocs.io/en/master/ for details.  

Once the custom application is ready for testing, add it to the mhl-framework/mhl-server/docker-compose.yml file and start it using "docker-compose up". The application will have access to all of the framework resources including ElasticSearch and the Kafka bus.

### 3.3 Android Applications

The reference Android application was developed using Android Studio Arctic Fox 2020.3.1 Patch 3. We recommend using this version of Android studio to modify and recompile the application. Android Studio can be obtained from the Android Studio download archive here: https://developer.android.com/studio/archive. 

Using Android Studio, open the mhl-framework/mhl-android-src/mhl-android-demo folder. Once the project files have synched, you can proceed to inspect the project structure and modify the app. 

The app implements all code needed to connect to the server and stream data in mhl-Library. The demo application shows the basic workflow needed to connect to the server, generate a message, and send the message to the server. 

The app can easily be modified to stream data from Android phone sensors or sensors from wearable devices connected to the phone via Bluetooth. The message type used by the demo app is sensor-synthetic. Additional message types are included in the mhl-Library/MHLClient/Messages folder. 

Note that the server implements a basic authentication mechanism using the research token. On a first connection, the server verifies that the research token is in the user management database and sends a study configuration to the Android client. Updates to the participant management data model need to be reflected in the Android app code to capture additional participant and study configuration variables. These are managed in the mhl-Library/MHLClient/Configuration/IdentityConfig.java and mhl-Library/MHLClient/Configuration/StudyConfig.java files. 

## 4. Security Limitations

The code in this repository is intended to provide reference implementations of key server and client components needed for streaming data collection and data analytics. The quick start instructions provided above are suitable for initial testing with the included demo app on a secure network with the framework deployed on a local machine such as a laptop or desktop system with active firewall. 

The security of the deployed system can be increased for limited testing on the open Internet following the steps below, but ensuring secure deployment as well as meeting regulatory requirements for human subjects data collection and storage requires substantial expertise and is beyond the scope of this guide.  

### 4.1 Key Generation

The sever and client components use key-based authentication. This repository includes an initial set of keys for easy deployment for testing on secured local networks. These keys are used both in the Java server components and the Android client components. Security can be improved by re-generating these keys and recompiling the server and Android client applications.

To generate new keys, navigate to the mhl-framework/mhl-key-generator directory, run mhl_key_generator.sh and follow the instructions to generate new keys. Next, follow the instructions below to re-compile study applications and re-build the Docker services system.

### 4.2 Re-Compiling Server Applications

The custom server applications use the Apache Maven build system. First install Maven by following the instructions here: https://maven.apache.org/users/index.html. Mac users may find it easier to install Maven using a package manager such as Homebrew (e.g., https://formulae.brew.sh/formula/maven). Next, navigate to the mhl-framework root directory and run build-server.sh. Ensure that there are no build failures before continuing.

### 4.3 Re-Compiling Android Application

The mhl_key_generator.sh script places updated keys in the resources directory of the reference included reference application, MHLDemo. All that is needed is to re-compile to application. The application was developed using Android Studio Arctic Fox 2020.3.1 Patch 3. We recommend using this version of Android studio to recompile the application. It can be downloaded from the Android Studio download archive here: https://developer.android.com/studio/archive. Using Android Studio, open the mhl-framework/mhl-android-src/mhl-android-demo folder. Once Android Studio has synchronized the project files, the application can be re-compiled. Once the application can been re-compiled, it can be re-deployed to an Android device (with developer option enabled) using Android Studio or the compiled APK can be side loaded. 

### 4.4 Improving Server Security 

For test deployment with clients connected from the open Internet (as opposed to a secured local network), the server framework should be placed on a secured system with only the data ingestion port exposed (port 9400). Connections from clients that do not have access to a secure key matching that of the running mhl-ingest server will be rejected. Data streamed from the Android client into the system uses a TLS socket, so data are encrypted during transport from the client to the server.

All ports used by the framework other than 9400 should be closed to external traffic. Note that special care should be taken with the ElasticSearch database. Ports 9200, 9300, 9803 and 9822 should never be exposed to the open Internet under any circumstances. In addition, the ElasticSearch database is not encrypted at rest by default, so anyone with either physical or SSH access to the machine where the server is running will have unrestricted access to view, download, modify or delete any data stored in ElasticSearch. Refer to to the Elastic.co guide on getting started with ElasticSearch security to configure project-specific security settings.

Once all mhl-framework ports have been closed (except for the data ingestion port, 9400), the web interfaces on ports 9801, 9802, 9803, and 9804 can be accessed from the server locally or through ssh tunnels with key-based security. It is also recommended to stop services that are not needed when running test deployments on the open Internet. Note that the services running on these ports do not have additional authentication layers, so individuals with SSH or physical access to the server running the framework will be able to interact with these services without providing credentials beyond those allowing access to the server. 
