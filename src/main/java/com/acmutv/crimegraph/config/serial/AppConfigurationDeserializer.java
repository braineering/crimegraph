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

package com.acmutv.crimegraph.config.serial;

import com.acmutv.crimegraph.config.AppConfiguration;
import com.acmutv.crimegraph.core.metric.HiddenMetrics;
import com.acmutv.crimegraph.core.metric.PotentialMetrics;
import com.acmutv.crimegraph.core.source.SourceType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class realizes the JSON deserializer for {@link AppConfiguration}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see AppConfiguration
 */
public class AppConfigurationDeserializer extends StdDeserializer<AppConfiguration> {

  /**
   * The singleton of {@link AppConfigurationDeserializer}.
   */
  private static AppConfigurationDeserializer instance;

  /**
   * Returns the singleton of {@link AppConfigurationDeserializer}.
   * @return the singleton.
   */
  public static AppConfigurationDeserializer getInstance() {
    if (instance == null) {
      instance = new AppConfigurationDeserializer();
    }
    return instance;
  }

  /**
   * Initializes the singleton of {@link AppConfigurationDeserializer}.
   */
  private AppConfigurationDeserializer() {
    super((Class<?>)null);
  }

  @Override
  public AppConfiguration deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
    AppConfiguration config = new AppConfiguration();
    JsonNode node = parser.getCodec().readTree(parser);

    if (node.hasNonNull("source")) {
      final SourceType source = SourceType.valueOf(node.get("source").asText());
      config.setSource(source);
    }

    if (node.hasNonNull("kafka.topic")) {
      final String topic = node.get("kafka.topic").asText();
      config.setTopic(topic);
    }

    if (node.hasNonNull("kafka.zookeeper")) {
      final String kafkaZookeper = node.get("kafka.zookeeper").asText();
      config.getKafkaProperties().setZookeeperConnect(kafkaZookeper);
    }

    if (node.hasNonNull("kafka.bootstrap")) {
      final String kafkaBootstrap = node.get("kafka.bootstrap").asText();
      config.getKafkaProperties().setBootstrapServers(kafkaBootstrap);
    }

    if (node.hasNonNull("kafka.group")) {
      final String kafkaGroup = node.get("kafka.group").asText();
      config.getKafkaProperties().setGroupId(kafkaGroup);
    }

    if (node.hasNonNull("dataset")) {
      final String dataset = node.get("dataset").asText();
      config.setDataset(dataset);
    }

    if (node.hasNonNull("hidden.metric")) {
      final HiddenMetrics hiddenMetric = HiddenMetrics.valueOf(node.get("hidden.metric").asText());
      config.setHiddenMetric(hiddenMetric);
    }

    if (node.hasNonNull("hidden.locality")) {
      final long hiddenLocality = node.get("hidden.locality").asLong();
      config.setHiddenLocality(hiddenLocality);
    }

    if (node.hasNonNull("hidden.weights")) {
      List<Double> hiddenWeights = new ArrayList<>();
      Iterator<JsonNode> iter = node.get("hidden.weights").elements();
      while (iter.hasNext()) {
        double w = iter.next().asDouble();
        hiddenWeights.add(w);
      }
      config.setHiddenWeights(hiddenWeights);
    }

    if (node.hasNonNull("hidden.threshold")) {
      final double hiddenThreshold = node.get("hidden.threshold").asDouble();
      config.setHiddenThreshold(hiddenThreshold);
    }

    if (node.hasNonNull("potential.metric")) {
      final PotentialMetrics potentialMetric = PotentialMetrics.valueOf(node.get("potential.metric").asText());
      config.setPotentialMetric(potentialMetric);
    }

    if (node.hasNonNull("potential.locality")) {
      final long potentialLocality = node.get("potential.locality").asLong();
      config.setPotentialLocality(potentialLocality);
    }

    if (node.hasNonNull("potential.weights")) {
      List<Double> potentialWeight = new ArrayList<>();
      Iterator<JsonNode> iter = node.get("potential.weights").elements();
      while (iter.hasNext()) {
        double w = iter.next().asDouble();
        potentialWeight.add(w);
      }
      config.setPotentialWeights(potentialWeight);
    }

    if (node.hasNonNull("potential.threshold")) {
      final double potentialThreshold = node.get("potential.threshold").asDouble();
      config.setPotentialThreshold(potentialThreshold);
    }

    if (node.hasNonNull("ewma.factor")) {
      final double ewmaFactor = node.get("ewma.factor").asDouble();
      config.setEwmaFactor(ewmaFactor);
    }

    if (node.hasNonNull("neo4j.hostname")) {
      final String neo4jHostname = node.get("neo4j.hostname").asText();
      config.getNeo4jConfig().setHostname(neo4jHostname);
    }

    if (node.hasNonNull("neo4j.username")) {
      final String neo4jUsername = node.get("neo4j.username").asText();
      config.getNeo4jConfig().setUsername(neo4jUsername);
    }

    if (node.hasNonNull("neo4j.password")) {
      final String neo4jPassword = node.get("neo4j.password").asText();
      config.getNeo4jConfig().setPassword(neo4jPassword);
    }

    if (node.hasNonNull("parallelism")) {
      final int parallelism = node.get("parallelism").asInt();
      config.setParallelism(parallelism);
    }

    return config;
  }
}
