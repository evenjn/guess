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
package org.github.evenjn.guess.m12.aligner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiFunction;

import org.github.evenjn.align.AlignmentElement;
import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentGraphFactory;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.BiValueTray;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Equivalencer;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.numeric.Cubix;
import org.github.evenjn.numeric.DenseCubix;
import org.github.evenjn.numeric.NumericLogarithm;

class AlignmentElementImpl<I, O> extends BiValueTray<I, O> implements AlignmentElement<I, O> {

	protected AlignmentElementImpl(I front, O back,
			Equivalencer<I, Object> front_equivalencer,
			Equivalencer<O, Object> back_equivalencer) {
		super( front, back, front_equivalencer, back_equivalencer );
	}
	
}

public class M12Aligner<I, O> implements
		TupleAligner<I, O> {

	private Markov core;

	private TupleAlignmentAlphabet<I, O> coalignment_alphabet;

	public M12Aligner(
			TupleAlignmentAlphabet<I, O> coalignment_alphabet,
			Markov core) {
		this.coalignment_alphabet = coalignment_alphabet;
		this.core = core;
	}

	private static class Source {

		int x;

		int y;

		int state;
	}

	public KnittingTuple<AlignmentElement<Integer, Integer>> align(
			Tuple<I> above,
			Tuple<O> below ) {

		Integer length_above = above.size( );
		Integer length_below = below.size( );

		TupleAlignmentGraph coalign;
		try {
			coalign = TupleAlignmentGraphFactory.graph(
					( a, b ) -> coalignment_alphabet.encode( a, b ),
					above,
					below,
					1,
					1,
					coalignment_alphabet.getMinBelow( ),
					coalignment_alphabet.getMaxBelow( ) );
		}
		catch ( NotAlignableException e ) {
			final Integer front = length_above;
			final Integer back = length_below;
			return KnittingTuple
					.on( new AlignmentElementImpl<>(
							front,
							back,
							KnittingTuple.getNullEquivalencer( ),
							KnittingTuple.getNullEquivalencer( ) ) );
		}

		// find the path with the max probability.
		// use Viterbi

		/**
		 * This tri-map contains information about the best source for each node.
		 * 
		 * [ a b ] -> { x y state }
		 * 
		 */

		Vector<BiHashMap<Integer, Integer, Source>> all_pointers = new Vector<>( );

		for ( int s = 0; s < core.number_of_states; s++ ) {
			all_pointers.add( new BiHashMap<Integer, Integer, Source>( null ) );
		}

		/**
		 * [ x y s ] -> the probability of the automa to be in state s after the
		 * emission of the first x observed symbols above and the first y symbols
		 * below
		 * 
		 * (given the whole sequence above/below).
		 */
		Cubix<Double> probability =
				new DenseCubix<>( 1 + length_above, 1 + length_below,
						core.number_of_states,
						NumericLogarithm::elnsum2,
						NumericLogarithm.smallLogValue );

		/**
		 * \ | t a k s - o . . . . T o o o . . A . . o o o X . . . . o
		 */

		/**
		 * For each node, for each state, compute the best path. The best path
		 * includes the incoming edge.
		 */

		Iterator<TupleAlignmentNode> it = coalign.forward( );
		while ( it.hasNext( ) ) {
			TupleAlignmentNode node = it.next( );
			// skip the root.
			if ( node.number_of_incoming_edges == 0 ) {
				System.out.println( "Skipping the root." );
				continue;
			}

			int a = node.a;
			int b = node.b;

			/**
			 * for each state, we must consider all ways to reach [a b]
			 */
			for ( int d = 0; d < core.number_of_states; d++ ) {

				final int edges = node.number_of_incoming_edges;

				double best_path_prob = NumericLogarithm.smallLogValue;
				int best_source_x = -1;
				int best_source_y = -1;
				int best_source_state = -1;
				boolean found = false;

				for ( int edge = 0; edge < edges; edge++ ) {

					final int x = node.incoming_edges[edge][0];
					final int y = node.incoming_edges[edge][1];
					final int encoded = node.incoming_edges[edge][2];

					/*
					 * consider this edge
					 */
					if ( a != 1 + x ) {
						throw new IllegalStateException(
								"This is not a valid M12 alignment!" );
					}

					if ( x == 0 && y == 0 ) {

						double this_path_prob = NumericLogarithm.elnproduct(
								core.initial_table[d],
								core.emission_table[d][encoded] );

						if ( !found || this_path_prob > best_path_prob ) {
							best_path_prob = this_path_prob;
							best_source_x = 0;
							best_source_y = 0;
							best_source_state = -1;
							found = true;
						}

						continue;
					}

					double p_emission = core.emission_table[d][encoded];

					/*
					 * assuming we have reached [a b] along this edge, what is the best
					 * state we came from?
					 */
					for ( int s = 0; s < core.number_of_states; s++ ) {

						double this_path_prob = NumericLogarithm.elnproduct(
								probability.get( x, y, s ),
								core.transition_table[s][d],
								p_emission );

						if ( !found || this_path_prob > best_path_prob ) {
							best_path_prob = this_path_prob;
							best_source_x = x;
							best_source_y = y;
							best_source_state = s;
							found = true;
						}
					}
				}

				probability.set( a, b, d, best_path_prob );

				/**
				 * Tracks the best source for [ a b d ]
				 */
				BiHashMap<Integer, Integer, Source> pointers =
						all_pointers.elementAt( d );
				Source existing_source = pointers.apply( a, b );
				if ( existing_source == null ) {
					existing_source = new Source( );
					pointers.map( a, b, existing_source );
				}

				existing_source.x = best_source_x;
				existing_source.y = best_source_y;
				existing_source.state = best_source_state;
			}
		}

		int best_final_state = 0;
		double final_max = NumericLogarithm.smallLogValue;
		boolean final_found = false;
		for ( int s = 0; s < core.number_of_states; s++ ) {
			Double tmp = probability.get( length_above, length_below, s );
			if ( !final_found || tmp > final_max ) {
				final_found = true;
				best_final_state = s;
				final_max = tmp;
			}
		}

		/**
		 * Reconstruct path
		 */
		int state = best_final_state;

		Vector<AlignmentElement<Integer, Integer>> result = new Vector<>( );
		while ( length_above != 0 || length_below != 0 ) {
			Source source =
					all_pointers.get( state ).apply( length_above, length_below );
			final Integer front = length_above - source.x;
			final Integer back = length_below - source.y;
			result.add( new AlignmentElementImpl<>(
					front,
					back,
					KnittingTuple.getNullEquivalencer( ),
					KnittingTuple.getNullEquivalencer( ) ) );
			state = source.state;
			length_above = source.x;
			length_below = source.y;
		}
		Collections.reverse( result );
		return KnittingTuple.wrap( result );
	}

}

class BiHashMap<K1, K2, V> implements
		BiFunction<K1, K2, V> {

	protected Map<K1, Map<K2, V>> core = new HashMap<K1, Map<K2, V>>( );

	protected final V ifAbsent;

	public BiHashMap(V ifAbsent) {
		this.ifAbsent = ifAbsent;
	}

	public void map( K1 row, K2 col, V val ) {
		Map<K2, V> map = core.get( row );
		if ( map == null ) {
			map = new HashMap<K2, V>( );
			core.put( row, map );
		}
		map.put( col, val );
	}

	public V apply( K1 row, K2 col ) {
		Map<K2, V> map = core.get( row );
		if ( map == null ) {
			return ifAbsent;
		}
		V double1 = map.get( col );
		if ( double1 == null )
			return ifAbsent;
		return double1;
	}

}
