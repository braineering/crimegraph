/*
  The MIT License (MIT)

  Copyright (c) 2016 Giacomo Marciani and Michele Porretta

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

package com.acmutv.crimegraph.evaluation.prediction;

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

import static com.acmutv.crimegraph.Common.HOSTNAME;
import static com.acmutv.crimegraph.Common.PASSWORD;
import static com.acmutv.crimegraph.Common.USERNAME;
import static com.acmutv.crimegraph.evaluation.EvaluationCommon.PREDICTION_ORIGIN;
import static com.acmutv.crimegraph.evaluation.EvaluationCommon.PREDICTION_TEST;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Utility for the evaluation of link prediction results.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class PredictionEvaluation {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredictionEvaluation.class);

  private static final String MATCH_POTENTIAL =
      "MATCH (a:Person {id:{src}})-[r:POTENTIAL]->(b:Person {id:{dst}}) " +
          "WITH r " +
          "RETURN r IS NOT NULL AS exists";

  private static final String GET_PARTIAL_N1N2_POTENTIAL =
      "MATCH (x1 {id:{src1}})-[r1:POTENTIAL]->(y1 {id:{dst1}}) " +
          "MATCH (x2 {id:{src2}})-[r2:POTENTIAL]->(y2 {id:{dst2}}) " +
          "WITH r1,r2 " +
          "RETURN r1.weight > r2.weight AS n1, r1.weight = r2.weight AS n2";

  private static final String GET_TOP_POTENTIAL =
      "MATCH (x)-[r:POTENTIAL]->(y) " +
          "RETURN x.id AS src, y.id AS dst " +
          "ORDER BY (r.weight) DESC " +
          "LIMIT {top}";

  /**
   * Evaluates prediction results: SIMPLE.
   */
  @Test
  public void evaluate_simple() throws IOException {
    long total = 0;
    long mined = 0;

    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    try (BufferedReader reader = Files.newBufferedReader(PREDICTION_TEST)) {
      while (reader.ready()) {
        Link link = Link.valueOf(reader.readLine());
        Value params = parameters("src", link.f0, "dst", link.f1);
        StatementResult result = session.run(MATCH_POTENTIAL, params);
        if (result.hasNext()) {
          Record rec = result.next();
          Boolean exists = rec.get("exists").asBoolean();
          if (exists) {
            mined ++;
          }
        }
        total ++;
      }
    }

    double simple = mined / total;

    LOGGER.info("SIMPLE %.3f", simple);

    session.close();
    driver.close();
  }

  /**
   * Evaluates prediction results: AUC.
   */
  @Test
  public void evaluate_auc() throws IOException {
    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    Set<Long> nodes = new HashSet<>();
    Set<Link> edges = new HashSet<>();
    try (BufferedReader originReader = Files.newBufferedReader(PREDICTION_ORIGIN)) {
      while (originReader.ready()) {
        Link link = Link.valueOf(originReader.readLine());
        long src = link.f0;
        long dst = link.f1;
        nodes.add(src);
        nodes.add(dst);
        link.f3 = LinkType.REAL;
        link.f2 = 1.0;
        edges.add(link);
      }
    }

    int numnodes = nodes.size();
    int numedges = edges.size();

    Set<Link> absentLinks = new HashSet<>();
    for (long src : nodes) {
      for (long dst : nodes) {
        Link link = new Link(src, dst, 1.0, LinkType.REAL);
        if (!edges.contains(link) && src != dst) {
          absentLinks.add(link);
        }
      }
    }
    long absent = absentLinks.size();
    Assert.assertEquals(absent, CombinatoricsUtils.binomialCoefficient(numnodes, 2) - numedges);

    Set<Link> testLinks = new HashSet<>();
    try(BufferedReader testReader = Files.newBufferedReader(PREDICTION_TEST)) {
      while (testReader.ready()) {
        Link link = Link.valueOf(testReader.readLine());
        link.f3 = LinkType.REAL;
        link.f2 = 1.0;
        testLinks.add(link);
      }
    }

    long n1 = 0; // numero di volte in cui lo score di un potential existent è maggiore di un non existent.
    long n2 = 0; // numero di volte in cui lo score di un potential existent è uguale ad un non existent.
    for (Link truePotential : testLinks) {
      for (Link absentLink : absentLinks) {
        long src1 = truePotential.f0;
        long dst1 = truePotential.f1;
        long src2 = absentLink.f0;
        long dst2 = absentLink.f1;
        Value params = parameters("src1", src1, "dst1", dst1, "src2", src2, "dst2", dst2);
        StatementResult result = session.run(GET_PARTIAL_N1N2_POTENTIAL, params);
        if (result.hasNext()) {
          Record rec = result.next();
          boolean n1_bool = rec.get("n1").asBoolean();
          boolean n2_bool = rec.get("n2").asBoolean();
          if (n1_bool) n1++;
          if (n2_bool) n2++;
        }
      }
    }

    long n = testLinks.size() * absent; // potential existent scores * non existent
    double auc = n1 + 0.5*n2 / n;

    LOGGER.info("AUC %.3f", auc);

    session.close();
    driver.close();
  }

  /**
   * Evaluates prediction results: PRECISION.
   */
  @Test
  public void evaluate_precision() throws IOException {
    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    int top = 3;

    Set<Link> topLinksTraining = new HashSet<>();
    Value params = parameters("top", top);
    StatementResult topTrainingResult = session.run(GET_TOP_POTENTIAL, params);
    while (topTrainingResult.hasNext()) {
      Record rec = topTrainingResult.next();
      long src = rec.get("src").asLong();
      long dst = rec.get("dst").asLong();
      Link link = new Link(src, dst, 1.0, LinkType.REAL);
      topLinksTraining.add(link);
    }

    Set<Link> linksTest = new HashSet<>();
    try (BufferedReader testReader = Files.newBufferedReader(PREDICTION_TEST)) {
      while (testReader.ready()) {
        Link link = Link.valueOf(testReader.readLine());
        link.f3 = LinkType.REAL;
        link.f2 = 1.0;
        linksTest.add(link);
      }
    }
    long numTestLinks = linksTest.size();

    long numTopTrainingHit = 0;
    for (Link trainingLink : topLinksTraining) {
      if (linksTest.contains(trainingLink)) {
        numTopTrainingHit ++;
      }
    }

    double precision = numTopTrainingHit / numTestLinks;

    LOGGER.info("PRECISION %.3f", precision);

    session.close();
    driver.close();
  }
}
