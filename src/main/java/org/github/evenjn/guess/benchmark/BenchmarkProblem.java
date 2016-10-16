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
package org.github.evenjn.guess.benchmark;

import java.util.function.Function;

import org.github.evenjn.yarn.Cursable;

public class BenchmarkProblem<I, O> {

	private Function<I, String> input_printer;

	private Function<O, String> output_printer;

	public static class Builder<I, O> {

		private BenchmarkProblem<I, O> built;

		private Builder(Cursable<BenchmarkDatum<I, O>> data) {
			built = new BenchmarkProblem<>( );
			built.data = data;
		}

		public Builder<I, O> label( String label ) {
			built.label = label;
			return this;
		}

		public Builder<I, O>
				inputPrinter( Function<I, String> input_printer ) {
			built.input_printer = input_printer;
			return this;
		}

		public Builder<I, O> outputPrinter(
				Function<O, String> output_printer ) {
			built.output_printer = output_printer;
			return this;
		}

		public BenchmarkProblem<I, O> build( ) {
			return built;
		}
	}

	public static <I, O> Builder<I, O> nu(
			Cursable<BenchmarkDatum<I, O>> data ) {
		Builder<I, O> result = new Builder<>( data );
		return result;
	}

	private String label;

	private Cursable<BenchmarkDatum<I, O>> data;

	public Cursable<BenchmarkDatum<I, O>> data( ) {
		return data;
	}

	public String label( ) {
		return label;
	}

	public Function<I, String> inputPrinter( ) {
		return input_printer;
	}

	public Function<O, String> outputPrinter( ) {
		return output_printer;
	}
}
