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

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Collections of NEO4J useful queries.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class Neo4JManager {

  /**
   * Query to create a new real link (AVERAGE).
   */
  private static final String SAVE_LINK_REAL_AVERAGE =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:REAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.num=1,r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight=(r.weight*r.num+{weight})/(r.num+1),r.num=r.num+1,r.updated=timestamp() " +
          "WITH u1,u2 " +
          "MATCH (u1)-[r2]-(u2) " +
          "WHERE type(r2)='POTENTIAL' OR type(r2)='HIDDEN' " +
          "DELETE r2";

  /**
   * Query to create a new real link (EWMA).
   */
  private static final String SAVE_LINK_REAL_EWMA =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:REAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.num=1,r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight=({weight}*{ewma} + r.weight*(1-{ewma}),r.num=r.num+1,r.updated=timestamp() " +
          "WITH u1,u2 " +
          "MATCH (u1)-[r2]-(u2) " +
          "WHERE type(r2)='POTENTIAL' OR type(r2)='HIDDEN' " +
          "DELETE r2";

  /**
   * Query to create a new potential link.
   */
  private static final String SAVE_LINK_POTENTIAL =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:POTENTIAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight={weight},r.updated=timestamp()";

  /**
   * Query to create a new hidden link.
   */
  private static final String SAVE_LINK_HIDDEN =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:HIDDEN]-(u2) " +
          "ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight={weight},r.updated=timestamp()";

  /**
   * Query to remove a link.
   */
  private static final String REMOVE_LINK =
      "MATCH (x:Person {id:{x}})-[r]-(y:Person {id:{y}}) " +
          "WHERE type(r) = '{type}' " +
          "DELETE r";

  /**
   * Query to match neighbours.
   */
  private static final String MATCH_NEIGHBOURS =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match neighbours and degree.
   */
  private static final String MATCH_NEIGHBOURS_WITH_DEGREE =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to match neighbours within maximum distance.
   */
  private static final String MATCH_NEIGHBOURS_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match neighbours with degree within maximum distance.
   */
  private static final String MATCH_NEIGHBOURS_WITH_DEGREE_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to match common neighbours.
   */
  private static final String MATCH_COMMON_NEIGHBOURS =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{dst}}) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match common neighbours with degree.
   */
  private static final String MATCH_COMMON_NEIGHBOURS_WITH_DEGREE =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{dst}}) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to match common neighbours within distance.
   */
  private static final String MATCH_COMMON_NEIGHBOURS_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)-[:REAL*1..%d]-(u2:Person {id:{dst}}) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match common neighbours with degree within distance.
   */
  private static final String MATCH_COMMON_NEIGHBOURS_WITH_DEGREE_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)-[:REAL*1..%d]-(u2:Person {id:{dst}}) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to check the existence of the extremes.
   */
  private static final String CHECK_EXTREMES =
      "OPTIONAL MATCH (u1:Person {id:{src}}) " +
          "OPTIONAL MATCH (u2:Person {id:{dst}}) " +
          "WITH u1,u2 " +
          "OPTIONAL MATCH (u1)-[r:REAL]-(u2) " +
          "RETURN u1 IS NOT NULL AS src,u2 IS NOT NULL AS dst,r IS NOT NULL AS arc";

  /**
   * Query to find pairs of unlinked nodes to update, with single node insertion.
   */
  private static final String PAIRS_TO_UPDATE =
      "MATCH (x:Person {id:{x}})-[:REAL*2]-(n:Person) " +
          "WHERE NOT (x)-[:REAL]-(n) " +
          "RETURN DISTINCT [x.id,n.id] AS pair " +
          "UNION ALL " +
          "MATCH (n1:Person)-[:REAL*2]-(x:Person {id:{x}})-[:REAL*2]-(n2:Person) " +
          "WHERE id(n1) > id(n2) AND NOT (x)-[:REAL]-(n1) AND NOT (x)-[:REAL]-(n2) " +
          "RETURN DISTINCT [n1.id,n2.id] as pair";

  /**
   * Query to find pairs of unlinked nodes to update, with double node insertion.
   */
  private static final String PAIRS_TO_UPDATE_TWICE =
      "MATCH (x:Person {id:{x}})-[:REAL*2]-(n:Person) " +
          "WHERE NOT (x)-[:REAL]-(n) " +
          "RETURN DISTINCT [x.id,n.id] AS pair " +
          "UNION ALL " +
          "MATCH (n1:Person)-[:REAL*2]-(x:Person {id:{x}})-[:REAL*2]-(n2:Person) " +
          "WHERE id(n1) > id(n2) AND NOT (x)-[:REAL]-(n1) AND NOT (x)-[:REAL]-(n2) " +
          "RETURN DISTINCT [n1.id,n2.id] as pair " +
          "UNION ALL " +
          "MATCH (y:Person {id:{y}})-[:REAL*2]-(n:Person) " +
          "WHERE NOT (y)-[:REAL]-(n) " +
          "RETURN DISTINCT [y.id,n.id] AS pair " +
          "UNION ALL " +
          "MATCH (n1:Person)-[:REAL*2]-(y:Person {id:{y}})-[:REAL*2]-(n2:Person) " +
          "WHERE id(n1) > id(n2) AND NOT (y)-[:REAL]-(n1) AND NOT (y)-[:REAL]-(n2) " +
          "RETURN DISTINCT [n1.id,n2.id] as pair";

  /**
   * Query to find pairs of unlinked nodes to update, with single node insertion.
   */
  private static final String PAIRS_TO_UPDATE_WITHIN_DISTANCE =
      "MATCH (x:Person {id:{x}})-[:REAL*%d]-(n:Person) " +
          "WHERE NOT (x)-[:REAL*%d]-(n) " +
          "RETURN DISTINCT [x.id,n.id] AS pair " +
          "UNION ALL " +
          "MATCH (n1:Person)-[:REAL*%d]-(x:Person {id:{x}})-[:REAL*%d]-(n2:Person) " +
          "WHERE id(n1) > id(n2) AND NOT (x)-[:REAL*%d]-(n1) AND NOT (x)-[:REAL*%d]-(n2) " +
          "RETURN DISTINCT [n1.id,n2.id] as pair";

  /**
   * Query to find pairs of unlinked nodes to update, with double node insertion.
   */
  private static final String PAIRS_TO_UPDATE_TWICE_WITHIN_DISTANCE =
      "MATCH (x:Person {id:{x}})-[:REAL*%d]-(n:Person) " +
          "WHERE NOT (x)-[:REAL*%d]-(n) " +
          "RETURN DISTINCT [x.id,n.id] AS pair " +
          "UNION ALL " +
          "MATCH (n1:Person)-[:REAL*%d]-(x:Person {id:{x}})-[:REAL*%d]-(n2:Person) " +
          "WHERE id(n1) > id(n2) AND NOT (x)-[:REAL*%d]-(n1) AND NOT (x)-[:REAL*%d]-(n2) " +
          "RETURN DISTINCT [n1.id,n2.id] as pair " +
          "UNION ALL " +
          "MATCH (y:Person {id:{y}})-[:REAL*%d]-(n:Person) " +
          "WHERE NOT (y)-[:REAL*%d]-(n) " +
          "RETURN DISTINCT [y.id,n.id] AS pair " +
          "UNION ALL " +
          "MATCH (n1:Person)-[:REAL*%d]-(y:Person {id:{y}})-[:REAL*%d]-(n2:Person) " +
          "WHERE id(n1) > id(n2) AND NOT (y)-[:REAL*%d]-(n1) AND NOT (y)-[:REAL*%d]-(n2) " +
          "RETURN DISTINCT [n1.id,n2.id] as pair";

  /**
   * Query to find common neighborhood and related details for score formulas.
   */
  private static final String GAMMA_INTERSECTION =
      "MATCH (u1:Person {id:{x}})-[r1:REAL]-(n:Person)-[r2:REAL]-(u2:Person {id:{y}}) " +
          "WITH DISTINCT n,r1,r2 " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg, (r1.weight+r2.weight) AS weight, SUM(r.weight) AS weightTot";

  /**
   * Query to find common nodes at fixed distance and related details for score formulas.
   */
  private static final String H_INTERSECTION =
      "MATCH (u1:Person {id:{x}})-[:REAL*%d]-(n:Person)-[:REAL*%d]-(u2:Person {id:{y}}) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to remove all nodes on Neo4J
   */
  private static final String EMPYTING =
          "MATCH (n:Person) DETACH DELETE n";

  /**
   * Opens a NEO4J connection.
   * @param dbconf the configuration to connect to Neo4J.
   * @return a open NEO4J driver.
   */
  public static Driver open(DbConfiguration dbconf) {
    AuthToken auth = AuthTokens.basic(dbconf.getUsername(), dbconf.getPassword());
    Config config = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE ).toConfig();
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
    Config config = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE ).toConfig();
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

    switch (type) {
      case REAL: session.run(SAVE_LINK_REAL_AVERAGE, params); break;
      case POTENTIAL: session.run(SAVE_LINK_POTENTIAL, params); break;
      case HIDDEN: session.run(SAVE_LINK_HIDDEN, params); break;
      default: break;
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

    switch (type) {
      case REAL: session.run(SAVE_LINK_REAL_EWMA, params); break;
      case POTENTIAL: session.run(SAVE_LINK_POTENTIAL, params); break;
      case HIDDEN: session.run(SAVE_LINK_HIDDEN, params); break;
      default: break;
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

}
