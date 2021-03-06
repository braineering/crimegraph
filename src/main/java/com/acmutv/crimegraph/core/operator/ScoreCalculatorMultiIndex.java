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
public class ScoreCalculatorMultiIndex extends RichFlatMapFunction<NodePair, NodePairScore> {

  /**
   * The Neo4J configuration.
   */
  private DbConfiguration dbconfig;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  /**
   * Constructs a new PotentialScoreOperator to compute the score
   * for a node pair stored in NodePair.
   * This operator required access to the NEO4J instance.
   * @param dbconfig the Neo4J configuration.
   */
  public ScoreCalculatorMultiIndex(DbConfiguration dbconfig) {
    this.dbconfig = dbconfig;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.dbconfig);
  }

  @Override
  public void close() throws Exception {
    this.driver.close();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void flatMap(NodePair nodePair, Collector<NodePairScore> out) {
    Session session = this.driver.session();

    long x = nodePair.f0;
    long y = nodePair.f1;
    UpdateType type = nodePair.f2;

    Set<Tuple4<Long,Long,Double,Double>> neighbours = Neo4JManager.gammaIntersection(session, x, y);

    if(type.equals(UpdateType.TM)){
      /* TRAFFIC METRICS */
      double trafficScore = 0.0;
      double totalWeight = 0.0;

      for (Tuple4<Long,Long,Double,Double> z : neighbours) {
        double pathWeight = z.f2;
        double nodeWeight = z.f3;
        assert nodeWeight != 0.0;
        trafficScore += (pathWeight / nodeWeight);
        totalWeight += nodeWeight;
      }

      /* TRAFFIC ALLOCATION */
      NodePairScore ta = new NodePairScore(x, y, trafficScore, ScoreType.TA);
      out.collect(ta);

      /* NORMALIZED TRAFFIC ALLOCATION */
      trafficScore /= totalWeight;
      NodePairScore nta = new NodePairScore(x, y, trafficScore, ScoreType.NTA);
      out.collect(nta);
    }

    /* for Multi-Index Computing (include traffic metrics) */
    if(type.equals(UpdateType.ALL)){

      /* multi indices: <cardinality intersection, cardinality union, x degree, y degree> */
      Tuple4<Long, Long, Long, Long> multiIndex = Neo4JManager.multiIndexTool(session, x, y);

      /* COMMON NEIGHBOURS INDEX */
      NodePairScore cn = new NodePairScore(x, y, (double) multiIndex.f0, ScoreType.CN);
      out.collect(cn);

      /* SALTON INDEX */
      Double saltonScore = (double) multiIndex.f0 / Math.sqrt(multiIndex.f2 * multiIndex.f3);
      NodePairScore salton = new NodePairScore(x, y, saltonScore, ScoreType.SALTON);
      out.collect(salton);

      /* JACCARD INDEX */
      Double jaccardScore =  (double)multiIndex.f0 / (double) multiIndex.f1;
      NodePairScore jaccard = new NodePairScore(x, y, jaccardScore, ScoreType.JACCARD);
      out.collect(jaccard);

      /* SORENSEN INDEX */
      Double sorensenScore = (double)  (2 * multiIndex.f0) / ((double)multiIndex.f2 + (double)multiIndex.f3);
      NodePairScore sorensen = new NodePairScore(x, y, sorensenScore, ScoreType.SORENSEN);
      out.collect(sorensen);

      /* HPI INDEX */
      Double hpiScore = (double) multiIndex.f0 / Double.min(multiIndex.f2, multiIndex.f3);
      NodePairScore hpi = new NodePairScore(x, y, hpiScore, ScoreType.HPI);
      out.collect(hpi);

      /* HDI INDEX */
      Double hdiScore = (double) multiIndex.f0 / Double.max(multiIndex.f2, multiIndex.f3);
      NodePairScore hdi = new NodePairScore(x, y, hdiScore, ScoreType.HDI);
      out.collect(hdi);

      /* LHN1 INDEX */
      Double lhn1Score = multiIndex.f0 / ((double) multiIndex.f2 * (double) multiIndex.f3);
      NodePairScore lhn1 = new NodePairScore(x, y, lhn1Score, ScoreType.LHN1);
      out.collect(lhn1);

      /* PA INDEX */
      Double psScore = ((double) multiIndex.f2 * (double) multiIndex.f3);
      NodePairScore pa = new NodePairScore(x, y, psScore, ScoreType.PA);
      out.collect(pa);

      double nraScore = 0.0;
      double adamicAdarScore = 0.0;
      double resourceAllocationScore = 0.0;

      long totalDegree = 0;

      for (Tuple4<Long, Long, Double, Double> z : neighbours) {
        long zDegree = z.f1;
        assert zDegree > 0;
        nraScore += (1.0 / zDegree);
        resourceAllocationScore += (1.0 / zDegree);
        adamicAdarScore += (1.0 / Math.log10(zDegree));
        totalDegree += zDegree;
      }

      /* ADAMIC ADAR INDEX */
      NodePairScore aa = new NodePairScore(x, y, adamicAdarScore, ScoreType.AA);
      out.collect(aa);

      /* RESOURCE ALLOCATION INDEX */
      NodePairScore ra = new NodePairScore(x, y, resourceAllocationScore, ScoreType.RA);
      out.collect(ra);

      /* NORMALIZED RESOURCE ALLOCATION */
      nraScore /= totalDegree;
      NodePairScore nra = new NodePairScore(x, y, nraScore, ScoreType.NRA);
      out.collect(nra);

      /* TRAFFIC METRICS */
      double trafficScore = 0.0;
      double totalWeight = 0.0;

      for (Tuple4<Long,Long,Double,Double> z : neighbours) {
        double pathWeight = z.f2;
        double nodeWeight = z.f3;
        assert nodeWeight != 0.0;
        trafficScore += (pathWeight / nodeWeight);
        totalWeight += nodeWeight;
      }

      /* TRAFFIC ALLOCATION */
      NodePairScore ta = new NodePairScore(x, y, trafficScore, ScoreType.TA);
      out.collect(ta);

      /* NORMALIZED TRAFFIC ALLOCATION */
      trafficScore /= totalWeight;
      NodePairScore nta = new NodePairScore(x, y, trafficScore, ScoreType.NTA);
      out.collect(nta);
    }

    session.close();
  }
}
