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

package com.acmutv.crimegraph.core.producer;

import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility for message publishing to Kafka sources.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Link
 */
public class SampleProducers {

  private static final Logger LOGGER = LoggerFactory.getLogger(SampleProducers.class);

  private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";


  /**
   * Produces links as string.
   */
  @Test
  public void simple() throws Exception {
    Link link = new Link(1, 2, 10.0);
    StringKafkaProducer producer = new StringKafkaProducer(KAFKA_BOOTSTRAP_SERVERS);
    LOGGER.info("Publishing link %s", link);
    producer.send("main-topic", link);
    producer.close();
  }

  /**
   * Produces links from file
   */
  @Test
  public void publishFromFile() throws Exception {
    Path path = FileSystems.getDefault().getPath("data/crimegraph.data");
    StringKafkaProducer producer = new StringKafkaProducer(KAFKA_BOOTSTRAP_SERVERS);

    BufferedReader reader = Files.newBufferedReader(path);

    while (reader.ready()) {
      Link link = Link.valueOf(reader.readLine());
      LOGGER.info("Publishing link %s", link);
      producer.send("main-topic", link);
    }

    producer.close();
  }
}
