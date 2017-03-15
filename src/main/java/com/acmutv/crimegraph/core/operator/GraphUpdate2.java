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

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.NodePair;
import com.acmutv.crimegraph.core.tuple.UpdateType;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.util.Set;

/**
 * A simple source function based on Neo4j.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class GraphUpdate2 extends RichFlatMapFunction<Link, NodePair> {

  /**
   * The Neo4J configuration.
   */
  private DbConfiguration dbconfig;

  /**
   * The NEO4J driver.
   */
  private Driver driver;

  /**
   * The NEO4J session.
   */
  private Session session;

  /**
   * The locality degree for link detection.
   */
  private long hiddenLocality;

  /**
   * The locality degree for link prediction.
   */
  private long potentialLocality;

  /**
   * Constructs for load edge to write {@link Link} on a NEO4J instance.
   * @param dbconfig the Neo4J configuration.
   * @param hiddenLocality the locality degree for link detection.
   * @param potentialLocality the locality degree for link prediction.
   */
  public GraphUpdate2(DbConfiguration dbconfig, long hiddenLocality, long potentialLocality) {
    this.dbconfig = dbconfig;
    this.hiddenLocality = hiddenLocality;
    this.potentialLocality = potentialLocality;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void flatMap(Link value, Collector<NodePair> out){

    Tuple3<Boolean,Boolean,Boolean> check = Neo4JManager.checkExtremes(this.session,value.f0,value.f1);
    Neo4JManager.save(this.session, value);
    System.out.println("ARRIVED LINK: "+ value.toString());


    // if x in G, y in G, and (x,y) in G
    if(check.f0 && check.f1 && check.f2){
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdateTwice(this.session, value.f0, value.f1);
      for(Tuple2<Long,Long> pair : pairs ) {
        NodePair update = new NodePair(pair.f0,pair.f1, UpdateType.HIDDEN);
        out.collect(update);
      }
    }

    // if x in G, y in G, and (x,y) NOT in G
    else if (check.f0 && check.f1 && !check.f2){
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdateTwice(this.session, value.f0, value.f1);
      for(Tuple2<Long,Long> pair : pairs ) {
        NodePair update = new NodePair(pair.f0,pair.f1, UpdateType.BOTH);
        System.out.println("NODE PAIR: "+ update.toString());
        out.collect(update);
      }
    }

    // if x NOT in G and y in G
    else if(!check.f0 && check.f1 && !check.f2){
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdate(this.session, value.f0);
      for(Tuple2<Long,Long> pair : pairs ) {
        NodePair update = new NodePair(pair.f0,pair.f1, UpdateType.BOTH);
        System.out.println("NODE PAIR: "+ update.toString());
        out.collect(update);
      }
    }

    // if x in G and y NOT in G
    else if(check.f0 && !check.f1 && !check.f2) {
      Set<Tuple2<Long,Long>> pairs = Neo4JManager.pairsToUpdate(this.session, value.f1);
      for(Tuple2<Long,Long> pair : pairs ) {
        NodePair update = new NodePair(pair.f0,pair.f1, UpdateType.BOTH);
        System.out.println("NODE PAIR: "+ update.toString());
        out.collect(update);
      }
    }
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    String hostname = this.dbconfig.getHostname();
    String username = this.dbconfig.getUsername();
    String password = this.dbconfig.getPassword();
    this.driver = Neo4JManager.open(hostname, username, password);
    this.session = driver.session();
  }

  @Override
  public void close() throws Exception {
    Neo4JManager.close(this.session, this.driver);
  }
}
