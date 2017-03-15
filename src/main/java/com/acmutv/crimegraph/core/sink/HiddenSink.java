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
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
import com.acmutv.crimegraph.core.tuple.NodePairScore;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

/**
 * A sink that saves/removes hidden links.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class HiddenSink extends RichSinkFunction<NodePairScore> {

  /**
   * The Neo4J configuration.
   */
  private DbConfiguration dbconfig;

  /**
   * The hidden score threshold.
   */
  private double threshold;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  /**
   * The NEO4J session.
   */
  private Session session;

  /**
   * Constructs a new sink.
   * @param dbconfig the Neo4J configuration.
   * @param threshold the hidden score threshold.
   */
  public HiddenSink(DbConfiguration dbconfig, double threshold) {
    this.dbconfig = dbconfig;
    this.threshold = threshold;
  }

  @Override
  public void invoke(NodePairScore value) throws Exception {
    System.out.println("NEW HIDDEN LINK  "+ value.toString());
    if (value.f2 >= this.threshold) {
      Link link = new Link(value.f0, value.f1, value.f2, LinkType.HIDDEN);
      Neo4JManager.save(this.session, link);
    } else {
      Neo4JManager.remove(this.session, value.f0, value.f1, LinkType.HIDDEN);
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    String hostname = this.dbconfig.getHostname();
    String username = this.dbconfig.getUsername();
    String password = this.dbconfig.getPassword();
    this.driver = Neo4JManager.open(hostname, username, password);
    this.session = driver.session();
  }

  @Override
  public void close() throws Exception {
    Neo4JManager.close(this.session, this.driver);
  }
}
