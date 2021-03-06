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

import com.acmutv.crimegraph.core.db.DbConfiguration;
import com.acmutv.crimegraph.core.metric.HiddenMetrics;
import com.acmutv.crimegraph.core.metric.PotentialMetrics;
import com.acmutv.crimegraph.core.source.KafkaProperties;
import com.acmutv.crimegraph.core.source.SourceType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

/**
 * JUnit tests for {@link AppConfigurationService} and {@link AppConfiguration}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see AppConfigurationService
 */
public class AppConfigurationServiceTest {

  /**
   * Tests the restoring of default settings in app configuration.
   */
  @Test
  public void test_fromDefault() {
    AppConfiguration actual = AppConfigurationService.fromDefault();
    AppConfiguration expected = new AppConfiguration();
    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests the app configuration parsing from an external YAML file.
   * In this test the configuration file provides with complete custom settings.
   * The configuration file has non-null values and template string (${RES}).
   */
  @Test
  public void test_fromJsonYaml_custom_file() throws IOException {
    InputStream in = AppConfigurationServiceTest.class.getResourceAsStream("/config/custom1.yaml");
    AppConfiguration actual = AppConfigurationService.fromYaml(in);
    AppConfiguration expected = new AppConfiguration();
    expected.setSource(SourceType.FILE);
    expected.setDataset("CustomDataset");
    expected.setHiddenMetric(HiddenMetrics.WEIGHTED_QUASI_LOCAL);
    expected.setHiddenLocality(3);
    expected.setHiddenWeights(new ArrayList<Double>(){{
      add(0.6);
      add(0.3);
      add(0.1);
    }});
    expected.setHiddenThreshold(0.8);
    expected.setPotentialMetric(PotentialMetrics.WEIGHTED_QUASI_LOCAL);
    expected.setPotentialLocality(3);
    expected.setPotentialWeights(new ArrayList<Double>(){{
      add(0.6);
      add(0.3);
      add(0.1);
    }});
    expected.setPotentialThreshold(0.8);
    expected.setEwmaFactor(0.8);
    expected.setNeo4jConfig(new DbConfiguration(
        "CustomNeo4jHostname", "CustomNeo4jUsername", "CustomNeo4jPassword"
    ));
    expected.setParallelism(16);
    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests the app configuration parsing from an external YAML file.
   * In this test the configuration file provides with complete custom settings.
   * The configuration file has non-null values and template string (${RES}).
   */
  @Test
  public void test_fromJsonYaml_custom_kafka() throws IOException {
    InputStream in = AppConfigurationServiceTest.class.getResourceAsStream("/config/custom2.yaml");
    AppConfiguration actual = AppConfigurationService.fromYaml(in);
    AppConfiguration expected = new AppConfiguration();
    expected.setSource(SourceType.KAFKA);
    expected.setTopic("CustomTopic");
    expected.setKafkaProperties(new KafkaProperties(
        "CustomBootstrap", "CustomZookeper", "CustomGroup"
    ));
    expected.setHiddenMetric(HiddenMetrics.WEIGHTED_QUASI_LOCAL);
    expected.setHiddenLocality(3);
    expected.setHiddenWeights(new ArrayList<Double>(){{
      add(0.6);
      add(0.3);
      add(0.1);
    }});
    expected.setHiddenThreshold(0.8);
    expected.setPotentialMetric(PotentialMetrics.WEIGHTED_QUASI_LOCAL);
    expected.setPotentialLocality(3);
    expected.setPotentialWeights(new ArrayList<Double>(){{
      add(0.6);
      add(0.3);
      add(0.1);
    }});
    expected.setPotentialThreshold(0.8);
    expected.setEwmaFactor(0.8);
    expected.setNeo4jConfig(new DbConfiguration(
        "CustomNeo4jHostname", "CustomNeo4jUsername", "CustomNeo4jPassword"
    ));
    expected.setParallelism(16);
    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests the app configuration parsing from an external YAML file.
   * In this test the configuration file provides with complete default settings.
   */
  @Test
  public void test_fromJsonYaml_default() throws IOException {
    InputStream in = AppConfigurationServiceTest.class.getResourceAsStream("/config/default.yaml");
    AppConfiguration actual = AppConfigurationService.fromYaml(in);
    AppConfiguration expected = new AppConfiguration();
    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests the configuration parsing from an external YAML file.
   * In this test the configuration file provides with empty settings.
   */
  @Test
  public void test_fromJsonYaml_empty() throws IOException {
    InputStream in = AppConfigurationServiceTest.class.getResourceAsStream("/config/empty.yaml");
    try {
      AppConfigurationService.fromYaml(in);
    } catch (IOException exc) {return;}
    Assert.fail();
  }

  /**
   * Tests the configuration parsing from an external YAML configuration file.
   * In this test the configuration file provides with partial custom settings.
   */
  @Test
  public void test_fromJsonYaml_partialCustom() throws IOException {
    InputStream in = AppConfigurationServiceTest.class.getResourceAsStream("/config/partial.yaml");
    AppConfiguration actual = AppConfigurationService.fromYaml(in);
    AppConfiguration expected = new AppConfiguration();
    expected.getNeo4jConfig().setHostname("CustomNeo4jHostname");
    Assert.assertEquals(expected, actual);
  }

}