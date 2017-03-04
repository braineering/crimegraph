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
   * Query to create a new real link.
   */
  private static final String SAVE_LINK_REAL =
      "MERGE (u1:Person {id:{src}}) MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:REAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.num=1,r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight=(r.weight*r.num+{weight})/(r.num+1),r.num=r.num+1,r.updated=timestamp()";

  /**
   * Query to create a new potential link.
   */
  private static final String SAVE_LINK_POTENTIAL =
      "MERGE (u1:Person {id:{src}}) MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:POTENTIAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight={weight},r.updated=timestamp()";

  /**
   * Query to create a new hidden link.
   */
  private static final String SAVE_LINK_HIDDEN =
      "MERGE (u1:Person {id:{src}}) MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:HIDDEN]-(u2) " +
          "ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight={weight},r.updated=timestamp()";

  /**
   * Query to match neighbours.
   */
  private static final String MATCH_NEIGHBOURS =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person) RETURN n.id AS id";

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
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person) RETURN DISTINCT n.id AS id";

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
      "OPTIONAL MATCH (u1:Person {id:{src}}),(u2:Person {id:{dst}}) " +
          "RETURN u1 IS NOT NULL AS src,u2 IS NOT NULL AS dst";

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
      case REAL: session.run(SAVE_LINK_REAL, params); break;
      case POTENTIAL: session.run(SAVE_LINK_POTENTIAL, params); break;
      case HIDDEN: session.run(SAVE_LINK_HIDDEN, params); break;
      default: break;
    }
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
   * Checks the existence of {@code a} and {@code b}.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   */
  public static Tuple2<Boolean,Boolean> checkExtremes(Session session, long a, long b) {
    Value params = parameters("src", a, "dst", b);
    StatementResult result = session.run(CHECK_EXTREMES, params);
    Tuple2<Boolean,Boolean> check = new Tuple2<>();
    if (result.hasNext()) {
      Record rec = result.next();
      Boolean src = rec.get("src").asBoolean();
      Boolean dst = rec.get("dst").asBoolean();
      check.setFields(src, dst);
    }
    return check;
  }


}
