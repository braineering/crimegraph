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
import com.acmutv.crimegraph.core.tuple.ScoreType;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

/**
 * A sink that saves/removes potential links.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class MultiIndexSink extends RichSinkFunction<NodePairScore> {

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
   * The NEO4J session.
   */
  private Session session;

  /**
   * Constructs a new sink.
   * @param dbconfig the Neo4J configuration.
   * @param threshold the potential score threshold.
   */
  public MultiIndexSink(DbConfiguration dbconfig, double threshold) {
    this.dbconfig = dbconfig;
    this.threshold = threshold;
  }

  @Override
  public void invoke(NodePairScore value) throws Exception {
    long x = value.f0;
    long y = value.f1;
    double score = value.f2;

    if (score >= this.threshold) {

      Link link = null;
      switch(value.f3) {
        case NRA:
          link = new Link(x, y, score, LinkType.NRA);
          break;
        case NTA:
          link = new Link(x, y, score, LinkType.NTA);
          break;
        case TA:
          link = new Link(x, y, score, LinkType.TA);
          break;
        case CN:
          link = new Link(x, y, score, LinkType.CN);
          break;
        case JACCARD:
          link = new Link(x, y, score, LinkType.JACCARD);
          break;
        case SALTON:
          link = new Link(x, y, score, LinkType.SALTON);
          break;
        case SORENSEN:
          link = new Link(x, y, score, LinkType.SORENSEN);
          break;
        case HPI:
          link = new Link(x, y, score, LinkType.HPI);
          break;
        case HDI:
          link = new Link(x, y, score, LinkType.HDI);
          break;
        case LHN1:
          link = new Link(x, y, score, LinkType.LHN1);
          break;
        case PA:
          link = new Link(x, y, score, LinkType.PA);
          break;
        case AA:
          link = new Link(x, y, score, LinkType.AA);
          break;
        case RA:
          link = new Link(x, y, score, LinkType.RA);
          break;
      }
      Neo4JManager.save(this.session, link);

    }
    else {
      switch(value.f3) {
        case NRA:
          Neo4JManager.remove(this.session, x, y, LinkType.NRA);
          break;
        case NTA:
          Neo4JManager.remove(this.session, x, y, LinkType.NTA);
          break;
        case TA:
          Neo4JManager.remove(this.session, x, y, LinkType.TA);
          break;
        case CN:
          Neo4JManager.remove(this.session, x, y, LinkType.CN);
          break;
        case JACCARD:
          Neo4JManager.remove(this.session, x, y, LinkType.JACCARD);
          break;
        case SALTON:
          Neo4JManager.remove(this.session, x, y, LinkType.SALTON);
          break;
        case SORENSEN:
          Neo4JManager.remove(this.session, x, y, LinkType.SORENSEN);
          break;
        case HPI:
          Neo4JManager.remove(this.session, x, y, LinkType.HPI);
          break;
        case HDI:
          Neo4JManager.remove(this.session, x, y, LinkType.HDI);
          break;
        case LHN1:
          Neo4JManager.remove(this.session, x, y, LinkType.LHN1);
          break;
        case PA:
          Neo4JManager.remove(this.session, x, y, LinkType.PA);
          break;
        case AA:
          Neo4JManager.remove(this.session, x, y, LinkType.AA);
          break;
        case RA:
          Neo4JManager.remove(this.session, x, y, LinkType.RA);
          break;
      }
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.dbconfig);
    this.session = driver.session(AccessMode.WRITE);
  }

  @Override
  public void close() throws Exception {
    Neo4JManager.close(this.session, this.driver);
  }
}
