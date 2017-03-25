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
import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.acmutv.crimegraph.Common.HOSTNAME;
import static com.acmutv.crimegraph.Common.PASSWORD;
import static com.acmutv.crimegraph.Common.USERNAME;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Utility for dataset generation.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class DetectionEvaluation {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetectionEvaluation.class);

  /**
   * The test set ratio.
   */
  private static final double TEST_RATIO = 0.2;

  /**
   * The original dataset.
   */
  final Path origin = FileSystems.getDefault().getPath("data/test/original.data");

  /**
   * The learn dataset
   */
  final Path dest = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.learn.data", 1 - TEST_RATIO));

  /**
   * The test dataset.
   */
  final Path test = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.test.data", TEST_RATIO));

  /**
   * Creates the learn/test datasets for link detection.
   */
  @Test
  public void datasetDetection() throws IOException {
    final Path temp0 = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.data-temp0", TEST_RATIO));
    final Path temp1 = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.data-temp1", TEST_RATIO));

    long originLines = Files.lines(origin).count();
    long testLines = Math.round(TEST_RATIO * originLines);
    Random rnd = new Random();

    BufferedReader reader = Files.newBufferedReader(origin, Charset.defaultCharset());
    BufferedWriter writer = Files.newBufferedWriter(temp0, Charset.defaultCharset(), StandardOpenOption.CREATE);
    BufferedWriter writerTest = Files.newBufferedWriter(test, Charset.defaultCharset(), StandardOpenOption.CREATE);
    int candidate = 0;

    long hidden = 0;
    String line;

    while (reader.ready()) {
      line = reader.readLine();
      if (hidden < testLines && rnd.nextBoolean()) {
        writerTest.write(line + "\n");
        hidden++;
      } else {
        writer.write(line + "\n");
      }
    }

    reader.close();
    writer.close();

    while (hidden < testLines) {
      candidate = (candidate + 1) % 2;

      if (candidate == 0) {
        reader = Files.newBufferedReader(temp1, Charset.defaultCharset());
        writer = Files.newBufferedWriter(temp0, Charset.defaultCharset(), StandardOpenOption.CREATE);
      } else if (candidate == 1) {
        reader = Files.newBufferedReader(temp0, Charset.defaultCharset());
        writer = Files.newBufferedWriter(temp1, Charset.defaultCharset(), StandardOpenOption.CREATE);
      }

      while (reader.ready()) {
        line = reader.readLine();
        if (hidden < testLines && rnd.nextBoolean()) {
          writerTest.write(line + "\n");
          hidden++;
        } else {
          writer.write(line + "\n");
        }
      }

      reader.close();
      writer.close();
    }

    writerTest.close();

    if (candidate == 0) {
      Files.move(temp0, dest, StandardCopyOption.REPLACE_EXISTING);
      Files.deleteIfExists(temp1);
    } else if (candidate == 1) {
      Files.move(temp1, dest, StandardCopyOption.REPLACE_EXISTING);
      Files.deleteIfExists(temp0);
    }
  }

  private static final String MATCH =
      "MATCH (a:Person {id:{src}})-[r:HIDDEN]->(b:Person {id:{dst}}) " +
          "WITH r " +
          "RETURN r IS NOT NULL AS exists";

  /**
   * Evaluates detection results.
   */
  @Test
  public void evaluate() throws IOException {
    long total = 0;
    long mined = 0;

    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    BufferedReader reader = Files.newBufferedReader(test);

    while (reader.ready()) {
      Link link = Link.valueOf(reader.readLine());
      Value params = parameters("src", link.f0, "dst", link.f1);
      StatementResult result = session.run(MATCH, params);
      if (result.hasNext()) {
        Record rec = result.next();
        Boolean exists = rec.get("exists").asBoolean();
        if (exists) {
          mined ++;
        }
      }
      total ++;
    }

    session.close();
    driver.close();

    double accuracy = mined / total;

    LOGGER.info("ACCURACY %.3f", accuracy);
  }
}
