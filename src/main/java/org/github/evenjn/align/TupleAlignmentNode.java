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

/**
 * Every path from an initial node to this node represents a partial alignment
 * of the subtuples Above[0..a] and Below[0..b].
 * 
 * Each node contains information about the incoming edges.
 */
public class TupleAlignmentNode {
	/**
	 * An array with as many sub-arrays as there are incoming edges.
	 * 
	 * Each sub-array is an array with three integer values (x, y, encout).
	 * It describes an edge incoming from another node.
	 * 
	 * The first value (x) is the row-index and the second value (y) is the
	 * column-index of the node the edge is coming from.
	 * 
	 * The third value, referred to as "encout", is a unique id that identifies 
	 * the pair [p q] where p is a symbol above and q is a finite sequence of
	 * symbols below.
	 * 
	 * This pair is such that Above[0..x]+p = Above[0..a] and
	 * Below[0..y]+q = Below[0..b] 
	 */
	public int[][] incoming_edges;
	
	/**
	 * The number of edges entering this node.
	 */
	public int number_of_incoming_edges;
	
	/**
	 * Marks whether the node is an initial node.
	 */
	public boolean is_reachable_from_beginning;
	
	/**
	 * Marks whether the node is a final node.
	 */
	public boolean is_reachable_from_end;
	
	/**
	 * The row-index of this node in the tuple-alignment-graph matrix.
	 */
	public int a;

	/**
	 * The column-index of this node in the tuple-alignment-graph matrix.
	 */
	public int b;
	
}