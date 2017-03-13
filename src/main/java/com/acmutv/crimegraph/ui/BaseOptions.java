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

    super.addOption(version);
    super.addOption(help);
    super.addOption(config);
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
   * Builds the option `data-hostname`.
   * @return the option.
   */
  private Option optDataHostname() {
    return Option.builder("H")
        .longOpt("data-hostname")
        .desc("Insert here description")
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("IP ADDRESS")
        .build();
  }

  /**
   * Builds the option `data-port`.
   * @return the option.
   */
  private Option optDataPort() {
    return Option.builder("P")
        .longOpt("data-port")
        .desc("Insert here description")
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("PORT NUMBER")
        .build();
  }

  /**
   * Builds the option `dataset`.
   * @return the option.
   */
  private Option optDataset() {
    return Option.builder("D")
        .longOpt("dataset")
        .desc("Insert here description")
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
        .desc("Insert here description")
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("FILE")
        .build();
  }

  /**
   * Builds the option `locality`.
   * @return the option.
   */
  private Option optLocality() {
    return Option.builder("L")
        .longOpt("locality")
        .desc("Insert here description")
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("NUMBER")
        .build();
  }

  /**
   * Builds the option `potential-threshold`.
   * @return the option.
   */
  private Option optPotentialThreshold() {
    return Option.builder("P")
        .longOpt("potential-threshold")
        .desc("Insert here description")
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
    return Option.builder("P")
        .longOpt("hidden-threshold")
        .desc("Insert here description")
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
    return Option.builder("N")
        .longOpt("neo4j-hostname")
        .desc("Insert here description")
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("HOST")
        .build();
  }

  /**
   * Builds the option `neo4j-username`.
   * @return the option.
   */
  private Option optNeo4JUsername() {
    return Option.builder("U")
        .longOpt("neo4j-username")
        .desc("Insert here description")
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
    return Option.builder("U")
        .longOpt("neo4j-password")
        .desc("Insert here description")
        .required(false)
        .hasArg(true)
        .numberOfArgs(1)
        .argName("PASSWORD")
        .build();
  }

}
