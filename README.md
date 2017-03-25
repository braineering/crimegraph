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


## Build
The app building is provided by Apache Maven. To build the app you need to run

    $app> mvn clean package -P [YOUR-SNA-CLASS]

where *[YOUR-SNA-CLASS]* could be one of the following:

* `local`: potential and hidden links are evaluated using local metrics.

* `quasi-local`: potential links are evaluated using quasi-local metrics, whereas hidden links are evaluated using local metrics.

If you want to skip tests, add the profile `skip-tests`.


## Usage
Start Neo4J

    $neo4j-home>./bin/neo4j start

Start the Flink cluster

    $flink-home>./bin/start-local.sh

Submit the app to the cluster

    $> flink run path/to/crimegraph/target/crimegraph-1.0.jar

You can inspect SNA results navigating the graph with Neo4J browser running at *http://localhost:7474*.

Once you have finished the analysis, stop the Flink cluster

    $flink-home>./bin/stop-local.sh

and Neo4J

    $neo4j-home>./bin/neo4j stop

## Deploy
Run the provisioning with Vagrant

  $crimegraph> vagrant up --provider=digital_ocean

When provisioning is complete, visit the following

  http://x.x.x.x:7474

To destroy the droplet, run

  $crimegraph>vagrant destroy

## Provisioning
The provider supports the following Vagrant sub-commands:

* `vagrant destroy` - Destroys the Droplet instance.
* `vagrant ssh` - Logs into the Droplet instance using the configured user account.
* `vagrant halt` - Powers off the Droplet instance.
* `vagrant provision` - Runs the configured provisioners and rsyncs any specified config.vm.synced_folder.
* `vagrant reload` - Reboots the Droplet instance.
* `vagrant rebuild` - Destroys the Droplet instance and recreates it with the same IP address which was previously assigned.
* `vagrant status` - Outputs the status (active, off, not created) for the Droplet instance.


## Authors
Giacomo Marciani, [gmarciani@acm.org](mailto:gmarciani@acm.org)

Michele Porretta, [mporretta@acm.org](mailto:mporretta@acm.org)


## References
Giacomo Marciani, Michele Porretta. 2017. *Crimegraph*. Series. Organization, Country [Read here](https://gmarciani.com)


## License
The project is released under the [MIT License](https://opensource.org/licenses/MIT).
