# UE : Initiation à la recherche - IMT Atlantique - FIL 2022

<p style="text-align: justify;"> This project aimed at giving a first taste to students of the world of 
research in computer science. Doing so by using published paper and 
implementing the algorithm described in it.</p>

# The paper : *AS-cast: Lock Down the Traffic of Decentralized Content Indexing at the Edge*

This project aims at
creating a decentralized partitioning protocol that
guarantees consistent partitioning and termination even
in dynamic settings where nodes join and
leave the system, create or destroy partitions. 
This was developed using [Spring Boot, it provides a comprehensive programming and configuration model for modern Java-based enterprise applications - on any kind of deployment platform.](https://spring.io/projects/spring-framework)


 Thus, you will find in the project :

* [Java class](src/main/java/fr/imtatlantique/simulation/Structures)
to represent the different instances in a network corresponding to an edge infrastructure.
*  A [Controller](src/main/java/fr/imtatlantique/simulation/Controller) class to handle incoming messages from edge.
*  A [Service](src/main/java/fr/imtatlantique/simulation/Service) class that implements the AS-cast algorithm as 
described in the research paper and http service to send data between servers.
   
# Requirements 
* Java 17 
* Maven

# How to test 

* For windows for example, you will need to install [java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
  and add a *JAVA_HOME* variable inside your environment path, [help here](https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html).
* You will need to [download](https://maven.apache.org/download.cgi) and [install](https://maven.apache.org/install.html) maven and add 
the *bin* folder path to the environment variables.
* Import the project into Intellij for example and launch three different terminal (you can name them B, C, D).
* Open the *application.properties* file and modify the value of the port, and the serverID, then, 
  inside each terminal execute the following command : 
  * *mvnw clean package*
  * *mvnw spring-boot:run*
* An instance of the project will be launch and will represent a node in the network.
* Once this is done, execute the *main.java* to launch server A and the tests. 
* **Be careful to change the values in *application.properties* whenever you launch a server, otherwise you will encounter
PORT and server ID conflicts**


# Explanation 

* A controller handles the incoming messages. They are of different types and have each a predefined routes:
  * */add_node* : this route is used to add a new neighbor to the current server
  * */update_weights* : this route is used to notify a server when the matrix of weights is updated
  * */del_node* : this route is used to delete a server from the list of neighbors of the current server
  * */add* : this route is used when the current server wants to become a source
  * */del* : this route is used when the current server wants to remove itself as a source
  * */receive_add* : this route is used when a server wants to propagate an add message to its neighbors
  * */receive_del* : this route is used when a server wants to propagate a del message to its neighbors
  

* A service is called whenever the controller needs to update the state of the service 
  (when becoming a source or receiving new neighbors or new weights). This service implements the AS-Cast algorithm.


* We are using Json to serialize data and by doing so we cannot send a Server instance, and a Message instance at the same time which are 
  needed by the algorithm. To tackle aforementioned problem we use a payload class for each type of message (PayloadMadd and PayloadMDel).
  Every time a server needs to send a message to its neighbor, the server will create a payload object. Depending on the type of 
  message it will be *PayloadMAdd* or *PayloadMDel*. This payload instance will be serialized and sent by http request to every neighbor
  of the current server. When received, the incoming JSON object is converted to the adequate payload, and we call the AS-Cast algorithm by passing 
  the server and the message.

  
* To send http request a JSONUtils class and a ServerService class are used. 
  The first one (JSONUtils) is used to transform either JSON to object or objet to JSON. 
  The second one (ServerService) is in charge of creating the http request. All the requests are POST requests.
 

* The file *application.properties* is used to specify the *PORT* of the server that it will listen to. 
  It is also used to specify the *server ID*. This file needs to be modified when testing are executed.


* At the beginning of the test we generate weights that needs to be sent to every server in order for them to know 
the same values.
  

# References 

* [AS-cast: Lock Down the Traffic of Decentralized Content Indexing at the Edge](https://hal.inria.fr/hal-03333669/)
* [peersim-partition github](https://anonymous.4open.science/r/peersim-partition-5592/README.md)
