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

package com.acmutv.crimegraph.core.db;

import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.neo4j.driver.v1.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.acmutv.crimegraph.core.db.Neo4JQueries.*;
import static com.acmutv.crimegraph.core.tuple.LinkType.REAL;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Collections of NEO4J useful queries.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class Neo4JManager {

  private static final long CONNECTION_LIVENESS_CHECK_TIMEOUT = 200;

  /**
   * Opens a NEO4J connection.
   * @param dbconf the configuration to connect to Neo4J.
   * @return a open NEO4J driver.
   */
  public static Driver open(DbConfiguration dbconf) {
    AuthToken auth = AuthTokens.basic(dbconf.getUsername(), dbconf.getPassword());
    Config config = Config.build()
        .withConnectionLivenessCheckTimeout(CONNECTION_LIVENESS_CHECK_TIMEOUT, TimeUnit.MILLISECONDS)
        .withEncryptionLevel(Config.EncryptionLevel.NONE )
        .toConfig();
    return GraphDatabase.driver(dbconf.getHostname(), auth, config);
  }

  /**
   * Opens a NEO4J connection.
   * @param hostname the instance hostname.
   * @param username the username for the authentication.
   * @param password the password for the authentication.
   * @return a open NEO4J driver.
   */
  public static Driver open(String hostname, String username, String password) {
    AuthToken auth = AuthTokens.basic(username, password);
    Config config = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig();
    return GraphDatabase.driver(hostname, auth, config);
  }

  /**
   * Closes the NEO4J connection.
   * @param session the NEO4J session.
   * @param driver the NEO4J driver.
   */
  public static void close(Session session, Driver driver) {
    session.close();
    driver.close();
  }

  /**
   * Saves a new link.
   * @param session the NEO4J open session.
   * @param link the link to save.
   */
  public static void save(Session session, Link link) {
    long src = link.f0;
    long dst = link.f1;
    double weight = link.f2;
    LinkType type = link.f3;

    Value params = parameters("src", src, "dst", dst, "weight", weight);

    if (REAL.equals(type)) {
      session.run(SAVE_LINK_REAL_AVERAGE, params);
    } else {
      final String SAVE_LINK_MINED = String.format(SAVE_LINK_MINED_GENERAL, type);
      session.run(SAVE_LINK_MINED, params);
    }

  }

  /**
   * Saves a new link.
   * @param session the NEO4J open session.
   * @param link the link to save.
   * @param ewmaFactor the EWMA factor for recent observation.
   */
  public static void save(Session session, Link link, double ewmaFactor) {
    long src = link.f0;
    long dst = link.f1;
    double weight = link.f2;
    LinkType type = link.f3;

    Value params = parameters("src", src, "dst", dst, "weight", weight, "ewma", ewmaFactor);

    if (REAL.equals(type)) {
      session.run(SAVE_LINK_REAL_EWMA, params);
    } else {
      final String SAVE_LINK_MINED = String.format(SAVE_LINK_MINED_GENERAL, type);
      session.run(SAVE_LINK_MINED, params);
    }
  }

  /**
   * Removes a link.
   * @param x the id of the first node.
   * @param y the id of the second node.
   * @param type the type of link.
   */
  public static void remove(Session session, long x, long y, LinkType type) {
    Value params = parameters("x", x, "y", y, "type", type.name());
    session.run(REMOVE_LINK, params);
  }

  /**
   * Removes a link.
   * @param session the session.
   * @param link the link.
   */
  public static void remove(Session session, Link link) {
    final long x = link.f0;
    final long y = link.f1;
    final LinkType type = link.f3;
    Value params = parameters("x", x, "y", y, "type", type.name());
    session.run(REMOVE_LINK, params);
  }

  /**
   * Matches neighbours of node {@code a}.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   */
  public static Set<Long> neighbours(Session session, long a) {
    Value params = parameters("src", a);
    StatementResult result = session.run(MATCH_NEIGHBOURS, params);
    Set<Long> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      neighbours.add(id);
    }
    return neighbours;
  }

  /**
   * Matches neighbours with degree.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   */
  public static Set<Tuple2<Long,Long>> neighboursWithDegree(Session session, long a) {
    Value params = parameters("src", a);
    StatementResult result = session.run(MATCH_NEIGHBOURS_WITH_DEGREE, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      Long deg = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(id, deg));
    }
    return neighbours;
  }

  /**
   * Matches neighbours within upper bound distance.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param dist the neighbourhood distance.
   */
  public static Set<Long> neighboursWithinDistance(Session session, long a, long dist) {
    Value params = parameters("src", a, "dist", dist);
    String query = String.format(MATCH_NEIGHBOURS_WITHIN_DISTANCE, dist);
    StatementResult result = session.run(query, params);
    Set<Long> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      neighbours.add(id);
    }
    return neighbours;
  }

  /**
   * Matches neighbours with degree within upper bound distance.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param dist the neighbourhood distance.
   */
  public static Set<Tuple2<Long,Long>> neighboursWithDegreeWithinDistance(Session session, long a, long dist) {
    Value params = parameters("src", a, "dist", dist);
    String query = String.format(MATCH_NEIGHBOURS_WITH_DEGREE_WITHIN_DISTANCE, dist);
    StatementResult result = session.run(query, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      Long deg = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(id, deg));
    }
    return neighbours;
  }

  /**
   * Matches common neighbours.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   */
  public static Set<Long> commonNeighbours(Session session, long a, long b) {
    Value params = parameters("src", a, "dst", b);
    StatementResult result = session.run(MATCH_COMMON_NEIGHBOURS, params);
    Set<Long> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      neighbours.add(id);
    }
    return neighbours;
  }

  /**
   * Matches common neighbours with degree.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   */
  public static Set<Tuple2<Long,Long>> commonNeighboursWithDegree(Session session, long a, long b) {
    Value params = parameters("src", a, "dst", b);
    StatementResult result = session.run(MATCH_COMMON_NEIGHBOURS_WITH_DEGREE, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      Long deg = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(id, deg));
    }
    return neighbours;
  }

  /**
   * Matches common neighbours within upper bound distance.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   * @param dist the neighbourhood distance.
   */
  public static Set<Long> commonNeighboursWithinDistance(Session session, long a, long b, long dist) {
    Value params = parameters("src", a, "dst", b, "dist", dist);
    String query = String.format(MATCH_COMMON_NEIGHBOURS_WITHIN_DISTANCE, dist, dist);
    StatementResult result = session.run(query, params);
    Set<Long> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      neighbours.add(id);
    }
    return neighbours;
  }

  /**
   * Matches common neighbours with degree within upper bound distance.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   * @param dist the neighbourhood distance.
   */
  public static Set<Tuple2<Long,Long>> commonNeighboursWithDegreeWithinDistance(Session session, long a, long b, long dist) {
    Value params = parameters("src", a, "dst", b, "dist", dist);
    String query = String.format(MATCH_COMMON_NEIGHBOURS_WITH_DEGREE_WITHIN_DISTANCE, dist, dist);
    StatementResult result = session.run(query, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long id = rec.get("id").asLong();
      Long deg = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(id, deg));
    }
    return neighbours;
  }

  /**
   * Checks the existence of nodes {@code a} and {@code b}, and the edge {@code (a,b)}.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   */
  public static Tuple3<Boolean,Boolean,Boolean> checkExtremes(Session session, long a, long b) {
    Value params = parameters("src", a, "dst", b);
    StatementResult result = session.run(CHECK_EXTREMES, params);
    Tuple3<Boolean,Boolean,Boolean> check = new Tuple3<>();
    if (result.hasNext()) {
      Record rec = result.next();
      Boolean src = rec.get("src").asBoolean();
      Boolean dst = rec.get("dst").asBoolean();
      Boolean arc = rec.get("arc").asBoolean();
      check.setFields(src, dst, arc);
    }
    return check;
  }

  /**
   * Finds pairs of unlinked nodes to update, with single node insertion.
   * @param session the NEO4J open session.
   * @param x the id of the node to update.
   */
  public static Set<Tuple2<Long,Long>> pairsToUpdate(Session session, long x) {
    Value params = parameters("x", x);
    StatementResult result = session.run(PAIRS_TO_UPDATE, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Value pair = rec.get("pair");
      Long node1 = pair.get(0).asLong();
      Long node2 = pair.get(1).asLong();
      Tuple2<Long,Long> tuple = new Tuple2<>();
      if (node1 <= node2) {
        tuple.setFields(node1, node2);
      } else {
        tuple.setFields(node2, node1);
      }
      neighbours.add(tuple);
    }
    return neighbours;
  }

  /**
   * Finds pairs of unlinked nodes to update, with single node insertion.
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Set<Tuple2<Long,Long>> pairsToUpdateTwice(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);
    StatementResult result = session.run(PAIRS_TO_UPDATE_TWICE, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Value pair = rec.get("pair");
      Long node1 = pair.get(0).asLong();
      Long node2 = pair.get(1).asLong();
      Tuple2<Long,Long> tuple = new Tuple2<>();
      if (node1 <= node2) {
        tuple.setFields(node1, node2);
      } else {
        tuple.setFields(node2, node1);
      }
      neighbours.add(tuple);
    }
    return neighbours;
  }

  /**
   * Finds pairs of unlinked nodes to update, with single node insertion.
   * @param session the NEO4J open session.
   * @param x the id of the node to update.
   * @param dist the distance.
   */
  public static Set<Tuple2<Long,Long>> pairsToUpdateWithinDistance(Session session, long x, long dist) {
    Value params = parameters("x", x);
    String query = String.format(PAIRS_TO_UPDATE_WITHIN_DISTANCE,
        dist + 1, dist, dist + 1, dist + 1, dist, dist);
    StatementResult result = session.run(query, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Value pair = rec.get("pair");
      Long node1 = pair.get(0).asLong();
      Long node2 = pair.get(1).asLong();
      Tuple2<Long,Long> tuple = new Tuple2<>();
      if (node1 <= node2) {
        tuple.setFields(node1, node2);
      } else {
        tuple.setFields(node2, node1);
      }
      neighbours.add(tuple);
    }
    return neighbours;
  }

  /**
   * Finds pairs of unlinked nodes to update, with single node insertion.
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   * @param dist the distance.
   */
  public static Set<Tuple2<Long,Long>> pairsToUpdateTwiceWithinDistance(Session session, long x, long y, long dist) {
    Value params = parameters("x", x, "y", y);
    String query = String.format(PAIRS_TO_UPDATE_TWICE_WITHIN_DISTANCE,
        dist + 1, dist, dist + 1, dist + 1, dist, dist,
        dist + 1, dist, dist + 1, dist + 1, dist, dist);
    StatementResult result = session.run(query, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Value pair = rec.get("pair");
      Long node1 = pair.get(0).asLong();
      Long node2 = pair.get(1).asLong();
      Tuple2<Long,Long> tuple = new Tuple2<>();
      if (node1 <= node2) {
        tuple.setFields(node1, node2);
      } else {
        tuple.setFields(node2, node1);
      }
      neighbours.add(tuple);
    }
    return neighbours;
  }

  /**
   * Finds Gamma-intersection and related details for score formulas.
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Set<Tuple4<Long,Long,Double,Double>> gammaIntersection(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);
    StatementResult result = session.run(GAMMA_INTERSECTION, params);
    Set<Tuple4<Long,Long,Double,Double>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long node = rec.get("id").asLong();
      Long degree = rec.get("deg").asLong();
      Double weight = rec.get("weight").asDouble();
      Double weightTot = rec.get("weightTot").asDouble();
      neighbours.add(new Tuple4<>(node, degree, weight, weightTot));
    }
    return neighbours;
  }

  /**
   * Finds H-intersection and related details for score formulas.
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   * @param dist the distance.
   */
  public static Set<Tuple2<Long,Long>> hIntersection(Session session, long x, long y, long dist) {
    Value params = parameters("x", x, "y", y);
    String query = String.format(H_INTERSECTION, dist, dist);
    StatementResult result = session.run(query, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long node = rec.get("id").asLong();
      Long degree = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(node, degree));
    }
    return neighbours;
  }

  /**
   * Empyting of Neo4J
   * @param dbconf the configuration to connect to Neo4J.
   */
  public static void empyting(DbConfiguration dbconf) {
    String hostname = dbconf.getHostname();
    String username = dbconf.getUsername();
    String password = dbconf.getPassword();
    Driver driver = Neo4JManager.open(hostname, username, password);
    Session session = driver.session();
    session.run(EMPYTING);
    Neo4JManager.close(session, driver);
  }

  /**
   * Empyting of Neo4J
   * @param session the Neo4J session.
   */
  public static void empyting(Session session) {
    session.run(EMPYTING);
  }

  /**
   * Common Neighbours Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Long countCommonNeighbours(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);
    StatementResult result = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record rec = result.next();
    Long cn = rec.get("cn").asLong();
    return cn;
  }

  /**
   * Jaccard Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double jaccard(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);
    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long cn = recInt.get("cn").asLong();

    StatementResult gamma = session.run(COUNT_GAMMA_UNION, params);
    Record recGam = gamma.next();
    Long gu = recGam.get("cardinality").asLong();
    return Double.valueOf(cn/gu);
  }

  /**
   * Sorensen Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double sorensen(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);

    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long cn = recInt.get("cn").asLong();

    params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return Double.valueOf((2*(cn))/(kx + ky));
  }

  /**
   * Hub Promoted Index (HPI) Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double HPI(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);

    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long cn = recInt.get("cn").asLong();

    params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return Double.valueOf(cn/Long.min(kx,ky));
  }

  /**
   * Hub Depressed Index (HDI) Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double HDI(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);

    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long cn = recInt.get("cn").asLong();

    params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return Double.valueOf(cn/Long.max(kx,ky));
  }

  /**
   * Leicht-Holme-Newman Index (LHN1) Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double LHN1(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);

    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long cn = recInt.get("cn").asLong();

    params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return Double.valueOf(cn/Long.max(kx,ky));
  }


  /**
   * Preferential Attachment Index (PA) Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double preferentialAttachment(Session session, long x, long y) {

    Value params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return Double.valueOf(kx * ky);
  }

  /**
   * Salton Index Metric
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Double salton(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);

    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long cn = recInt.get("cn").asLong();

    params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return Double.valueOf(cn/Math.sqrt(kx * ky));
  }

  /**
   * Adamic-Adar Index
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Set<Tuple2<Long,Long>> adamicAdar(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);
    StatementResult result = session.run(GAMMA_INTERSECTION, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long node = rec.get("id").asLong();
      Long degree = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(node, degree));
    }
    return neighbours;
  }

  /**
   * RA Index
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Set<Tuple2<Long,Long>> resourceAllocation(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);
    StatementResult result = session.run(GAMMA_INTERSECTION, params);
    Set<Tuple2<Long,Long>> neighbours = new HashSet<>();
    while (result.hasNext()) {
      Record rec = result.next();
      Long node = rec.get("id").asLong();
      Long degree = rec.get("deg").asLong();
      neighbours.add(new Tuple2<>(node, degree));
    }
    return neighbours;
  }

  /**
   * Multi Indices Tool for ScoreCalculatorMultiIndices
   * @param session the NEO4J open session.
   * @param x the id of the first node to update.
   * @param y the id of the second node to update.
   */
  public static Tuple4<Long,Long,Long,Long> multiIndexTool(Session session, long x, long y) {
    Value params = parameters("x", x, "y", y);

    StatementResult intersection = session.run(COUNT_COMMON_NEIGHBOURS, params);
    Record recInt = intersection.next();
    Long countintersection = recInt.get("cn").asLong();

    StatementResult gamma = session.run(COUNT_GAMMA_UNION, params);
    Record recGam = gamma.next();
    Long countunion = recGam.get("cardinality").asLong();

    params = parameters("src", x);
    StatementResult degreeX = session.run(NODE_DEGREE, params);
    Record recX = degreeX.next();
    Long kx = recX.get("degree").asLong();

    params = parameters("src", y);
    StatementResult degreeY = session.run(NODE_DEGREE, params);
    Record recY = degreeY.next();
    Long ky = recY.get("degree").asLong();

    return new Tuple4<>(countintersection,countunion,kx,ky);
  }

}
