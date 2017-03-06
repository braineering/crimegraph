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

import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.NodePair;
import com.acmutv.crimegraph.core.tuple.NodePairScore;
import com.acmutv.crimegraph.core.tuple.UpdateType;
import com.acmutv.crimegraph.core.tuple.ScoreType;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.util.HashSet;
import java.util.Set;

/**
 * This operator parses interactions from input datastream.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class ScoreCalculator extends RichFlatMapFunction<NodePair, NodePairScore> {

    /**
     * The hostname of the NEO4J instance.
     */
    private String hostname;

    /**
     * The username of the NEO4J instance.
     */
    private String username;

    /**
     * The password of the NEO4J instance.
     */
    private String password;

    /**
     * The NEO4J driver.
     */
    private Driver driver;

    /**
     * The NEO4J session.
     */
    private Session session;

    /**
     * Constructs a new PotentialScoreOperator to compute the score
     * for a node pair stored in NodePair.
     * This operator required access to the NEO4J instance.
     * @param hostname the hostname of the NEO4J instance.
     * @param username the username of the NEO4J instance.
     * @param password the password of the NEO4J instance.
     */
    public ScoreCalculator(String hostname, String username, String password) {
      this.hostname = hostname;
      this.username = username;
      this.password = password;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
      this.driver = Neo4JManager.open(this.hostname, this.username, this.password);
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

        Set<Tuple2<Long,Long>> neighbours = Neo4JManager.commonNeighboursWithDegree(this.session, nodePair.f0, nodePair.f1);

        for (Tuple2<Long,Long> z : neighbours) {
          System.out.println("computing ("+nodePair.f0 + ";"+nodePair.f1+")"+ " - neighbour id: " +z.f0.toString() +" with degree "+z.f1.toString() + " for potential");
          potentialScore += (1.0 / z.f1);
        }
        //manca la gestione del timestamp
        NodePairScore potential = new NodePairScore(nodePair.f0, nodePair.f1, potentialScore, ScoreType.POTENTIAL, 1);
        out.collect(potential);
      }

      if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.HIDDEN)){
        double hiddenScore = 0.0;

        Set<Tuple3<Long,Long,Double>> neighbours = Neo4JManager.gammaIntersection(this.session, nodePair.f0, nodePair.f1);
        for (Tuple3<Long,Long,Double> z : neighbours) {
          System.out.println("computing ("+nodePair.f0 + ";"+nodePair.f1+")"+ " - neighbour id: " +z.f0.toString() +" with degree "+z.f1.toString() + " for hidden");
          hiddenScore += (z.f2 / z.f1);
        }
        //manca la gestione del timestamp
        NodePairScore hidden = new NodePairScore(nodePair.f0, nodePair.f1, hiddenScore, ScoreType.HIDDEN, 1);
        out.collect(hidden);
      }

    }
}
