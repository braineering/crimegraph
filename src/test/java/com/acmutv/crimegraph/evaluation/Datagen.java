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

import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility for the original dataset generation.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Link
 */
public class Datagen {

  private static final Logger LOGGER = LoggerFactory.getLogger(Datagen.class);

  private static final int NUM_NODES = 250;

  private static final int NUM_LINKS = 1000;

  private static final double MIN_WEIGHT = 1.0;

  private static final double MAX_WEIGHT = 10.0;

  /**
   * Generates a simple random dataset.
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
   * Generates a simple circular dataset.
   */
  @Test
  public void circular() throws Exception {
    Path path = FileSystems.getDefault().getPath("data/crimegraph.data");

    if (!Files.isDirectory(path.getParent())) {
      Files.createDirectories(path.getParent());
    }

    Random rnd = new Random();

    List<Link> data = new ArrayList<>();

    for (int i = 1; i < NUM_NODES; i++) {
      double weight = rnd.nextDouble() * (MAX_WEIGHT - MIN_WEIGHT) + MIN_WEIGHT;
      Link link = new Link(i, i+1, weight);
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
}
