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

package com.acmutv.crimegraph.core.sink;

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.util.List;
import java.util.Properties;

/**
 * A sink that saves/removes potential links.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class MultiIndexSink2 extends RichSinkFunction<NodePairScores> {

  /**
   * The Neo4J configuration.
   */
  private DbConfiguration dbconfig;

  /**
   * The potential score threshold.
   */
  private double threshold;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  /**
   * Constructs a new sink.
   * @param dbconfig the Neo4J configuration.
   * @param threshold the potential score threshold.
   */
  public MultiIndexSink2(DbConfiguration dbconfig, double threshold) {
    this.dbconfig = dbconfig;
    this.threshold = threshold;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.dbconfig);
  }

  @Override
  public void close() throws Exception {
    this.driver.close();
  }

  @Override
  public void invoke(NodePairScores value) throws Exception {
    Session session = this.driver.session();

    final long src = value.f0;
    final long dst = value.f1;
    final Properties scores = value.f2;

    for (String scoreType : scores.stringPropertyNames()) {
      final LinkType type = LinkType.valueOf(scoreType);
      final double val = Double.valueOf(scores.getProperty(scoreType));
      final Link link = new Link(src, dst, val, type);

      if (val >= this.threshold) {
        Neo4JManager.save(session, link);
      } else {
        Neo4JManager.remove(session, link);
      }
    }

    session.close();
  }
}
