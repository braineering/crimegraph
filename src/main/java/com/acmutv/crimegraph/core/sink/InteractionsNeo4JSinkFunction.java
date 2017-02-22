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

package com.acmutv.crimegraph.core.sink;

import com.acmutv.crimegraph.core.tuple.Interaction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * A simple sink function based on ElasticSearch.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class InteractionsNeo4JSinkFunction extends RichSinkFunction<Interaction> {

  //private static final Logger LOGGER = LogManager.getLogger(InteractionsNeo4JSinkFunction.class);

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
   * Constructs a new sink to write {@link Interaction} on a NEO4J instance.
   * @param hostname the hostname of the NEO4J instance.
   * @param username the username of the NEO4J instance.
   * @param password the password of the NEO4J instance.
   */
  public InteractionsNeo4JSinkFunction(String hostname, String username, String password) {
    this.hostname = hostname;
    this.username = username;
    this.password = password;
  }

  private static final String CREATE =
      "CREATE (a:Person {id:{src}})-[:INTERACTION {weight:{weight}}]->(b:Person {id:{dst}})";

  @Override
  public void invoke(Interaction value) throws Exception {
    this.session.run(CREATE,
        parameters("src", value.f0, "dst", value.f1, "weight", value.f2)
    );
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = GraphDatabase.driver(this.hostname, AuthTokens.basic(this.username, this.password),
        Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE ).toConfig());
    this.session = driver.session();
  }

  @Override
  public void close() throws Exception {
    this.session.close();
    this.driver.close();
  }
}
