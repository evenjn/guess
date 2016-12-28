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

/**
 * <p>A TupleAligmentNode object represents a node in an alignment graph.</p>
 * <p>
 * {@code let be function composition}<br>
 * {@code let + be tuple concatenation}
 * </p>
 * <p>
 * Every node carries two integers, A and B.
 * </p>
 * <p>
 * Every path from the initial node to a node carrying A and B represents a
 * partial alignment of the subtuples {@code Above[0..A]} and
 * {@code Below[0..B]}.
 * </p>
 * <p>
 * Each node contains information about the incoming edges.
 * </p>
 */
public class TupleAlignmentNode {

	/**
	 * <p>
	 * An array with as many sub-arrays as there are incoming edges.
	 * </p>
	 * <p>
	 * Each sub-array is an array with three integer values: x, y and encout. It
	 * describes an edge incoming from another node.
	 * </p>
	 * <p>
	 * The first value (x) is the row-index and the second value (y) is the
	 * column-index of the node the edge is coming from.
	 * </p>
	 * <p>
	 * The third value, referred to as "encout", is a unique id that identifies
	 * the pair [p q] where p is a tuple of symbols above and q is a tuple of
	 * symbols below.
	 * </p>
	 * <p>
	 * This pair is such that {@code Above[0..x] + p = Above[0..a]} and
	 * {@code Below[0..y] + q = Below[0..b]}.
	 * </p>
	 */
	public int[][] incoming_edges;

	/**
	 * The number of edges entering this node.
	 */
	public int number_of_incoming_edges;

	/**
	 * The row-index of this node in the tuple-alignment-graph matrix.
	 */
	public int a;

	/**
	 * The column-index of this node in the tuple-alignment-graph matrix.
	 */
	public int b;

}
