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

import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.github.evenjn.align.Tael;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.OptionalPurl;

public class TupleAlignmentAlphabetDeserializer<SymbolAbove, SymbolBelow>
		implements
		OptionalPurl<String, TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>> {

	private final Function<String, SymbolAbove> a_deserializer;

	private final Function<String, SymbolBelow> b_deserializer;

	private final TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
			new TupleAlignmentAlphabet<>( );

	private final static Pattern splitter = Pattern.compile( "," );

	private boolean closed = false;

	@Override
	public Optional<TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>> end( ) {
		if ( closed ) {
			throw new IllegalStateException( );
		}
		closed = true;
		return Optional.of( result );
	}

	public TupleAlignmentAlphabetDeserializer(
			Function<String, SymbolAbove> a_deserializer,
			Function<String, SymbolBelow> b_deserializer) {
		this.a_deserializer = a_deserializer;
		this.b_deserializer = b_deserializer;
	}

	@Override
	public Optional<TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>>
			next( String object ) {
		if ( closed ) {
			throw new IllegalStateException( );
		}

		int prev_stop = 0;
		int next_stop = object.indexOf( ';', prev_stop );
		if ( next_stop == -1 ) {
			// this block is for backward-compatibility
			String[] split = splitter.split( object );
			int id = Integer.parseInt( split[0] );
			if ( result.size( ) != id ) {
				throw new IllegalStateException( );
			}
			SymbolAbove sa = a_deserializer.apply( split[1] );

			Vector<SymbolBelow> below = new Vector<>( );
			for ( int i = 2; i < split.length; i++ ) {
				below.add( b_deserializer.apply( split[i] ) );
			}
			result.add(
					new Tael<>( KnittingTuple.on( sa ).asTupleValue( ), KnittingTuple.wrap( below ).asTupleValue( ) ) );
			return Optional.empty( );
		}

		String chunk = object.substring( prev_stop, next_stop );

		int id = Integer.parseInt( chunk );
		if ( result.size( ) != id ) {
			throw new IllegalStateException( );
		}

		prev_stop = next_stop + 1;
		next_stop = object.indexOf( ';', prev_stop );
		chunk = object.substring( prev_stop, next_stop );

		String[] split;

		Vector<SymbolAbove> above = new Vector<>( );
		if ( !chunk.isEmpty( ) ) {
			split = splitter.split( chunk );
			for ( int i = 0; i < split.length; i++ ) {
				above.add( a_deserializer.apply( split[i] ) );
			}
		}
		prev_stop = next_stop + 1;
		next_stop = object.indexOf( ';', prev_stop );
		chunk = object.substring( prev_stop, next_stop );

		Vector<SymbolBelow> below = new Vector<>( );
		if ( !chunk.isEmpty( ) ) {
			split = splitter.split( chunk );
			for ( int i = 0; i < split.length; i++ ) {
				below.add( b_deserializer.apply( split[i] ) );
			}
		}

		result.add( new Tael<>( KnittingTuple.wrap( above ).asTupleValue( ),
				KnittingTuple.wrap( below ).asTupleValue( ) ) );
		return Optional.empty( );
	}

}
