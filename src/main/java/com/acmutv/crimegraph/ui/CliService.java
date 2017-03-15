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

    /* option: dataset */
    if (cmd.hasOption("dataset")) {
      final String dataset = cmd.getOptionValue("dataset");
      config.setDataset(dataset);
    }

    /* option: hidden-metric */
    if (cmd.hasOption("hidden-metric")) {
      final HiddenMetrics hiddenMetric = HiddenMetrics.valueOf(cmd.getOptionValue("hidden-metric"));
      config.setHiddenMetric(hiddenMetric);
    }

    /* option: hidden-locality */
    if (cmd.hasOption("hidden-locality")) {
      final long potentialLocality = Long.valueOf(cmd.getOptionValue("hidden-locality"));
      config.setPotentialLocality(potentialLocality);
    }

    /* option: hidden-weights */
    if (cmd.hasOption("hidden-weights")) {
      final String csHiddenWeight = cmd.getOptionValue("hidden-weights");
      List<Double> hiddenWeight = Pattern.compile(",").splitAsStream(csHiddenWeight).map(Double::valueOf).collect(Collectors.toList());
      if (hiddenWeight.size() != config.getHiddenLocality()) {
        throw new IllegalArgumentException("The hidden weight vector mus contain a number of value equal to the potential locality.");
      }
      if (hiddenWeight.stream().mapToDouble(Double::valueOf).sum() != 1.0) {
        throw new IllegalArgumentException("The sum of hidden weights must be equal to 1.0.");
      }
      config.setHiddenWeights(hiddenWeight);
    }

    /* option: hidden-threshold */
    if (cmd.hasOption("hidden-threshold")) {
      final double hiddenThreshold = Double.valueOf(cmd.getOptionValue("hidden-threshold"));
      config.setHiddenThreshold(hiddenThreshold);
    }

    /* option: potential-metric */
    if (cmd.hasOption("potential-metric")) {
      final PotentialMetrics potentialMetric = PotentialMetrics.valueOf(cmd.getOptionValue("potential-metric"));
      config.setPotentialMetric(potentialMetric);
    }

    /* option: potential-locality */
    if (cmd.hasOption("potential-locality")) {
      final long potentialLocality = Long.valueOf(cmd.getOptionValue("potential-locality"));
      config.setPotentialLocality(potentialLocality);
    }

    /* option: potential-weights */
    if (cmd.hasOption("potential-weights")) {
      final String csPotentialWeight = cmd.getOptionValue("potential-weights");
      List<Double> potentialWeight = Pattern.compile(",").splitAsStream(csPotentialWeight).map(Double::valueOf).collect(Collectors.toList());
      if (potentialWeight.size() != config.getPotentialLocality()) {
        throw new IllegalArgumentException("The potential weight vector mus contain a number of value equal to the potential locality.");
      }
      if (potentialWeight.stream().mapToDouble(Double::valueOf).sum() != 1.0) {
        throw new IllegalArgumentException("The sum of potential weights must be equal to 1.0.");
      }
      config.setPotentialWeights(potentialWeight);
    }

    /* option: potential-threshold */
    if (cmd.hasOption("potential-threshold")) {
      final double potentialThreshold = Double.valueOf(cmd.getOptionValue("potential-threshold"));
      config.setPotentialThreshold(potentialThreshold);
    }

    /* option: neo4j-hostname */
    if (cmd.hasOption("neo4j-hostname")) {
      final String neo4jHostname = cmd.getOptionValue("neo4j-hostname");
      config.setNeo4jHostname(neo4jHostname);
    }

    /* option: neo4j-username */
    if (cmd.hasOption("neo4j-username")) {
      final String neo4jUsername = cmd.getOptionValue("neo4j-username");
      config.setNeo4jUsername(neo4jUsername);
    }

    /* option: neo4j-password */
    if (cmd.hasOption("neo4j-password")) {
      final String neo4jPassword = cmd.getOptionValue("neo4j-password");
      config.setNeo4jPassword(neo4jPassword);
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
