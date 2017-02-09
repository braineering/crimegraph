# CRIMEGRAPH

*Crime network analysis leveraging data stream processing*

Social Network Analysis (SNA) applied to criminal networks leveraging Apache Flink.


## Build
The app building is provided by Apache Maven. To build the app you need to run

    $app> mvn clean package
    
If you want to skip tests:

    $app> mvn clean package -P skip-tests
    
If you want to build with code optimization:

    $app> mvn clean package -P optimize


## Usage
The app can be run both with and without Apache Maven.


### Usage with Apache Maven
To run the app with Apache Maven, you need to run
    
    $app>mvn exec:java -Dargs="YOUR ARGUMENTS HERE"

For example, to print the app version, you need to run

    $app>mvn exec:java -Dargs="--version"
    
Running the app this way could be useful during development, 
because it is repackaged at every execution.
    
    
### Usage without Apache Maven    
To run the app without Apache Maven, you need to run

    $>java -jar path/to/app-1.0-SNAPSHOT-jar-with-dependencies.jar YOUR ARGUMENTS HERE
    
For example, to print the app version, you need to run

    $>java -jar path/to/app-1.0-SNAPSHOT-jar-with-dependencies.jar --version


## Authors
Giacomo Marciani, [gmarciani@acm.org](mailto:gmarciani@acm.org)
Michele Porretta, [mporretta@acm.org](mailto:mporretta@acm.org)


## References
Giacomo Marciani. 2017. *Title*. Series. Organization, Country [Read here](https://gmarciani.com)


## License
The project is released under the [MIT License](https://opensource.org/licenses/MIT).
