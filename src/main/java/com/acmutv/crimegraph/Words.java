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
import com.acmutv.crimegraph.core.connector.ElasticsearchSink;
import com.acmutv.crimegraph.core.operator.LineSplitter;
import com.acmutv.crimegraph.core.sink.WordsElasticSinkFunction;
import com.acmutv.crimegraph.core.tuple.WordCount;
import com.acmutv.crimegraph.tool.runtime.RuntimeManager;
import com.acmutv.crimegraph.tool.runtime.ShutdownHook;
import com.acmutv.crimegraph.ui.CliService;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The app word-point for {@code Words} application.
 * Before starting the application, it is necessary to open the socket, running
 * {@code $> ncat 127.0.0.1 9000 -l}
 * and start typing words.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see AppConfigurationService
 * @see RuntimeManager
 */
public class Words {

  private static final Logger LOGGER = LoggerFactory.getLogger(Words.class);

  /**
   * The app main method, executed when the program is launched.
   * @param args the command line arguments.
   */
  public static void main(String[] args) throws Exception {

    CliService.printSplash();

    CliService.handleArguments(args);

    AppConfiguration config = AppConfigurationService.getConfigurations();

    RuntimeManager.registerShutdownHooks(new ShutdownHook());

    LOGGER.info("Connecting to data stream on {}:{}...",
        config.getDataHostname(), config.getDataPort());

    LOGGER.info("Connecting to Elastic Search instance on {}:{}...",
        config.getDataHostname(), config.getDataPort());

    final StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<String> text =
        env.socketTextStream(config.getDataHostname(), config.getDataPort(), "\n");

    DataStream<Tuple2<String,Integer>> counts =
        text.flatMap(new LineSplitter())
            .keyBy(0)
            .sum(1);

    Map<String, String> esConfig = new HashMap<>();
    Map<String, String> sinkConfig = new HashMap<>();
    sinkConfig.put("cluster.name", config.getElasticClusterName());
    sinkConfig.put("bulk.flush.max.actions", "1");

    List<InetSocketAddress> transports = new ArrayList<>();
    transports.add(new InetSocketAddress(config.getElasticHostname(), config.getElasticPort()));

    counts.addSink(new ElasticsearchSink<>(esConfig, sinkConfig, transports, new WordsElasticSinkFunction()));

    env.execute("Words on socket");
  }
}
