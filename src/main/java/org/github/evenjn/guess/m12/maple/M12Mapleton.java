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
import java.util.Set;
import java.util.Vector;
import java.util.function.BiFunction;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.TupleValue;
import org.github.evenjn.numeric.Cubix;
import org.github.evenjn.numeric.DenseCubix;
import org.github.evenjn.numeric.DenseMatrix;
import org.github.evenjn.numeric.Matrix;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.yarn.Maple;
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
public class M12Mapleton<I, O> implements
		Maple<I, O> {

	private final Markov core;
	
	private TupleAlignmentAlphabet<I, O> coalignment_alphabet;

	public M12Mapleton(
			TupleAlignmentAlphabet<I, O> coalignment_alphabet,
			Markov core) {
		this.coalignment_alphabet = coalignment_alphabet;
		this.core = core;
	}


	@Override
	public Tuple<O> apply( Tuple<I> t ) {
		return KnittingTuple.wrap( mostLikelySequenceOfSymbolsBelow( t ) );
	}


	/**
	 * <p>
	 * Creates a graph of virtual traveled states. Each virtual traveled state is
	 * associated with a number of steps and an output symbol. Each virtual
	 * traveled state also assigns a probability distribution to all real states.
	 * </p>
	 * 
	 * <p>
	 * For example, consider a model with three states {@code A B C}, two symbols
	 * above {@code 0 1}, two symbols below {@code 0 1}.
	 * </p>
	 * 
	 * <p>
	 * {@code initial(A) = 1}<br>
	 * 
	 * {@code transition[A B] = 0.5}<br>
	 * {@code transition[A C] = 0.5}<br>
	 * 
	 * {@code transition[B A] = 0.1}<br>
	 * {@code transition[B C] = 0.7}<br>
	 * 
	 * {@code transition[C A] = 0.1}<br>
	 * {@code transition[C B] = 0.4}<br>
	 * 
	 * {@code emission[A 1/0] = 0.2}<br>
	 * {@code emission[B 0/0] = 0.1}<br>
	 * {@code emission[B 0/1] = 0.2}<br>
	 * {@code emission[B 1/0] = 0.3}<br>
	 * 
	 * {@code emission[C 0/0] = 0.4}<br>
	 * {@code emission[C 0/1] = 0.3}<br>
	 * {@code emission[C 1/0] = 0.2}<br>
	 * </p>
	 * 
	 * <p>
	 * Given input sequence {@code [ 1 1 0 ]}, we want to compute viterbi on
	 * virtual states Z and O (corresponding to zero and one emitted).
	 * </p>
	 * 
	 * 
	 * {@code let p be [ x s e ] -> apple} where apple is the probability that
	 * automa was in state {@code s} after {@code x} transitions, emitted symbol
	 * below {@code e} after {@code x} transitions (given the whole observed
	 * sequence above/below).
	 * <p>
	 * {@code p[0 A 0] = initial(A) * emission[A 1/0]}<br>
	 * {@code p[0 B 0] = initial(B) * emission[B 1/0]}<br>
	 * {@code p[0 C 0] = initial(C) * emission[C 1/0]}<br>
	 * 
	 * {@code p[0 A 1] = initial(A) * emission[A 1/1]}<br>
	 * {@code p[0 B 1] = initial(B) * emission[B 1/1]}<br>
	 * {@code p[0 C 1] = initial(C) * emission[C 1/1]}<br>
	 * </p>
	 * <p>
	 * The probability of Z0 is {@code [A B C]=>p[0 ? 0]+>sum}.
	 * </p>
	 * <p>
	 * The probability of Z1 is {@code [A B C]=>p[0 ? 1]+>sum}.
	 * </p>
	 * <p>
	 * To compute Z1, we must consider all possible transitions.
	 * {@code p[1 A 0] = [A B C]=>( p[0 ? 0] * transition[? A] )+>sum * emission[A 1/0]}<br>
	 * </p>
	 * 
	 * 
	 */
	private Vector<O>
	mostLikelySequenceOfSymbolsBelow( Tuple<? extends I> observed ) {
		Vector<O> result = new Vector<>( );
		int length = observed.size( );
		/**
		 * For each input symbol, we build a virtual state for each possible output.
		 * 
		 * Then we assign probability to each virtual state based on the probability
		 * of emitting that output in all real states.
		 */

		KnittingTuple<TupleValue<O>> ka_below = coalignment_alphabet.below( );
		
		Matrix<Double> probability_real = new DenseMatrix<>(
						length,
						core.number_of_states,
						NumericLogarithm::elnsum2,
						NumericLogarithm.smallLogValue );
		
		Matrix<Double> probability_virtual = new DenseMatrix<>(
						length,
						ka_below.size( ),
						NumericLogarithm::elnsum2,
						NumericLogarithm.smallLogValue );
		
		Cubix<Double> probability_real_virtual = new DenseCubix<>(
						length,
						core.number_of_states,
						ka_below.size( ),
						NumericLogarithm::elnsum2,
						NumericLogarithm.smallLogValue );

		for ( int t = 0; t < length; t++ ) {
			I current_above = observed.get( t );
			TupleValue<I> current_above_tuple = KnittingTuple.on( current_above ).asTupleValue( );
			Set<TupleValue<O>> correspondingBelow = coalignment_alphabet.correspondingBelow( current_above_tuple );
			for ( int s = 0; s < core.number_of_states; s++ ) {
				
				double prob_for_each_output_max = NumericLogarithm.smallLogValue;
				int prob_for_each_output_size = 0;
				double[] prob_for_each_output = new double[core.number_of_symbols];
				
				
				for (int below_id = 0; below_id < ka_below.size( ); below_id++ ) { 
					TupleValue<O> sb = ka_below.get( below_id );
					
					if (! correspondingBelow.contains( sb )) {
						continue;
					}
					// for this state, there is a fixed cost, the cost of emission.
					int encode = coalignment_alphabet
							.encode( current_above_tuple, sb );
					
					double cost = core.emission_table[s][encode];
					
					if ( t > 0 ) {
						double[] sources = new double[core.number_of_states];
						double max = NumericLogarithm.smallLogValue;
						int input = 0;
						for ( ; input < core.number_of_states; input++ ) {

							double tmp = elnproduct( probability_real.apply( t - 1, input ),
									core.transition_table[input][s] );
							
							sources[input] = tmp;
							if (max < tmp) {
								max = tmp;
							}
						}
						double total_from_sources = NumericLogarithm.elnsum( max, sources, input );

						cost = elnproduct( cost, total_from_sources );

					} else {
						cost = elnproduct( cost, core.initial_table[s] );
					}
					
					probability_real_virtual.set(t, s, below_id, cost);
					prob_for_each_output[prob_for_each_output_size] = cost;
					prob_for_each_output_size++;
					if (prob_for_each_output_max < cost) {
						prob_for_each_output_max = cost;
					}
				}
				
				double total_from_symbols = NumericLogarithm.elnsum(
						prob_for_each_output_max,
						prob_for_each_output,
						prob_for_each_output_size );
				
				probability_real.set( t, s, total_from_symbols );
			}


			for ( int below_id = 0; below_id < ka_below.size( ); below_id++) {
				TupleValue<O> sb = ka_below.get( below_id );
				
				if (! correspondingBelow.contains( sb )) {
					continue;
				}
				// for this state, there is a fixed cost, the cost of emission.
				int encode = coalignment_alphabet
						.encode( current_above_tuple, sb );

				double prob_for_each_state_max = NumericLogarithm.smallLogValue;
				int prob_for_each_state_size = 0;
				double[] prob_for_each_state = new double[core.number_of_states];
				
				for ( int s = 0; s < core.number_of_states; s++ ) {
					double cost = core.emission_table[s][encode];


					if ( t > 0 ) {
						double[] sources = new double[core.number_of_states];
						double max = NumericLogarithm.smallLogValue;
						int input = 0;
						for ( ; input < core.number_of_states; input++ ) {

							double tmp = elnproduct( probability_real.apply( t - 1, input ),
									core.transition_table[input][s] );

							sources[input] = tmp;
							if ( max < tmp ) {
								max = tmp;
							}
						}
						double total_from_sources =
								NumericLogarithm.elnsum( max, sources, input );

						cost = elnproduct( cost, total_from_sources );

					}
					else {
						cost = elnproduct( cost, core.initial_table[s] );
					}

					prob_for_each_state[prob_for_each_state_size] = cost;
					prob_for_each_state_size++;
					if ( prob_for_each_state_max < cost ) {
						prob_for_each_state_max = cost;
					}
				}
				double total_from_states = NumericLogarithm.elnsum(
						prob_for_each_state_max,
						prob_for_each_state,
						prob_for_each_state_size );
				
				probability_virtual.set( t, below_id, total_from_states );
			}
		}
		
		/**
		 * 
		 */
		

		Matrix<Double> probability =
				new DenseMatrix<>( length, ka_below.size( ),
						NumericLogarithm::elnsum2,
						NumericLogarithm.smallLogValue );

		Matrix<Integer> pointers =
				new DenseMatrix<>( length, ka_below.size( ), Integer::sum, -1 );


		for ( int t = 0; t < length; t++ ) {

			I current_above = observed.get( t );
			TupleValue<I> current_above_tuple = KnittingTuple.on( current_above ).asTupleValue( );
			Set<TupleValue<O>> correspondingBelow = coalignment_alphabet.correspondingBelow( current_above_tuple );
			/*
			 * For each state s, we must compute the probability of the most probable
			 * state sequence responsible for input:0..t that have s as the final
			 * state AND that the emission at s is a sequence of symbols with length
			 * == gap.
			 */
			for ( int vs_dest = 0; vs_dest < ka_below.size( ); vs_dest++ ) {
				TupleValue<O> vs_dest_below = ka_below.get( vs_dest );

				if (! correspondingBelow.contains( vs_dest_below )) {
					continue;
				}
				
				// for this state, there is a fixed cost, the cost of emission.
				double cost = NumericLogarithm.oneLogValue;

				if ( t > 0 ) {

					I previous_above = observed.get( t - 1 );
					TupleValue<I> previous_above_tuple = KnittingTuple.on( previous_above ).asTupleValue( );
					Set<TupleValue<O>> previous_correspondingBelow = coalignment_alphabet.correspondingBelow( previous_above_tuple );
					
					double max = 0d;
					boolean found = false;
					int best_source = 0;
					for ( int vs_source = 0; vs_source < ka_below.size( ); vs_source++ ) {
						
						Tuple<O> vs_source_below = ka_below.get( vs_source );
						
						if (! previous_correspondingBelow.contains( vs_source_below )) {
							continue;
						}

						/**
						 * we compute the combined cost of transition
						 */

						double prob_for_each_state_max = NumericLogarithm.smallLogValue;
						int prob_for_each_state_size = 0;
						double[] prob_for_each_state =
								new double[core.number_of_states * core.number_of_states];

						for ( int s_source = 0; s_source < core.number_of_states; s_source++ ) {
							
							double xx = probability_real_virtual.get( t - 1, s_source, vs_source );
							
							for ( int s_dest = 0; s_dest < core.number_of_states; s_dest++ ) {
								double c = elnproduct(
										xx,
										probability_real_virtual.get( t, s_dest, vs_dest ),
										core.transition_table[s_source][s_dest] );

								prob_for_each_state[prob_for_each_state_size] = c;
								prob_for_each_state_size++;
								if ( prob_for_each_state_max < c ) {
									prob_for_each_state_max = c;
								}
							}
						}

						double transition_cost = NumericLogarithm.elnsum(
								prob_for_each_state_max,
								prob_for_each_state,
								prob_for_each_state_size );
						transition_cost = elnproduct(transition_cost,
								probability_virtual.get( t - 1 , vs_source ));
						if ( !found || transition_cost > max ) {
							found = true;
							best_source = vs_source;
							max = transition_cost;
						}
					}

					pointers.set( t, vs_dest, best_source );
					cost = max;

				} else {
					cost = probability_virtual.get( t, vs_dest );
				}
				probability.set( t, vs_dest, cost );

			}
		}

		int best_final_state = 0;
		double final_max = 0d;
		boolean final_found = false;
		for ( int s = 0; s < ka_below.size( ); s++ ) {
			Double tmp = probability.apply( length - 1, s );
			if ( !final_found || tmp > final_max ) {
				final_found = true;
				best_final_state = s;
				final_max = tmp;
			}
		}

		Vector<Integer> reconstructPath = reconstructPath(
				( time, state ) -> pointers.apply( time, state ),
				length,
				best_final_state );

		for ( Integer encoded : reconstructPath ) {
			Tuple<O> elements = ka_below.get( encoded );
			for ( int i = 0; i < elements.size( ); i++ )
				result.add( elements.get( i ) );

		}
		return result;
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
