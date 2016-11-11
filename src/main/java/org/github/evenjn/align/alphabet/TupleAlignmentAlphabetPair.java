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

import java.util.function.Function;

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.AutoHook;

public class TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> {

	public SymbolAbove above;

	public KnittingTuple<SymbolBelow> below;

	public boolean equals( Object other ) {
		if ( other == null || !( other instanceof TupleAlignmentAlphabetPair ) ) {
			return false;
		}
		TupleAlignmentAlphabetPair<?, ?> o = (TupleAlignmentAlphabetPair<?, ?>) other;
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
			sb.append( sa_label.apply( above ) );
			sb.append( " >-> [" );
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
}
