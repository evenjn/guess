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

import java.util.function.Function;

import org.github.evenjn.align.TupleAlignmentPair;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.PastTheEndException;

public class TupleAlignmentAlphabetSerializer<SymbolAbove, SymbolBelow>
		implements Cursable<String> {


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
	}

	@Override
	public Cursor<String> pull( Hook hook ) {
		
		return new Cursor<String>( ) {

			int id = 0;

			@Override
			public String next( )
					throws PastTheEndException {
				TupleAlignmentPair<SymbolAbove, SymbolBelow> next = alphabet.get( id );
				StringBuilder builder = new StringBuilder( );
				builder.append( id );
				id++;
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
