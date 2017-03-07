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

package com.acmutv.crimegraph.core.operator;

import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.Link;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.io.IOException;

/**
 * A map operator that saves the flowing links to Neo4J.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class LinkStore extends RichMapFunction<Link,Link> {

  private static final long serialVersionUID = 1L;

  /**
   * The hostname of the NEO4J instance.
   */
  private String hostname;

  /**
   * The username of the NEO4J instance.
   */
  private String username;

  /**
   * The password of the NEO4J instance.
   */
  private String password;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  /**
   * The NEO4J session.
   */
  private Session session;

  /**
   * Constructs a new sink to write {@link Link} on a NEO4J instance.
   * @param hostname the hostname of the NEO4J instance.
   * @param username the username of the NEO4J instance.
   * @param password the password of the NEO4J instance.
   */
  public LinkStore(String hostname, String username, String password) {
    this.hostname = hostname;
    this.username = username;
    this.password = password;
  }

  /**
   * The mapping method. Takes an element from the input data set and transforms
   * it into exactly one element.
   *
   * @param value The input value.
   * @return The transformed value
   * @throws Exception This method may throw exceptions. Throwing an exception will cause the operation
   *                   to fail and may trigger recovery.
   */
  @Override
  public Link map(Link value) throws Exception {
    Neo4JManager.save(this.session, value);
    return value;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.hostname, this.username, this.password);
    this.session = driver.session();
  }

  @Override
  public void close() throws IOException {
    Neo4JManager.close(this.session, this.driver);
  }
}
