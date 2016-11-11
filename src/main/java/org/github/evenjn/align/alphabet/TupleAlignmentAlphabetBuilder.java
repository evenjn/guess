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
package org.github.evenjn.align.alphabet;

import java.util.HashSet;

import org.github.evenjn.knit.KnittingTuple;

public class TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private final TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
			new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
	
	private HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> observed_so_far =
			new HashSet<>( );
	
	public int record(SymbolAbove suba, KnittingTuple<SymbolBelow> subb) {
		TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair =
				new TupleAlignmentAlphabetPair<>( );
		pair.above = suba;
		pair.below = subb;
		if ( !observed_so_far.contains( pair ) ) {
			observed_so_far.add( pair );
			result.add( pair );
		}
		return result.encode( suba, subb );
	}
	
	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build() {
		return result;
	}
}
