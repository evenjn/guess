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

import static org.github.evenjn.numeric.NumericLogarithm.eexp;
import static org.github.evenjn.numeric.NumericLogarithm.eln;
import static org.github.evenjn.numeric.NumericLogarithm.elndivision;
import static org.github.evenjn.numeric.NumericLogarithm.elnproduct;
import static org.github.evenjn.numeric.NumericLogarithm.elnsum;
import static org.github.evenjn.numeric.NumericLogarithm.elnsum2;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.numeric.NumericUtils;
import org.github.evenjn.numeric.NumericUtils.Summation;
import org.github.evenjn.numeric.SixCharFormat;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12BaumWelch {

	private final double[][][] alpha;

	private final double[][][] beta;

	private final double[][][] gamma;

	private final double[] buffer_states;

	private final double[] buffer_total_edges;

	private final double[][] buffer_transitions;

	private final int number_of_states;

	private final int number_of_symbols;

	private final Markov hmm;

	private final static boolean erase_buffers = true;

	private final static boolean print_debug_expectation = false;

	private final static boolean print_debug_maximization = false;

	private Function<Markov, Boolean> core_inspector;

	public M12BaumWelch(
			Markov hmm,
			Function<Markov, Boolean> core_inspector,
			int total_number_of_edges,
			int max_length_above,
			int max_length_below) {
		this.hmm = hmm;
		this.core_inspector = core_inspector;
		number_of_states = hmm.number_of_states;
		number_of_symbols = hmm.number_of_symbols;
		alpha = new double[max_length_above + 1][max_length_below
				+ 1][number_of_states];
		beta = new double[max_length_above + 1][max_length_below
				+ 1][number_of_states];
		gamma = new double[max_length_above + 1][max_length_below
				+ 1][number_of_states];
		buffer_states = new double[number_of_states];
		buffer_total_edges = new double[1 + total_number_of_edges];
		buffer_transitions = new double[number_of_states][number_of_states];
	}

	public Markov BaumWelch(
			Consumer<String> logger,
			KnittingCursable<TupleAlignmentGraph> observed_cursable,
			final int period,
			final int epochs,
			ProgressSpawner progress_spawner ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"M12BaumWelch::BaumWelch" );
			if ( logger != null ) {
				logger.accept( "Baum-Welch using " + epochs + 
						" epochs, with " + period + " data points each." );
				logger.accept( "Training a model with " + number_of_states + 
						" states and " + number_of_symbols + " symbols." );
				logger.accept( "At the end of each epoch, we will display here the average");
				logger.accept( " probability assigned to a data point by the initial model." );
				logger.accept( "If possible, we will also display the ratio between that average" );
				logger.accept( " and the one obtained in the previous epoch." );
			}
			spawn.target( epochs * period );

			BasicAutoHook[] local = {
					new BasicAutoHook( )
			};
			hook.hook( new AutoCloseable( ) {

				@Override
				public void close( ) {
					local[0].close( );
				}
			} );

			KnittingCursor<TupleAlignmentGraph> observed_re =
					observed_cursable.pull( local[0] );
			/**
			 * Smoothing..
			 */
			final double smoothing_count = 0.000001;
			final int max_epoch = epochs;
			final double uniform_state =
					eln( smoothing_count / ( 1.0 * number_of_states ) );
			final double uniform_symbols =
					eln( smoothing_count / ( 1.0 * number_of_symbols ) );

			Double previous_probability = null;
			/* example: states = 100(!) total space = 800 b */
			final double[] new_initial = new double[number_of_states];

			/* example: states = 100(!) total space = 80 kb */
			final double[][] new_transition =
					new double[number_of_states][number_of_states];

			/* example: states = 100(!) symbols = 1 000 000 total space = 800 Mb */
			final double[][] new_emission =
					new double[number_of_states][number_of_symbols];
			
			for ( int epoch = 0; epoch < max_epoch; epoch++ ) {

				if ( core_inspector != null ) {
					spawn.info( "core inspection at the beginning of epoch " + epoch  );
					Boolean quality_is_ok = core_inspector.apply( hmm );
					if ( quality_is_ok ) {
						return hmm;
					}
				}
				spawn.info( "training at epoch " + epoch );

				for ( int s = 0; s < number_of_states; s++ ) {
					new_initial[s] = uniform_state;
					final double[] new_transitions_from_s = new_transition[s];
					final double[] new_emissions_from_s = new_emission[s];
					for ( int d = 0; d < number_of_states; d++ ) {
						new_transitions_from_s[d] = uniform_state;
					}
					for ( int e = 0; e < number_of_symbols; e++ ) {
						new_emissions_from_s[e] = uniform_symbols;
					}
				}
				double[] probability_of_this_graph = {
						NumericLogarithm.smallLogValue
				};
				Summation summation = null;
				int total = 0;
				if (logger == null) {
					probability_of_this_graph = null;
				}
				else {
					summation = NumericUtils.summation( 10000,
							x -> NumericLogarithm.elnsum( KnittingCursable.wrap( x ) ) );
				}
				int samples = 0;
				for ( ; samples < period; samples++ ) {
					if ( !observed_re.hasNext( ) ) {
						local[0].close( );
						local[0] = new BasicAutoHook( );
						observed_re = observed_cursable.pull( local[0] );
						if ( !observed_re.hasNext( ) ) {
							throw new IllegalArgumentException( "Empty training set" );
						}
					}
					try {
						TupleAlignmentGraph graph = observed_re.next( );
						expectation(
								graph,
								new_initial,
								new_transition,
								new_emission,
								probability_of_this_graph);
						if ( logger != null ) {
							summation.add( probability_of_this_graph[0] );
							total++;
						}
					}
					catch ( PastTheEndException e ) {
						throw new IllegalArgumentException( "Empty training set" );
					}

					spawn.step( 1 );
				}
				if ( logger != null ) {
					double current_probability =
							NumericLogarithm.eexp( summation.getSum( ) ) / ( 1.0 * total );
					if ( previous_probability != null ) {
						double probability_change =
								current_probability / previous_probability;
						logger.accept( "Epoch: " + epoch
								+ "  probability indicator: "
								+ SixCharFormat.nu( false ).apply( current_probability )
								+ "  new/old: "
								+ SixCharFormat.nu( false ).apply( probability_change ) );
					}
					else {
						logger.accept( "Epoch: " + epoch
								+ "  probability indicator: "
								+ SixCharFormat.nu( false ).apply( current_probability ) );
					}
					previous_probability = current_probability;
				}
				maximization(
						new_initial,
						new_transition,
						new_emission,
						samples,
						smoothing_count );
			}

			if ( core_inspector != null ) {
				spawn.info( "final core inspection" );
				Boolean quality_is_ok = core_inspector.apply( hmm );
				if ( quality_is_ok ) {
					return hmm;
				}
			}
			return hmm;
		}
	}

	/*
	 * probability_of_this_graph is (the natural logarithm of) the probability
	 * of the observed graph using the m12 core in its present state.
	 * 
	 * It is useful to compute statistics. It is optional.
	 */
	private void expectation(
			TupleAlignmentGraph observed,
			double[] new_initial,
			double[][] new_transition,
			double[][] new_emission,
			double[] probability_of_this_graph ) {
		if ( observed.la( ) < 2 ) {
			throw new IllegalArgumentException(
					"Sequences of length 0 or 1 as training data are not supported." );
		}
		forward( observed );
		if (probability_of_this_graph != null) {
			probability_of_this_graph[0] = probabilityOf( observed );
		}
		backward( observed );
		double R = r( observed );
		gamma( observed, R );
		initial( observed, R, new_initial );
		transitions( observed, R, new_transition );
		emissions( observed, R, new_emission );
	}

	private double probabilityOf( TupleAlignmentGraph observed ) {
		double max = NumericLogarithm.smallLogValue;
		for ( int s = 0; s < number_of_states; s++ ) {
			final double v = alpha[observed.la( )][observed.lb( )][s];
			if ( max < v ) {
				max = v;
			}
			buffer_states[s] = v;
		}
		return elnsum( max, buffer_states, number_of_states );
	}

	private double r( TupleAlignmentGraph observed ) {
		final int la = observed.la( );
		final int lb = observed.lb( );
		double max = NumericLogarithm.smallLogValue;
		for ( int s = 0; s < number_of_states; s++ ) {
			final double v = elnproduct( alpha[la][lb][s], beta[la][lb][s] );
			buffer_states[s] = v;
			if ( max < v ) {
				max = v;
			}
		}
		return elnsum( max, buffer_states, number_of_states );
	}

	private void forward( TupleAlignmentGraph observed ) {

		if ( erase_buffers ) {
			final int la = observed.la( );
			final int lb = observed.lb( );
			for ( int a = 0; a <= la; a++ ) {
				for ( int b = 0; b <= lb; b++ ) {
					for ( int s = 0; s < number_of_states; s++ ) {
						alpha[a][b][s] = NumericLogarithm.smallLogValue;
					}
				}
			}
		}
		for ( int s = 0; s < number_of_states; s++ ) {
			alpha[0][0][s] = NumericLogarithm.smallLogValue;
		}

		Iterator<TupleAlignmentNode> it = observed.forward( );
		while ( it.hasNext( ) ) {
			TupleAlignmentNode cell = it.next( );
			final int edges = cell.number_of_incoming_edges;
			for ( int destination_s =
					0; destination_s < number_of_states; destination_s++ ) {
				double edge_buffer_max = NumericLogarithm.smallLogValue;
				for ( int edge = 0; edge < edges; edge++ ) {
					final int x = cell.incoming_edges[edge][0];
					final int y = cell.incoming_edges[edge][1];
					final int encoded = cell.incoming_edges[edge][2];
					double cost;
					if ( x == 0 && y == 0 ) {
						cost = hmm.initial_table[destination_s];
					} else {
						double max = NumericLogarithm.smallLogValue;
						for ( int source_s = 0; source_s < number_of_states; source_s++ ) {
							final double v = elnproduct(
									alpha[x][y][source_s],
									hmm.transition_table[source_s][destination_s] );
							buffer_states[source_s] = v;
							if ( max < v ) {
								max = v;
							}
						}
						cost = elnsum( max, buffer_states, number_of_states );
					}
					cost = elnproduct( cost, hmm.emission_table[destination_s][encoded] );
					buffer_total_edges[edge] = cost;
					if ( edge_buffer_max < cost ) {
						edge_buffer_max = cost;
					}
				}
				alpha[cell.a][cell.b][destination_s] =
						elnsum( edge_buffer_max, buffer_total_edges, edges );
			}
		}

		if ( print_debug_expectation ) {
			System.out.println( "Forward - M12" );
			final int la1 = observed.la( );
			final int lb1 = observed.lb( );
			for ( int a = 0; a <= la1; a++ ) {
				for ( int b = 0; b <= lb1; b++ ) {
					for ( int s = 0; s < number_of_states; s++ ) {
						System.out
								.println( "" + a + " " + b + " " + eexp( alpha[a][b][s] ) );
					}
				}
			}
		}
	}

	private void backward( TupleAlignmentGraph observed ) {
		final int la = observed.la( );
		final int lb = observed.lb( );
		if ( erase_buffers ) {
			for ( int a = 0; a <= la; a++ ) {
				for ( int b = 0; b <= lb; b++ ) {
					for ( int s = 0; s < number_of_states; s++ ) {
						beta[a][b][s] = NumericLogarithm.smallLogValue;
					}
				}
			}
		}
		for ( int s = 0; s < hmm.number_of_states; s++ ) {
			beta[la][lb][s] = NumericLogarithm.oneLogValue;
		}

		Iterator<TupleAlignmentNode> it = observed.backward( );
		while ( it.hasNext( ) ) {
			TupleAlignmentNode cell = it.next( );
			final int a_ = cell.a;
			final int b_ = cell.b;
			final int edges = cell.number_of_incoming_edges;
			for ( int edge = 0; edge < edges; edge++ ) {
				int x = cell.incoming_edges[edge][0];
				int y = cell.incoming_edges[edge][1];
				int encoded = cell.incoming_edges[edge][2];
				for ( int source_s = 0; source_s < number_of_states; source_s++ ) {
					double max = NumericLogarithm.smallLogValue;
					for ( int destination_s =
							0; destination_s < number_of_states; destination_s++ ) {
						final double v = elnproduct(
								beta[a_][b_][destination_s],
								hmm.transition_table[source_s][destination_s],
								hmm.emission_table[destination_s][encoded] );
						buffer_states[destination_s] = v;
						if ( max < v ) {
							max = v;
						}
					}
					final double cost = elnsum( max, buffer_states, number_of_states );
					beta[x][y][source_s] = elnsum2( cost, beta[x][y][source_s] );
				}
			}
		}
		if ( print_debug_expectation ) {
			System.out.println( "Backward - M12" );
			for ( int a = 0; a <= la; a++ ) {
				for ( int b = 0; b <= lb; b++ ) {
					for ( int s = 0; s < number_of_states; s++ ) {
						System.out
								.println( "" + a + " " + b + " " + eexp( beta[a][b][s] ) );
					}
				}
			}
		}
	}

	private void gamma( TupleAlignmentGraph observed, double R ) {
		Iterator<TupleAlignmentNode> it = observed.forward( );
		while ( it.hasNext( ) ) {
			TupleAlignmentNode cell = it.next( );
			final int a = cell.a;
			final int b = cell.b;
			for ( int s = 0; s < number_of_states; s++ ) {
				final double val = elnproduct( alpha[a][b][s], beta[a][b][s] );
				gamma[a][b][s] = elndivision( val, R );
			}
		}
		if ( print_debug_expectation ) {
			final int la = observed.la( );
			final int lb = observed.lb( );
			System.out.println( "Gamma - M12" );
			for ( int a = 0; a <= la; a++ ) {
				for ( int b = 0; b <= lb; b++ ) {
					for ( int s = 0; s < number_of_states; s++ ) {
						System.out
								.println( "" + a + " " + b + " " + eexp( gamma[a][b][s] ) );
					}
				}
			}
		}
	}

	/**
	 * Asymptotic computational time cost is N*E
	 * 
	 */
	private void initial(
			TupleAlignmentGraph observed,
			double R,
			double[] new_initial ) {
		for ( int s = 0; s < number_of_states; s++ ) {
			double max = new_initial[s];
			int len = 1;
			buffer_total_edges[0] = max;
			Iterator<TupleAlignmentNode> it = observed.forward( );
			while ( it.hasNext( ) ) {
				TupleAlignmentNode cell = it.next( );
				final int edges = cell.number_of_incoming_edges;
				for ( int edge = 0; edge < edges; edge++ ) {
					final int x = cell.incoming_edges[edge][0];
					final int y = cell.incoming_edges[edge][1];
					if ( x == 0 && y == 0 ) {
						double v = gamma[cell.a][cell.b][s];
						if ( max < v ) {
							max = v;
						}
						buffer_total_edges[len++] = v;
					}
				}
			}
			new_initial[s] = elnsum( max, buffer_total_edges, len );
		}
		if ( print_debug_maximization ) {
			System.out.println( "NewInitial - M12" );
			for ( int s = 0; s < number_of_states; s++ ) {
				System.out.println( "" + s + " " + eexp( new_initial[s] ) );
			}
		}
	}

	/**
	 * Asymptotic computational time cost is N*N*E
	 * 
	 */
	private void transitions(
			TupleAlignmentGraph observed,
			double R,
			double[][] new_transition ) {
		for ( int s = 0; s < number_of_states; s++ ) {
			for ( int d = 0; d < number_of_states; d++ ) {
				double transition_cost = hmm.transition_table[s][d];
				double max = NumericLogarithm.smallLogValue;
				int len = 0;
				Iterator<TupleAlignmentNode> it = observed.forward( );
				while ( it.hasNext( ) ) {
					TupleAlignmentNode cell = it.next( );
					final int a = cell.a;
					final int b = cell.b;
					final int edges = cell.number_of_incoming_edges;
					final double beta_cost = beta[a][b][d];
					for ( int edge = 0; edge < edges; edge++ ) {
						final int x = cell.incoming_edges[edge][0];
						final int y = cell.incoming_edges[edge][1];
						final int e = cell.incoming_edges[edge][2];
						final double emission_cost = hmm.emission_table[d][e];
						double v = elnproduct(
								alpha[x][y][s],
								transition_cost,
								emission_cost,
								beta_cost );
						if ( max < v ) {
							max = v;
						}
						buffer_total_edges[len++] = v;
					}
				}
				buffer_transitions[s][d] =
						elndivision( elnsum( max, buffer_total_edges, len ), R );
			}
		}
		for ( int s = 0; s < number_of_states; s++ ) {
			double max = NumericLogarithm.smallLogValue;
			int len = 0;
			Iterator<TupleAlignmentNode> it = observed.forward( );
			while ( it.hasNext( ) ) {
				TupleAlignmentNode cell = it.next( );
				if ( !it.hasNext( ) ) {
					/* we want to count transitions */
					break;
				}
				final double v = gamma[cell.a][cell.b][s];
				if ( max < v ) {
					max = v;
				}
				buffer_total_edges[len++] = v;
			}
			double denominator = elnsum( max, buffer_total_edges, len );
			for ( int d = 0; d < number_of_states; d++ ) {
				double numerator = buffer_transitions[s][d];
				new_transition[s][d] =
						elnsum2(
								new_transition[s][d],
								elndivision( numerator, denominator ) );
			}
		}
		if ( print_debug_maximization ) {
			System.out.println( "NewTransitions - M12" );
			for ( int s = 0; s < number_of_states; s++ ) {
				for ( int d = 0; d < number_of_states; d++ ) {
					System.out
							.println( "" + s + " " + d + " " + eexp( new_transition[s][d] ) );
				}
			}
		}
	}

	private void emissions(
			TupleAlignmentGraph observed,
			double R,
			double[][] new_emission ) {
		for ( int s = 0; s < number_of_states; s++ ) {
			int len = 0;
			double max = NumericLogarithm.smallLogValue;
			Iterator<TupleAlignmentNode> it = observed.forward( );
			while ( it.hasNext( ) ) {
				TupleAlignmentNode cell = it.next( );
				double v = gamma[cell.a][cell.b][s];
				if ( max < v ) {
					max = v;
				}
				buffer_total_edges[len++] = v;
			}
			double denominator = elnsum( max, buffer_total_edges, len );
			double[] new_emission_for_this_state = new_emission[s];
			for ( Integer e_type : observed.combinations( ) ) {
				max = NumericLogarithm.smallLogValue;
				double max_across_all_edges = max;
				len = 0;
				double emission_cost = hmm.emission_table[s][e_type];
				it = observed.forward( );
				while ( it.hasNext( ) ) {
					TupleAlignmentNode cell = it.next( );
					final int a = cell.a;
					if ( a == 0 ) {
						continue;
					}
					final int b = cell.b;
					final int edges = cell.number_of_incoming_edges;
					for ( int edge = 0; edge < edges; edge++ ) {
						final int e = cell.incoming_edges[edge][2];
						if ( e != e_type ) {
							continue;
						}
						final int x = cell.incoming_edges[edge][0];
						final int y = cell.incoming_edges[edge][1];
						double zeno;
						if ( x == 0 && y == 0 ) {
							zeno = hmm.initial_table[s];
						} else {
							for ( int z = 0; z < number_of_states; z++ ) {
								double v =
										elnproduct( alpha[x][y][z], hmm.transition_table[z][s] );
								if ( max < v ) {
									max = v;
								}
								buffer_states[z] = v;
							}
							zeno = elnsum( max, buffer_states, number_of_states );
						}
						double cost = elnproduct(
								emission_cost,
								zeno,
								beta[a][b][s] );
						cost = elndivision( cost, R );
						if ( max_across_all_edges < cost ) {
							max_across_all_edges = cost;
						}
						buffer_total_edges[len++] = cost;
					}
				}
				double numerator =
						elnsum( max_across_all_edges, buffer_total_edges, len );
				double value_to_add = elndivision( numerator, denominator );
				new_emission_for_this_state[e_type] = elnsum2(
						new_emission_for_this_state[e_type],
						value_to_add );
			}
		}
		if ( print_debug_maximization ) {
			System.out.println( "NewEmissions - M12" );
			for ( int s = 0; s < number_of_states; s++ ) {
				for ( int e = 0; e < number_of_symbols; e++ ) {
					System.out
							.println( "" + s + " " + e + " " + eexp( new_emission[s][e] ) );
				}
			}
		}
	}

	private void maximization(
			double[] new_initial,
			double[][] new_transition,
			double[][] new_emission,
			int samples,
			double smoothing_count ) {
		double denominator = eln( samples );
		double denominator_smoothing = eln( smoothing_count );
		denominator = elnsum2( denominator, denominator_smoothing );
		for ( int s = 0; s < number_of_states; s++ ) {
			hmm.initial_table[s] = elndivision( new_initial[s], denominator );
			double[] new_transition_for_this_state = new_transition[s];
			for ( int d = 0; d < number_of_states; d++ ) {
				hmm.transition_table[s][d] =
						elndivision( new_transition_for_this_state[d], denominator );
			}
			double[] new_emission_for_this_state = new_emission[s];
			for ( int y = 0; y < number_of_symbols; y++ ) {
				hmm.emission_table[s][y] =
						elndivision( new_emission_for_this_state[y], denominator );
			}
		}
	}

}
