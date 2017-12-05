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

import org.github.evenjn.align.Tael;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Rook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

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
	public Cursor<String> pull( Rook rook ) {
		
		return new Cursor<String>( ) {

			int id = 0;

			@Override
			public String next( )
					throws EndOfCursorException {
				if (id >= alphabet.size( )) {
					throw EndOfCursorException.neo();
				}
				Tael<SymbolAbove, SymbolBelow> next = alphabet.get( id );
				StringBuilder builder = new StringBuilder( );
				String separator = "";
				builder.append( id );
				id++;
				builder.append( ";" );
				
				for ( SymbolAbove sa : KnittingTuple.wrap(next.getAbove( )).asIterable( ) ) {
					builder.append( separator );
					builder.append( a_serializer.apply( sa ) );
					separator = ",";
				}

				builder.append( ";" );
				
				separator = "";
				
				for ( SymbolBelow sb : KnittingTuple.wrap(next.getBelow( )).asIterable( ) ) {
					builder.append( separator );
					builder.append( b_serializer.apply( sb ) );
					separator = ",";
				}
				builder.append( ";" );
				return builder.toString( );
			}
		};
	}
}
