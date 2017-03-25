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

import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.*;

import static com.acmutv.crimegraph.Common.HOSTNAME;
import static com.acmutv.crimegraph.Common.PASSWORD;
import static com.acmutv.crimegraph.Common.USERNAME;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * JUnit test suite for {@link Neo4JManager}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Neo4JManager
 */
public class Neo4JManagerTest {

  private static Driver DRIVER;

  private static final String MATCH =
      "MATCH (a:Person {id:{src}})-[r:%s {weight:{weight}}]->(b:Person {id:{dst}}) " +
          "RETURN a.id as src, b.id as dst, r.weight as weight";

  private static final List<Link> DATA = new ArrayList<Link>(){{
    add(new Link(1,2,10.0, LinkType.REAL));
    add(new Link(1,3,10.0, LinkType.REAL));
    add(new Link(2,3,10.0, LinkType.REAL));
    add(new Link(2,4,10.0, LinkType.REAL));
    add(new Link(3,6,10.0, LinkType.REAL));
    add(new Link(4,5,10.0, LinkType.REAL));
    add(new Link(6,7,10.0, LinkType.REAL));
  }};

  @BeforeClass
  public static void init() {
    DbConfiguration dbconf = new DbConfiguration(HOSTNAME, USERNAME, PASSWORD);
    boolean neo4jActive = true;
    try {
      Neo4JManager.empyting(dbconf);
    } catch (Exception exc) {
      neo4jActive = false;
    }
    Assume.assumeTrue(neo4jActive);
    DRIVER = Neo4JManager.open(dbconf);

    Session session = DRIVER.session();

    for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    for (Link link : DATA) {
      String query = String.format(MATCH, link.f3.name());
      Value params = parameters("src", link.f0, "dst", link.f1, "weight", link.f2);
      StatementResult result = session.run(query, params);
      Assert.assertTrue(result.hasNext());
      Record record = result.next();
      Long src = record.get("src").asLong();
      Long dst = record.get("dst").asLong();
      Double weight = record.get("weight").asDouble();
      Assume.assumeTrue(link.f0.equals(src));
      Assume.assumeTrue(link.f1.equals(dst));
      Assume.assumeTrue(link.f2.equals(weight));
    }

    session.close();
  }

  @AfterClass
  public static void deinit() {
    Session session = DRIVER.session();
    Neo4JManager.empyting(session);
    session.close();
    DRIVER.close();
  }

  /**
   * Tests creation of real/potential and hidden links on NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  /*
  @Test
  public void test_save() throws Exception {
    Session session = DRIVER.session();

    for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    for (Link link : DATA) {
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
  */

