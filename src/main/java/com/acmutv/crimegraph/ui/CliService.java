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

package com.acmutv.crimegraph.ui;

import com.acmutv.crimegraph.config.AppConfiguration;
import com.acmutv.crimegraph.config.AppManifest;
import com.acmutv.crimegraph.config.AppConfigurationService;
import com.acmutv.crimegraph.core.metric.HiddenMetrics;
import com.acmutv.crimegraph.core.metric.PotentialMetrics;
import com.acmutv.crimegraph.core.source.SourceType;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class realizes the Command Line Interface services.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class CliService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CliService.class);

  /**
   * Handles the command line arguments passed to the main method, according to {@link BaseOptions}.
   * Loads the configuration and returns the list of arguments.
   * @param argv the command line arguments passed to the main method.
   * @return the arguments list.
   * @see CommandLine
   * @see AppConfiguration
   * @throws IllegalArgumentException when there are errors in arguments.
   */
  public static List<String> handleArguments(String[] argv) throws IllegalArgumentException {
    LOGGER.trace("argv={}", Arrays.asList(argv));
    CommandLine cmd = getCommandLine(argv);

    /* OPTION: version */
    if (cmd.hasOption("version")) {
      LOGGER.trace("Detected option VERSION");
      printVersion();
      System.exit(0);
    }

    /* OPTION: help */
    if (cmd.hasOption("help")) {
      LOGGER.trace("Detected option HELP");
      printHelp();
      System.exit(0);
    }

    boolean configured = false;
    /* OPTION: config */
    if (cmd.hasOption("config")) {
      final String configPath = cmd.getOptionValue("config");
      LOGGER.trace("Detected option CONFIG with configPath={}", configPath);
      LOGGER.trace("Loading custom configuration {}", configPath);
      try {
        loadConfiguration(configPath);
        configured = true;
      } catch (IOException exc) {
        LOGGER.warn("Cannot load custom configuration");
      }
    }

    if (!configured) {
      final String configPath = AppConfigurationService.DEFAULT_CONFIG_FILENAME;
      LOGGER.trace("Loading local configuration {}", configPath);
      try {
        loadConfiguration(configPath);
        configured = true;
      } catch (IOException exc) {
        LOGGER.warn("Cannot load local configuration");
      }
    }

    if (!configured) {
      LOGGER.trace("Loading default configuration");
      AppConfigurationService.loadDefault();
    }

    AppConfiguration config = AppConfigurationService.getConfigurations();

    /* option: source */
    if (cmd.hasOption("source")) {
      final SourceType source = SourceType.valueOf(cmd.getOptionValue("source"));
      config.setSource(source);
    }

    /* option: dataset */
    if (cmd.hasOption("dataset")) {
      final String dataset = cmd.getOptionValue("dataset");
      config.setDataset(dataset);
    }

    /* option: kafkaTopic */
    if (cmd.hasOption("kafkaTopic")) {
      final String kafkaTopic = cmd.getOptionValue("kafkaTopic");
      config.setTopic(kafkaTopic);
    }

    /* option: kafkaBootstrap */
    if (cmd.hasOption("kafkaBootstrap")) {
      final String kafkaBootstrap = cmd.getOptionValue("kafkaBootstrap");
      config.getKafkaProperties().setBootstrapServers(kafkaBootstrap);
    }

    /* option: kafkaZookeper */
    if (cmd.hasOption("kafkaZookeper")) {
      final String kafkaZookeper = cmd.getOptionValue("kafkaZookeper");
      config.getKafkaProperties().setZookeperConnect(kafkaZookeper);
    }

    /* option: kafkaGroup */
    if (cmd.hasOption("kafkaGroup")) {
      final String kafkaGroup = cmd.getOptionValue("kafkaGroup");
      config.getKafkaProperties().setGroupId(kafkaGroup);
    }

    /* option: hiddenMetric */
    if (cmd.hasOption("hiddenMetric")) {
      final HiddenMetrics hiddenMetric = HiddenMetrics.valueOf(cmd.getOptionValue("hiddenMetric"));
      config.setHiddenMetric(hiddenMetric);
    }

    /* option: hiddenLocality */
    if (cmd.hasOption("hiddenLocality")) {
      final long potentialLocality = Long.valueOf(cmd.getOptionValue("hiddenLocality"));
      config.setPotentialLocality(potentialLocality);
    }

    /* option: hiddenWeights */
    if (cmd.hasOption("hiddenWeights")) {
      final String csHiddenWeight = cmd.getOptionValue("hiddenWeights");
      List<Double> hiddenWeight = Pattern.compile(",").splitAsStream(csHiddenWeight).map(Double::valueOf).collect(Collectors.toList());
      if (hiddenWeight.size() != config.getHiddenLocality()) {
        throw new IllegalArgumentException("The hidden weight vector mus contain a number of value equal to the potential locality.");
      }
      if (hiddenWeight.stream().mapToDouble(Double::valueOf).sum() != 1.0) {
        throw new IllegalArgumentException("The sum of hidden weights must be equal to 1.0.");
      }
      config.setHiddenWeights(hiddenWeight);
    }

    /* option: hiddenThreshold */
    if (cmd.hasOption("hiddenThreshold")) {
      final double hiddenThreshold = Double.valueOf(cmd.getOptionValue("hiddenThreshold"));
      config.setHiddenThreshold(hiddenThreshold);
    }

    /* option: potentialMetric */
    if (cmd.hasOption("potentialMetric")) {
      final PotentialMetrics potentialMetric = PotentialMetrics.valueOf(cmd.getOptionValue("potentialMetric"));
      config.setPotentialMetric(potentialMetric);
    }

    /* option: potentialLocality */
    if (cmd.hasOption("potentialLocality")) {
      final long potentialLocality = Long.valueOf(cmd.getOptionValue("potentialLocality"));
      config.setPotentialLocality(potentialLocality);
    }

    /* option: potentialWeights */
    if (cmd.hasOption("potentialWeights")) {
      final String csPotentialWeight = cmd.getOptionValue("potentialWeights");
      List<Double> potentialWeight = Pattern.compile(",").splitAsStream(csPotentialWeight).map(Double::valueOf).collect(Collectors.toList());
      if (potentialWeight.size() != config.getPotentialLocality()) {
        throw new IllegalArgumentException("The potential weight vector mus contain a number of value equal to the potential locality.");
      }
      if (potentialWeight.stream().mapToDouble(Double::valueOf).sum() != 1.0) {
        throw new IllegalArgumentException("The sum of potential weights must be equal to 1.0.");
      }
      config.setPotentialWeights(potentialWeight);
    }

    /* option: potentialThreshold */
    if (cmd.hasOption("potentialThreshold")) {
      final double potentialThreshold = Double.valueOf(cmd.getOptionValue("potentialThreshold"));
      config.setPotentialThreshold(potentialThreshold);
    }

    /* option: neo4jHostname */
    if (cmd.hasOption("neo4jHostname")) {
      final String neo4jHostname = cmd.getOptionValue("neo4jHostname");
      config.getNeo4jConfig().setHostname(neo4jHostname);
    }

    /* option: neo4jUsername */
    if (cmd.hasOption("neo4jUsername")) {
      final String neo4jUsername = cmd.getOptionValue("neo4jUsername");
      config.getNeo4jConfig().setUsername(neo4jUsername);
    }

    /* option: neo4jPassword */
    if (cmd.hasOption("neo4jPassword")) {
      final String neo4jPassword = cmd.getOptionValue("neo4jPassword");
      config.getNeo4jConfig().setPassword(neo4jPassword);
    }

    /* option: parallelism */
    if (cmd.hasOption("parallelism")) {
      final int parallelism = Integer.valueOf(cmd.getOptionValue("parallelism"));
      config.setParallelism(parallelism);
    }

    LOGGER.trace("Configuration loaded: {}",
        AppConfigurationService.getConfigurations());

    return cmd.getArgList();
  }

  /**
   * Returns command line options/arguments parsing utility.
   * @param argv The command line arguments passed to the main method.
   * @return The command line options/arguments parsing utility.
   * @see CommandLineParser
   * @see CommandLine
   */
  private static CommandLine getCommandLine(String argv[]) {
    CommandLineParser cmdParser = new DefaultParser();
    CommandLine cmd = null;

    try {
      cmd = cmdParser.parse(BaseOptions.getInstance(), argv);
    } catch (ParseException e) {
      LOGGER.error(e.getMessage());
      printHelp();
    }

    return cmd;
  }

  /**
   * Prints the application version.
   */
  private static void printVersion() {
    System.out.format("%s version %s\n",
        AppManifest.APP_NAME,
        AppManifest.APP_VERSION);
  }

  /**
   * Prints the application command line helper.
   * @see Option
   * @see Options
   */
  public static void printHelp() {
    System.out.format("%s version %s\nTeam: %s (%s)\n\n%s\n\n",
        AppManifest.APP_NAME,
        AppManifest.APP_VERSION,
        AppManifest.APP_TEAM_NAME,
        AppManifest.APP_TEAM_URL,
        AppManifest.APP_DESCRIPTION.replaceAll("(.{80})", "$1\n"));
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(AppManifest.APP_NAME, BaseOptions.getInstance(), true);
  }

  /**
   * Print the splash message to {@code stdout}.
   */
  public static void printSplash() {
    System.out.println();
    System.out.println("#=========================================================================");
    System.out.println("# CRIMEGRAPH");
    System.out.println("#=========================================================================");
  }

  /**
   * Configures the app with the specified YAML configuration file.
   * @param configPath the path to configuration file.
   * @throws IOException when configuration cannot be read.
   */
  private static void loadConfiguration(final String configPath) throws IOException {
    try(InputStream in = new FileInputStream(configPath)) {
      AppConfigurationService.loadYaml(in);
    }
  }

}
