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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiFunction;

import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> {

	private Vector<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
			new Vector<>( );

	private Vector<SymbolAbove> alphabet_above = new Vector<>( );

	private Vector<Tuple<SymbolBelow>> sequences_below = new Vector<>( );

	private HashMap<SymbolAbove, HashSet<Tuple<SymbolBelow>>> map =
			new HashMap<>( );

	private HashSet<SymbolAbove> above_set = new HashSet<>( );
	
	private int min_below = 0;
	
	private int max_below = 0;

	private HashMap<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>, Integer> encode_map =
			new HashMap<>( );

	int add( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair ) {
		Integer integer = encode_map.get( pair );
		if (integer != null) {
			return integer;
		}
		alphabet_above.add( pair.above );
		sequences_below.add( pair.below );
		encode_map.put( pair, alphabet.size( ) );
		alphabet.add( pair );

		HashSet<Tuple<SymbolBelow>> m = map.get( pair.above );
		if ( m == null ) {
			m = new HashSet<>( );
			map.put( pair.above, m );
			above_set.add( pair.above );
		}
		m.add( pair.below );
		if (min_below > pair.below.size( )) {
			min_below = pair.below.size( );
		}
		if (max_below < pair.below.size( )) {
			max_below = pair.below.size( );
		}
		return alphabet.size( );
	}

	public Set<SymbolAbove> above( ) {
		return above_set;
	}
	
	public int getMinBelow() {
		return min_below;
	}

	public int getMaxBelow() {
		return max_below;
	}

	public Set<Tuple<SymbolBelow>> correspondingBelow( SymbolAbove above ) {
		return map.get( above );
	}

	public TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> get( int t ) {
		return alphabet.get( t );
	}

	public int size( ) {
		return alphabet.size( );
	}
	
	public BiFunction<SymbolAbove, Tuple<SymbolBelow>, Integer> asEncoder(){
		return (a, b) -> encode(a, b);
	}

	public Integer encode( SymbolAbove above, Tuple<SymbolBelow> below ) {
		TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair =
				new TupleAlignmentAlphabetPair<>( );
		pair.above = above;
		pair.below = KnittingTuple.wrap( below );
		return encode_map.get( pair );
	}
}
