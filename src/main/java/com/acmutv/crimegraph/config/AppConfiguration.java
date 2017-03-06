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

package com.acmutv.crimegraph.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * The app configuration model.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see Yaml
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfiguration.class);

  /**
   * Default value for property {@code dataHostname}.
   */
  public static final String DATA_HOSTNAME = "127.0.0.1";

  /**
   * Default value for property {@code dataPort}.
   */
  public static final int DATA_PORT = 9000;

  /**
   * Default value for property {@code dataset}.
   */
  public static final String DATASET = "crimegraph-dataset.txt";

  /**
   * Default value for property {@code neo4jHostname}.
   */
  public static final String NEO4J_HOSTNAME = "bolt://localhost:7687";

  /**
   * Default value for property {@code neo4jUsername}.
   */
  public static final String NEO4J_USERNAME = "neo4j";

  /**
   * Default value for property {@code neo4jPassword}.
   */
  public static final String NEO4J_PASSWORD = "password";

  /**
   * The hostname of the data stream socket.
   * Default is: {@code 127.0.0.1}.
   */
  private String dataHostname = DATA_HOSTNAME;

  /**
   * The port number of the data stream socket.
   * Default is: {@code 9000}.
   */
  private int dataPort = DATA_PORT;

  /**
   * The pathname of the file or directory containing the dataset.
   * Default is: {@code crimegraph-dataset.txt}
   */
  private String dataset = DATASET;

  /**
   * The hostname of the NEO4J instance.
   * Default is: {@code bolt://localhost:7474}.
   */
  private String neo4jHostname = NEO4J_HOSTNAME;

  /**
   * The username of the NEO4J instance.
   * Default is: {@code neo4j}.
   */
  private String neo4jUsername = NEO4J_USERNAME;

  /**
   * The password of the NEO4J instance.
   * Default is: {@code password}.
   */
  private String neo4jPassword = NEO4J_PASSWORD;

  /**
   * Constructs a configuration as a copy of the one specified.
   * @param other the configuration to copy.
   */
  public AppConfiguration(AppConfiguration other) {
    this.copy(other);
  }

  /**
   * Copies the settings of the configuration specified.
   * @param other the configuration to copy.
   */
  public void copy(AppConfiguration other) {
    this.dataHostname = other.dataHostname;
    this.dataPort = other.dataPort;
    this.dataset = other.dataset;
    this.neo4jHostname = other.neo4jHostname;
    this.neo4jUsername = other.neo4jUsername;
    this.neo4jPassword = other.neo4jPassword;
  }

  /**
   * Restores the default configuration settings.
   */
  public void toDefault() {
    this.dataHostname = DATA_HOSTNAME;
    this.dataPort = DATA_PORT;
    this.dataset = DATASET;
    this.neo4jHostname = NEO4J_HOSTNAME;
    this.neo4jUsername = NEO4J_USERNAME;
    this.neo4jPassword = NEO4J_PASSWORD;
  }

}
