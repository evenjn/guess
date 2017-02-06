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

import java.util.Iterator;

import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.EndOfCursorException;

public class TupleAlignmentGraphSerializer
		implements
		Iterable<String> {

	private TupleAlignmentGraph graph;

	public TupleAlignmentGraphSerializer(TupleAlignmentGraph graph) {
		this.graph = graph;
	}

	@Override
	public Iterator<String> iterator( ) {
		return KnittingCursor.wrap(new Cursor<String>( ) {

			boolean started = false;

			private int a = 0;

			private int b = 0;

			private int e = 0;

			@Override
			public String next( )
					throws EndOfCursorException {
				if ( !started ) {
					started = true;
					return "";
				}
				int la = graph.la( );
				int lb = graph.lb( );

				for ( ; a <= la; a++ ) {
					for ( ; b <= lb; b++ ) {
						if ( graph.matrix[a][b] == null || ( a == 0 && b == 0 ) ) {
							continue;
						}
						if ( e < graph.matrix[a][b].number_of_incoming_edges ) {
							int[] js = graph.matrix[a][b].incoming_edges[e];
							StringBuilder sb = new StringBuilder( );
							sb.append( a );
							sb.append( " " );
							sb.append( b );
							sb.append( " " );
							sb.append( js[0] );
							sb.append( " " );
							sb.append( js[1] );
							sb.append( " " );
							sb.append( js[2] );
							e++;
							return sb.toString( );
						}
						e = 0;
					}
					b = 0;
				}
				throw EndOfCursorException.neo();
			}
		}).asIterator( );
	}

}
