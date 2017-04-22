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

package com.acmutv.crimegraph.core.tuple;

import lombok.Getter;

/**
 * All types of links.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
@Getter
public enum ScoreType {

    POTENTIAL   ("POTENTIAL"),
    HIDDEN      ("HIDDEN"),

    /* FOR MULTI-INDEX COMPUTING */
    NRA    ("NRA"),
    NTA    ("NTA"),
    TA     ("TA"),
    CN ("CN"),
    JACCARD ("JACCARD"),
    SALTON ("SALTON"),
    SORENSEN ("SORENSEN"),
    HPI ("HPI"),
    HDI ("HDI"),
    LHN1 ("LHN1"),
    PA ("PA"),
    AA ("AA"),
    RA ("RA");

    private final String name;

    ScoreType(final String name) {
      this.name = name;
    }
}
