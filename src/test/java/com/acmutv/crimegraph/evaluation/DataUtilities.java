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

package com.acmutv.crimegraph.evaluation;

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.producer.StringKafkaProducer;
import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.acmutv.crimegraph.Common.*;
import static com.acmutv.crimegraph.evaluation.EvaluationCommon.PREDICTION_TRAINING;

/**
 * Utility for the dataset interaction with Neo4J.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Link
 */
public class DataUtilities {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataUtilities.class);

  private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";

  /**
   * Writes links to Neo4J from file.
   * @throws IOException
   */
  @Test
  @Ignore
  public void writeLinksOnNeo4j() throws IOException {
    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    try (BufferedReader reader = Files.newBufferedReader(PREDICTION_TRAINING)) {
      int i = 1;
      while (reader.ready()) {
        Link link = Link.valueOf(reader.readLine());
        Neo4JManager.save(session, link, 0.5);
        System.out.format("WRITING LINK ON NEO4J (%d): %s", i++, link);
      }
    }

    session.close();
    driver.close();
  }

  /**
   * Publish links from file.
   */
  @Test
  @Ignore
  public void publishFromFile() throws Exception {
    Path path = FileSystems.getDefault().getPath("data/crimegraph.data");
    StringKafkaProducer producer = new StringKafkaProducer(KAFKA_BOOTSTRAP_SERVERS);

    System.out.println("Path: " + path.toString());

    try (BufferedReader reader = Files.newBufferedReader(path)) {
      int i = 1;
      while (reader.ready()) {
        Link link = Link.valueOf(reader.readLine());
        producer.send("main-topic", link);
        System.out.format("PUBLISHING LINK TO KAFKA (%d): %s", i++, link);
      }
    }

    producer.close();
  }
}
