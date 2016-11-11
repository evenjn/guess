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

import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.align.graph.TupleAlignmentGraphFactory;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.align.graph.TupleAlignmentGraph;

public class DisplayAlignmentGraph {

	public static void main( String[] args ) {

		KnittingTuple<String> above = KnittingTuple.on( "T", "A", "X" );
		KnittingTuple<String> below = KnittingTuple.on( "t", "a", "k", "s" );
		try {
			TupleAlignmentGraph graph = TupleAlignmentGraphFactory.graph(
					( x, y ) -> 1,
					above, below, 0, 2 );
			String print = TupleAlignmentGraphPrinter.print( graph );
			System.out.println( print );
		}
		catch ( NotAlignableException e ) {
		}

	}

}
