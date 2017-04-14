/*
  The MIT License (MIT)

  Copyright (c) 2016 Giacomo Marciani and Michele Porretta

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:


  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.


  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */

package com.acmutv.crimegraph.config;

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.metric.HiddenMetrics;
import com.acmutv.crimegraph.core.metric.PotentialMetrics;
import com.acmutv.crimegraph.core.source.KafkaProperties;
import com.acmutv.crimegraph.core.source.SourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * The app configuration model.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Yaml
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);

  /**
   * Default value for property {@code source}.
   */
  public static final SourceType SOURCE = SourceType.FILE;

  /**
   * Default value for property {@code topic}.
   */
  public static final String TOPIC = "main-topic";

  /**
   * Default value for property {@code kafkaProperties}.
   */
  public static final KafkaProperties KAFKA_PROPERTIES = new KafkaProperties();

  /**
   * Default value for property {@code dataset}.
   */
  public static final String DATASET = "resources/crimegraph/data/crimegraph.data";

  /**
   * Default value for property {@code hiddenMetric}.
   */
  public static final HiddenMetrics HIDDEN_METRIC = HiddenMetrics.LOCAL;

  /**
   * Default value for property {@code hiddenLocality}.
   */
  public static final long HIDDEN_LOCALITY = 1;

  /**
   * Default value for property {@code hiddenWeights}.
   */
  public static final List<Double> HIDDEN_WEIGHTS = new ArrayList<Double>(){{add(1.0);}};

  /**
   * Default value for property {@code hiddenThreshold}.
   */
  public static final double HIDDEN_THRESHOLD = 0.8;

  /**
   * Default value for property {@code potentialMetric}.
   */
  public static final PotentialMetrics POTENTIAL_METRIC = PotentialMetrics.LOCAL;

  /**
   * Default value for property {@code potentialLocality}.
   */
  public static final long POTENTIAL_LOCALITY = 1;

  /**
   * Default value for property {@code potentialWeights}.
   */
  public static final List<Double> POTENTIAL_WEIGHTS = new ArrayList<Double>(){{add(1.0);}};

  /**
   * Default value for property {@code potentialThreshold}.
   */
  public static final double POTENTIAL_THRESHOLD = 0.8;

  /**
   * Default value for property {@code ewmaFactor}.
   */
  public static final double EWMA_FACTOR = 0.5;

  /**
   * Default value for property {@code neo4j.config}.
   */
  public static final DbConfiguration NEO4J_CONFIG =
      new DbConfiguration("bolt://localhost:7687", "neo4j", "password");

  /**
   * Default value for property {@code parallelism}.
   */
  public static final int PARALLELISM = 2;

  /**
   * The source type.
   * Default is: {@code FILE}.
   */
  private SourceType source = SOURCE;
  /**
   * The topic to subscribe to.
   * Default is: {@code crimegraph}.
   */
  private String topic = TOPIC;

  /**
   * Kafka connector properties.
   * Default is {@code {}}.
   */
  private KafkaProperties kafkaProperties = new KafkaProperties(KAFKA_PROPERTIES);

  /**
   * The pathname of the file or directory containing the dataset.
   * Default is: {@code crimegraph-dataset.txt}
   */
  private String dataset = DATASET;

  /**
   * The metric used to compute hidden links.
   * Default is: {@code LOCAL}
   */
  private HiddenMetrics hiddenMetric = HIDDEN_METRIC;

  /**
   * The locality degree for hidden link score.
   * Default is: {@code 1}.
   */
  private long hiddenLocality = HIDDEN_LOCALITY;

  /**
   * The weight vector for hidden link score.
   * Default value is {@code [1.0]}.
   */
  private List<Double> hiddenWeights = HIDDEN_WEIGHTS;

  /**
   * The threshold for the hidden link score.
   * Default is: {@code 0.5}
   */
  private double hiddenThreshold = HIDDEN_THRESHOLD;

  /**
   * The metric used to compute potential links.
   * Default is: {@code LOCAL}
   */
  private PotentialMetrics potentialMetric = POTENTIAL_METRIC;

  /**
   * The locality degree for potential link score.
   * Default is: {@code 1}.
   */
  private long potentialLocality = POTENTIAL_LOCALITY;

  /**
   * The weight vector for potential link score.
   * Default value is {@code [1.0]}.
   */
  private List<Double> potentialWeights = POTENTIAL_WEIGHTS;

  /**
   * The threshold for the potential link score.
   * Default is: {@code 0.5}
   */
  private double potentialThreshold = POTENTIAL_THRESHOLD;

  /**
   * The EWMA factor.
   * Default is: {@code 0.5}.
   */
  private double ewmaFactor = EWMA_FACTOR;

  /**
   * Configuration for Neo4J instance.
   * Default is {@code (bolt://localhost:7474, neo4j, password)}.
   */
  private DbConfiguration neo4jConfig = NEO4J_CONFIG;

  /**
   * The operator parallelism.
   * Default is: {@code 2}.
   */
  private int parallelism = PARALLELISM;

  /**
   * Constructs a configuration as a copy of the one specified.
   * @param other the configuration to copy.
   */
  public AppConfiguration(AppConfiguration other) {
    this.copy(other);
  }

  /**
   * Copies the settings of the configuration specified.
   * @param other the configuration to copy.
   */
  public void copy(AppConfiguration other) {
    this.source = other.source;
    this.topic = other.topic;
    this.kafkaProperties = other.kafkaProperties;
    this.dataset = other.dataset;
    this.hiddenMetric = other.hiddenMetric;
    this.hiddenLocality = other.hiddenLocality;
    this.hiddenWeights = other.hiddenWeights;
    this.hiddenThreshold = other.hiddenThreshold;
    this.potentialMetric = other.potentialMetric;
    this.potentialLocality = other.potentialLocality;
    this.potentialWeights = new ArrayList<>(other.potentialWeights);
    this.potentialThreshold = other.potentialThreshold;
    this.ewmaFactor = other.ewmaFactor;
    this.neo4jConfig = other.neo4jConfig;
    this.parallelism = other.parallelism;
  }

  /**
   * Restores the default configuration settings.
   */
  public void toDefault() {
    this.source = SOURCE;
    this.topic = TOPIC;
    this.kafkaProperties = KAFKA_PROPERTIES;
    this.dataset = DATASET;
    this.hiddenMetric = HIDDEN_METRIC;
    this.hiddenLocality = HIDDEN_LOCALITY;
    this.hiddenWeights = HIDDEN_WEIGHTS;
    this.hiddenThreshold = HIDDEN_THRESHOLD;
    this.potentialMetric = POTENTIAL_METRIC;
    this.potentialLocality = POTENTIAL_LOCALITY;
    this.potentialWeights = POTENTIAL_WEIGHTS;
    this.potentialThreshold = POTENTIAL_THRESHOLD;
    this.ewmaFactor = EWMA_FACTOR;
    this.neo4jConfig = NEO4J_CONFIG;
    this.parallelism = PARALLELISM;
  }

}
