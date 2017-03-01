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
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.driver.v1.*;

import java.io.IOException;

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

  private static final String CREATE =
      "CREATE (a:Person {id:'{src}'})-[:INTERACTION {weight:{weight}}]->(b:Person {id:'{dst}'})";

  private static final String MATCH =
      "MATCH (a:Person {id:{src}})-[r:INTERACTION {weight:{weight}}]->(b:Person {id:{dst}}) " +
          "RETURN a.id as src, b.id as dst, r.weight as weight";

  /**
   * Tests creation of {@link Link} on NEO4J.
   * @throws IOException
   */
  @Test
  public void create() throws Exception {
    LinkSink sink =
        new LinkSink(HOSTNAME, USERNAME, PASSWORD);

    Link link = new Link(1, 2, 20);

    sink.open(null);

    sink.invoke(link);

    sink.close();

    // Check
    Driver driver = GraphDatabase.driver(HOSTNAME, AuthTokens.basic(USERNAME, PASSWORD),
        Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE ).toConfig());
    Session session = driver.session();
    StatementResult result = session.run(MATCH,
        parameters("src", link.f0, "dst", link.f1, "weight", link.f2)
    );
    Assert.assertTrue(result.hasNext());
    Record record = result.next();
    long src = record.get("src").asLong();
    long dst = record.get("dst").asLong();
    long weight = record.get("weight").asLong();
    Assert.assertEquals(1, src);
    Assert.assertEquals(2, dst);
    Assert.assertEquals(20, weight);
    session.close();
    driver.close();
  }
}
