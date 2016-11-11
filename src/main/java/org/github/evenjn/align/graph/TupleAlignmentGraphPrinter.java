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

public class TupleAlignmentGraphPrinter {

	public static String print( TupleAlignmentGraph graph ) {
		TupleAlignmentNode[][] mx = graph.matrix;
		int la = mx.length - 1;
		int lb = mx[0].length - 1;
		StringBuilder sb = new StringBuilder( );
		sb.append( " - nodes -\n" );
		for ( int a = 0; a <= la; a++ ) {
			TupleAlignmentNode[] columns = mx[a];
			for ( int b = 0; b <= lb; b++ ) {
				TupleAlignmentNode edges = columns[b];
				if ( edges == null ) {
					sb.append( " . " );
				} else {
					if ( edges.is_reachable_from_end ) {
						sb.append( " o " );
					} else {
						if ( edges.is_reachable_from_beginning ) {
							sb.append( " ? " );
						} else {
							sb.append( " Q " );
						}
					}
				}
			}
			sb.append( "\n" );
		}
		sb.append( "\n" );
		int max = 0;
		sb.append( " - incoming edges -\n" );
		for ( int a = 0; a <= la; a++ ) {
			TupleAlignmentNode[] columns = mx[a];
			for ( int b = 0; b <= lb; b++ ) {
				TupleAlignmentNode edges = columns[b];
				if ( edges == null ) {
					sb.append( " . " );
				} else {
					int val = edges.number_of_incoming_edges;
					if ( val > max ) {
						max = val;
					}
					sb.append( " " ).append( val ).append( " " );
				}
			}
			sb.append( "\n" );
		}
		sb.append( "\n" );

		for ( int layer = 0; layer < max; layer++ ) {
			sb.append( " - layer " + layer + " -\n" );
			for ( int a = 0; a <= la; a++ ) {
				TupleAlignmentNode[] columns = mx[a];
				for ( int b = 0; b <= lb; b++ ) {
					TupleAlignmentNode edges = columns[b];
					if ( edges != null && edges.number_of_incoming_edges > layer ) {
						sb.append( edges.incoming_edges[layer][0] )
								.append( edges.incoming_edges[layer][1] )
								.append( " " );
					} else {
						sb.append( ".. " );
					}
				}
				sb.append( "\n" );
			}
			sb.append( "\n" );
		}
		return sb.toString( );
	}
}
