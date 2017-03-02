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

package com.acmutv.crimegraph.core.sink;

import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.LinkType;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.driver.v1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * JUnit test suite for {@link LinkSink}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class Neo4jSinkTest {

  private static final String HOSTNAME = "bolt://localhost:7687";

  private static final String USERNAME = "neo4j";

  private static final String PASSWORD = "password";

  private static final String MATCH =
      "MATCH (a:Person {id:{src}})-[r:%s {weight:{weight}}]->(b:Person {id:{dst}}) " +
          "RETURN a.id as src, b.id as dst";

  /**
   * Tests creation of real/potential and hidden links on NEO4J.
   * @throws IOException when operator cannot be managed.
   */
  @Test
  public void test_create() throws Exception {
    LinkSink sink =
        new LinkSink(HOSTNAME, USERNAME, PASSWORD);

    List<Link> links = data();

    sink.open(null);

    for (Link link : links) sink.invoke(link);

    sink.close();

    // Check
    Driver driver = GraphDatabase.driver(HOSTNAME, AuthTokens.basic(USERNAME, PASSWORD),
        Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());
    Session session = driver.session();
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
    driver.close();
  }

  private static List<Link> data() {
    List<Link> data = new ArrayList<>();
    data.add(new Link(1,2,10.0, LinkType.REAL));
    data.add(new Link(1,3,20.0, LinkType.REAL));
    data.add(new Link(1,4,30.0, LinkType.REAL));
    data.add(new Link(2,3,50.0, LinkType.POTENTIAL));
    data.add(new Link(3,4,100.0, LinkType.HIDDEN));
    return data;
  }
}
