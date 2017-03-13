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
import com.acmutv.crimegraph.core.keyer.NodePairScoreKeyer;
import com.acmutv.crimegraph.core.operator.GraphUpdate;
import com.acmutv.crimegraph.core.operator.ScoreCalculatorTStepsWithWeights;
import com.acmutv.crimegraph.core.operator.ScoreSplitter;
import com.acmutv.crimegraph.core.sink.HiddenSink;
import com.acmutv.crimegraph.core.sink.PotentialSink;
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
@Deprecated
public class QuasiLocalAnalysisWithWeights {

  private static final String ANALYSIS = "Quasi-Local Analysis With Weights for Potential Metric";

  /**
   * The app main method, executed when the program is launched.
   * @param args the command line arguments.
   */
  public static void main(String[] args) throws Exception {

    CliService.printSplash();

    CliService.handleArguments(args);

    AppConfiguration config = AppConfigurationService.getConfigurations();

    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    final DbConfiguration dbconf = new DbConfiguration(
        config.getNeo4jHostname(), config.getNeo4jUsername(), config.getNeo4jPassword()
    );

    DataStream<Link> links = env.addSource(new LinkSource(config.getDataset()));

    DataStream<NodePair> updates = links.flatMap(new GraphUpdate(dbconf)).shuffle();

    DataStream<NodePairScore> scores = updates.flatMap(new ScoreCalculatorTStepsWithWeights(
        dbconf, config.getPotentialLocality(), config.getPotentialWeights())).keyBy(new NodePairScoreKeyer());

    SplitStream<NodePairScore> split = scores.split(new ScoreSplitter());

    DataStream<NodePairScore> hiddenScores = split.select(ScoreType.HIDDEN.name());

    DataStream<NodePairScore> potentialScores = split.select(ScoreType.POTENTIAL.name());

    hiddenScores.addSink(new HiddenSink(dbconf, config.getHiddenThreshold()));

    potentialScores.addSink(new PotentialSink(dbconf, config.getPotentialThreshold()));

    env.execute(ANALYSIS);
  }
}
