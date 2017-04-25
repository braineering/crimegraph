/*
  The MIT License (MIT)

  Copyright (c) 2017 Giacomo Marciani

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

/**
 * Collection of Neo4J queries.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class Neo4JQueries {

  /**
   * Query to create a new real link (AVERAGE).
   */
  public static final String SAVE_LINK_REAL_AVERAGE =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:REAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.num=1,r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight=(r.weight*r.num+{weight})/(r.num+1),r.num=r.num+1,r.updated=timestamp() " +
          "WITH u1,u2 " +
          "MATCH (u1)-[r2]-(u2) " +
          "WHERE NOT type(r2) = 'REAL' " +
          "DELETE r2";

  /**
   * Query to create a new real link (EWMA).
   */
  public static final String SAVE_LINK_REAL_EWMA =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:REAL]-(u2) " +
          "ON CREATE SET r.weight={weight},r.num=1,r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight=({weight}*{ewma} + r.weight*(1-{ewma})),r.num=(r.num+1),r.updated=timestamp() " +
          "WITH u1,u2 " +
          "MATCH (u1)-[r2]-(u2) " +
          "WHERE NOT type(r2) = 'REAL' " +
          "DELETE r2";

  /**
   * Query to remove a link.
   */
  public static final String REMOVE_LINK =
      "MATCH (x:Person {id:{x}})-[r]-(y:Person {id:{y}}) " +
          "WHERE type(r) = '{type}' " +
          "DELETE r";

  /**
   * Query to match neighbours.
   */
  public static final String MATCH_NEIGHBOURS =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match neighbours and degree.
   */
  public static final String MATCH_NEIGHBOURS_WITH_DEGREE =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to match neighbours within maximum distance.
   */
  public static final String MATCH_NEIGHBOURS_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match neighbours with degree within maximum distance.
   */
  public static final String MATCH_NEIGHBOURS_WITH_DEGREE_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to match common neighbours.
   */
  public static final String MATCH_COMMON_NEIGHBOURS =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{dst}}) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match common neighbours with degree.
   */
  public static final String MATCH_COMMON_NEIGHBOURS_WITH_DEGREE =
      "MATCH (u1:Person {id:{src}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{dst}}) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to match common neighbours within distance.
   */
  public static final String MATCH_COMMON_NEIGHBOURS_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)-[:REAL*1..%d]-(u2:Person {id:{dst}}) " +
          "RETURN DISTINCT n.id AS id";

  /**
   * Query to match common neighbours with degree within distance.
   */
  public static final String MATCH_COMMON_NEIGHBOURS_WITH_DEGREE_WITHIN_DISTANCE =
      "MATCH (u1:Person {id:{src}})-[:REAL*1..%d]-(n:Person)-[:REAL*1..%d]-(u2:Person {id:{dst}}) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to check the existence of the extremes.
   */
  public static final String CHECK_EXTREMES =
      "OPTIONAL MATCH (u1:Person {id:{src}}) " +
          "OPTIONAL MATCH (u2:Person {id:{dst}}) " +
          "WITH u1,u2 " +
          "OPTIONAL MATCH (u1)-[r:REAL]-(u2) " +
          "RETURN u1 IS NOT NULL AS src,u2 IS NOT NULL AS dst,r IS NOT NULL AS arc";

  /**
   * Query to find pairs of unlinked nodes to update, with single node insertion.
   */
  public static final String PAIRS_TO_UPDATE =
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
  public static final String PAIRS_TO_UPDATE_TWICE =
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
  public static final String PAIRS_TO_UPDATE_WITHIN_DISTANCE =
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
  public static final String PAIRS_TO_UPDATE_TWICE_WITHIN_DISTANCE =
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
  public static final String GAMMA_INTERSECTION =
      "MATCH (u1:Person {id:{x}})-[r1:REAL]-(n:Person)-[r2:REAL]-(u2:Person {id:{y}}) " +
          "WITH DISTINCT n,r1,r2 " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg, (r1.weight+r2.weight) AS weight, SUM(r.weight) AS weightTot";

  /**
   * Query to find common nodes at fixed distance and related details for score formulas.
   */
  public static final String H_INTERSECTION =
      "MATCH (u1:Person {id:{x}})-[:REAL*%d]-(n:Person)-[:REAL*%d]-(u2:Person {id:{y}}) " +
          "WITH DISTINCT n " +
          "MATCH (n)-[r:REAL]-() " +
          "RETURN n.id AS id,COUNT(r) AS deg";

  /**
   * Query to remove all nodes on Neo4J
   */
  public static final String EMPYTING =
      "MATCH (n:Person) DETACH DELETE n ";

  /**
   * Query to count the common neighbours.
   */
  public static final String COUNT_COMMON_NEIGHBOURS =
      "MATCH (u1:Person {id:{x}})-[:REAL]-(n:Person)-[:REAL]-(u2:Person {id:{y}}) " +
          "RETURN COUNT(DISTINCT n.id) AS cn ";

  /**
   * Query to find the union on the neighborhood between
   */
  public static final String COUNT_GAMMA_UNION=
      "MATCH (u1:Person {id:{x}})-[r1:REAL]-(n:Person) " +
          "WITH collect(DISTINCT n.id) as set1 "   +
          "MATCH (u2:Person {id:{y}})-[r2:REAL]-(n2:Person) " +
          "WITH collect(DISTINCT n2.id) as set2, set1 "     +
          "WITH set1 + set2 as BOTH "+
          "UNWIND BOTH as res "+
          "RETURN COUNT(DISTINCT res) as cardinality ";

  /**
   * Query to count the neighbors of node x
   */
  public static final String NODE_DEGREE=
      "MATCH (u1:Person {id:{src}})-[r:REAL]-(n:Person) "+
          "RETURN COUNT(DISTINCT r) as degree ";

  /**
   * Query to create a new general link.
   */
  public static final String SAVE_LINK_MINED_GENERAL =
      "MERGE (u1:Person {id:{src}}) " +
          "MERGE (u2:Person {id:{dst}}) " +
          "MERGE (u1)-[r:%s]-(u2) " +
          "ON CREATE SET r.weight={weight},r.created=timestamp(),r.updated=r.created " +
          "ON MATCH SET r.weight={weight},r.updated=timestamp()";
}
