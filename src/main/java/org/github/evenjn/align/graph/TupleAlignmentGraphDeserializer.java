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

import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Pattern;

import org.github.evenjn.yarn.OptionalPurl;

public class TupleAlignmentGraphDeserializer
		implements
		OptionalPurl<String, TupleAlignmentGraph> {

	private TupleAlignmentNode[][] matrix;

	private TupleAlignmentNode current_node;

	private int current_a;

	private int current_b;

	private int current_la;

	private int current_lb;

	private LinkedList<int[]> current_edges = new LinkedList<>( );

	private final Pattern splitter = Pattern.compile( " " );

	private int record_max_length_above;

	private int record_max_length_below;

	public TupleAlignmentGraphDeserializer(
			int record_max_length_above,
			int record_max_length_below) {
		this.record_max_length_above = record_max_length_above;
		this.record_max_length_below = record_max_length_below;
	}

	private void wrapUp( ) {
		if ( current_node != null ) {
			int size = current_edges.size( );
			current_node.number_of_incoming_edges = size;
			current_node.incoming_edges = new int[size][3];
			int i = 0;
			for ( int[] edge : current_edges ) {
				current_node.incoming_edges[i][0] = edge[0];
				current_node.incoming_edges[i][1] = edge[1];
				current_node.incoming_edges[i][2] = edge[2];
				i++;
			}
			current_edges.clear( );
			current_node = null;
		}
	}

	@Override
	public Optional<TupleAlignmentGraph> end( ) {
		if ( matrix != null ) {
			wrapUp( );
			return Optional
					.of( new TupleAlignmentGraph( matrix, current_la, current_lb ) );
		}
		return Optional.empty( );
	}

	@Override
	public Optional<TupleAlignmentGraph> next( String object ) {
		if ( object.isEmpty( ) ) {
			if ( matrix != null ) {
				wrapUp( );
				TupleAlignmentGraph tmp =
						new TupleAlignmentGraph( matrix, current_la, current_lb );
				matrix = null;
				current_node = null;
				current_a = 0;
				current_b = 0;
				current_la = 0;
				current_lb = 0;
				return Optional.of( tmp );
			}
			return Optional.empty( );
		}
		boolean just_created = false;
		if ( matrix == null ) {
			just_created = true;
			matrix = new TupleAlignmentNode[1 + record_max_length_above][1
					+ record_max_length_below];
		}
		String[] split = splitter.split( object );
		int a = Integer.parseInt( split[0] );
		int b = Integer.parseInt( split[1] );
		int x = Integer.parseInt( split[2] );
		int y = Integer.parseInt( split[3] );
		int e = Integer.parseInt( split[4] );
		if ( current_la < a ) {
			current_la = a;
		}
		if ( current_lb < b ) {
			current_lb = b;
		}
		if ( !just_created ) {
			if ( a != current_a || b != current_b ) {
				wrapUp( );
				current_a = a;
				current_b = b;
			}
		}

		if ( current_node == null ) {
			current_node = new TupleAlignmentNode( );
			current_node.a = current_a;
			current_node.b = current_b;
			matrix[a][b] = current_node;
		}

		int[] edge = new int[3];
		edge[0] = x;
		edge[1] = y;
		edge[2] = e;
		current_edges.add( edge );
		return Optional.empty( );
	}

}
