/*
  The MIT License (MIT)

  Copyright (c) 2017 Giacomo Marciani and Michele Porretta

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

package com.acmutv.crimegraph.core.filter;

import com.acmutv.crimegraph.core.tuple.NodePairScore;
import org.apache.flink.api.common.functions.FilterFunction;

/**
 * A threshold filter for potential links.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
@Deprecated
public class PotentialFilter implements FilterFunction<NodePairScore> {

  /**
   * The threshold for potential links score.
   */
  private double threshold;

  /**
   * Creates a new threshold filter for potential links score.
   * @param threshold the score threshold.
   */
  public PotentialFilter(double threshold) {
    this.threshold = threshold;
  }

  /**
   * The filter function that evaluates the predicate.
   * <p>
   * <strong>IMPORTANT:</strong> The system assumes that the function does not
   * modify the elements on which the predicate is applied. Violating this assumption
   * can lead to incorrect results.
   *
   * @param value The value to be filtered.
   * @return True for values that should be retained, false for values to be filtered out.
   * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
   *                   to fail and may trigger recovery.
   */
  @Override
  public boolean filter(NodePairScore value) throws Exception {
    return value.f2 >= this.threshold;
  }
}
