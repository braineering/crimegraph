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

package com.acmutv.crimegraph.core.connector;

import org.apache.flink.api.common.functions.Function;
import org.apache.flink.api.common.functions.RuntimeContext;

import java.io.Serializable;

/**
 * Method that creates an {@link org.elasticsearch.action.ActionRequest} from an element in a Stream.
 *
 * <p>
 * This is used by {@link ElasticsearchSink} to prepare elements for sending them to Elasticsearch.
 *
 * <p>
 * Example:
 *
 * <pre>{@code
 *					private static class TestElasticSearchSinkFunction implements
 *						ElasticsearchSinkFunction<Tuple2<Integer, String>> {
 *
 *					public IndexRequest createIndexRequest(Tuple2<Integer, String> element) {
 *						Map<String, Object> json = new HashMap<>();
 *						json.put("data", element.f1);
 *
 *						return Requests.indexRequest()
 *							.index("my-index")
 *							.type("my-type")
 *							.id(element.f0.toString())
 *							.source(json);
 *						}
 *
 *				public void process(Tuple2<Integer, String> element, RuntimeContext ctx, RequestIndexer indexer) {
 *					indexer.add(createIndexRequest(element));
 *				}
 *		}
 *
 * }</pre>
 *
 * @param <T> The type of the element handled by this {@code ElasticsearchSinkFunction}
 */
public interface ElasticsearchSinkFunction<T> extends Serializable, Function {
	void process(T element, RuntimeContext ctx, RequestIndexer indexer);
}
