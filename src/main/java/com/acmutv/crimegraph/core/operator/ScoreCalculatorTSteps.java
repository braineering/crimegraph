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
import com.acmutv.crimegraph.core.tuple.NodePair;
import com.acmutv.crimegraph.core.tuple.NodePairScore;
import com.acmutv.crimegraph.core.tuple.ScoreType;
import com.acmutv.crimegraph.core.tuple.UpdateType;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.util.Set;

/**
 * This operator parses interactions from input datastream.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
@Deprecated
public class ScoreCalculatorTSteps extends RichFlatMapFunction<NodePair, NodePairScore> {

  /**
   * The Neo4J configuration.
   */
  private DbConfiguration dbconfig;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  /**
   * The NEO4J session.
   */
  private Session session;

  /**
   * The distance between nodes
   */
  private long distance;

  /**
   * Constructs a new PotentialScoreOperator to compute the score
   * for a node pair stored in NodePair.
   * This operator required access to the NEO4J instance.
   * @param dbconfig the Neo4J configuration.
   */
  public ScoreCalculatorTSteps(DbConfiguration dbconfig, long distance) {
    this.dbconfig = dbconfig;
    this.distance = distance;
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

  @SuppressWarnings("unchecked")
  @Override
  public void flatMap(NodePair nodePair, Collector<NodePairScore> out) {

    UpdateType type = nodePair.f2;

    if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
      double potentialScore = 0.0;

      Long totalDegree = 0L;
      for(long i = 1; i<= distance;i++) {

        Set<Tuple2<Long, Long>> neighbours = Neo4JManager.commonNeighboursWithDegreeWithinDistance(this.session, nodePair.f0, nodePair.f1, i);

        for (Tuple2<Long, Long> z : neighbours) {
          System.out.println("computing (" + nodePair.f0 + ";" + nodePair.f1 + ")" + " - neighbour id: " + z.f0.toString() + " with degree " + z.f1.toString() + " for potential");
          potentialScore += (1.0 / z.f1);
          totalDegree += z.f1;
        }
      }
      potentialScore /= totalDegree;
      NodePairScore potential = new NodePairScore(nodePair.f0, nodePair.f1, potentialScore, ScoreType.POTENTIAL);
      out.collect(potential);
    }

    if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.HIDDEN)){
      double hiddenScore = 0.0;
      double totalWeight = 0.0;

      Set<Tuple4<Long,Long,Double,Double>> neighbours = Neo4JManager.gammaIntersection(this.session, nodePair.f0, nodePair.f1);
      for (Tuple4<Long,Long,Double,Double> z : neighbours) {
        //System.out.println("computing ("+nodePair.f0 + ";"+nodePair.f1+")"+ " - neighbour id: " +z.f0.toString() +" with degree "+z.f1.toString() + " for hidden");
        hiddenScore += (z.f2 / z.f3);
        totalWeight += z.f3;
      }
      hiddenScore /= totalWeight;
      NodePairScore hidden = new NodePairScore(nodePair.f0, nodePair.f1, hiddenScore, ScoreType.HIDDEN);
      out.collect(hidden);
    }

  }
}
