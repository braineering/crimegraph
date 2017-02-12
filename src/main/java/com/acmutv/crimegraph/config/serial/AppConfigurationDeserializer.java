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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class realizes the JSON deserializer for {@link AppConfiguration}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see AppConfiguration
 */
public class AppConfigurationDeserializer extends StdDeserializer<AppConfiguration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationDeserializer.class);

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

    if (node.hasNonNull("dataHostname")) {
      final String dataHostname = node.get("dataHostname").asText();
      config.setDataHostname(dataHostname);
    }

    if (node.hasNonNull("dataPort")) {
      final int dataPort = node.get("dataPort").asInt();
      config.setDataPort(dataPort);
    }

    if (node.hasNonNull("elasticHostname")) {
      final String elasticHostname = node.get("elasticHostname").asText();
      config.setElasticHostname(elasticHostname);
    }

    if (node.hasNonNull("elasticPort")) {
      final int elasticPort = node.get("elasticPort").asInt();
      config.setElasticPort(elasticPort);
    }

    return config;
  }
}
