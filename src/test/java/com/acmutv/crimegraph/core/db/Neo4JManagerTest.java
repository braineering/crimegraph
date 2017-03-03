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

package com.acmutv.crimegraph.core.db;

import com.acmutv.crimegraph.core.sink.LinkSink;
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * JUnit test suite for {@link Neo4JManager}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Neo4JManager
 */
public class Neo4JManagerTest {

  private static final String HOSTNAME = "bolt://localhost:7687";

  private static final String USERNAME = "neo4j";

  private static final String PASSWORD = "password";

  private static Driver DRIVER;

  private static Config CONFIG = Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig();

  private static final String MATCH =
      "MATCH (a:Person {id:{src}})-[r:%s {weight:{weight}}]->(b:Person {id:{dst}}) " +
          "RETURN a.id as src, b.id as dst";

  @BeforeClass
  public static void init() {
    DRIVER = GraphDatabase.driver(HOSTNAME, AuthTokens.basic(USERNAME, PASSWORD), CONFIG);
  }

  @AfterClass
  public static void deinit() {
    DRIVER.close();
  }

  /**
   * Tests creation of real/potential and hidden links on NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_create() throws Exception {
    Session session = DRIVER.session();

    List<Link> links = data();

    for (Link link : links) Neo4JManager.saveLink(session, link);

    // Check
    for (Link link : links) {
      String query = String.format(MATCH, link.f3.name());
      Value params = parameters("src", link.f0, "dst", link.f1, "weight", link.f2);
      StatementResult result = session.run(query, params);
      Assert.assertTrue(result.hasNext());
      Record record = result.next();
      Long src = record.get("src").asLong();
      Long dst = record.get("dst").asLong();
      Assert.assertEquals(link.f0, src);
      Assert.assertEquals(link.f1, dst);
    }

    session.close();
  }

  /**
   * Tests common neighbours from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_commonNeighbours() throws Exception {
    Session session = DRIVER.session();

    List<Link> links = data();

    for (Link link : links) Neo4JManager.saveLink(session, link);

    // Check
    Set<Long> actual = Neo4JManager.matchCommonNeighbours(session, 1, 5);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests neighbours from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_neighbours() throws Exception {
    Session session = DRIVER.session();

    List<Link> links = data();

    for (Link link : links) Neo4JManager.saveLink(session, link);

    // Check
    Set<Long> actual = Neo4JManager.matchNeighbours(session, 1);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    expected.add(3L);
    expected.add(4L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests neighbours with upper bound distance from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_neighbours_distance() throws Exception {
    Session session = DRIVER.session();

    List<Link> links = data();

    for (Link link : links) Neo4JManager.saveLink(session, link);

    // Check
    Set<Long> actual = Neo4JManager.matchNeighbours(session, 1, 2);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    expected.add(3L);
    expected.add(4L);
    expected.add(5L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  private static List<Link> data() {
    List<Link> data = new ArrayList<>();
    data.add(new Link(1,2,10.0, LinkType.REAL));
    data.add(new Link(1,3,20.0, LinkType.REAL));
    data.add(new Link(2,3,30.0, LinkType.REAL));
    data.add(new Link(1,4,50.0, LinkType.REAL));
    data.add(new Link(2,5,100.0, LinkType.REAL));
    return data;
  }
}
