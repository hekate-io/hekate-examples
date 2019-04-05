# Hekate.io Examples

This repository contains a collection of code examples for [Hekate.io](https://github.com/hekate-io/hekate) project.

All examples are organized as a single module Maven project that produces multiple build artifacts (one per example). 
Each such artifact is a self-contained  Spring Boot application that can easily be copied to multiple hosts.

### Software requirements:

 - Latest stable [Oracle JDK 8](http://www.oracle.com/technetwork/java/) or [Open JDK 8](http://openjdk.java.net/)


### How to build:

 - `cd` to the project's root folder
 - run `./mvnw clean package` 

### How to run:

 - `cd` to the project's `/target` folder
 - run `java -jar <example-jar>` where `<example-jar>` is the name of the example jar file 
 
 For example:
 
 - `java -jar hekate-example-cluster-view.jar` 
 - `java -jar hekate-example-distributed-lock.jar`
 - etc...

### Configuration:

All examples use the same **[application.yml](src/main/resources/application.yml)** configuration file (Spring Boot YAML format).

This file gets automatically included into all of the jar-files at build time. It is possible to override configuration properties by 
specifying an external configuration file as described in Spring Boot's 
[documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files).

## Examples Reference

### Cluster

 - `hekate-example-cluster-listener.jar` 
    ([source code](src/main/java/io/hekate/examples/cluster/listener/ClusterListenerExample.java)) - 
    An example which shows how to implement a simple listener of cluster events 
 - `hekate-example-cluster-view.jar` 
    ([source code](src/main/java/io/hekate/examples/cluster/view/ClusterViewExample.java)) - 
    An example which shows how to build different cluster views based on dynamic filtering of the cluster topology
 - `hekate-example-cluster-future.jar` 
    ([source code](src/main/java/io/hekate/examples/cluster/future/ClusterFutureExample.java)) - 
    An example which shows how to register a cluster topology condition and get a notification once it is met
    

### Remote Procedure Calls (RPC)

 - `hekate-example-rpc-request.jar` 
    ([source code](src/main/java/io/hekate/examples/rpc/request/RpcRequestExample.java)) - 
    A simple example of request/response RPCs
 - `hekate-example-rpc-agregate.jar` 
    ([source code](src/main/java/io/hekate/examples/rpc/aggregate/RpcAggregateExample.java)) - 
    An example of how to submit an RPC request to all cluster nodes and how to aggregate RPC results
 - `hekate-example-rpc-split-agregate.jar` 
    ([source code](src/main/java/io/hekate/examples/rpc/split/RpcSplitAggregateExample.java)) - 
    An example of how to perform a Map/Reduce style of RPC communications

### Distributed Locks

 - `hekate-example-distributed-lock.jar` 
    ([source code](src/main/java/io/hekate/examples/lock/DistributedLockExample.java)) - 
    An example of how to use distributed locks

### Leader Election

 - `hekate-example-election.jar` 
    ([source code](src/main/java/io/hekate/examples/election/ElectionExample.java)) -
    An example of how to use Hekate election service to implement a cluster-wide singleton

### Messaging

 - `hekate-example-messaging-request.jar` 
    ([source code](src/main/java/io/hekate/examples/messaging/request/MessagingRequestExample.java)) - 
    A simple example of request/response messaging
 - `hekate-example-messaging-aggregate.jar` 
    ([source code](src/main/java/io/hekate/examples/messaging/aggregate/MessagingAggregateExample.java)) - 
    An example of how to submit a message to all cluster nodes and how to aggregate their responses
 - `hekate-example-messaging-broadcast.jar` 
    ([source code](src/main/java/io/hekate/examples/messaging/broadcast/MessagingBroadcastExample.java)) - 
    A simple example of broadcast messaging

