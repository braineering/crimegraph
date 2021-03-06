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

package com.acmutv.crimegraph.core.operator;

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.NodePair;
import com.acmutv.crimegraph.core.tuple.UpdateType;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * A simple source function based on Neo4j.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class GraphUpdateMultiIndex extends RichFlatMapFunction<Link, NodePair> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphUpdateMultiIndex.class);

  /**
   * The Neo4J configuration.
   */
  private DbConfiguration dbconfig;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  private IntCounter numLinks = new IntCounter();

  private IntCounter numAll = new IntCounter();

  private IntCounter numTm = new IntCounter();

  /**
   * Constructs for load edge to write {@link Link} on a NEO4J instance.
   * @param dbconfig the Neo4J configuration.
   */
  public GraphUpdateMultiIndex(DbConfiguration dbconfig) {
    this.dbconfig = dbconfig;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.dbconfig);
    super.getRuntimeContext().addAccumulator("numLinks", this.numLinks);
    super.getRuntimeContext().addAccumulator("numAll", this.numAll);
    super.getRuntimeContext().addAccumulator("numTm", this.numTm);
  }

  @Override
  public void close() throws Exception {
    this.driver.close();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void flatMap(Link value, Collector<NodePair> out) {
    Session session = this.driver.session(AccessMode.WRITE);

    long x = value.f0;
    long y = value.f1;

    if (x == y) return;

    this.numLinks.add(1);

    Tuple3<Boolean,Boolean,Boolean> check = Neo4JManager.checkExtremes(session, x, y);
    boolean xInG = check.f0;
    boolean yinG = check.f1;
    boolean edgeInG = check.f2;

    Neo4JManager.save(session, value);

    // if x in G, y in G, and (x,y) in G
    if (xInG && yinG && edgeInG) {
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdateTwice(session, x, y);
      for (Tuple2<Long,Long> pair : pairs) {
        NodePair update = new NodePair(pair.f0, pair.f1, UpdateType.ALL);
        LOGGER.trace("({},{}) -> UPDATE: {}", x, y, update);
        out.collect(update);
        this.numTm.add(1);
      }
    }

    // if x in G, y in G, and (x,y) NOT in G
    else if (xInG && yinG) {
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdateTwice(session, x, y);
      for (Tuple2<Long,Long> pair : pairs) {
        NodePair update = new NodePair(pair.f0, pair.f1, UpdateType.ALL);
        LOGGER.trace("({},{}) -> UPDATE: {}", x, y, update);
        out.collect(update);
        this.numAll.add(1);
      }
    }

    // if x NOT in G and y in G
    else if (!xInG && yinG) {
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdate(session, x);
      for (Tuple2<Long,Long> pair : pairs ) {
        NodePair update = new NodePair(pair.f0, pair.f1, UpdateType.ALL);
        LOGGER.trace("({},{}) -> UPDATE: {}", x, y, update);
        out.collect(update);
        this.numAll.add(1);
      }
    }

    // if x in G and y NOT in G
    else if (xInG && !yinG) {
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdate(session, y);
      for (Tuple2<Long,Long> pair : pairs) {
        NodePair update = new NodePair(pair.f0, pair.f1, UpdateType.ALL);
        LOGGER.info("({},{}) -> UPDATE: {}", x, y, update);
        out.collect(update);
        this.numAll.add(1);
      }
    }

    session.close();
  }
}
