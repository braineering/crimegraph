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
import com.acmutv.crimegraph.core.CustomObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class realizes the JSON deserializer for {@link AppConfiguration}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see AppConfiguration
 */
public class AppConfigurationDeserializer extends StdDeserializer<AppConfiguration> {

  private static final Logger LOGGER = LogManager.getLogger(AppConfigurationDeserializer.class);

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
    LOGGER.traceEntry();
    AppConfiguration config = new AppConfiguration();
    JsonNode node = parser.getCodec().readTree(parser);
    LOGGER.trace("node={}", node);

    if (node.has("propertyBoolean")) {
      final boolean propertyBoolean = node.get("propertyBoolean").asBoolean();
      config.setPropertyBoolean(propertyBoolean);
    }

    if (node.has("propertyString")) {
      final String propertyString = node.get("propertyString").asText(null);
      config.setPropertyString(propertyString);
    }

    if (node.has("propertyObject")) {
      CustomObject propertyObject = new CustomObject();
      JsonNode customObjectNode = node.get("propertyObject");
      if (customObjectNode.has("a")) {
        final long a = customObjectNode.get("a").asLong();
        propertyObject.setA(a);
      }

      if (customObjectNode.has("b")) {
        final TimeUnit b = TimeUnit.valueOf(customObjectNode.get("b").asText());
        propertyObject.setB(b);
      }

      config.setPropertyObject(propertyObject);
    }

    return LOGGER.traceExit(config);
  }
}
