# The Credit Card code test

# The cloud context

In the project [Credit cards tiers](https://github.com/antoniosignore/credit-card-tiers) we have provided a solution of the trivial credit card CRUD (partial) test in stacked layer traditional fashion.

In software engineering, multitier architecture (often referred to as n-tier architecture) or multilayered architecture is a client–server architecture in which presentation, business logic, and data management functions are physically separated.

Scalability happens by multiple deployments in parallel of the whole monolith application.

In recents years new alternative architectural patterns have emerged thanks to the some pioneering companies (netflix, uber, airbnb) that have open sourced internal projects designed to exploit the benefit of cloud technologies.

Application from monolith become distributed and that required a mentality shift about how to:

* how the business problems are approached:  DDD / CQRS 
* how the software teams are organized:  Agile/ Scrum and/or Kanban  
* how the software is built, tested integrated and quickly deploied: CI/CD infrastructure
* how the clouds are created and managed: Terraform, Kubernetes, Istio  
 
There is a general consent in the software community that all the above listed aspects must to fall in place in order to increase the chances of success in microservices application both in case of greenfield applications or digital transformation of monoliths to distributed systems in the cloud.
   
 This project focused on the DDD aspect and proposes a CQRS event driven and event sourced implementation to compare it with the monolith tiered approach.

The framework selected to help us is the Axon framework produced by a Ducth company called AxonIQ

Its main feature is the approach that suggests to the developer:  

* the developer must think in terms of the business use cases (Data driven design) and select an language that is tailored and understood by expert domain and developers (ubiquitus language) 
* identify the bounded contexts in the business organization and identify the Root Aggregates of the data
* model the bounded contexts to microservices (i.e. one microservice per bounded context) sometimes also referred as Actor model 
* model the problem in terms of state change commands that can be sent to each microservices and publish the immutable events to an event sourcing DB 

The framework then offers a rich set of Annotations that drive the developer towards the solution and most importantly developing starting with a monolith.

As Martin Fowler wrote: most of the failures in microservices projects are projects where the developers started immediately in distributed fashion.

Success happens when teams start with a monolith and then as the project evolves and new bounded contexts emerge the new microservices are added.

One of the key properties that Axon offers to achieve this goal is the location transarency: 

    the developer develop with specific java annotations without bothering about where the other components are located. 

Axon offers a vast choice of adapters/bus technologies that can be select to realize the distribution as well the the event sourcing (i.e. Kafka, ActiveMQ, RabbitMq) as well several dataSources SQL and/or NO_Sql for the Query model projections and/or the validation side of the CommandModel

For particular test I have used the Axon Server (recently announced on the 18th of October) as event bus and events db to implement the CQRS Event Sourcing and the Event driven pattern.


### AGGREGATE

Decide what is the Root Aggregate: in this example it is clearly the Game and then attached to it the players, the current latest number and so on.

The Aggregate is implemented in:

    CreditCardAggregate.java

This is a clear example of a Non-Anemic model implementation: the class repreenting the data includes also business logic.

# Structure of the App

The Credit card application is split into four parts, using four sub-packages of `com/sapient/demo/creditcard`:
* The `api` package contains the ([Kotlin](https://kotlinlang.org/)) sourcecode of the messages and entity. They form the API (sic) of the application.
* The `command` package contains the Creditcard Aggregate class, with all command- and associated eventsourcing handlers.
* The `query` package provides the query handlers, with their associated event handlers.
* The `gui` package contains the [Vaadin](https://vaadin.com/)-based Web GUI.

### Command/Events as messages.  

Define in one Kotlin  file the command and events that mode the state of the system:

    com/sapient/demo/creditcard/api/api.kt

Kotlin is particularly effective to provide in very concise way all the commands and events in the system.

### Command and Query models

    com/sapient/demo/creditcard/command  
    com/sapient/demo/creditcard/query

### GUI

    com/sapient/demo/creditcard/gui


### EventSourcing

Axon server


I adoped the default embedded EventSource provided by Axon to keep it simple

### TEST FIRST

Axon provide test fictures that allow the user to write tests in given()/when()/expect() fashion which is
formidable because allows the programmer to write the tests thinking about commands and events (or error) to
be expected.

    io/axoniq/labs/game/commandmodel/CreditCardAggregateTest.java

Warning: test with given older events are not working yet with the external event store (working with Axon people to solve the issue) 

## Microservices decomposition

The app can be run in various modes, using [Spring-boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html): by selecting a specific profile, only the corresponding parts of the app will be active. Select none, and the default behaviour is activated, which activates everything. This way you can experiment with Axon in a (structured) monolith as well as in micro-services.


## Background story

* Credit card can be issued: a new credit card gets created with some amount of money as credit limit.
* Credit cards can be _purchased_: all or part of the monetary value stored on the credit card is used to purchase something.


Of these packages, `command`, `query`, and `gui` are also configured as profiles.

## IntelliJ Editor settings

For Intellij Add the annotation processor for Lombok 

    File/Settings/Annotation processor  -->  Make sure the checkbox: Enable annotation processor is selected.

### Building the Credit Card app from the sources

To build the demo app, simply run the provided [Maven wrapper](https://www.baeldung.com/maven-wrapper):

```
mvnw clean package
```
Note that for Mac OSX or Linux you probably have to add "`./`" in front of `mvnw`.


# Running the Credit Card app

### Running Axon Server

By default the Axon Framework is configured to expect a running Axon Server instance, and it will complain if the server is not found. To run Axon Server, you'll need a Java runtime (JRE versions 8 through 10 are currently supported, Java 11 still has Spring-boot related growing-pains).  A copy of the server JAR file has been provided in the demo package. You can run it locally, in a Docker container (including Kubernetes or even Mini-kube), or on a separate server.

### Running Axon Server locally

To run Axon Server locally, all you need to do is put the server JAR file in the directory where you want it to live, and start it using:

```
java -jar axonserver-4.0-RC1.jar
```

You will see that it creates a subdirectory `data` where it will store its information.

### Running the credit card app

The simplest way to run the app is by using the Spring-boot maven plugin:

```
./mvnw spring-boot:run
```

A simple UX has been implemented in Vaadin and available at:

```
http://localhost:8080
```


# REST

## Swagger URL

    http://localhost:8080/swagger-ui.html

# CURL test

### Get all credit cards

    curl --request GET  --url http://localhost:8080/creditcards --header 'content-type: application/json'

### Create a credit card
	
	curl --request POST  --url http://localhost:8080/creditcards \
	--header 'content-type: application/json'  \
	--data '{"creditLimit": 1000,"id": "34345678900666","name": "antonio"}'

### Make a purchase with the credit card
	
	curl --request POST  --url http://localhost:8080/creditcards/s/purchase \
	--header 'content-type: application/json'  \
	--data '1'

### Get all events from the Event Store

    curl --request GET  --url http://localhost:8080/events/s



However, if you have copied the jar file `creditcard-distributed-1.0.jar` from the Maven `target` directory to some other location, you can also start it with:

```
java -jar creditcard-distributed-1.0.jar
```

The Web GUI can be found at [`http://localhost:8080`](http://localhost:8080).

If you want to activate only the `command` profile, use:

```
java -Dspring.profiles.active=command creditcard-distributed-1.0.jar
```
Idem for `query` and `gui`.



### Running the Credit Card app as micro-services

To run the Credit Card app as if it were three seperate micro-services, use the Spring-boot `spring.profiles.active` option as follows:

```
$ java -Dspring.profiles.active=command -jar creditcard-distributed-1.0.jar
```
This will start only the command part. To complete the app, open two other command shells, and start one with profile `query`, and the last one with `gui`. Again you can open the Web GUI at [`http://localhost:8080`](http://localhost:8080). The three parts of the application work together through the running instance of the Axon Server, which distributes the Commands, Queries, and Events.


# FUTURE DEVELOPMENT (not tested yer)

### Running the Creditcard app as micro-services

To run the Creditcard app as if it were three seperate micro-services, use the Spring-boot `spring.profiles.active` option as follows:

    ```
        $ java -Dspring.profiles.active=command -jar creditcard-distributed-1.0.jar
    ```
This will start only the command part. To complete the app, open two other command shells, and start one with profile `query`, and the last one with `gui`. Again you can open the Web GUI at [`http://localhost:8080`](http://localhost:8080). The three parts of the application work together through the running instance of the Axon Server, which distributes the Commands, Queries, and Events.


### Running Axon Server in a Docker container

To run Axon Server in Docker you can use the image provided on Docker Hub:

```
$ docker run -d --name my-axon-server -p 8024:8024 -p 8124:8124 axoniq/axonserver
...some container id...
$
```

*WARNING* This is not a supported image for production purposes. Please use with caution.

If you want to run the clients in Docker containers as well, and are not using something like Kubernetes, use the "`--hostname`" option of the `docker` command to set a useful name like "axonserver", and pass the `AXONSERVER_HOSTNAME` environment variable to adjust the properties accordingly:

```
$ docker run -d --name my-axon-server -p 8024:8024 -p 8124:8124 --hostname axonserver -e AXONSERVER_HOSTNAME=axonserver axoniq/axonserver
```

When you start the client containers, you can now use "`--link axonserver`" to provide them with the correct DNS entry. The Axon Server-connector looks at the "`axon.axonserver.servers`" property to determine where Axon Server lives, so don't forget to set it to "`axonserver`".

### Running Axon Server in Kubernetes and Mini-Kube

*WARNING*: Although you can get a pretty functional cluster running locally using Mini-Kube, you can run into trouble when you want to let it serve clients outside of the cluster. Mini-Kube can provide access to HTTP servers running in the cluster, for other protocols you have to run a special protocol-agnostic proxy like you can with "`kubectl port-forward` _&lt;pod-name&gt;_ _&lt;port-number&gt;_". For non-development scenarios, we don't recommend using Mini-Kube.

Deployment requires the use of a YAML descriptor, an working example of which can be found in the "`kubernetes`" directory. To run it, use the following commands in a separate window:

```
$ kubectl apply -f kubernetes/axonserver.yaml
statefulset.apps "axonserver" created
service "axonserver-gui" created
service "axonserver" created
$ kubectl port-forward axonserver-0 8124
Forwarding from 127.0.0.1:8124 -> 8124
Forwarding from [::1]:8124 -> 8124
```

You can now run the Creditcard app, which will connect throught the proxied gRPC port. To see the Axon Server Web GUI, use "`minikube service --url axonserver-gui`" to obtain the URL for your browser. Actually, if you leave out the "`--url`", minikube will open the the GUI in your default browser for you.

To clean up the deployment, use:

```
$ kubectl delete sts axonserver
statefulset.apps "axonserver" deleted
$ kubectl delete svc axonserver
service "axonserver" deleted
$ kubectl delete svc axonserver-gui
service "axonserver-gui" deleted
```

If you're using a 'real' Kubernetes cluster, you'll naturally not want to use "`localhost`" as hostname for Axon Server, so you need to add three lines to the container spec to specify the "`AXONSERVER_HOSTNAME`" setting:

```
...
      containers:
      - name: axonserver
        image: axoniq/axonserver
        imagePullPolicy: Always
        ports:
        - name: grpc
          containerPort: 8124
          protocol: TCP
        - name: gui
          containerPort: 8024
          protocol: TCP
        readinessProbe:
          httpGet:
            port: 8024
            path: /actuator/health
          initialDelaySeconds: 5
          periodSeconds: 5
          timeoutSeconds: 1
        env:
        - name: AXONSERVER_HOSTNAME
          value: axonserver
---
apiVersion: v1
kind: Service
...
```

Use "`axonserver`" (as that is the name of the Kubernetes service) if you're going to deploy the client next to the server in the cluster, which is what you'ld probably want. Running the client outside the cluster, with Axon Server *inside*, entails extra work to enable and secure this, and is definitely beyond the scope of this example.

# Configuring Axon Server

Axon Server uses sensible defaults for all of its settings, so it will actually run fine without any further configuration. However, if you want to make some changes, below are the most common options.

### Environment variables for customizing the Docker image of Axon Server

The `axoniq/axonserver` image can be customized at start by using one of the following environment variables. If no default is mentioned, leaving the environement variable unspecified will not add a line to the properties file.

* `AXONSERVER_NAME`

    This is the name the Axon Server uses for itself.
* `AXONSERVER_HOSTNAME`

    This is the hostname Axon Server communicates to the client as its contact point. Default is "`localhost`", because Docker generates a random name that is not resolvable outside of the container.
* `AXONSERVER_DOMAIN`

    This is the domain Axon Server can suffix the hostname with.
* `AXONSERVER_HTTP_PORT`

    This is the port Axon Server uses for its Web GUI and REST API.
* `AXONSERVER_GRPC_PORT`

    This is the gRPC port used by clients to exchange data with the server.
* `AXONSERVER_TOKEN`

    Setting this will enable access control, which means the clients need to pass this token with each request.
* `AXONSERVER_EVENTSTORE`

    This is the directory used for storing the Events.
* `AXONSERVER_CONTROLDB`

    This is where Axon Server stores information of clients and what types of messages they are interested in.

### Axon Server configuration

There are a number of things you can finetune in the server configuration. You can do this using an "`axonserver.properties`" file. All settings have sensible defaults.

* `axoniq.axonserver.name`

    This is the name Axon Server uses for itself. The default is to use the hostname.
* `axoniq.axonserver.hostname`

    This is the hostname clients will use to connect to the server. Note that an IP address can be used if the name cannot be resolved through DNS. The default value is the actual hostname reported by the OS.
* `server.port`

    This is the port where Axon Server will listen for HTTP requests, by default `8024`.
* `axoniq.axonserver.port`

    This is the port where Axon Server will listen for gRPC requests, by default `8124`.
* `axoniq.axonserver.event.storage`

    This setting determines where event messages are stored, so make sure there is enough diskspace here. Losing this data means losing your Events-sourced Aggregates' state! Conversely, if you want a quick way to start from scratch, here's where to clean.
* `axoniq.axonserver.controldb-path`

    This setting determines where the message hub stores its information. Losing this data will affect Axon Server's ability to determine which applications are connected, and what types of messages they are interested in.
* `axoniq.axonserver.accesscontrol.enabled`

    Setting this to `true` will require clients to pass a token.
* `axoniq.axonserver.accesscontrol.token`

    This is the token used for access control.

### The Axon Server HTTP server

Axon Server provides two servers; one serving HTTP requests, the other gRPC. By default these use ports 8024 and 8124 respectively, but you can change these in the settings.

The HTTP server has in its root context a management Web GUI, a health indicator is available at `/actuator/health`, and the REST API at `/v1`. The API's Swagger endpoint finally, is available at `/swagger-ui.html`, and gives the documentation on the REST API.


