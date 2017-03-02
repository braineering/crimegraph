# CRIMEGRAPH

*Crime network analysis leveraging data stream processing*

Social Network Analysis (SNA) applied to criminal networks leveraging Apache Flink.


## Build
The app building is provided by Apache Maven. To build the app you need to run

    $app> mvn clean package -P build-jar
    
If you want to skip tests:

    $app> mvn clean package -P skip-tests
    
If you want to build with code optimization:

    $app> mvn clean package -P optimize


## Usage
Start the Neo4J instance

    $neo4j-home>./bin/neo4j start
    
and visit the Neo4J browser running at *http://localhost:7474*.

Start the Flink cluster
    
    $flink-home>./bin/start-local.sh
    
Submit the application to the cluster

    $> flink run path/to/crimegraph/target/crimegraph-1.0.jar
    
Stop the Flink cluster
    
    $flink-home>./bin/stop-local.sh
    
Stop the Neo4J instance

    $neo4j-home>./bin/neo4j stop


## Authors
Giacomo Marciani, [gmarciani@acm.org](mailto:gmarciani@acm.org)
Michele Porretta, [mporretta@acm.org](mailto:mporretta@acm.org)


## References
Giacomo Marciani. 2017. *Title*. Series. Organization, Country [Read here](https://gmarciani.com)


## License
The project is released under the [MIT License](https://opensource.org/licenses/MIT).
