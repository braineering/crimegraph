/*
  The MIT License (MIT)

  Copyright (c) 2017 Giacomo Marciani and Michele Porretta

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

package com.acmutv.crimegraph.data;

import com.acmutv.crimegraph.core.tuple.Link;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A dataset generator.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class Datagen {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Datagen.class);

  private static final String PATH = "dataset.txt";

  public static void main(String[] args) {
    String filename = (args.length > 0) ? args[0] : PATH;

    Path path = FileSystems.getDefault().getPath(filename);

    List<Link> data = data();

    try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
     for (Link link : data) {
       writer.append(link.toString() + "\n");
     }
    } catch (IOException exc) {
      LOGGER.error(exc.getMessage());
    }
  }

  public static List<Link> data() {
    List<Link> data = new ArrayList<>();
    data.add(new Link(1L, 2L, 20.0));
    return data;
  }
}
