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
import com.acmutv.crimegraph.core.tuple.NodePair;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.neo4j.driver.v1.*;

import java.util.Set;

/**
 * A simple source function based on Neo4j.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class LinkUpload extends RichFlatMapFunction<Link, NodePair> {

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
   * Constructs for load edge to write {@link Link} on a NEO4J instance.
   * @param hostname the hostname of the NEO4J instance.
   * @param username the username of the NEO4J instance.
   * @param password the password of the NEO4J instance.
   */
  public LinkUpload(String hostname, String username, String password) {
    this.hostname = hostname;
    this.username = username;
    this.password = password;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void flatMap(Link value, Collector<NodePair> out){

    Link newLink;

    // Link's indexes sorting
    if(value.f0 > value.f1)
      newLink = new Link(value.f1,value.f0,value.f2);
    else
      newLink = value;

    Tuple3<Boolean,Boolean,Boolean> check = Neo4JManager.checkExtremes(this.session,newLink.f0,newLink.f1);

    //NodePair Structure: (long src, long dst, ScoreType type) types are: Hidden, Potential or Both

    // if x in G, y in G, and (x,y) in G
    if(check.f0 && check.f1 && check.f2){
      Neo4JManager.save(this.session, newLink);
      // emettere coppie (a,b) solo per calcolo degli hidden
    }

    // if x in G, y in G, and (x,y) NOT in G
    else if (check.f0 && check.f1 && !check.f2){
      Neo4JManager.save(this.session, newLink);
      // update x e y - emettere coppie (a,b) per il calcolo entrambi i tipi di score

    }

    // if x NOT in G and y in G
    else if(!check.f0 && check.f1 && !check.f2){
      Neo4JManager.save(this.session, newLink);
      //update x - emettere coppie (a,b) per il calcolo entrambi i tipi di score

    }

    // if x in G and y NOT in G
    else if(!check.f0 && check.f1 && !check.f2) {
      Neo4JManager.save(this.session, newLink);
      //update y - emettere coppie (a,b) per il calcolo di entrambi i tipi di score

    }

    // if x NOT in G and Y NOT in G
    else if(!check.f0 && !check.f1 && !check.f2){
      Neo4JManager.save(this.session, newLink);
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    this.driver = Neo4JManager.open(this.hostname, this.username, this.password);
    this.session = driver.session();
  }

  @Override
  public void close() throws Exception {
    Neo4JManager.close(this.session, this.driver);
  }
}
