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

package com.acmutv.crimegraph.core.data;

import com.acmutv.crimegraph.core.tuple.Link;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for dataset generation.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @since 1.0
 * @see Link
 */
public class Datagen {

  private static final Logger LOGGER = LoggerFactory.getLogger(Datagen.class);

  /**
   * Generates a simple dataset.
   */
  @Test
  public void simple() throws Exception {
    String filename = "data/dataset.txt";

    List<Link> data = new ArrayList<>();
    data.add(new Link(1,2,10.0));
    data.add(new Link(1,3,10.0));
    data.add(new Link(2,3,10.0));
    data.add(new Link(2,4,10.0));
    data.add(new Link(3,6,10.0));
    data.add(new Link(4,5,10.0));
    data.add(new Link(6,7,10.0));

    writeDataset(filename, data);
  }

  /**
   * Writes the dataset {@code data} into {@code filename}.
   * @param filename the filename to write onto.
   * @param data the dataset to write.
   */
  private static final void writeDataset(String filename, List<Link> data) {
    Path path = FileSystems.getDefault().getPath(filename);
    try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
      for (Link link : data) {
        writer.append(link.toString()).append("\n");
      }
    } catch (IOException exc) {
      LOGGER.error(exc.getMessage());
    }
  }
}
