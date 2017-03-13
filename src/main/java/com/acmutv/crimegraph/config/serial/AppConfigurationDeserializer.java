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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    if (node.hasNonNull("dataset")) {
      final String dataset = node.get("dataset").asText();
      config.setDataset(dataset);
    }

    if (node.hasNonNull("output")) {
      final String output = node.get("output").asText();
      config.setOutput(output);
    }

    if (node.hasNonNull("hidden.metric")) {
      final HiddenMetrics hiddenMetric = HiddenMetrics.valueOf(node.get("hidden.metric").asText());
      config.setHiddenMetric(hiddenMetric);
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

    if (node.hasNonNull("potential.weight")) {
      List<Double> potentialWeight = new ArrayList<>();
      Iterator<JsonNode> iter = node.get("potential.weight").elements();
      while (iter.hasNext()) {
        double w = iter.next().asDouble();
        potentialWeight.add(w);

      }
      config.setPotentialWeight(potentialWeight);
    }

    if (node.hasNonNull("potential.threshold")) {
      final double potentialThreshold = node.get("potential.threshold").asDouble();
      config.setPotentialThreshold(potentialThreshold);
    }

    if (node.hasNonNull("neo4j.hostname")) {
      final String neo4jHostname = node.get("neo4j.hostname").asText();
      config.setNeo4jHostname(neo4jHostname);
    }

    if (node.hasNonNull("neo4j.username")) {
      final String neo4jUsername = node.get("neo4j.username").asText();
      config.setNeo4jUsername(neo4jUsername);
    }

    if (node.hasNonNull("neo4j.password")) {
      final String neo4jPassword = node.get("neo4j.password").asText();
      config.setNeo4jPassword(neo4jPassword);
    }

    return config;
  }
}
