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
package org.github.evenjn.align.cache;

import org.github.evenjn.align.NotAlignableException;
import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.align.TupleAlignmentGraph;

public class DisplayAlignmentGraph {

	public static void main( String[] args ) {

		String above = "TAX";
		String below = "taks";
		try {
			TupleAlignmentGraph.printCompleteEdgeMatrix(
					TupleAligner.pathMatrix( above.length( ), below.length( ), 0, 2 ),
					above.length( ), below.length( ) );
		}
		catch ( NotAlignableException e ) {
		}
		
	}

}
