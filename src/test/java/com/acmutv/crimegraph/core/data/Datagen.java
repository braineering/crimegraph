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

package com.acmutv.crimegraph.core.data;

import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * Utility for dataset generation.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class Datagen {

  private static final Logger LOGGER = LoggerFactory.getLogger(Datagen.class);

  private static final int NUM_NODES = 25;

  private static final int NUM_LINKS = 100;

  private static final double MIN_WEIGHT = 1.0;

  private static final double MAX_WEIGHT = 10.0;

  /**
   * Generates a simple dataset.
   */
  @Test
  public void simple() throws Exception {
    Path path = FileSystems.getDefault().getPath("data/crimegraph.data");

    if (!Files.isDirectory(path.getParent())) {
      Files.createDirectories(path.getParent());
    }

    Random rnd = new Random();

    List<Link> data = new ArrayList<>();

    for (int i = 0; i < NUM_LINKS; i++) {
      int x = rnd.nextInt(NUM_NODES);
      int y;
      do {
        y = rnd.nextInt(NUM_NODES);
      } while (y == x);

      double weight = rnd.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT) + MIN_WEIGHT;
      Link link = new Link(x, y, weight);
      data.add(link);
    }

    writeDataset(path, data);

    String flinkHome = System.getenv("FLINK_HOME");

    if (flinkHome != null) {
      Path flinkPath = FileSystems.getDefault().getPath(flinkHome + "/resources/crimegraph/data/crimegraph.data");
      if (!Files.isDirectory(flinkPath.getParent())) {
        Files.createDirectories(flinkPath.getParent());
      }
      Files.copy(path, flinkPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Writes the dataset {@code data} into {@code path}.
   * @param path the file to write onto.
   * @param data the dataset to write.
   */
  private static void writeDataset(Path path, List<Link> data) {
    Charset charset = Charset.defaultCharset();
    try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
      for (Link link : data) {
        writer.append(link.toString()).append("\n");
      }
    } catch (IOException exc) {
      LOGGER.error(exc.getMessage());
    }
  }

  /**
   * Creates the dataset to test link detection.
   */
  @Test
  public void datasetDetection() throws IOException {
    final double testRatio = 0.2; // test set ratio
    final Path origin = FileSystems.getDefault().getPath("data/test/original.data");
    final Path temp0 = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.data-temp0", testRatio));
    final Path temp1 = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.data-temp1", testRatio));
    final Path dest = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.learn.data", 1 - testRatio));
    final Path test = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.test.data", testRatio));

    long originLines = Files.lines(origin).count();
    long testLines = Math.round(testRatio * originLines);
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

  /**
   * Creates the dataset to test link prediction.
   */
  @Test
  public void datasetPrediction() throws IOException {
    final double testRatio = 0.2; // test set ratio
    final Path origin = FileSystems.getDefault().getPath("data/test/original.data");
    final Path dest = FileSystems.getDefault().getPath(String.format("data/test/%.3f.prediction.learn.data", 1 - testRatio));
    final Path test = FileSystems.getDefault().getPath(String.format("data/test/%.3f.prediction.test.data", testRatio));

    long originLines = Files.lines(origin).count();
    long trainingLines = Math.round((1.0 - testRatio) * originLines);

    BufferedReader reader = Files.newBufferedReader(origin, Charset.defaultCharset());
    BufferedWriter writer = Files.newBufferedWriter(dest, Charset.defaultCharset(), StandardOpenOption.CREATE);
    BufferedWriter writerTest = Files.newBufferedWriter(test, Charset.defaultCharset(), StandardOpenOption.CREATE);

    String line;
    for (long lines = 1; lines <= trainingLines; lines++) {
      line = reader.readLine();
      writer.write(line + "\n");
    }

    while (reader.ready()) {
      line = reader.readLine();
      writerTest.write(line + "\n");
    }

    reader.close();
    writer.close();
    writerTest.close();
  }
}
