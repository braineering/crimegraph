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

package com.acmutv.crimegraph.core.sink;

import com.acmutv.crimegraph.core.connector.ElasticsearchSinkFunction;
import com.acmutv.crimegraph.core.connector.RequestIndexer;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple sink function based on ElasticSearch.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class WordsElasticSinkFunction implements ElasticsearchSinkFunction<Tuple2<String,Integer>> {

  private static final String INDEX = "words";

  private static final String MAPPING = "word-counter";

  @Override
  public void process(Tuple2<String,Integer> element, RuntimeContext ctx, RequestIndexer indexer) {
    indexer.add(createIndexRequest(element));
  }

  private IndexRequest createIndexRequest(Tuple2<String,Integer> element) {
    Map<String, String> json = new HashMap<>();
    json.put("word", element.f0);
    json.put("count", element.f1.toString());

    return Requests.indexRequest()
        .index(INDEX)
        .type(MAPPING)
        .source(json);
  }
}
