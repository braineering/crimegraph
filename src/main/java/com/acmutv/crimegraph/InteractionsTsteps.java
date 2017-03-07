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
import com.acmutv.crimegraph.core.keyer.NodePairScoreKeyer;
import com.acmutv.crimegraph.core.operator.*;
import com.acmutv.crimegraph.core.sink.ToStringSink;
import com.acmutv.crimegraph.core.source.LinkSource;
import com.acmutv.crimegraph.core.tuple.Link;
import com.acmutv.crimegraph.core.tuple.NodePair;
import com.acmutv.crimegraph.core.tuple.NodePairScore;
import com.acmutv.crimegraph.core.tuple.ScoreType;
import com.acmutv.crimegraph.tool.runtime.RuntimeManager;
import com.acmutv.crimegraph.ui.CliService;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The app word-point for {@code Interactions} application.
 * Before starting the application, it is necessary to open the socket, running
 * {@code $> ncat 127.0.0.1 9000 -l}
 * and start typing tuples.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see AppConfigurationService
 * @see RuntimeManager
 */
public class InteractionsTsteps {

  private static final Logger LOGGER = LoggerFactory.getLogger(InteractionsTsteps.class);

  /**
   * The app main method, executed when the program is launched.
   * @param args the command line arguments.
   */
  public static void main(String[] args) throws Exception {

    CliService.printSplash();

    CliService.handleArguments(args);

    AppConfiguration config = AppConfigurationService.getConfigurations();

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // source
    DataStream<Link> links = env.addSource(new LinkSource(config.getDataset()));

    // links upload to Neo4J
    DataStream<NodePair> pairstoupdate =
            links.flatMap(new LinkUpload(
                     config.getNeo4jHostname(), config.getNeo4jUsername(), config.getNeo4jPassword()
            )).shuffle();

    // Score Calculator
    DataStream<NodePairScore> pairsscore =
            pairstoupdate.flatMap(new ScoreCalculatorTSteps(
                    config.getNeo4jHostname(), config.getNeo4jUsername(), config.getNeo4jPassword(), distance
            )).keyBy(new NodePairScoreKeyer());

    SplitStream<NodePairScore> split = pairsscore.split(new ScoreSplitter());

    DataStream<NodePairScore> hidden = split.select(ScoreType.HIDDEN.name());
    DataStream<NodePairScore> potential = split.select(ScoreType.POTENTIAL.name());
    
    hidden.addSink(new ToStringSink<>("resources/hidden.out"));
    potential.addSink(new ToStringSink<>("resources/potential.out"));


    env.execute("Interactions to Neo4J");
  }

  private static Long distance = 3L;
}
