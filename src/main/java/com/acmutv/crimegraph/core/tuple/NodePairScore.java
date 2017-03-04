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

package com.acmutv.crimegraph.core.tuple;

import org.apache.flink.api.java.tuple.Tuple5;

import java.util.Locale;

/**
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class NodePairScore extends Tuple5<Long,Long,Double,String,Long> {

    /**
     * Creates a new node pair score updated.
     * @param src the id of the source node.
     * @param dst the id of the destination node.
     * @param score the score of (src,dst) edge
     */
    public NodePairScore(long src, long dst, double score, String scoretype, long ts) {
        super(src, dst, score, scoretype,ts);
    }

    /**
     * Creates an empty NodePairScore.
     * This constructor is mandatory for Flink serialization.
     */
    public NodePairScore(){}

    @Override
    public String toString() {
        return String.format(Locale.ROOT,"(%d,%d,%.3f,%s,%d)", super.f0, super.f1, super.f2, super.f3, super.f4);
    }

}