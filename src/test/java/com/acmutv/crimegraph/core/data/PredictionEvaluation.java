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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 * Utility for dataset generation.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class PredictionEvaluation {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredictionEvaluation.class);

  private static final double TEST_RATIO = 0.2;

  /**
   * Creates the dataset to test link prediction.
   */
  @Test
  public void datasetPrediction() throws IOException {
    final Path origin = FileSystems.getDefault().getPath("data/test/original.data");
    final Path dest = FileSystems.getDefault().getPath(String.format("data/test/%.3f.prediction.learn.data", 1 - TEST_RATIO));
    final Path test = FileSystems.getDefault().getPath(String.format("data/test/%.3f.prediction.test.data", TEST_RATIO));

    long originLines = Files.lines(origin).count();
    long trainingLines = Math.round((1.0 - TEST_RATIO) * originLines);

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
