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
package org.github.evenjn.guess.m12.maple;

import static org.github.evenjn.numeric.NumericLogarithm.elnproduct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiFunction;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.BasicAutoRook;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.knit.TupleValue;
import org.github.evenjn.numeric.DenseMatrix;
import org.github.evenjn.numeric.Matrix;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.numeric.NumericUtils.Summation;
import org.github.evenjn.yarn.AutoRook;
import org.github.evenjn.yarn.Maple;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;


/**
 * Uses a one-to-many hidden markov model to implement a transducer.
 * 
 * Upon encountering a never-seen-before input symbol, the system ignores the
 * input symbol information when choosing the most likely state.
 * By assigning emission probability "one" to unknown symbols, the system
 * chooses the most likely state based on transition probabilities only. 
 * 
 * @author Marco Trevisan
 *
 * @param <I> The type of input symbols.
 * @param <O> The type of output symbols.
 */
public class M12ClassicMaple<I, O> implements
		Maple<I, O> {

	private final Markov core;
	
	private final boolean fail_on_unknown_input_symbol;

	public M12ClassicMaple(
			TupleAlignmentAlphabet<I, O> coalignment_alphabet,
			Markov core,
			boolean fail_on_unknown_input_symbol,
			ProgressSpawner progress_spawner) {
		this(coalignment_alphabet, core, null, fail_on_unknown_input_symbol, progress_spawner);
	}
	
	/**
	 * 
	 * {@code descendant_test} is a function that returns true when the second argument
	 * is a descendant of the first one.
	 */
	public M12ClassicMaple(
			TupleAlignmentAlphabet<I, O> coalignment_alphabet,
			Markov core,
			BiFunction<Tuple<I>, Tuple<I>, Boolean> descendant_test,
			boolean fail_on_unknown_input_symbol,
			ProgressSpawner progress_spawner) {
		this.fail_on_unknown_input_symbol = fail_on_unknown_input_symbol;
//		Map<I, Set<Tuple<O>>> actual_pairs = new HashMap<>();
		
		try ( AutoRook rook = new BasicAutoRook( ) ) {

			this.core = core;
			init_cache( core.number_of_states );

			double[] buffer = new double[coalignment_alphabet.size( )];

			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner, "M12Maple::constructor" );
			spawn.target( core.number_of_states * coalignment_alphabet.size( ) );
			/**
			 * for each state we want to cache: for each symbol above, the probability
			 * of observing it for each symbol above, the most probable symbol below.
			 */
			for ( int s = 0; s < core.number_of_states; s++ ) {
				cache_prediction[s] = new HashMap<I, Tuple<O>>( );
				cache_partial_prob[s] = new HashMap<I, Double>( );

				for ( TupleValue<I> sa : coalignment_alphabet.above( ).asIterable( ) ) {
					int len = 0;
					double max = 0;
					Tuple<O> best = null;
					for ( TupleValue<O> sb : coalignment_alphabet.correspondingBelow( sa ) ) {
						int encode = coalignment_alphabet.encode( sa, sb );
						double prob = core.emission_table[s][encode];
						if ( best == null || prob > max ) {
							max = prob;
							best = sb;
						}
						buffer[len++] = prob;
						if ( spawn != null ) {
							spawn.step( 1 );
						}
					}
					if ( best == null ) {
						if (descendant_test == null) {
							throw new IllegalStateException( "A symbol above in the alphabet"
									+ " does not have any corresponding symbols below,"
									+ " and no descendant test function is set." );	
						}
						/**
						 * This symbol above is not a leaf in the tree of symbols.
						 */
						HashMap<Tuple<O>, Summation> sum_map = new HashMap<>( );
						for ( TupleValue<I> sa_descendant : coalignment_alphabet.above( ).asIterable( ) ) {
							if (! descendant_test.apply( sa, sa_descendant )) {
								continue;
							}

							for ( TupleValue<O> sb : coalignment_alphabet.correspondingBelow( sa_descendant ) ) {
								int encode = coalignment_alphabet.encode( sa_descendant, sb );
								double prob = core.emission_table[s][encode];
								
								Summation summation = sum_map.get( sb );
								if (summation == null) {
									summation = new Summation( coalignment_alphabet.size( ), NumericLogarithm::elnsumIterable );
								}
								summation.add( prob );
							}
						}
						
						for (Tuple<O> key : sum_map.keySet( )) {
							double prob = sum_map.get( key ).getSum( );
							if ( best == null || prob > max ) {
								max = prob;
								best = key;
							}
							buffer[len++] = prob;
							if ( spawn != null ) {
								spawn.step( 1 );
							}
						}

						if ( best == null ) {
							throw new IllegalStateException( "A symbol above in the alphabet"
									+ " does not have any corresponding symbols below,"
									+ " even considering descendants." );
						}
					}

//					Set<Tuple<O>> fd = actual_pairs.get( sa );
//					if (fd == null) {
//						fd = new HashSet<>( );
//						actual_pairs.put( sa, fd );
//					}
//					fd.add( best );
					cache_prediction[s].put( sa.get( 0 ), best );
					cache_partial_prob[s].put( sa.get( 0 ),
							NumericLogarithm.elnsum( max, buffer, len ) );
				}

			}

		}

//		for ( I sa : coalignment_alphabet.above( ) ) { 
//			System.out.println( Unicode.aboutCodepoint( Integer.parseInt( sa.toString( ) ) ));
//			for (Tuple<O> sb : actual_pairs.get( sa )) {
//				System.out.println(   " >-> " + sb.toString( ) );
//			}
//		}
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
			if ( fail_on_unknown_input_symbol ) {
				throw new IllegalArgumentException(
						"M12Maple unknown symbol: " + i.toString( ) );
			}
			else {
				return NumericLogarithm.oneLogValue;
			}
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

	/**
	 * The classic viterbi algorithm.
	 * 
	 */
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

					pointers.set( t, s, best_source );
					cost = elnproduct(
							cost, probability.apply( t, best_source ),
							core.transition_table[best_source][s] );

				} else {
					cost = elnproduct( cost, core.initial_table[s] );
				}
				probability.set( t + 1, s, cost );

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
