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

import com.acmutv.crimegraph.core.CustomObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.concurrent.TimeUnit;

/**
 * This class realizes the app configuration model.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Yaml
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfiguration {

  private static final Logger LOGGER = LogManager.getLogger(AppConfiguration.class);

  /**
   * Default value for property [property name].
   */
  public static final Boolean PROPERTY_BOOLEAN = false;

  /**
   * Default value for property [property name].
   */
  public static final String PROPERTY_STRING = "Default";

  /**
   * Default value for property [property name].
   */
  public static final CustomObject PROPERTY_OBJECT = new CustomObject(10, TimeUnit.SECONDS);

  /**
   * Property description.
   * Default is: default value.
   */
  private Boolean propertyBoolean = PROPERTY_BOOLEAN;

  /**
   * Property description.
   * Default is: default value.
   */
  private String propertyString = PROPERTY_STRING;

  /**
   * Property description.
   * Default is: default value.
   */
  private CustomObject propertyObject = PROPERTY_OBJECT;

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
    this.propertyBoolean = other.propertyBoolean;
    this.propertyString = other.propertyString;
    this.propertyObject = other.propertyObject;
  }

  /**
   * Restores the default configuration settings.
   */
  public void toDefault() {
    this.propertyBoolean = PROPERTY_BOOLEAN;
    this.propertyString = PROPERTY_STRING;
    this.propertyObject = PROPERTY_OBJECT;
  }

}
