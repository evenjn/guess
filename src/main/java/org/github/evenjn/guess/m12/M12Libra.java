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
import static org.github.evenjn.numeric.NumericLogarithm.elnsum;

import java.util.Iterator;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentGraphFactory;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.guess.m12.core.M12Core;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.yarn.Tuple;

public class M12Libra<I, O> {

	private M12Core core;

	private TupleAlignmentAlphabet<I, O> coalignment_alphabet;
	private int max_length_above = 0;
	private int max_length_below = 0;
	private int total_number_of_edges;

	public M12Libra(
			TupleAlignmentAlphabet<I, O> coalignment_alphabet,
			M12Core hmm ) {
		core = hmm;
		this.coalignment_alphabet = coalignment_alphabet;
		buffer_states = new double[hmm.number_of_states];
	}
	
	public double weigh(Bi<
			Tuple<I>,
			Tuple<O> > bi ) throws NotAlignableException {
		Tuple<O> below = bi.back( );
		Tuple<I> above = bi.front( );
		boolean must_update_alpha = false;
		boolean must_update_eb = false;
		if (above.size( ) > max_length_above) { 
			max_length_above = above.size( );
			must_update_alpha = true;
		}
		if (below.size( ) > max_length_below) { 
			max_length_below = below.size( );
			must_update_alpha = true;
		}
		
		TupleAlignmentGraph observed = TupleAlignmentGraphFactory.graph(
				(a, b) -> coalignment_alphabet.encode( a,  b ),
				above,
				below,
				1,
				1,
				coalignment_alphabet.getMinBelow( ),
				coalignment_alphabet.getMaxBelow( ) );
		

		Iterator<TupleAlignmentNode> it = observed.forward( );
		while ( it.hasNext( ) ) {
			TupleAlignmentNode cell = it.next( );
			if ( cell.number_of_incoming_edges > total_number_of_edges) {
				total_number_of_edges = cell.number_of_incoming_edges;
				must_update_eb = true;
			}
		}

		if ( must_update_eb ) {
			buffer_total_edges = new double[1 + total_number_of_edges];
		}

		if ( must_update_alpha ) {
			alpha = new double
					[max_length_above + 1]
					[max_length_below + 1]
					[core.number_of_states];
		}
		
		forward( observed );
		double max = NumericLogarithm.smallLogValue;
		for ( int s = 0; s < core.number_of_states; s++ ) {
			final double v = alpha[observed.la( )][observed.lb( )][s];
			if ( max < v ) {
				max = v;
			}
			buffer_states[s] = v;
		}
		return elnsum( max, buffer_states, core.number_of_states );

	}

	private double[][][] alpha;

	private double[] buffer_total_edges;

	private final double[] buffer_states;

	private void forward( TupleAlignmentGraph observed ) {

		final int la = observed.la( );
		final int lb = observed.lb( );
		for ( int a = 0; a <= la; a++ ) {
			for ( int b = 0; b <= lb; b++ ) {
				for ( int s = 0; s < core.number_of_states; s++ ) {
					alpha[a][b][s] = NumericLogarithm.smallLogValue;
				}
			}
		}
		for ( int s = 0; s < core.number_of_states; s++ ) {
			alpha[0][0][s] = NumericLogarithm.smallLogValue;
		}

		Iterator<TupleAlignmentNode> it = observed.forward( );
		while ( it.hasNext( ) ) {
			TupleAlignmentNode cell = it.next( );
			final int edges = cell.number_of_incoming_edges;
			for ( int destination_s = 0; destination_s < core.number_of_states; destination_s++ ) {
				double edge_buffer_max = NumericLogarithm.smallLogValue;
				for ( int edge = 0; edge < edges; edge++ ) {
					final int x = cell.incoming_edges[edge][0];
					final int y = cell.incoming_edges[edge][1];
					final int encoded = cell.incoming_edges[edge][2];
					double cost;
					if ( x == 0 && y == 0 ) {
						cost = core.initial_table[destination_s];
					}
					else {
						double max = NumericLogarithm.smallLogValue;
						for ( int source_s = 0; source_s < core.number_of_states; source_s++ ) {
							final double v = elnproduct(
									alpha[x][y][source_s],
									core.transition_table[source_s][destination_s] );
							buffer_states[source_s] = v;
							if ( max < v ) {
								max = v;
							}
						}
						cost = elnsum( max, buffer_states, core.number_of_states );
					}
					cost = elnproduct( cost, core.emission_table[destination_s][encoded] );
					buffer_total_edges[edge] = cost;
					if ( edge_buffer_max < cost ) {
						edge_buffer_max = cost;
					}
				}
				alpha[cell.a][cell.b][destination_s] =
						elnsum( edge_buffer_max, buffer_total_edges, edges );
			}
		}
	}

}
