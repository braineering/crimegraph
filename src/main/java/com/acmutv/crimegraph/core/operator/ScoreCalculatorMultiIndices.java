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
import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.util.Set;

/**
 * This operator parses interactions from input datastream.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class ScoreCalculatorMultiIndices extends RichFlatMapFunction<NodePair, NodePairScore> {

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
   * The type of index
   */
  private String index;

  /**
   * Constructs a new PotentialScoreOperator to compute the score
   * for a node pair stored in NodePair.
   * This operator required access to the NEO4J instance.
   * @param dbconfig the Neo4J configuration.
   */
  public ScoreCalculatorMultiIndices(DbConfiguration dbconfig, String index) {
    this.dbconfig = dbconfig;
    this.index = index;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.dbconfig);
    this.session = driver.session(AccessMode.READ);
  }

  @Override
  public void close() throws Exception {
      Neo4JManager.close(this.session, this.driver);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void flatMap(NodePair nodePair, Collector<NodePairScore> out) {
    long x = nodePair.f0;
    long y = nodePair.f1;
    UpdateType type = nodePair.f2;

    Set<Tuple4<Long,Long,Double,Double>> neighbours = Neo4JManager.gammaIntersection(this.session, x, y);

    /* HIDDEN */
    if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.HIDDEN)){
      double hiddenScore = 0.0;
      double totalWeight = 0.0;

      for (Tuple4<Long,Long,Double,Double> z : neighbours) {
        double pathWeight = z.f2;
        double nodeWeight = z.f3;
        assert nodeWeight != 0.0;
        hiddenScore += (pathWeight / nodeWeight);
        totalWeight += nodeWeight;
      }
      hiddenScore /= totalWeight;
      System.out.format("Hidden(%d,%d)=%f\n", x, y, hiddenScore);
      NodePairScore hidden = new NodePairScore(x, y, hiddenScore, ScoreType.HIDDEN);
      out.collect(hidden);
    }

    /* POTENTIALS */
    if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){

      if(index == "CommonNeighbours") {
        Long cn = Neo4JManager.countCommonNeighbours(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = cn;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "Salton") {
        Double score = Neo4JManager.salton(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = score;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "Jaccard") {
        Double score = Neo4JManager.jaccard(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = score;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "Soresen") {
        Double score = Neo4JManager.soresen(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = score;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "HPI") {
        Double score = Neo4JManager.HPI(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = score;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "HDI") {
        Double score = Neo4JManager.HDI(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = score;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "LHN1") {
        Double score = Neo4JManager.LHN1(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = score;
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "PA") {
        Set<Tuple2<Long,Long>> neighboursPA = Neo4JManager.resourceAllocation(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = 0.0;

          for (Tuple2<Long,Long> z : neighboursPA) {
            long zDegree = z.f1;
            assert zDegree > 0;
            potentialScore += (1.0 / zDegree);
          }

          System.out.format("Potential(%d,%d)=%f\n", x, y, potentialScore);
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "AA") {
        Set<Tuple2<Long,Long>> neighboursAA = Neo4JManager.adamicAdar(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = 0.0;

          for (Tuple2<Long,Long> z : neighboursAA) {
            long zDegree = z.f1;
            assert zDegree > 0;
            potentialScore += (1.0 / Math.log10(zDegree));
          }

          System.out.format("Potential(%d,%d)=%f\n", x, y, potentialScore);
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else if(index == "RA") {
        Set<Tuple2<Long,Long>> neighboursRA = Neo4JManager.resourceAllocation(this.session, x, y);

        if(type.equals(UpdateType.BOTH) || type.equals(UpdateType.POTENTIAL)){
          double potentialScore = 0.0;

          for (Tuple2<Long,Long> z : neighboursRA) {
            long zDegree = z.f1;
            assert zDegree > 0;
            potentialScore += (1.0 / zDegree);
          }

          System.out.format("Potential(%d,%d)=%f\n", x, y, potentialScore);
          NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
          out.collect(potential);
        }
      }
      else {
        /* MARCIANI AND PORRETTA INDEX */
        double potentialScore = 0.0;
        long totalDegree = 0;

        for (Tuple4<Long,Long,Double,Double> z : neighbours) {
          long zDegree = z.f1;
          assert zDegree > 0;
          potentialScore += (1.0 / zDegree);
          totalDegree += zDegree;
        }

        potentialScore /= totalDegree;
        System.out.format("Potential(%d,%d)=%f\n", x, y, potentialScore);
        NodePairScore potential = new NodePairScore(x, y, potentialScore, ScoreType.POTENTIAL);
        out.collect(potential);
      }
    }

  }
}
