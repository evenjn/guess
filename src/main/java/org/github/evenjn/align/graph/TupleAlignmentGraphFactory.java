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
package org.github.evenjn.align.graph;

import java.util.function.BiFunction;

import org.github.evenjn.knit.KnittingTuple;
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
public class TupleAlignmentGraphFactory {

	public static <SymbolAbove, SymbolBelow>
			TupleAlignmentGraph
			graph(
					BiFunction<SymbolAbove, Tuple<SymbolBelow>, Integer> pair_encoder,
					Tuple<SymbolAbove> above,
					Tuple<SymbolBelow> below,
					final int min_below,
					final int max_below )
					throws NotAlignableException {

		KnittingTuple<SymbolAbove> ka = KnittingTuple.wrap( above );
		KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( below );
		final int labove = above.size( );
		final int lbelow = below.size( );

		/*
		 * If we force a ONE-to-many schema, in each cell there can be at most as
		 * many edges as there are elements in the string below.
		 */
		final int max_number_of_edges = lbelow + 1;

		/*
		 * In a MANY-to-MANY schema, we would need two ints to identify the target
		 * of each edge. In a ONE-to-MANY, we just need one. In a MANY-to-MANY
		 * schema, each edge would be represented as a short array with length 2.
		 */

		TupleAlignmentNode[][] matrix =
				new TupleAlignmentNode[1 + labove][1 + lbelow];

		// we fill in the structure

		for ( int a = 0; a <= labove; a++ ) {
			for ( int b = 0; b <= lbelow; b++ ) {

				if ( a == 0 && b == 0 ) {
					TupleAlignmentNode root = new TupleAlignmentNode( );
					matrix[0][0] = root;
					continue;
				}

				if ( a == 0 && b != 0 ) {
					matrix[a][b] = null;
					continue;
				}

				TupleAlignmentNode source_node = matrix[a - 1][b];

				if ( source_node == null ) {
					// the macro state above us is not reachable.
					continue;
				}
				/* Indexing may be confusing.
				 * Remember that in position [4 7] the graph holds a node representing
				 * information about the prefix of length 4 above and length 7 below.
				 */
				SymbolAbove key = ka.get( a - 1 );

				/* We are leaping forwared from [a - 1, b] by inserting one symbol
				 * above. In case of min=0 and max=2, we will most likely insert new
				 * nodes in positions:
				 * [a b] [a b+1] [a b+2]
				 */
				for ( int z = b + min_below; z <= b + max_below && z <= lbelow; z++ ) {

					// additional condition to prune tree: the end of the string
					// must be in range from [a z].

					int leftover_above = labove - a;

					if ( z + leftover_above * max_below < lbelow
							|| z + leftover_above * min_below > lbelow ) {
						continue;
					}

					// when the above/below pair is not in the pair alphabet, 
					// the edge is not legal. skip it.
					KnittingTuple<SymbolBelow> sub = kb.head( b, z - b );
					Integer enc = pair_encoder.apply( key, sub );
					if (enc == null) {
						continue;
					}
					
					TupleAlignmentNode target_node = matrix[a][z];
					if ( target_node == null ) {
						target_node = new TupleAlignmentNode( );
						target_node.incoming_edges = new int[max_number_of_edges][3];
						matrix[a][z] = target_node;
					}

					int edges = target_node.number_of_incoming_edges;

					
					target_node.incoming_edges[edges][0] = a - 1;
					target_node.incoming_edges[edges][1] = b;
					target_node.incoming_edges[edges][2] = enc;

					target_node.number_of_incoming_edges = edges + 1;
				}
			}
		}

		if ( matrix[labove][lbelow] == null ) {
			/* The pair is not representable */
			throw NotAlignableException.neo;
		}

		matrix[labove][lbelow].is_reachable_from_end = true;

		for ( int a = labove; a >= 0; a-- ) {
			for ( int b = lbelow; b >= 0; b-- ) {
				TupleAlignmentNode node = matrix[a][b];

				if ( node == null ) {
					continue;
				}
				if ( !node.is_reachable_from_end ) {
					matrix[a][b] = null;
				}

				int edges = node.number_of_incoming_edges;

				for ( int e = 0; e < edges; e++ ) {

					int x = node.incoming_edges[e][0];
					int y = node.incoming_edges[e][1];

					TupleAlignmentNode origin = matrix[x][y];
					origin.is_reachable_from_end = true;
				}
			}
		}
		
		TupleAlignmentGraph graph = new TupleAlignmentGraph( matrix, labove, lbelow );

		for ( int a = 0; a <= labove; a++ ) {
			for ( int b = 0; b <= lbelow; b++ ) {
				if ( matrix[a][b] == null || ( a == 0 && b == 0 ) ) {
					continue;
				}
				int[][] ie = matrix[a][b].incoming_edges;
				int no_ie = matrix[a][b].number_of_incoming_edges;

				for ( int e_i = 0; e_i < no_ie; e_i++ ) {
					int x = ie[e_i][0];
					int y = ie[e_i][1];

					SymbolAbove key = ka.get( x );
					KnittingTuple<SymbolBelow> sub = kb.head( y, b - y );
					Integer enc = pair_encoder.apply( key, sub );
					ie[e_i][2] = enc;
				}
			}
		}
		return graph;
	}


}
