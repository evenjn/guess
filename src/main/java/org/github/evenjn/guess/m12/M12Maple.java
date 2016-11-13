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

import static org.github.evenjn.numeric.NumericLogarithm.elnproduct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiFunction;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.m12.core.M12Core;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.numeric.DenseMatrix;
import org.github.evenjn.numeric.Matrix;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Maple;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12Maple<I, O> implements
		Maple<I, O> {

	private final M12Core core;

	public M12Maple(
			TupleAlignmentAlphabet<I, O> coalignment_alphabet,
			M12Core core,
			ProgressSpawner progress) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			this.core = core;
			init_cache( core.number_of_states );

			double[] buffer = new double[coalignment_alphabet.size( )];
			int len = 0;

			Progress spawn =
					ProgressManager.safeSpawn( hook, progress, "M12Maple::constructor" );
			spawn.target( core.number_of_states * coalignment_alphabet.size( ) );
			/**
			 * for each state we want to cache: for each symbol above, the probability
			 * of observing it for each symbol above, the most probable symbol below.
			 */
			for ( int s = 0; s < core.number_of_states; s++ ) {
				cache_prediction[s] = new HashMap<I, Tuple<O>>( );
				cache_partial_prob[s] = new HashMap<I, Double>( );

				for ( I sa : coalignment_alphabet.above( ) ) {
					len = 0;
					double max = 0;
					Tuple<O> best = null;
					for ( Tuple<O> sb : coalignment_alphabet.correspondingBelow( sa ) ) {
						int encode = coalignment_alphabet.encode( sa, sb );
						double prob = core.emission_table[s][encode];
						if ( len == 0 || prob > max ) {
							max = prob;
							best = sb;
						}
						buffer[len++] = prob;
						if ( spawn != null ) {
							spawn.step( 1 );
						}
					}
					if ( best == null ) {
						throw new RuntimeException( "who screwed up?" );
					}

					cache_prediction[s].put( sa, best );
					cache_partial_prob[s].put( sa,
							NumericLogarithm.elnsum( max, buffer, len ) );
				}

			}

		}
	}

	@SuppressWarnings("unchecked")
	private void init_cache( int number_of_states ) {
		cache_partial_prob = (Map<I, Double>[]) new Map<?, ?>[number_of_states];
		cache_prediction = (Map<I, Tuple<O>>[]) new Map<?, ?>[number_of_states];
	}

	private Map<I, Double>[] cache_partial_prob;

	private Map<I, Tuple<O>>[] cache_prediction;

	/*
	 * This is used by Viterbi
	 */
	private double emission( int s, I i ) {
		Double result = cache_partial_prob[s].get( i );
		if ( result == null ) {
			System.err.println( "M12Maple unknown symbol: " + i.toString( ) );
			return NumericLogarithm.smallLogValue;
		}
		return result;
	}

	/*
	 * This is cached here, used by Viterbi.
	 */
	Tuple<O> mostLikelyBelowGivenStateAndAbove( I i, int s ) {
		return cache_prediction[s].get( i );
	}

	@Override
	public Tuple<O> apply( Tuple<I> t ) {
		return KnittingTuple.wrap( mostLikelySequenceOfSymbolsBelow( t ) );
	}

	public Vector<O> mostLikelySequenceOfSymbolsBelow(
			Tuple<? extends I> observed ) {
		Vector<Integer> steps =
				mostLikelySequenceOfStates( observed );
		Vector<O> result = new Vector<>( );

		int i = 0;
		for ( Integer s : steps ) {
			Tuple<O> mostLikelyBelowGivenStateAndAbove =
					mostLikelyBelowGivenStateAndAbove( observed.get( i ), s );
			if ( mostLikelyBelowGivenStateAndAbove != null ) {
				for ( O sb : KnittingTuple.wrap( mostLikelyBelowGivenStateAndAbove )
						.asIterable( ) ) {
					result.add( sb );
				}
			} else {
				System.err.println(
						"M12Maple unknown symbol: " + observed.get( i ).toString( ) );
			}
			i++;
		}
		return result;
	}

	public Vector<Integer> mostLikelySequenceOfStates(
			Tuple<? extends I> observed ) {

		int length = observed.size( );
		Matrix<Integer> pointers =
				new DenseMatrix<>( length, core.number_of_states, Integer::sum, -1 );

		/**
		 * [ x s ] -> the probability of the automa to be in state s and the
		 * emission of the first x observed symbols above (given the whole sequence
		 * above/below).
		 */
		Matrix<Double> probability =
				new DenseMatrix<>( 1 + length, core.number_of_states,
						NumericLogarithm::elnsum2,
						NumericLogarithm.smallLogValue );

		for ( int t = 0; t < length; t++ ) {

			/*
			 * For each state s, we must compute the probability of the most probable
			 * state sequence responsible for input:0..t that have s as the final
			 * state AND that the emission at s is a sequence of symbols with length
			 * == gap.
			 */
			for ( int s = 0; s < core.number_of_states; s++ ) {
				// for this state, there is a fixed cost, the cost of emission.
				double cost = emission( s, observed.get( t ) );

				if ( t > 0 ) {
					double max = 0d;
					boolean found = false;
					int best_source = 0;
					for ( int input = 0; input < core.number_of_states; input++ ) {
						/*
						 * in classic HMM, we would consider only states as they occurred at
						 * time t - 1.
						 * 
						 */

						double tmp = elnproduct( probability.apply( t, input ),
								core.transition_table[input][s] );

						if ( !found || tmp > max ) {
							found = true;
							best_source = input;
							max = tmp;
						}
					}

					pointers.map( t, s, best_source );
					cost = elnproduct(
							cost, probability.apply( t, best_source ),
							core.transition_table[best_source][s] );

				} else {
					cost = elnproduct( cost, core.initial_table[s] );
				}
				probability.map( t + 1, s, cost );

			}
		}

		int best_final_state = 0;
		double final_max = 0d;
		boolean final_found = false;
		for ( int s = 0; s < core.number_of_states; s++ ) {
			Double tmp = probability.apply( length, s );
			if ( !final_found || tmp > final_max ) {
				final_found = true;
				best_final_state = s;
				final_max = tmp;
			}
		}
		return reconstructPath(
				( time, state ) -> pointers.apply( time, state ),
				length,
				best_final_state );
	}

	private Vector<Integer> reconstructPath(
			BiFunction<Integer, Integer, Integer> pointers,
			int length,
			Integer s ) {
		Vector<Integer> result = new Vector<>( );
		for ( int t = length - 1; t >= 0; t-- ) {
			result.add( s );
			if ( t > 0 ) {
				s = pointers.apply( t, s );
			}
		}
		Collections.reverse( result );
		return result;
	}

}
