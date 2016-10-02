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
import java.util.regex.Pattern;

import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipFold;

public class TupleAlignmentAlphabetDeserializer<SymbolAbove, SymbolBelow>
		implements
		SkipFold<String, TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>> {

	private final Function<String, SymbolAbove> a_deserializer;

	private final Function<String, SymbolBelow> b_deserializer;

	private final TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
			new TupleAlignmentAlphabet<>( );

	private final static Pattern splitter = Pattern.compile( "," );

	private boolean closed = false;
	private boolean first = true;

	@Override
	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> end( ) {
		if ( closed ) {
			throw new IllegalStateException( );
		}
		closed = true;
		return result;
	}

	public TupleAlignmentAlphabetDeserializer(
			Function<String, SymbolAbove> a_deserializer,
			Function<String, SymbolBelow> b_deserializer) {
		this.a_deserializer = a_deserializer;
		this.b_deserializer = b_deserializer;
	}

	@Override
	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> next( String object )
			throws SkipException {
		if ( closed ) {
			throw new IllegalStateException( );
		}
		String[] split = splitter.split( object );
		if (first) {
			first = false;
			result.record_max_length_above = Integer.parseInt( split[0] );
			result.record_max_length_below = Integer.parseInt( split[1] );
			result.record_max_number_of_edges = Integer.parseInt( split[2] );
			throw SkipException.neo;
		}
		int id = Integer.parseInt( split[0] );
		if ( result.size( ) != id ) {
			throw new IllegalStateException( );
		}
		SymbolAbove sa = a_deserializer.apply( split[1] );
		Vector<SymbolBelow> below = new Vector<>( );
		for ( int i = 2; i < split.length; i++ ) {
			below.add( b_deserializer.apply( split[i] ) );
		}
		TupleAlignmentPair<SymbolAbove, SymbolBelow> cp = new TupleAlignmentPair<>( );
		cp.above = sa;
		cp.below = KnittingTuple.wrap( below );
		result.add( cp );
		throw SkipException.neo;
	}

}
