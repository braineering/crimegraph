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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * This class realizes the command line interface options of the whole application.
 * The class is implemented as a singleton.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Option
 */
public class BaseOptions extends Options {

  private static final long serialVersionUID = 1L;

  /**
   * The CLI description for the option `version`.
   */
  private static final String DESCRIPTION_VERSION = "Show app version.";

  /**
   * The CLI description for the option `help`.
   */
  private static final String DESCRIPTION_HELP = "Show app helper.";

  /**
   * The CLI description for the option `config`.
   */
  private static final String DESCRIPTION_CONFIG = "Custom configuration.";

  /**
   * The CLI description for the option `dataset`.
   */
  private static final String DESCRIPTION_DATASET = "The path of the dataset.";

  /**
   * The CLI description for the option `output`.
   */
  private static final String DESCRIPTION_OUTPUT = "The path of the output file.";

  /**
   * The CLI description for the option `potential-locality`.
   */
  private static final String DESCRIPTION_POTENTIAL_LOCALITY = "The locality degree for potential link metric. The number must be an integer grater than 0.";

  /**
   * The CLI description for the option `potential-weight`.
   */
  private static final String DESCRIPTION_POTENTIAL_WEIGHT = "The weught vector for potential link metric. The sum of weights must be in (0,1).";

  /**
   * The CLI description for the option `potential-threshold`.
   */
  private static final String DESCRIPTION_POTENTIAL_THRESHOLD = "The threshold for potential link metric. The number must be in (0,1).";

  /**
   * The CLI description for the option `hidden-threshold`.
   */
  private static final String DESCRIPTION_HIDDEN_THRESHOLD = "The threshold for hidden link metric. The number must be in (0,1).";

  /**
   * The CLI description for the option `neo4j-hostname`.
   */
  private static final String DESCRIPTION_NEO4J_HOSTNAME = "The Neo4J hostname.";

  /**
   * The CLI description for the option `neo4j-username`.
   */
  private static final String DESCRIPTION_NEO4J_USERNAME = "The Neo4J username.";

  /**
   * The CLI description for the option `neo4j-password`.
   */
  private static final String DESCRIPTION_NEO4J_PASSWORD = "The Neo4J password.";

  /**
   * The singleton instance of {@link BaseOptions}.
   */
  private static BaseOptions instance;

  /**
   * Returns the singleton instance of {@link BaseOptions}.
   * @return the singleton.
   */
  public static BaseOptions getInstance() {
    if (instance == null) {
      instance = new BaseOptions();
    }
    return instance;
  }

  /**
   * Constructs the singleton of {@link BaseOptions}.
   */
  private BaseOptions() {
    Option version = this.optVersion();
    Option help = this.optHelp();
    Option config = this.optConfig();
    Option dataset = this.optDataset();
    Option output = this.optOutput();
    Option potentialLocality = this.optPotentialLocality();
    Option potentialWeight = this.optPotentialWeight();
    Option potentialThreshold = this.optPotentialThreshold();
    Option hiddenThreshold = this.optHiddenThreshold();
    Option neo4jHostname = this.optNeo4jHostname();
    Option neo4jUsername = this.optNeo4JUsername();
    Option neo4jPassword = this.optNeo4JPassword();

    super.addOption(version);
    super.addOption(help);
    super.addOption(config);
    super.addOption(dataset);
    super.addOption(output);
    super.addOption(potentialLocality);
    super.addOption(potentialWeight);
    super.addOption(potentialThreshold);
    super.addOption(hiddenThreshold);
    super.addOption(neo4jHostname);
    super.addOption(neo4jUsername);
    super.addOption(neo4jPassword);
  }

  /**
   * Builds the option `version`.
   * @return the option.
   */
  private Option optVersion() {
    return Option.builder("v")
        .longOpt("version")
        .desc(DESCRIPTION_VERSION)
        .required(false)
        .hasArg(false)
        .build();
  }

  /**
   * Builds the option `help`.
   * @return the option.
   */
  private Option optHelp() {
    return Option.builder("h")
        .longOpt("help")
        .desc(DESCRIPTION_HELP)
        .required(false)
        .hasArg(false)
        .build();
  }

  /**
   * Builds the option `config`.
   * @return the option.
   */
  private Option optConfig() {
    return Option.builder("c")
        .longOpt("config")
        .desc(DESCRIPTION_CONFIG)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("YAML-FILE")
        .build();
  }

  /**
   * Builds the option `dataset`.
   * @return the option.
   */
  private Option optDataset() {
    return Option.builder("D")
        .longOpt("dataset")
        .desc(DESCRIPTION_DATASET)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("FILE")
        .build();
  }

  /**
   * Builds the option `output`.
   * @return the option.
   */
  private Option optOutput() {
    return Option.builder("O")
        .longOpt("output")
        .desc(DESCRIPTION_OUTPUT)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("FILE")
        .build();
  }

  /**
   * Builds the option `potential-locality`.
   * @return the option.
   */
  private Option optPotentialLocality() {
    return Option.builder("L")
        .longOpt("potential-locality")
        .desc(DESCRIPTION_POTENTIAL_LOCALITY)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("NUMBER")
        .build();
  }

  /**
   * Builds the option `potential-weight`.
   * @return the option.
   */
  private Option optPotentialWeight() {
    return Option.builder("W")
        .longOpt("potential-weight")
        .desc(DESCRIPTION_POTENTIAL_WEIGHT)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("NUMBER,NUMBER,...")
        .build();
  }

  /**
   * Builds the option `potential-threshold`.
   * @return the option.
   */
  private Option optPotentialThreshold() {
    return Option.builder("P")
        .longOpt("potential-threshold")
        .desc(DESCRIPTION_POTENTIAL_THRESHOLD)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("NUMBER")
        .build();
  }

  /**
   * Builds the option `hidden-threshold`.
   * @return the option.
   */
  private Option optHiddenThreshold() {
    return Option.builder("H")
        .longOpt("hidden-threshold")
        .desc(DESCRIPTION_HIDDEN_THRESHOLD)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("NUMBER")
        .build();
  }

  /**
   * Builds the option `neo4j-hostname`.
   * @return the option.
   */
  private Option optNeo4jHostname() {
    return Option.builder("n")
        .longOpt("neo4j-hostname")
        .desc(DESCRIPTION_NEO4J_HOSTNAME)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("HOSTNAME")
        .build();
  }

  /**
   * Builds the option `neo4j-username`.
   * @return the option.
   */
  private Option optNeo4JUsername() {
    return Option.builder("u")
        .longOpt("neo4j-username")
        .desc(DESCRIPTION_NEO4J_USERNAME)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("USERNAME")
        .build();
  }

  /**
   * Builds the option `neo4j-password`.
   * @return the option.
   */
  private Option optNeo4JPassword() {
    return Option.builder("p")
        .longOpt("neo4j-password")
        .desc(DESCRIPTION_NEO4J_PASSWORD)
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("PASSWORD")
        .build();
  }

}
