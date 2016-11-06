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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>
		implements
		Tuple<TupleAlignmentPair<SymbolAbove, SymbolBelow>> {

	int record_max_length_above = 0;
	int record_max_length_below = 0;
	int record_max_number_of_edges = 0 ;
	
	private Vector<TupleAlignmentPair<SymbolAbove, SymbolBelow>> alphabet =
			new Vector<>( );

	private Vector<SymbolAbove> alphabet_above = new Vector<>( );
	private Vector<Tuple<SymbolBelow>> sequences_below = new Vector<>( );

	private HashMap<SymbolAbove, HashSet<Tuple<SymbolBelow>>> map =
			new HashMap<>( );

	private HashSet<SymbolAbove> above_set = new HashSet<>( );
	
	private HashMap<TupleAlignmentPair<SymbolAbove, SymbolBelow>, Integer> encode_map = new HashMap<>( );
	
	public void add( TupleAlignmentPair<SymbolAbove, SymbolBelow> pair ) {
		alphabet_above.add( pair.above );
		sequences_below.add( pair.below );
		encode_map.put( pair, alphabet.size( ) );
		alphabet.add( pair );
		
		HashSet<Tuple<SymbolBelow>> m = map.get( pair.above );
		if (m == null) {
			m = new HashSet<>( );
			map.put( pair.above, m );
			above_set.add( pair.above );
		}
		m.add( pair.below );
	}
	
	public Set<SymbolAbove> above() {
		return above_set;
	}

	public Set<Tuple<SymbolBelow>> correspondingBelow(SymbolAbove above) {
		return map.get( above );
	}
	
	@Override
	public TupleAlignmentPair<SymbolAbove, SymbolBelow> get( int t ) {
		return alphabet.get( t );
	}

	@Override
	public int size( ) {
		return alphabet.size( );
	}

	public int encode(SymbolAbove above, Tuple<SymbolBelow> below) {
		TupleAlignmentPair<SymbolAbove, SymbolBelow> pair = new TupleAlignmentPair<>( );
		pair.above = above;
		pair.below = KnittingTuple.wrap(below);
		Integer result = encode_map.get( pair );
		if ( result == null ) {
			System.err.println(pair.print( ));
			throw new IllegalStateException(
					"Attempting to encode a pair that is not in the alphabet.");

		}
		return result;
	}
}
