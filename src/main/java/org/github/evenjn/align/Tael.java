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

import java.util.Vector;
import java.util.function.Function;

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Tuple;

public class Tael<SymbolAbove, SymbolBelow> {

	public KnittingTuple<SymbolAbove> above;

	public KnittingTuple<SymbolBelow> below;

	public boolean equals( Object other ) {
		if ( other == null || !( other instanceof Tael ) ) {
			return false;
		}
		Tael<?, ?> o = (Tael<?, ?>) other;
		return above.equals( o.above ) && below.equals( o.below );
	}

	public int hashCode( ) {
		return ( 17 * above.hashCode( ) ) + below.hashCode( );
	}

	public String print(
			Function<? super SymbolAbove, String> sa_label,
			Function<? super SymbolBelow, String> sb_label ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			StringBuilder sb = new StringBuilder( );
			sb.append( "[" );
			for ( SymbolAbove a : above.asIterable( ) ) {
				sb.append( " " ).append( sa_label.apply( a ) );
			}
			sb.append( "] >-> [" );
			for ( SymbolBelow b : below.asIterable( ) ) {
				sb.append( " " ).append( sb_label.apply( b ) );
			}
			sb.append( " ]" );
			return sb.toString( );
		}
	}

	public String print( ) {
		return print( Object::toString, Object::toString );
	}

	/* Creates a tuple of Tael from two tuples and their alignment
	 * 
	 */
	public static <SymbolAbove, SymbolBelow>
			Tuple<Tael<SymbolAbove, SymbolBelow>>
			tael(
					Tuple<SymbolAbove> above,
					Tuple<SymbolBelow> below,
					Tuple<Di<Integer, Integer>> alignment ) {
		Vector<Tael<SymbolAbove, SymbolBelow>> result =
				new Vector<>( );
		int a_so_far = 0;
		int b_so_far = 0;
		for ( int i = 0; i < alignment.size( ); i++ ) {
			Di<Integer, Integer> di = alignment.get( i );

			Tael<SymbolAbove, SymbolBelow> pair =
					new Tael<>( );
			Vector<SymbolAbove> suba = KnittingTuple.wrap( above )
					.head( a_so_far, di.front( ) ).asCursor( ).collect( new Vector<>( ) );

			pair.above = KnittingTuple.wrap( suba );
			Vector<SymbolBelow> subb = KnittingTuple.wrap( below )
					.head( b_so_far, di.back( ) ).asCursor( ).collect( new Vector<>( ) );
			pair.below = KnittingTuple.wrap( subb );
			a_so_far = a_so_far + di.front( );
			b_so_far = b_so_far + di.back( );
			result.add( pair );
		}
		return KnittingTuple.wrap(result);
	}

}
