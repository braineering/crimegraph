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
import com.acmutv.crimegraph.config.serial.AppConfigurationYamlMapper;
import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.db.Neo4JManager;
import com.acmutv.crimegraph.core.keyer.NodePairScoreKeyer;
import com.acmutv.crimegraph.core.operator.*;
import com.acmutv.crimegraph.core.sink.HiddenSink;
import com.acmutv.crimegraph.core.sink.PotentialSink;
import com.acmutv.crimegraph.core.source.KafkaProperties;
import com.acmutv.crimegraph.core.source.LinkKafkaSource;
import com.acmutv.crimegraph.core.source.SourceType;
import com.acmutv.crimegraph.core.tuple.*;

import com.acmutv.crimegraph.core.source.LinkSource;
import com.acmutv.crimegraph.tool.runtime.RuntimeManager;
import com.acmutv.crimegraph.ui.CliService;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SplitStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.nio.file.FileSystems;

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
    System.out.println(new AppConfigurationYamlMapper().writeValueAsString(config));
    final SourceType source = config.getSource();
    final DbConfiguration dbconf = config.getNeo4jConfig();

    /* ENVIRONMENT */
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setParallelism(config.getParallelism());
    Neo4JManager.empyting(dbconf);

    /* TOPOLOGY */
    DataStream<Link> links;
    if (source.equals(SourceType.KAFKA)) {
      final KafkaProperties props = config.getKafkaProperties();
      final String topic = config.getTopic();
      links = env.addSource(new LinkKafkaSource(topic, props));
    } else {
      final String dataset = FileSystems.getDefault().getPath(
          System.getenv("FLINK_HOME"), config.getDataset()
      ).toAbsolutePath().toString();
      links = env.addSource(new LinkSource(dataset));
    }

    DataStream<NodePair> updates = links.flatMap(new GraphUpdate(dbconf)).shuffle();

    DataStream<NodePairScore> scores = updates.flatMap(new ScoreCalculator(dbconf))
        .keyBy(new NodePairScoreKeyer());

    SplitStream<NodePairScore> split = scores.split(new ScoreSplitter());

    DataStream<NodePairScore> hiddenScores = split.select(ScoreType.HIDDEN.name());

    DataStream<NodePairScore> potentialScores = split.select(ScoreType.POTENTIAL.name());

    hiddenScores.addSink(new HiddenSink(dbconf, config.getHiddenThreshold()));

    potentialScores.addSink(new PotentialSink(dbconf, config.getPotentialThreshold()));

    /* EXECUTION */
    env.execute("Crimegraph");
  }

}
