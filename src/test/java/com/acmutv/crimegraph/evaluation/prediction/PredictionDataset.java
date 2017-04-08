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

package com.acmutv.crimegraph.evaluation.prediction;

import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import static com.acmutv.crimegraph.evaluation.EvaluationCommon.*;

/**
 * Utility for dataset generation for link prediction.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class PredictionDataset {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredictionDataset.class);

  /**
   * Creates the training and test set for link prediction.
   */
  @Test
  public void dataset_generation() throws IOException {
    long originLines = Files.lines(PREDICTION_ORIGIN).count();
    long trainingLines = Math.round((1.0 - PREDICTION_TEST_RATIO) * originLines);

    BufferedReader reader = Files.newBufferedReader(PREDICTION_ORIGIN, Charset.defaultCharset());
    BufferedWriter writer = Files.newBufferedWriter(PREDICTION_TRAINING, Charset.defaultCharset(), StandardOpenOption.CREATE);
    BufferedWriter writerTest = Files.newBufferedWriter(PREDICTION_TEST, Charset.defaultCharset(), StandardOpenOption.CREATE);

    Set<Long> knownNodes = new HashSet<>();

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
