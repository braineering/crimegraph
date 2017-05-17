# CRIMEGRAPH

*Criminal networks analysis leveraging data stream processing*

The ability to discover patterns of interest in criminal networks can support and ease the investigation tasks by security and law enforcement agencies. Being the criminal network a special case of social network, we can properly reuse most of the state-of-the-art techniques to discover patterns of interests, i.e., hidden and potential links. Nevertheless, in time-sensible scenarios, like the one involving criminal actions, the ability to discover patterns in a (near) real-time manner can be of primary importance.
In this paper, we investigate the identification of patterns for link detection and prediction on an evolving criminal network. Being the criminal network a dynamic social graph, we exploit a stream processing approach to extract information of interest as soon as data is generated. To this end, we also propose three new similarity social network metrics, specifically tailored for criminal link detection and prediction. 
Then, we develop a flexible data stream processing application, relying on the framework Apache Flink; this solution allows to deploy and evaluate the newly proposed metrics as well as the one existing in literature. The experimental results show that the new metrics we propose can reach up to 83% accuracy in detection and 82% accuracy in prediction, resulting competitive with the state of the art metrics. 

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

  $crimegraph>mvn clean package -Pbuild-jar

  $crimegraph>vagrant rsync

  $crimegraph>vagrant provision

To destroy all the EC2 instances:

  $crimegraph>vagrant destroy


## Usage

    $crimegraph> vagrant ssh kafka_master -c "sudo systemctl restart kafka"

    $crimegraph-monitor> vagrant ssh -c "sudo crimegraph-monitor check_kafka --kafkaBroker [EC2_KAFKA_MASTER]:9092 --kafkaTopic main-topic"

    $crimegraph-monitor> vagrant ssh -c "sudo crimegraph-monitor check_kafka --kafkaBroker [EC2_KAFKA_MASTER]:9092 --kafkaTopic main-topic"

where *[EC2_KAFKA_MASTER]* is the public address of the EC2 instance named *kafka_master*.

You need to run *crimegraph-monitor check_kafka* twice, because the first time creates the topic, while the second one tests that everything is ok.

If *crimegraph check_kafka* returns *true*, everything is ok, and you can proceed to start Crimegraph.

    $crimegraph> vagrant ssh flink_master -c "sudo flink run /vagrant/target/crimegraph-1.0.jar --config /vagrant/data/instance/crimegraph/config.yaml"

Now you can monitor the running application, visiting `[EC2_FLINK_MASTER]:8081` and `[EC2_NEO4J_MASTER]:7474`.

It is time to submit some data!

First of all, list all the available datasets:

    $crimegraph-monitor> vagrant ssh -c "sudo ls /vagrant/data/datasets"

Generate trainset and testset for detection:

    $crimegraph-monitor> vagrant ssh -c "sudo crimegraph-monitor traintest_detection --dataset /vagrant/data/datasets/[DATASET] --trainset /vagrant/data/datasets/[DATASET]\_train_detection.data --testset /vagrant/data/datasets/[DATASET]\_test_detection.data --testRatio [RATIO]"

where *[DATASET]* is the chosen dataset and *[RATIO]* is the test ratio, that is a number in (0.0,1.0) indicating the percentage of original dataset that will be deleted (a typical value is 0.10).

Verify that datasets have been created:

  $crimegraph-monitor> vagrant ssh -c "sudo ls /vagrant/data/datasets"

Now publish the trainset against Crimegraph:

    $crimegraph-monitor> vagrant ssh -c "sudo crimegraph-monitor publish --dataset /vagrant/data/datasets/[DATASET]\_train_detection.data --kafkaBroker [EC2_KAFKA_MASTER]:9092 --kafkaTopic main-topic"

where *[EC2_KAFKA_MASTER]* is the public address of the EC2 instance named *kafka_master*.

Check the publication status:

    $crimegraph-monitor> vagrant ssh -c "sudo crimegraph-monitor check_dataset_db --dataset /vagrant/data/datasets/[DATASET]\_train_detection.data --neo4jHostname bolt://[EC2_NEO4J_MASTER] --neo4jUsername neo4j --neo4jPassword password --timeout 10"

where *[EC2_NEO4J_MASTER]* is the public address of the EC2 instance named *neo4j_master*.

Now evaluate AUC for detection:

    $crimegraph-monitor> vagrant ssh -c "sudo crimegraph-monitor auc_detection --neo4jHostname bolt://[EC2_NEO4J_MASTER]:7687 --neo4jUsername neo4j --neo4jPassword password --dataset /vagrant/data/datasets/[DATASET] --trainset /vagrant/data/datasets/[DATASET]\_train_detection.data --testset /vagrant/data/datasets/[DATASET]\_test_detection.data --output /vagrant/data/[DATASET]\_auc_detection.out"

## Authors
Giacomo Marciani, [gmarciani@acm.org](mailto:gmarciani@acm.org)

Michele Porretta, [mporretta@acm.org](mailto:mporretta@acm.org)


## References
Giacomo Marciani, Michele Porretta. 2017. *Crimegraph*. Series. Organization, Country [Read here](https://gmarciani.com)


## License
The project is released under the [MIT License](https://opensource.org/licenses/MIT).
