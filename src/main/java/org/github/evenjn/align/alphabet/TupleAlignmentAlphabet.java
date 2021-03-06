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

import org.github.evenjn.align.Tael;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.TupleValue;
import org.github.evenjn.lang.Tuple;

public class TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> {

	private Vector<Tael<SymbolAbove, SymbolBelow>> alphabet =
			new Vector<>( );

	private HashMap<TupleValue<SymbolAbove>, HashSet<TupleValue<SymbolBelow>>> map_above_to_below =
			new HashMap<>( );

	private Vector<TupleValue<SymbolAbove>> above_vector = new Vector<>( );

	private Vector<TupleValue<SymbolBelow>> below_vector = new Vector<>( );

	private HashSet<TupleValue<SymbolAbove>> above_set = new HashSet<>( );

	private HashSet<TupleValue<SymbolBelow>> below_set = new HashSet<>( );

	private int min_above = -1;

	private int max_above = 0;

	private int min_below = -1;

	private int max_below = 0;

	private HashMap<Tael<SymbolAbove, SymbolBelow>, Integer> encode_map =
			new HashMap<>( );

	int add( Tael<SymbolAbove, SymbolBelow> pair ) {
		Integer integer = encode_map.get( pair );
		if ( integer != null ) {
			return integer;
		}
		encode_map.put( pair, alphabet.size( ) );
		alphabet.add( pair );

		TupleValue<SymbolAbove> kabove = pair.getAbove( );

		if ( min_above == -1 || min_above > kabove.size( ) ) {
			min_above = kabove.size( );
		}
		if ( max_above < kabove.size( ) ) {
			max_above = kabove.size( );
		}

		HashSet<TupleValue<SymbolBelow>> m = map_above_to_below.get( kabove );
		if ( m == null ) {
			m = new HashSet<>( );
			map_above_to_below.put( kabove, m );
		}

		if ( !above_set.contains( kabove ) ) {
			above_set.add( kabove );
			above_vector.add( kabove );
		}
		if ( !below_set.contains( pair.getBelow( ) ) ) {
			below_set.add( pair.getBelow( ) );
			below_vector.add( pair.getBelow( ) );
		}

		m.add( pair.getBelow( ) );
		if ( min_below == -1 || min_below > pair.getBelow( ).size( ) ) {
			min_below = pair.getBelow( ).size( );
		}
		if ( max_below < pair.getBelow( ).size( ) ) {
			max_below = pair.getBelow( ).size( );
		}
		return alphabet.size( );
	}

	public KnittingTuple<TupleValue<SymbolAbove>> above( ) {
		return KnittingTuple.wrap( above_vector );
	}

	public KnittingTuple<TupleValue<SymbolBelow>> below( ) {
		return KnittingTuple.wrap( below_vector );
	}

	public int getMinBelow( ) {
		return min_below;
	}

	public int getMaxBelow( ) {
		return max_below;
	}

	public int getMinAbove( ) {
		return min_above;
	}

	public int getMaxAbove( ) {
		return max_above;
	}

	public Set<TupleValue<SymbolBelow>>
			correspondingBelow( TupleValue<SymbolAbove> above ) {
		return map_above_to_below.get( above );
	}

	public Tael<SymbolAbove, SymbolBelow> get( int t ) {
		return alphabet.get( t );
	}

	public int size( ) {
		return alphabet.size( );
	}

	public BiFunction<Tuple<SymbolAbove>, Tuple<SymbolBelow>, Integer>
			asEncoder( ) {
		return ( a, b ) -> encode( a, b );
	}

	public Integer encode( Tuple<SymbolAbove> above, Tuple<SymbolBelow> below ) {
		if ( above.size( ) != 1 ) {
			throw new IllegalArgumentException( );
		}
		return encode_map
				.get( new Tael<>( KnittingTuple.wrap( above ).asTupleValue( ),
						KnittingTuple.wrap( below ).asTupleValue( ) ) );
	}
}
