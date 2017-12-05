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

import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public class TupleEqualsEvaluator<T, I, O extends Tuple<? extends T>> implements
		Evaluator<I, O> {

	private int total;

	private int total_distance;

	private int total_elements;

	private int positive;

	public static <K, T, I, O extends Tuple<? extends T>>
			Builder<K, T, I, O>
			builder(
					Function<I, O> guesser,
					Cursable<K> data,
					Function<K, I> get_input,
					Function<K, O> get_output ) {
		return new Builder<>( guesser, data, get_input, get_output );
	}

	public static class Builder<K, T, I, O extends Tuple<? extends T>> {

		private Function<I, O> guesser;

		private Cursable<K> data;

		private Function<K, I> get_input;

		private Function<K, O> get_output;

		public Builder(Function<I, O> guesser,
						Cursable<K> data,
						Function<K, I> get_input,
						Function<K, O> get_output) {
			this.guesser = guesser;
			this.data = data;
			this.get_input = get_input;
			this.get_output = get_output;
		}

		public TupleEqualsEvaluator<T, I, O> doEvaluate( ) {
			TupleEqualsEvaluator<T, I, O> result =
					new TupleEqualsEvaluator<T, I, O>( );
			result.evaluate( guesser, data, get_input, get_output );
			return result;
		}
	}

	public <K> void evaluate(
			Function<I, O> guesser,
			Cursable<K> data,
			Function<K, I> get_input,
			Function<K, O> get_output ) {
		try ( BasicRook rook = new BasicRook() ) {
			for ( K k : KnittingCursable.wrap( data ).pull( rook ).once( ) ) {
				I i = get_input.apply( k );
				O target_output = get_output.apply( k );
				O guessed_output = guesser.apply( i );
				record(i, target_output, guessed_output);
			}
		}
	}

	public double precision( ) {
		double a = positive;
		double b = total;
		System.out.println( positive + " / " + total );
		return a / b;
	}

	public double one_minus_relative_distance( ) {
		double a = total_distance;
		double b = total_elements;
		System.out
				.println( "1 - (" + total_distance + " / " + total_elements + ")" );
		return 1 - ( a / b );

	}

	@Override
	public String printEvaluation( ) {
		double a = total_distance;
		double b = total_elements;
		return "1 - (" + total_distance + " / " + total_elements + ") = "
				+ ( 1 - ( a / b ) );
	}

	@Override
	public void reset( ) {
		total = 0;
		total_distance = 0;
		total_elements = 0;
		positive = 0;
	}

	@Override
	public void record( I input, O target, O guessed ) {
		Tuple<? extends T> target_output = target;
		Tuple<? extends T> guessed_output = guessed;
		KnittingTuple<T> kgo = KnittingTuple.wrap( guessed_output ).map( x -> x );
		KnittingTuple<T> koo = KnittingTuple.wrap( target_output ).map( x -> x );
		int distance = koo.distance( KnittingTuple.wrap(guessed_output).map( x->x ));
		total_distance += distance;
		int size_go = kgo.size( );
		int size_oo = koo.size( );
		/**
		 * Taking the largest size guarantees that the relative distance is
		 * always in the interval [0,1]
		 */
		total_elements += ( size_go > size_oo ) ? size_go : size_oo;
		if ( distance == 0 ) {
			positive++;
		}
		total++;
	}
}
