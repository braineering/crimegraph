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
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The tuple representing an interaction between two nodes.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class Link extends Tuple4<Long,Long,LinkType,Double> {

  /**
   * The regular expression
   */
  private static final String REGEXP =
      "^\\(([0-9]+),([0-9]+),(INTERACTION,|POTENTIAL,|HIDDEN,)?([0-9]+)\\)$";

  /**
   * The pattern matcher used to match strings on {@code REGEXP}.
   */
  private static final Pattern PATTERN = Pattern.compile(REGEXP);

  /**
   * Creates a new interaction.
   * @param src the id of the source node.
   * @param dst the id of the destination node.
   * @param type the link type.
   * @param weight the weight of the interaction.
   */
  public Link(long src, long dst, LinkType type, double weight) {
    super(src, dst, type, weight);
  }

  /**
   * Creates a new interaction.
   * @param src the id of the source node.
   * @param dst the id of the destination node.
   * @param weight the weight of the interaction.
   */
  public Link(long src, long dst, double weight) {
    super(src, dst, LinkType.INTERACTION, weight);
  }

  @Override
  public String toString() {
    return String.format("(%d,%d,%s,%f)", super.f0, super.f1, super.f2, super.f3);
  }

  /**
   * Parses {@link Link} from string.
   * @param string the string to parse.
   * @return the parsed {@link Link}.
   * @throws IllegalArgumentException when {@code string} cannot be parsed.
   */
  public static Link valueOf(String string) throws IllegalArgumentException {
    if (string == null) throw new IllegalArgumentException();
    Matcher matcher = PATTERN.matcher(string);
    if (!matcher.matches()) throw new IllegalArgumentException();
    long src = Long.valueOf(matcher.group(1));
    long dst = Long.valueOf(matcher.group(2));
    LinkType type = LinkType.valueOf(matcher.group(3));
    double weight = Double.valueOf(matcher.group(4));
    return new Link(src, dst, type, weight);
  }
}
