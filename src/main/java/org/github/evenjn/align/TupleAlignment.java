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
package org.github.evenjn.align;

import java.util.HashSet;

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.Tuple;

/*
 * These are notes about how Markov models use the tuple alignment graph
 * data structure.
 * 
 * T is the number of symbols (above), N is the number of states.
 * 
 * in the regular forward procedure (same applies to backward procedure)
 * we partition the paths in 1+T sections, each section having N nodes,
 * each node associated to a state, and each state is connected to all
 * the nodes of the next section, each edge having a cost that is
 * the cost of the node-to-node transition times the cost of the emission
 * of the T-th symbol.  
 * 
 * Section 0 has only one node, and there is an edge to each node
 * of section 1, with cost set to the initial probability.
 * 
 * We can sort of compress this representation into a chain of macro-states
 * where each macro-state represents a section.
 * In this chain, the first arc represent the choice of initial state.
 * Each arc represents the emission of one symbol.
 * 
 * 
 * In a one-to-n setting, under the assumption of one arc per input
 * element, an arc represents 0..(n + 1) symbols. We can still compress the
 * paths graph using macro-states, but the graph will be a DAG. The macro
 * states can be arranged into a matrix NxM, where M is the number of output
 * symbols.
 * 
 * When only 1-to-1 emissions are allowed, there is one state at each cell
 * in the main diagonal. Only pairs of the same length are supported.
 * 
 * When there is no limit to the "many" size of the one-to-many emissions,
 * there will be one macro-state at each cell.
 * 
 *
 *
 * In the one-to-many system, we extends the alpha table to account for
 * the situation where the system takes an alternate route and emits
 * 0 or more symbols below. To this purpose, we extend the alpha table to
 * track how many symbols above and below have been emitted.
 * 
 *           C/k     A/o     L/5     L/-     E/-     D/d
 * B -> [s1] -> [s1] -> [s1] -> [s1] -> [s1] -> [s1] -> E
 * 
 *   - k o l d
 * - B * * * *
 * C * * * * *
 * A * * * * *
 * L * * * * *
 * L * * * * *
 * E * * * * *
 * D * * * * *
 *             
 * In position [0 0 s1] we store nothing because we will never allow
 * -/- emissions.
 * 
 * In position [0 1 s1] we could store the probability of being in s1 after
 * observing -/k but this makes sense only if we allow 0-to-1 emissions.
 * 
 * In position [1 0 s1] we store the probability of being in s1 after
 * observing C/-.
 * 
 * [1 0] can be reached only from the initial state, so the value to cache
 * in [ 1 0 s1 ] is computed as
 * 
 * initial( s1 ) * emissions( s1 , "C/-" )
 * 
 * 
 * In position [1 1 s3] we store the probability of being in s3 after
 * observing C/k.
 * 
 * 
 * Because we consider only ONE-to-many, it's not possible to observe
 * -/k C/-, so [1 1] cannot be reached from [0 1]. In fact, [1 0] can be
 * reached only from [0 0], so the value to cache in [ 1 1 s3 ]
 * is also computed as
 * 
 * initial( s3 ) * emissions( s3 , "C/k" )
 * 
 * In position [2 2 s3] we store the probability of being in s3 after
 * observing CA/ko.
 * 
 * [2 2] can be reached from [1 1] through the composition of C/k A/o
 * In addition, it can be reached from [1 0], throught the composition of
 * C/- A/ko. Finally, it can be reached from [1 2], through the composition
 * of C/ko A/-
 * 
 * This algorithm computes, for each cell in the matrix, a set of edges.
 * Each edge is represented using the coordinate of the target cell.
 * 
 * For example: in position [0 0], no edges. in position [1 0] a single edge
 * [0 0]. In position [1 1] again [0 0]. In position [2 2] we store
 * [1 0] [1 1] [1 2].
 * 
 * What exact edges there are, it depends on two parameters.
 * 
 * If we force a ONE-to-many schema, in each cell there can be at most as
 * many edges as there are elements in the string below. 
 *
 */
public class TupleAlignment {
	
	
	
	public static <SymbolAbove, SymbolBelow>
			TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>
			createAlphabet(
					Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
					int min_below,
					int max_below,
					Progress mexus ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			int record_max_length_above = 0;
			int record_max_length_below = 0;
			int record_max_number_of_edges = 0;
			TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> alphabet =
					new TupleAlignmentAlphabet<>( );
			HashSet<TupleAlignmentPair<SymbolAbove, SymbolBelow>> observed_so_far =
					new HashSet<>( );
			for ( Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).once( hook ) ) {
				if ( mexus != null ) {
					mexus.step( );
				}
				KnittingTuple<? extends SymbolAbove> ka =
						KnittingTuple.wrap( datum.first );
				KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( datum.second );

				int la = ka.size( );
				int lb = kb.size( );

				if ( record_max_length_above < la ) {
					record_max_length_above = la;
				}

				if ( record_max_length_below < lb ) {
					record_max_length_below = lb;
				}

				try {
					TupleAlignmentNode[][] matrix =
							pathMatrix( ka.size( ), kb.size( ), min_below, max_below );
					int current_number_of_edges = 0;
					for ( int a = 0; a <= la; a++ ) {
						for ( int b = 0; b <= lb; b++ ) {
							if ( matrix[a][b] == null || ( a == 0 && b == 0 ) ) {
								continue;
							}
							int[][] ie = matrix[a][b].incoming_edges;
							int no_ie = matrix[a][b].number_of_incoming_edges;
							current_number_of_edges = current_number_of_edges + no_ie;

							for ( int e_i = 0; e_i < no_ie; e_i++ ) {
								int x = ie[e_i][0];
								int y = ie[e_i][1];
								SymbolAbove suba = ka.get( x );
								KnittingTuple<SymbolBelow> subb = kb.sub( y, b - y );

								TupleAlignmentPair<SymbolAbove, SymbolBelow> pair =
										new TupleAlignmentPair<>( );
								pair.above = suba;
								pair.below = subb;
								if ( !observed_so_far.contains( pair ) ) {
									observed_so_far.add( pair );
									alphabet.add( pair );
								}

							}
						}
					}
					if ( record_max_number_of_edges < current_number_of_edges ) {
						record_max_number_of_edges = current_number_of_edges;
					}
				}
				catch ( NotAlignableException e ) {
					// simply ignore them.
				}
			}

