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
package org.github.evenjn.align.alphabet;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;

public class TupleAlignmentAlphabetDataManagerBlueprint<I, O> {

	private int min_below = 1;

	private int max_below = 1;

	private Function<I, String> a_printer;

	private Function<O, String> b_printer;

	private Function<I, String> a_serializer;

	private Function<O, String> b_serializer;

	private Function<String, I> a_deserializer;

	private Function<String, O> b_deserializer;

	private Function<Hook, Consumer<String>> putter_coalignment_alphabet;

	private Cursable<String> reader_coalignment_alphabet;

	private TupleAlignmentAlphabetBuilder<I, O> builder;

	private Function<Hook, Consumer<String>> logger;

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O>
			setMinMaxBelow( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
		return this;
	}

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O> setPrinter(
			Function<Hook, Consumer<String>> logger,
			Function<I, String> a_printer,
			Function<O, String> b_printer ) {
		this.logger = logger;
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		return this;
	}

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O> setInputCoDec(
			Function<I, String> a_serializer,
			Function<String, I> a_deserializer ) {
		this.a_serializer = a_serializer;
		this.a_deserializer = a_deserializer;
		return this;
	}

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O> setOutputCoDec(
			Function<O, String> b_serializer,
			Function<String, O> b_deserializer ) {
		this.b_serializer = b_serializer;
		this.b_deserializer = b_deserializer;
		return this;
	}

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O>
			deserializeTupleAlignmentAlphabet(
					Cursable<String> reader_alphabet ) {
		this.reader_coalignment_alphabet = reader_alphabet;
		return this;
	}

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O>
			serializeTupleAlignmentAlphabet(
					Function<Hook, Consumer<String>> putter_alphabet ) {
		this.putter_coalignment_alphabet = putter_alphabet;
		return this;
	}

	public TupleAlignmentAlphabetDataManagerBlueprint<I, O> setAlphabetBuilder(
					TupleAlignmentAlphabetBuilder<I, O> builder ) {
		this.builder = builder;
		return this;
	}

	public TupleAlignmentAlphabetDataManager<I, O> create( ) {
		return new TupleAlignmentAlphabetDataManager<>(
				min_below,
				max_below,
				builder,
				putter_coalignment_alphabet,
				reader_coalignment_alphabet,
				a_serializer,
				b_serializer,
				a_deserializer,
				b_deserializer,
				a_printer,
				b_printer,
				logger);
	}
}
