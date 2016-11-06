/**
 *
 * Copyright 2016 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.align;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;

public class TupleAlignmentGraphDataManagerBlueprint<I, O> {

	private int min_below;

	private int max_below;

	private Function<I, String> a_serializer;

	private Function<O, String> b_serializer;

	private Function<String, I> a_deserializer;

	private Function<String, O> b_deserializer;

	private Function<Hook, Consumer<String>> putter_coalignment_alphabet;

	private Cursable<String> reader_coalignment_alphabet;

	private Function<Hook, Consumer<String>> putter_coalignment_graphs;

	private Cursable<String> reader_coalignment_graphs;

	private boolean refresh_cache;

	public TupleAlignmentGraphDataManagerBlueprint<I, O> setMinBelow( int min ) {
		this.min_below = min;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O> setMaxBelow( int max ) {
		this.max_below = max;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O> setInputCoDec(
			Function<I, String> a_serializer,
			Function<String, I> a_deserializer ) {
		this.a_serializer = a_serializer;
		this.a_deserializer = a_deserializer;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O> setOutputCoDec(
			Function<O, String> b_serializer,
			Function<String, O> b_deserializer ) {
		this.b_serializer = b_serializer;
		this.b_deserializer = b_deserializer;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			deserializeTupleAlignmentAlphabet(
					Cursable<String> reader_alphabet ) {
		this.reader_coalignment_alphabet = reader_alphabet;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			serializeTupleAlignmentAlphabet(
					Function<Hook, Consumer<String>> putter_alphabet ) {
		this.putter_coalignment_alphabet = putter_alphabet;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			deserializeTupleAlignmentGraphs(
					Cursable<String> reader_coalignments ) {
		this.reader_coalignment_graphs = reader_coalignments;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			serializeTupleAlignmentGraphs(
					Function<Hook, Consumer<String>> putter_coalignments ) {
		this.putter_coalignment_graphs = putter_coalignments;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O> refreshCache(
			boolean refresh_cache ) {
		this.refresh_cache = refresh_cache;
		return this;
	}

	public TupleAlignmentGraphDataManager<I, O> create( ) {

		return new TupleAlignmentGraphDataManager<>(
				min_below,
				max_below,
				putter_coalignment_alphabet,
				reader_coalignment_alphabet,
				putter_coalignment_graphs,
				reader_coalignment_graphs,
				a_serializer,
				b_serializer,
				a_deserializer,
				b_deserializer,
				refresh_cache );
	}
}