			alphabet.record_max_length_above = record_max_length_above;
			alphabet.record_max_length_below = record_max_length_below;
			alphabet.record_max_number_of_edges = record_max_number_of_edges;

			return alphabet;
		}
	}

	public static <SymbolAbove, SymbolBelow>
			TupleAlignmentGraph
			coalign(
					TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> alphabet,
					Tuple<? extends SymbolAbove> above,
					Tuple<SymbolBelow> below,
					int min_below,
					int max_below )
					throws NotAlignableException {
		TupleAlignmentNode[][] matrix =
				pathMatrix( above.size( ), below.size( ), min_below, max_below );
		TupleAlignmentGraph graph = new TupleAlignmentGraph( );
		graph.matrix = matrix;
		KnittingTuple<? extends SymbolAbove> ka = KnittingTuple.wrap( above );
		KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( below );

		int la = above.size( );
		int lb = below.size( );
		graph.set( la, lb );

		for ( int a = 0; a <= la; a++ ) {
			for ( int b = 0; b <= lb; b++ ) {
				if ( matrix[a][b] == null || ( a == 0 && b == 0 ) ) {
					continue;
				}
				int[][] ie = matrix[a][b].incoming_edges;
				int no_ie = matrix[a][b].number_of_incoming_edges;

				for ( int e_i = 0; e_i < no_ie; e_i++ ) {
					int x = ie[e_i][0];
					int y = ie[e_i][1];

					SymbolAbove key = ka.get( x );
					KnittingTuple<SymbolBelow> sub = kb.sub( y, b - y );
					ie[e_i][2] = alphabet.encode( key, sub );
				}
			}
		}
		return graph;
	}

	public static TupleAlignmentNode[][]
			pathMatrix(
					int labove,
					int lbelow,
					int min_below,
					int max_below )
					throws NotAlignableException {

		/*
		 * If we force a ONE-to-many schema, in each cell there can be at most as
		 * many edges as there are elements in the string below.
		 */
		int max_number_of_edges = lbelow + 1;

		/*
		 * In a MANY-to-MANY schema, we would need two ints to identify the target
		 * of each edge. In a ONE-to-MANY, we just need one. In a MANY-to-MANY
		 * schema, each edge would be represented as a short array with length 2.
		 */

		TupleAlignmentNode[][] rich_structure =
				new TupleAlignmentNode[1 + labove][1 + lbelow];

		// we fill in the structure

		for ( int a = 0; a <= labove; a++ ) {
			for ( int b = 0; b <= lbelow; b++ ) {

				if ( a == 0 && b == 0 ) {
					TupleAlignmentNode root = new TupleAlignmentNode( );
					root.is_reachable_from_beginning = true;
					rich_structure[0][0] = root;
					continue;
				}

				if ( a == 0 && b != 0 ) {
					rich_structure[a][b] = null;
					continue;
				}

				TupleAlignmentNode parent_node = rich_structure[a - 1][b];

				if ( parent_node == null
						|| !parent_node.is_reachable_from_beginning ) {
					// the macro state above us is not reachable.
					continue;
				}

				for ( int z = b + min_below; z <= b + max_below && z <= lbelow; z++ ) {

					// additional condition to prune tree: the end of the string
					// must be in range from [a z].

					int leftover_above = labove - a;

					if ( z + leftover_above * max_below < lbelow
							|| z + leftover_above * min_below > lbelow ) {
						continue;
					}

					TupleAlignmentNode rich_dest = rich_structure[a][z];
					if ( rich_dest == null ) {
						rich_dest = new TupleAlignmentNode( );
						rich_dest.incoming_edges = new int[max_number_of_edges][3];
						rich_structure[a][z] = rich_dest;
					}

					int edges_in_rich_destination = rich_dest.number_of_incoming_edges;

					rich_dest.incoming_edges[edges_in_rich_destination][0] = a - 1;
					rich_dest.incoming_edges[edges_in_rich_destination][1] = b;

					rich_dest.is_reachable_from_beginning = true;

					rich_dest.number_of_incoming_edges = edges_in_rich_destination + 1;
				}
			}
		}

		if ( rich_structure[labove][lbelow] == null
				|| !rich_structure[labove][lbelow].is_reachable_from_beginning ) {
			/* The pair is not representable */
			throw NotAlignableException.neo;
		}

		rich_structure[labove][lbelow].is_reachable_from_end = true;

		for ( int a = labove; a >= 0; a-- ) {
			for ( int b = lbelow; b >= 0; b-- ) {
				TupleAlignmentNode node = rich_structure[a][b];

				if ( node == null ) {
					continue;
				}
				if ( !node.is_reachable_from_end ) {
					rich_structure[a][b] = null;
				}

				int rich_edges = node.number_of_incoming_edges;

				for ( int e = 0; e < rich_edges; e++ ) {

					int x = node.incoming_edges[e][0];
					int y = node.incoming_edges[e][1];

					TupleAlignmentNode origin = rich_structure[x][y];
					origin.is_reachable_from_end = true;
				}
			}
		}
		return rich_structure;
	}

}
