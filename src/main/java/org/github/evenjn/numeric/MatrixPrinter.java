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
package org.github.evenjn.numeric;

import java.util.function.Function;

import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.PastTheEndException;

public class MatrixPrinter {

	public static String printWithIterator(
			Cursor<String> row_labels,
			Cursor<String> column_labels,
			Cursor<Double> matrix,
			String indent ) {
		return print( row_labels, column_labels, matrix, indent, new SixCharFormat(
				true ) );
	}

	public static <T> String print(
			Cursor<String> row_labels,
			Cursor<String> column_labels,
			Cursor<T> matrix,
			String indent,
			Function<T, String> doublePrinter ) {
		int cell_size = 7;
		StringBuilder sb = new StringBuilder( );
		int number_of_columns = 0;
		for ( String col : KnittingCursor.wrap( column_labels ).once( ) ) {
			sb.append( indent );
			for ( int i = 0; i < number_of_columns; i++ ) {
				sb.append( "|" );
				for ( int p = 0; p < cell_size; p++ ) {
					sb.append( " " );
				}
			}
			sb.append( col );
			sb.append( "\n" );
			number_of_columns++;
		}

		sb.append( indent );
		for ( int p = 0; p < number_of_columns * ( 1 + cell_size ); p++ ) {
			if ( p % ( 1 + cell_size ) == 0 )
				sb.append( "|" );
			else
				sb.append( "_" );
		}
		sb.append( "\n" );

		for ( String row : KnittingCursor.wrap( row_labels ).once( ) ) {
			sb.append( indent );

			for ( int j = 0; j < number_of_columns; j++ ) {

				try {
					T current = matrix.next( );
					String printed;
					printed = doublePrinter.apply( current );
					sb.append( printed );
					for ( int p = printed.length( ); p < cell_size + 1; p++ ) {
						sb.append( " " );
					}
				}
				catch ( PastTheEndException e ) {
					throw new IllegalArgumentException( );
				}

			}
			sb.append( "|- " );
			sb.append( row );
			sb.append( "\n" );
		}

		return sb.toString( );
	}

	public static void main( String[] args ) {
		String print =
				printWithIterator(
						KnittingCursor.on( "Alice", "Bob", "Carol", "David" ),
						KnittingCursor.on( "likes peach", "likes ananas", "likes wasabi" ),
						KnittingCursor.on(
								0.6d, 1d, 0d,
								0d, 0.51d, 0d,
								2d, 0.2d, 0d,
								3d, 0.999d, 0d ),
						" " );
		System.out.println( print );
	}

}
