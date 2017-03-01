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
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Collections of NEO4J useful queries.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class Neo4JManager {

  /**
   * Query to create a new link.
   */
  private static final String CREATE_LINK =
      "CREATE (a:Person {id:{src}})-[:{type} {weight:{weight}}]->(b:Person {id:{dst}})";

  /**
   * Query to match a neighborhood intersection.
   */
  private static final String MATCH_NEIGHBORHOOD_INTERSECTION =
      "MATCH (a:Person {id:{src}})-[:INTERACTION]->(c:Person)<-[:INTERACTION]-(b:Person {id:{dst}}) RETURN c.id";

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
  public static void saveLink(Session session, Link link) {
    long src = link.f0;
    long dst = link.f1;
    LinkType type = link.f2;
    double weight = link.f3;

    Value params = parameters("src", src, "dst", dst, "type", type, "weight", weight);

    session.run(MATCH_NEIGHBORHOOD_INTERSECTION, params);
  }

  /**
   * Matches a neighborhood intersection.
   * @param session the NEO4J open session.
   * @param a the id of the first node.
   * @param b the id of the second node.
   */
  public static void matchNeighborhodIntersection(Session session, long a, long b) {
    Value params = parameters("src", a, "dst", b);
    session.run(MATCH_NEIGHBORHOOD_INTERSECTION, params);
  }
}
