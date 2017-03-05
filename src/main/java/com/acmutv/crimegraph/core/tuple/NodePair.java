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

import org.apache.flink.api.java.tuple.Tuple3;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The tuple representing an node pair to update.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class NodePair extends Tuple3<Long,Long,ScoreType> {

  /**
   * The regular expression
   */
  //private static final String REGEXP = "^\\(([0-9]+),([0-9]+)\\)$";

  /**
   * The pattern matcher used to match strings on {@code REGEXP}.
   */
  //private static final Pattern PATTERN = Pattern.compile(REGEXP);

  /**
   * Creates a new interaction.
   * @param src the id of the source node.
   * @param dst the id of the destination node.
   */
  public NodePair(long src, long dst, ScoreType type) {
    super(src, dst, type);
  }

  /**
   * Creates an empty link.
   * This constructor is mandatory for Flink serialization.
   */
  public NodePair(){}

  @Override
  public String toString() {
    return String.format(Locale.ROOT,"(%d,%d)", super.f0, super.f1);
  }

  /*/**
   * Parses {@link NodePair} from string.
   * @param string the string to parse.
   * @return the parsed {@link NodePair}.
   * @throws IllegalArgumentException when {@code string} cannot be parsed.
   */
  /*public static NodePair valueOf(String string) throws IllegalArgumentException {
    if (string == null) throw new IllegalArgumentException();
    Matcher matcher = PATTERN.matcher(string);
    if (!matcher.matches()) throw new IllegalArgumentException();
    long src = Long.valueOf(matcher.group(1));
    long dst = Long.valueOf(matcher.group(2));
    return new NodePair(src, dst,weight);
  }*/
}
