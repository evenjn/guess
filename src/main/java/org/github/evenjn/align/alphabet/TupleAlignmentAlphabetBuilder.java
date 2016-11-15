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
import java.util.Vector;
import java.util.function.Function;

import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.numeric.FrequencyData;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private final TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
			new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );

	private HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> observed_so_far =
			new HashSet<>( );

	private FrequencyDistribution<SymbolAbove> fd_base =
			new FrequencyDistribution<>( );

	private HashMap<SymbolAbove, Vector<FrequencyDistribution<Tuple<SymbolBelow>>>> fds =
			new HashMap<>( );

	private Function<SymbolAbove, String> a_printer = null;

	private Function<SymbolBelow, String> b_printer = null;

	public void setPrinters(
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer ) {
		if ( a_printer != null ) {
			this.a_printer = a_printer;
		}
		if ( b_printer != null ) {
			this.b_printer = b_printer;
		}
	}

	public void record( SymbolAbove suba, KnittingTuple<SymbolBelow> subb ) {
		TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair =
				new TupleAlignmentAlphabetPair<>( );
		pair.above = suba;
		pair.below = subb;
		Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector = fds.get( suba );

		if ( !observed_so_far.contains( pair ) ) {
			observed_so_far.add( pair );
		}
		if ( vector == null ) {
			vector = new Vector<>( );
			fds.put( suba, vector );
		}
		int size = subb.size( );
		for ( int i = 0; i < size; i++ ) {
			if ( vector.size( ) < i + 1 ) {
				vector.add( new FrequencyDistribution<Tuple<SymbolBelow>>( ) );
			}
		}
		if ( size > 0 ) {
			vector.get( size - 1 ).accept( subb );
		}
		fd_base.accept( suba );
	}

	private String tuple_printer( Tuple<SymbolBelow> tuple ) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "[" );
		for ( int i = 0; i < tuple.size( ); i++ ) {
			sb.append( " " ).append( b_printer.apply( tuple.get( i ) ) );
		}
		sb.append( " ]" );
		return sb.toString( );
	}

	private static final String decorator_line =
			"----------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "----------";

	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build( ) {
		if ( a_printer != null && b_printer != null ) {
			System.out.println( decorator_line );
			System.out.println( fd_base.plot( ).setLabels( a_printer ).print( ) );
			System.out.println( decorator_line );
			KnittingTuple<FrequencyData<SymbolAbove>> dataSorted =
					fd_base.dataSorted( true );
			for ( int i = 0; i < dataSorted.size( ); i++ ) {
				FrequencyData<SymbolAbove> local_fd = dataSorted.get( i );
				System.out.println( a_printer.apply( local_fd.front( ) ) );
				System.out.println( decorator_line );
				Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector = fds.get( local_fd.front( ) );
				for (int j = 0; j < vector.size( ); j++) {

					System.out.println(
							vector.get( j )
									.plot( )
									.setLimit( 10 )
									.setLabels( this::tuple_printer )
									.print( ) );
				}
			}
		}
		for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> x : observed_so_far ) {
			result.add( x );
		}
		return result;
	}
}
