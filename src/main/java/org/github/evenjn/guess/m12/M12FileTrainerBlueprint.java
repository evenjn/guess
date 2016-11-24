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
package org.github.evenjn.guess.m12;

import java.util.function.Function;

public class M12FileTrainerBlueprint<I, O> {

	private boolean shrink_alphabet;
	
	private boolean full_alphabet;

	private int min_below;

	private int max_below;

	private Function<I, String> a_printer;

	private Function<O, String> b_printer;

	private Function<I, String> a_serializer;

	private Function<O, String> b_serializer;

	private Function<String, I> a_deserializer;

	private Function<String, O> b_deserializer;

	private int period;

	private int epochs;

	private long seed;

	private int number_of_states;

	public M12FileTrainerBlueprint<I, O>
			setShrinkAlphabet( boolean shrink_alphabet ) {
		this.shrink_alphabet = shrink_alphabet;
		return this;
	}

	public M12FileTrainerBlueprint<I, O>
			setFullAlphabet( boolean full_alphabet ) {
		this.full_alphabet = full_alphabet;
		return this;
	}

	public M12FileTrainerBlueprint<I, O>
			setMinMaxBelow( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
		return this;
	}

	public M12FileTrainerBlueprint<I, O> setPrinter(
			Function<I, String> a_printer,
			Function<O, String> b_printer ) {
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		return this;
	}

	public M12FileTrainerBlueprint<I, O> setInputCoDec(
			Function<I, String> a_serializer,
			Function<String, I> a_deserializer ) {
		this.a_serializer = a_serializer;
		this.a_deserializer = a_deserializer;
		return this;
	}

	public M12FileTrainerBlueprint<I, O> setOutputCoDec(
			Function<O, String> b_serializer,
			Function<String, O> b_deserializer ) {
		this.b_serializer = b_serializer;
		this.b_deserializer = b_deserializer;
		return this;
	}

	public M12FileTrainerBlueprint<I, O> trainingTime( int period, int epochs ) {
		this.period = period;
		this.epochs = epochs;
		return this;
	}

	public M12FileTrainerBlueprint<I, O> seed( long seed ) {
		this.seed = seed;
		return this;
	}

	public M12FileTrainerBlueprint<I, O> states( int number_of_states ) {
		this.number_of_states = number_of_states;
		return this;
	}

	public M12FileTrainer<I, O> create( ) {
		return new M12FileTrainer<>(
				shrink_alphabet,
				full_alphabet,
				min_below,
				max_below,
				a_printer,
				b_printer,
				a_serializer,
				b_serializer,
				a_deserializer,
				b_deserializer,
				period,
				epochs,
				seed,
				number_of_states);
	}
}
