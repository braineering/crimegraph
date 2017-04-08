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

package com.acmutv.crimegraph.evaluation.detection;

import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Random;

import static com.acmutv.crimegraph.evaluation.EvaluationCommon.*;

/**
 * Utility for dataset generation for link detection.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class DetectionDataset {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetectionDataset.class);

  /**
   * Creates the training and test set for link detection.
   */
  @Test
  public void datasetDetection() throws IOException {
    final Path temp0 = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.data-temp0", DETECTION_TEST_RATIO));
    final Path temp1 = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.data-temp1", DETECTION_TEST_RATIO));

    long originLines = Files.lines(DETECTION_ORIGIN).count();
    long testLines = Math.round(DETECTION_TEST_RATIO * originLines);
    Random rnd = new Random();

    BufferedReader reader = Files.newBufferedReader(DETECTION_ORIGIN, Charset.defaultCharset());
    BufferedWriter writer = Files.newBufferedWriter(temp0, Charset.defaultCharset(), StandardOpenOption.CREATE);
    BufferedWriter writerTest = Files.newBufferedWriter(DETECTION_TEST, Charset.defaultCharset(), StandardOpenOption.CREATE);
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
      Files.move(temp0, DETECTION_TRAINING, StandardCopyOption.REPLACE_EXISTING);
      Files.deleteIfExists(temp1);
    } else if (candidate == 1) {
      Files.move(temp1, DETECTION_TRAINING, StandardCopyOption.REPLACE_EXISTING);
      Files.deleteIfExists(temp0);
    }
  }

}
