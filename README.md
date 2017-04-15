# CRIMEGRAPH

*Crime network analysis leveraging data stream processing*

Big data analytics is a disruptive technology that can reshape core tasks of security intelligence.
The real-time discovery of hidden criminal patterns is an outstanding challenge for security and law enforcement agencies.
In particular, predicting the evolution of criminal networks and uncovering concealed relationships can efficiently guide investigations for better decision-making.

In this context, it is necessary to develop social network metrics that are both domain-aware and ready to be executed in a data stream environment.
That is why we propose two structural local metrics for link detection and prediction, together with their data stream processing implementation.
The experimental results show that the proposed metrics can reach up to ??\% accuracy with an average latency of ?? ms.

## Requirements
To execute the app locally you need the following to be installed to your system:

* Java
* Maven
* Flink
* Neo4J
* Kafka
* Kafka client (Python)

To deploy the app to a Digital Ocean droplet, you need the following to be installed on your system:
* Vagrant
* Vagrant plugin for Digital Ocean
* Ansible

## Setup
First you need to create the Kafka topic `main-topic`:

    $kafka-home> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic main-topic

Test the topic creation:

    $kafka-home> bin/kafka-topics.sh --list --zookeeper localhost:2181

To test message publishing:

    $kafka-home> bin/kafka-console-producer.sh --broker-list localhost:9092 --topic main-topic

    $kafka-home> bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic main-topic


## Build
The app building is provided by Apache Maven. To build the app you need to run

    $crimegraph> mvn clean package -Pbuild-jar

If you want to skip tests, add the profile `skip-tests`.

Copy the target `target/crimegraph-1.0.jar` into `data/flink/flink_master/crimegraph/crimegraph-1.0.jar`.


## Deploy
Provision EC2 instances:

  $crimegraph> vagrant up

Every time you want to test some changes, run:

  $crimegraph>vagrant provision

To destroy all the EC2 instances:

  $crimegraph>vagrant destroy


## Usage
Log into the flink master:

    $crimegraph>vagrant ssh flink_master

and launch crimegraph:

    $flink_master>sudo flink run /vagrant/data/instance/crimegraph/crimegraph-1.0.jar --config /vagrant/data/instance/crimegraph/config.yaml


## Authors
Giacomo Marciani, [gmarciani@acm.org](mailto:gmarciani@acm.org)

Michele Porretta, [mporretta@acm.org](mailto:mporretta@acm.org)


## References
Giacomo Marciani, Michele Porretta. 2017. *Crimegraph*. Series. Organization, Country [Read here](https://gmarciani.com)


## License
The project is released under the [MIT License](https://opensource.org/licenses/MIT).
