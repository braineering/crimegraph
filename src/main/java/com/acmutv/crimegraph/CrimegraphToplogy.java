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

package com.acmutv.crimegraph;

import com.acmutv.crimegraph.config.AppConfiguration;
import com.acmutv.crimegraph.config.AppConfigurationService;
import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.keyer.NodePairScoreKeyer;
import com.acmutv.crimegraph.core.metric.HiddenMetrics;
import com.acmutv.crimegraph.core.metric.PotentialMetrics;
import com.acmutv.crimegraph.core.operator.*;
import com.acmutv.crimegraph.core.sink.HiddenSink;
import com.acmutv.crimegraph.core.sink.PotentialSink;
import com.acmutv.crimegraph.core.tuple.*;

import com.acmutv.crimegraph.core.source.LinkSource;
import com.acmutv.crimegraph.tool.runtime.RuntimeManager;
import com.acmutv.crimegraph.ui.CliService;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * The app word-point for {@code CrimegraphToplogy} application.
 * Before starting the application, it is necessary to open the socket, running
 * {@code $> ncat 127.0.0.1 9000 -l}
 * and start typing tuples.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see AppConfigurationService
 * @see RuntimeManager
 */
public class CrimegraphToplogy {

  /**
   * The app main method, executed when the program is launched.
   * @param args the command line arguments.
   */
  public static void main(String[] args) throws Exception {

    CliService.printSplash();

    /* CONFIGURATION */
    CliService.handleArguments(args);

    AppConfiguration config = AppConfigurationService.getConfigurations();

    final String dataset = FileSystems.getDefault().getPath(
        System.getenv("FLINK_HOME"), config.getDataset()
    ).toAbsolutePath().toString();
    final DbConfiguration dbconf = new DbConfiguration(
        config.getNeo4jHostname(), config.getNeo4jUsername(), config.getNeo4jPassword()
    );
    final HiddenMetrics hiddenMetric = config.getHiddenMetric();
    final PotentialMetrics potentialMetric = config.getPotentialMetric();

    /* TOPOLOGY */
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // EMPYTING
    String hostname = dbconf.getHostname();
    String username = dbconf.getUsername();
    String password = dbconf.getPassword();
    Driver driver = Neo4JManager.open(hostname, username, password);
    Session session = driver.session();
    Neo4JManager.empyting(session);

    DataStream<Link> links = env.addSource(new LinkSource(dataset));

    DataStream<NodePair> updates = links.flatMap(
        new GraphUpdate(dbconf, config.getHiddenLocality(), config.getPotentialLocality())
    ).shuffle();

    DataStream<NodePairScore> scores = updates.flatMap(new ScoreCalculator(dbconf))
        .keyBy(new NodePairScoreKeyer());

    SplitStream<NodePairScore> split = scores.split(new ScoreSplitter());

    DataStream<NodePairScore> hiddenScores = split.select(ScoreType.HIDDEN.name());

    DataStream<NodePairScore> potentialScores = split.select(ScoreType.POTENTIAL.name());

    hiddenScores.addSink(new HiddenSink(dbconf, 0.5));

    potentialScores.addSink(new PotentialSink(dbconf, 0.5));

    env.execute("Crimegraph");
  }

}
