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

import java.util.function.Function;

import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.PastTheEndException;

public class TupleAlignmentAlphabetSerializer<SymbolAbove, SymbolBelow>
		implements Cursable<String> {

	private final KnittingTuple<TupleAlignmentPair<SymbolAbove, SymbolBelow>> iterator;

	private final Function<SymbolAbove, String> a_serializer;

	private final Function<SymbolBelow, String> b_serializer;

	private TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> alphabet;

	public TupleAlignmentAlphabetSerializer(
			TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> alphabet,
			Function<SymbolAbove, String> a_serializer,
			Function<SymbolBelow, String> b_serializer) {
		this.alphabet = alphabet;
		this.a_serializer = a_serializer;
		this.b_serializer = b_serializer;
		iterator = KnittingTuple.wrap( alphabet );
	}

	@Override
	public Cursor<String> pull( Hook hook ) {
		
		return new Cursor<String>( ) {

			private KnittingCursor<TupleAlignmentPair<SymbolAbove, SymbolBelow>> outer = iterator.asCursor( );

			private boolean first = true;

			@Override
			public String next( )
					throws PastTheEndException {
				if (first) {
					first = false;
					StringBuilder builder = new StringBuilder( );
					builder.append( alphabet.record_max_length_above );
					builder.append( "," );
					builder.append( alphabet.record_max_length_below );
					builder.append( "," );
					builder.append( alphabet.record_max_number_of_edges );
					return builder.toString( );
				}
				int id = outer.soFar( );
				TupleAlignmentPair<SymbolAbove, SymbolBelow> next = outer.next( );
				StringBuilder builder = new StringBuilder( );
				builder.append( id );
				builder.append( "," );
				builder.append( a_serializer.apply( next.above ) );
				for ( SymbolBelow sb : next.below.asIterable( ) ) {
					builder.append( "," );
					builder.append( b_serializer.apply( sb ) );
				}
				return builder.toString( );
			}
		};
	}
}
