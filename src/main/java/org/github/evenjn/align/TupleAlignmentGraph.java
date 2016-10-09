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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/** A TupleAlignmentGraph represents alignments between two tuples A and B
 * (Above and Below). The graph consists of:
 * 
 * The actual directed acyclic graph (DAG) reprsenting all the possible
 * alignments. The DAG is represented by a matrix (array of arrays).
 * 
 * The array has as many sub-arrays (rows) as there are elements Above.
 * Each sub-array has as many cells (columns) as there are elements Below.
 * 
 * Each cell is a pointer to a TupleAlignmentNode (or null).
 * 
 * Each TupleAlignmentNode represents a node in the graph, and contains
 * information about incoming edges.
 * 
 */
public class TupleAlignmentGraph {

	
	/**
	 * This function iterates over all the cells of the matrix except [0 0].
	 * It populates the alphabet of actually occurring atomic alignments.
	 * It populates the list of nodes. 
	 */
	private void computeNodesList() {
		int la = la();
		int lb = lb();
		for (int a = 0; a <= la; a++) {
			for ( int b = (a == 0 ? 1 : 0); b <= lb; b++) {
				TupleAlignmentNode cell =  matrix[a][b];
				if (cell == null ) {
					/* this cell is not in any path */
					continue;
				}
				cell.a = a;
				cell.b = b;
				for (int e = 0; e < cell.number_of_incoming_edges; e++) {
					encout_combinations.add( cell.incoming_edges[e][2] );
				}
				nodes.add( cell );
			}
		}
	}

	public TupleAlignmentNode[][] matrix;
	
	private final HashSet<Integer> encout_combinations = new HashSet<>( );

	public Set<Integer> combinations() {
		if (encout_combinations.isEmpty( )) {
			computeNodesList( );
		}
		return encout_combinations;
	}

	private final LinkedList<TupleAlignmentNode> nodes = new LinkedList<>( );
	private int above;
	private int below;
	
	public void set(int a, int b) {
		above = a;
		below = b;
	}
	public Iterator<TupleAlignmentNode> backward() {
		if (nodes.isEmpty( )) {
			computeNodesList();
		}
		return nodes.descendingIterator( );
	}
	
	/**
	 * @return An iterator over all the nodes of this graph, starting
	 * with all the nodes of the first row (from the first to the last column),
	 * then the nodes of the second row, etc.
	 */
	public Iterator<TupleAlignmentNode> forward() {
		if (nodes.isEmpty( )) {
			computeNodesList();
		}
		return nodes.iterator( );
	}

	public TupleAlignmentNode get(int a, int b) {
		return matrix[a][b];
	}
	
	public int la() {
		return above;
	}
	
	public int lb() {
		return below;
	}

	public static void printCompleteEdgeMatrix(TupleAlignmentNode[][] mx, int la, int lb) {
		StringBuilder sb;

		sb = new StringBuilder( );
		sb.append( " - nodes -\n" );
		for ( int a = 0; a <= la; a++ ) {
			TupleAlignmentNode[] columns = mx[a];

			for ( int b = 0; b <= lb; b++ ) {
				TupleAlignmentNode edges = columns[b];
				if (edges == null) {
					sb.append( " . " );
				}
				else {
					if (edges.is_reachable_from_end) {
						sb.append( " o " );
					}
					else {
  					if (edges.is_reachable_from_beginning) {
  						sb.append( " ? " );
  					}
  					else {
  						sb.append( " Q " );
  					}
					}
				}
			}
			sb.append( "\n" );
		}
		System.out.println( sb.toString( ) );

		int max = 0;

		sb = new StringBuilder( );
		sb.append( " - incoming edges -\n" );
		for ( int a = 0; a <= la; a++ ) {
			TupleAlignmentNode[] columns = mx[a];

			for ( int b = 0; b <= lb; b++ ) {
				TupleAlignmentNode edges = columns[b];
				if (edges == null) {
					sb.append( " . " );
				}
				else {
					int val = edges.number_of_incoming_edges;
					if ( val > max )
						max = val;
					sb.append( " " ).append( val ).append( " " );
				}
			}
			sb.append( "\n" );
		}
		System.out.println( sb.toString( ) );

		for ( int layer = 0; layer < max; layer++ ) {
			sb = new StringBuilder( );
			sb.append( " - layer " + layer + " -\n" );
			for ( int a = 0; a <= la; a++ ) {
				TupleAlignmentNode[] columns = mx[a];

				for ( int b = 0; b <= lb; b++ ) {
					TupleAlignmentNode edges = columns[b];
					if ( edges != null && edges.number_of_incoming_edges > layer ) {
						sb.append( edges.incoming_edges[layer][0] ).append( edges.incoming_edges[layer][1] ).append( " " );
					}
					else {
						sb.append( ".. " );
					}
				}
				sb.append( "\n" );
			}
			System.out.println( sb.toString( ) );
		}
	}
}
