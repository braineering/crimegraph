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

package com.acmutv.crimegraph.evaluation.detection;

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
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
import static com.acmutv.crimegraph.evaluation.EvaluationCommon.*;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Utility for the evaluation of link prediction results.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class DetectionEvaluation {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetectionEvaluation.class);

  private static final String MATCH =
      "OPTIONAL MATCH (a:Person {id:{src}}) " +
          "OPTIONAL MATCH (b:Person {id:{dst}}) " +
          "WITH a,b " +
          "OPTIONAL MATCH (a)-[r1:REAL]->(b) " +
          "OPTIONAL MATCH (a)-[r2:HIDDEN]->(b) " +
          "RETURN a IS NOT NULL AS existsSrc, b IS NOT NULL AS existsDst, r1 IS NOT NULL AS existsReal, r2 IS NOT NULL AS existsHidden";

  private static final String GET_PARTIAL_N1N2_HIDDEN =
      "MATCH (x1 {id:{src1}})-[r1:HIDDEN]->(y1 {id:{dst1}}) " +
          "MATCH (x2 {id:{src2}})-[r2:HIDDEN]->(y2 {id:{dst2}}) " +
          "WITH r1,r2 " +
          "RETURN " +
          "r1.weight > r2.weight AS n1, " +
          "r1.weight = r2.weight AS n2";

  private static final String GET_TOP_HIDDEN =
      "MATCH (x)-[r:HIDDEN]->(y) " +
          "RETURN x.id AS src, y.id AS dst " +
          "ORDER BY (r.weight) DESC " +
          "LIMIT {top}";

  /**
   * Evaluates detection results: SIMPLE.
   */
  @Test
  public void evaluate_simple() throws IOException {
    long total = 0;
    long mined = 0;

    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    try (BufferedReader reader = Files.newBufferedReader(DETECTION_TEST)) {
      while (reader.ready()) {
        Link link = Link.valueOf(reader.readLine());
        Value params = parameters("src", link.f0, "dst", link.f1);
        StatementResult result = session.run(MATCH, params);
        if (result.hasNext()) {
          Record rec = result.next();
          Boolean existsSrc = rec.get("existsSrc").asBoolean();
          Boolean existsDst = rec.get("existsDst").asBoolean();
          Boolean existsReal = rec.get("existsReal").asBoolean();
          Boolean existsPotential = rec.get("existsHidden").asBoolean();
          if (existsSrc && existsDst && ! existsReal) {
            total ++;
            System.out.format("Can be detected: (%d,%d)\n", link.f0, link.f1);
            if (existsPotential) {
              mined ++;
              System.out.format("\tHas been detected: (%d,%d)\n", link.f0, link.f1);
            }
          }
        }
      }
    }

    double simple = (double)mined / (double)total;

    System.out.format("SIMPLE (mined: %d | total: %d) : %.5f\n", mined, total, simple);

    session.close();
    driver.close();
  }

  /**
   * Evaluates detection results: AUC.
   */
  @Test
  public void evaluate_auc() throws IOException {
    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    Set<Long> nodes = new HashSet<>(); // existent nodes
    Set<Link> links = new HashSet<>(); // existent links
    try (BufferedReader trainingReader = Files.newBufferedReader(DETECTION_ORIGIN)) {
      while (trainingReader.ready()) {
        Link link = Link.valueOf(trainingReader.readLine());
        long src = link.f0;
        long dst = link.f1;
        nodes.add(src);
        nodes.add(dst);
        link.f3 = LinkType.REAL;
        link.f2 = 1.0;
        links.add(link);
      }
    }

    Set<Long> nodes_training = new HashSet<>(); // existent nodes in training set
    Set<Link> links_training = new HashSet<>(); // existent links in training set
    try (BufferedReader trainingReader = Files.newBufferedReader(DETECTION_TRAINING)) {
      while (trainingReader.ready()) {
        Link link = Link.valueOf(trainingReader.readLine());
        long src = link.f0;
        long dst = link.f1;
        nodes_training.add(src);
        nodes_training.add(dst);
        link.f3 = LinkType.REAL;
        link.f2 = 1.0;
        links_training.add(link);
      }
    }

    int numnodes_training = nodes_training.size();
    int numlinks_training = links_training.size();

    System.out.println("numnodes_training: " + numnodes_training);
    System.out.println("numlinks_training: " + numlinks_training);

    Set<Link> missingLinks = new HashSet<>(); // links in test set between nodes in training set
    try (BufferedReader testReader = Files.newBufferedReader(DETECTION_TEST)) {
      while (testReader.ready()) {
        Link link = Link.valueOf(testReader.readLine());
        long src = link.f0;
        long dst = link.f1;
        if (nodes_training.contains(src) && nodes_training.contains(dst)) {
          link.f3 = LinkType.REAL;
          link.f2 = 1.0;
          missingLinks.add(link);
          System.out.format("Missing link: (%d,%d)\n", src, dst);
        }
      }
    }
    long numlinks_missing = missingLinks.size();

    Set<Link> notExistentLinks = new HashSet<>(); // link not existent in origin, between nodes in training set
    for (long src : nodes_training) {
      for (long dst : nodes_training) {
        if (dst <= src) continue;
        Link link = new Link(src, dst, 1.0, LinkType.REAL);
        if (!links.contains(link)) {
          notExistentLinks.add(link);
          System.out.format("Not existent link: (%d,%d)\n", src, dst);
        }
      }
    }
    long numlinks_notExistent = notExistentLinks.size();

    long n1 = 0; // numero di volte in cui lo score di un test link è maggiore di quello di un link non esistente nel training set.
    long n2 = 0; // numero di volte in cui lo score di un test link è uguale a quello di un link non esistente nel training set.
    for (Link missingLink : missingLinks) {
      for (Link notExistentLink : notExistentLinks) {
        long src1 = missingLink.f0;
        long dst1 = missingLink.f1;
        long src2 = notExistentLink.f0;
        long dst2 = notExistentLink.f1;
        Value params = parameters("src1", src1, "dst1", dst1, "src2", src2, "dst2", dst2);
        StatementResult result = session.run(GET_PARTIAL_N1N2_HIDDEN, params);
        if (result.hasNext()) {
          Record rec = result.next();
          boolean n1_bool = rec.get("n1", true);
          boolean n2_bool = rec.get("n2", true);
          if (n1_bool) n1++;
          if (n2_bool) n2++;
        }
      }
    }

    long n = numlinks_missing * numlinks_notExistent;
    double auc = ((double)n1 + 0.5*n2) / (double)n;

    System.out.format("AUC (missing_links: %d | notexistent_links: %d | n1: %d | n2: %d) : %.5f", numlinks_missing, numlinks_notExistent, n1, n2, auc);

    session.close();
    driver.close();
  }

  /**
   * Evaluates detection results: PRECISION.
   */
  @Test
  public void evaluate_precision() throws IOException {
    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    Driver driver = Neo4JManager.open(dbconf);
    Session session = driver.session();

    int k = 3;

    Set<Link> topLinksTraining = new HashSet<>(); // top k detected
    Value params = parameters("top", k);
    StatementResult topTrainingResult = session.run(GET_TOP_HIDDEN, params);
    while (topTrainingResult.hasNext()) {
      Record rec = topTrainingResult.next();
      long src = rec.get("src").asLong();
      long dst = rec.get("dst").asLong();
      Link link = new Link(src, dst, 1.0, LinkType.REAL);
      topLinksTraining.add(link);
      System.out.format("TOP-TRAINING (k:%d): (%d,%d)\n", k, src, dst);
    }

    Set<Long> nodes_training = new HashSet<>(); // existent nodes in training set
    try (BufferedReader trainingReader = Files.newBufferedReader(DETECTION_TEST)) {
      while (trainingReader.ready()) {
        Link link = Link.valueOf(trainingReader.readLine());
        long src = link.f0;
        long dst = link.f1;
        nodes_training.add(src);
        nodes_training.add(dst);
      }
    }

    Set<Link> linksTest = new HashSet<>();
    try (BufferedReader testReader = Files.newBufferedReader(DETECTION_TEST)) {
      while (testReader.ready()) {
        Link link = Link.valueOf(testReader.readLine());
        long src = link.f0;
        long dst = link.f1;
        if (nodes_training.contains(src) && nodes_training.contains(dst)) {
          link.f3 = LinkType.REAL;
          link.f2 = 1.0;
          linksTest.add(link);
          System.out.format("Can be detected: (%d,%d)\n", src, dst);
        }
      }
    }
    long numTestLinks = linksTest.size();

    long numTopTrainingHit = 0;
    for (Link trainingLink : topLinksTraining) {
      if (linksTest.contains(trainingLink)) {
        numTopTrainingHit ++;
      }
    }

    double precision = (double)numTopTrainingHit / (double)numTestLinks;

    System.out.format("PRECISION (top: %d | hits: %d | links_test: %d) : %.5f\n", k, numTopTrainingHit, numTestLinks, precision);

    session.close();
    driver.close();
  }
}
