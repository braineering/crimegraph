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

package com.acmutv.crimegraph.tool.string;

import com.acmutv.crimegraph.config.AppConfiguration;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.HashMap;

/**
 * This class realizes a string template that can be used by {@link StrSubstitutor}.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see StrSubstitutor
 */
public class StringTemplateMap extends HashMap<String,String> {

  /**
   * The singleton of {@link StringTemplateMap}.
   */
  private static StringTemplateMap instance;

  /**
   * Returns the singleton of {@link StringTemplateMap}.
   * @return the singleton.
   */
  public static StringTemplateMap getInstance() {
    if (instance == null) {
      instance = new StringTemplateMap();
    }
    return instance;
  }

  /**
   * Initializes the singleton of {@link StringTemplateMap}.
   * Available properties are:
   * ${PWD}: the present working directory (e.g.: /home/gmarciani/workspace/app);
   * ${RES}: the target resources folder (target/classes)
   */
  private StringTemplateMap() {
    super();
    super.put("PWD",
        System.getProperty("user.dir"));
    super.put("RES",
        AppConfiguration.class.getResource("/").getPath().replaceAll("/$", ""));
  }
}
