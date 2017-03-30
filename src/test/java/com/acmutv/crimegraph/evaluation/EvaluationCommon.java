/*
  The MIT License (MIT)

  Copyright (c) 2017 Giacomo Marciani

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

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Collection of common variables for evaluation.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 */
public class EvaluationCommon {

  /**
   * Prediction: test set ratio.
   */
  public static final double PREDICTION_TEST_RATIO = 0.1;

  /**
   * Prediction: the original dataset.
   */
  public static final Path PREDICTION_ORIGIN = FileSystems.getDefault().getPath("data/test/original.data");

  /**
   * Prediction: the training set.
   */
  public static final Path PREDICTION_TRAINING = FileSystems.getDefault().getPath(String.format("data/test/%.3f.prediction.learn.data", 1 - PREDICTION_TEST_RATIO));

  /**
   * Prediction: the test set.
   */
  public static final Path PREDICTION_TEST = FileSystems.getDefault().getPath(String.format("data/test/%.3f.prediction.test.data", PREDICTION_TEST_RATIO));

  /**
   * Detection: the test set ratio.
   */
  public static final double DETECTION_TEST_RATIO = 0.1;

  /**
   * Detection: the original dataset.
   */
  public static final Path DETECTION_ORIGIN = FileSystems.getDefault().getPath("data/test/original.data");

  /**
   * Detection: the training set.
   */
  public static final Path DETECTION_TRAINING = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.learn.data", 1 - DETECTION_TEST_RATIO));

  /**
   * Detection: the test set.
   */
  public static final Path DETECTION_TEST = FileSystems.getDefault().getPath(String.format("data/test/%.3f.detection.test.data", DETECTION_TEST_RATIO));
}