  /**
   * Tests matching of neighbours from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_neighbours() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Long> actual = Neo4JManager.neighbours(session, 1);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    expected.add(3L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests matching of neighbours with degree from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_neighboursWithDegree() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Tuple2<Long,Long>> actual = Neo4JManager.neighboursWithDegree(session, 1);
    Set<Tuple2<Long,Long>> expected = new HashSet<>();
    expected.add(new Tuple2<>(2L,3L));
    expected.add(new Tuple2<>(3L,3L));
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests matching of neighbours within upper bound distance from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_neighboursWithinDistance() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Long> actual = Neo4JManager.neighboursWithinDistance(session, 1, 2);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    expected.add(3L);
    expected.add(4L);
    expected.add(6L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests matching of neighbours with degree within upper bound distance from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_neighboursWithDegreeWithinDistance() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Tuple2<Long,Long>> actual = Neo4JManager.neighboursWithDegreeWithinDistance(session, 1, 2);
    Set<Tuple2<Long,Long>> expected = new HashSet<>();
    expected.add(new Tuple2<>(2L,3L));
    expected.add(new Tuple2<>(3L,3L));
    expected.add(new Tuple2<>(4L,2L));
    expected.add(new Tuple2<>(6L,2L));
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests common neighbours from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_commonNeighbours() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Long> actual = Neo4JManager.commonNeighbours(session, 1, 4);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests matching of common neighbours with degree from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_commonNeighboursWithDegree() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Tuple2<Long,Long>> actual = Neo4JManager.commonNeighboursWithDegree(session, 1, 4);
    Set<Tuple2<Long,Long>> expected = new HashSet<>();
    expected.add(new Tuple2<>(2L,3L));
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests matching of common neighbours within upper bound distance from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_commonNeighboursWithinDistance() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Long> actual = Neo4JManager.commonNeighboursWithinDistance(session, 1, 4, 2);
    Set<Long> expected = new HashSet<>();
    expected.add(2L);
    expected.add(3L);
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests matching of common neighbours with degree within upper bound distance from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_commonNeighboursWithDegreeWithinDistance() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Set<Tuple2<Long,Long>> actual = Neo4JManager.commonNeighboursWithDegreeWithinDistance(session, 1, 4, 2);
    Set<Tuple2<Long,Long>> expected = new HashSet<>();
    expected.add(new Tuple2<>(2L,3L));
    expected.add(new Tuple2<>(3L,3L));
    Assert.assertEquals(expected, actual);

    session.close();
  }

  /**
   * Tests checking of extremes existence from NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_checkExtremes() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Tuple2<Long,Long>,Tuple3<Boolean,Boolean,Boolean>> expectedMap = new HashMap<>();
    expectedMap.put(new Tuple2<>(1L,2L), new Tuple3<>(true, true, true));
    expectedMap.put(new Tuple2<>(1L,4L), new Tuple3<>(true, true, false));
    expectedMap.put(new Tuple2<>(1L,10L), new Tuple3<>(true, false, false));
    expectedMap.put(new Tuple2<>(10L,1L), new Tuple3<>(false, true, false));
    expectedMap.put(new Tuple2<>(100L,101L), new Tuple3<>(false, false, false));
    for (Tuple2<Long,Long> key : expectedMap.keySet()) {
      Tuple3<Boolean,Boolean,Boolean> actual = Neo4JManager.checkExtremes(session, key.f0, key.f1);
      Tuple3<Boolean,Boolean,Boolean> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }

  /**
   * Tests checking of pairs to update, with single node insertion.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_pairsToUpdate() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Long,Set<Tuple2<Long,Long>>> expectedMap = new HashMap<>();
    expectedMap.put(1L, new HashSet<Tuple2<Long,Long>>(){{
      add(new Tuple2<>(1L,4L));
      add(new Tuple2<>(1L,6L));
      add(new Tuple2<>(4L,6L));
    }});
    for (Long key : expectedMap.keySet()) {
      Set<Tuple2<Long, Long>> actual = Neo4JManager.pairsToUpdate(session, key);
      Set<Tuple2<Long, Long>> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }

  /**
   * Tests checking of pairs to update, with double node insertion.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_pairsToUpdateTwice() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Tuple2<Long,Long>,Set<Tuple2<Long,Long>>> expectedMap = new HashMap<>();
    expectedMap.put(new Tuple2<>(1L,2L), new HashSet<Tuple2<Long,Long>>(){{
      add(new Tuple2<>(1L,4L));
      add(new Tuple2<>(1L,6L));
      add(new Tuple2<>(4L,6L));
      add(new Tuple2<>(2L,6L));
      add(new Tuple2<>(2L,5L));
      add(new Tuple2<>(5L,6L));
    }});
    for (Tuple2<Long,Long> key : expectedMap.keySet()) {
      Set<Tuple2<Long, Long>> actual = Neo4JManager.pairsToUpdateTwice(session, key.f0, key.f1);
      Set<Tuple2<Long, Long>> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }


  /**
   * Tests checking of pairs to update, with single node insertion.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_pairsToUpdateWithinDistance() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Long,Set<Tuple2<Long,Long>>> expectedMap = new HashMap<>();
    expectedMap.put(1L, new HashSet<Tuple2<Long,Long>>(){{
      add(new Tuple2<>(1L,4L));
      add(new Tuple2<>(1L,6L));
      add(new Tuple2<>(4L,6L));
    }});
    for (Long key : expectedMap.keySet()) {
      Set<Tuple2<Long, Long>> actual = Neo4JManager.pairsToUpdateWithinDistance(session, key, 1);
      Set<Tuple2<Long, Long>> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }

  /**
   * Tests checking of pairs to update, with double node insertion.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_pairsToUpdateTwiceWithinDistance() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Tuple2<Long,Long>,Set<Tuple2<Long,Long>>> expectedMap = new HashMap<>();
    expectedMap.put(new Tuple2<>(1L,2L), new HashSet<Tuple2<Long,Long>>(){{
      add(new Tuple2<>(1L,4L));
      add(new Tuple2<>(1L,6L));
      add(new Tuple2<>(4L,6L));
      add(new Tuple2<>(2L,6L));
      add(new Tuple2<>(2L,5L));
      add(new Tuple2<>(5L,6L));
    }});
    for (Tuple2<Long,Long> key : expectedMap.keySet()) {
      Set<Tuple2<Long, Long>> actual = Neo4JManager.pairsToUpdateTwiceWithinDistance(session, key.f0, key.f1, 1);
      Set<Tuple2<Long, Long>> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }

  /**
   * Tests finding of Gamma-intersection and details related to score formulas.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_gammaIntersection() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Tuple2<Long,Long>,Set<Tuple4<Long,Long,Double,Double>>> expectedMap = new HashMap<>();
    expectedMap.put(new Tuple2<>(2L,6L), new HashSet<Tuple4<Long,Long,Double,Double>>(){{
      add(new Tuple4<>(3L, 3L, 20.0, 30.0));
    }});
    for (Tuple2<Long,Long> key : expectedMap.keySet()) {
      Set<Tuple4<Long,Long,Double,Double>> actual = Neo4JManager.gammaIntersection(session, key.f0, key.f1);
      Set<Tuple4<Long,Long,Double,Double>> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }

  /**
   * Tests finding of H-intersection and details related to score formulas.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_hIntersection() throws Exception {
    Session session = DRIVER.session();

    //for (Link link : DATA) Neo4JManager.save(session, link, 0.5);

    // Check
    Map<Tuple3<Long,Long,Long>,Set<Tuple2<Long,Long>>> expectedMap = new HashMap<>();
    expectedMap.put(new Tuple3<>(3L,5L,1L), new HashSet<Tuple2<Long,Long>>(){{}});
    expectedMap.put(new Tuple3<>(3L,5L,2L), new HashSet<Tuple2<Long,Long>>(){{
      add(new Tuple2<>(2L,3L));
    }});
    for (Tuple3<Long,Long,Long> key : expectedMap.keySet()) {
      Set<Tuple2<Long,Long>> actual = Neo4JManager.hIntersection(session, key.f0, key.f1, key.f2);
      Set<Tuple2<Long,Long>> expected = expectedMap.get(key);
      Assert.assertEquals(expected, actual);
    }

    session.close();
  }
}
