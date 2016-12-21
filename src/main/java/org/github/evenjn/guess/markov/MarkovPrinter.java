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
package org.github.evenjn.guess.markov;

import java.util.function.Function;

import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.numeric.SixCharFormat;

public class MarkovPrinter {

	public static String print( Markov core,
			Function<Integer, String> debug_symbol_printer ) {
		SixCharFormat format = new SixCharFormat( false );
		StringBuilder sb = new StringBuilder( );

		double min = NumericLogarithm.smallLogValue;

		for ( int s = 0; s < core.number_of_states; s++ ) {
			double c = core.initial_table[s];
			if ( c < min ) {
				min = c;
			}
		}

		for ( int s = 0; s < core.number_of_states; s++ ) {
			double c = core.initial_table[s];
			if ( c <= min )
				continue;
			sb.append( format.apply( NumericLogarithm.eexp( c ) ) );
			sb.append( " * -> " + s );
			sb.append( "\n" );
		}

		min = NumericLogarithm.smallLogValue;

		for ( int s = 0; s < core.number_of_states; s++ ) {
			for ( int d = 0; d < core.number_of_states; d++ ) {
				double c = core.transition_table[s][d];
				if ( c < min ) {
					min = c;
				}
			}
		}

		for ( int s = 0; s < core.number_of_states; s++ ) {
			for ( int d = 0; d < core.number_of_states; d++ ) {
				double c = core.transition_table[s][d];
				if ( c <= min )
					continue;
				sb.append( format.apply( NumericLogarithm.eexp( c ) ) );
				sb.append( " " + s + " -> " + d );
				sb.append( "\n" );
			}
		}

		for ( int s = 0; s < core.number_of_states; s++ ) {
		}

		for ( int s = 0; s < core.number_of_states; s++ ) {
			
			double max = NumericLogarithm.smallLogValue;
			
			for ( int e = 0; e < core.number_of_symbols; e++ ) {
				double c = core.emission_table[s][e];
				if ( c > max ) {
					max = c;
				}
				if ( c < min ) {
					min = c;
				}
			}
			for ( int e = 0; e < core.number_of_symbols; e++ ) {
				double c = core.emission_table[s][e];
				if ( NumericLogarithm.eexp(c)
						< NumericLogarithm.eexp(min) + ( ( NumericLogarithm.eexp( max) - NumericLogarithm.eexp(min) ) / 2 ) )
					continue;
				sb.append( format.apply( NumericLogarithm.eexp( c ) ) );
				sb.append( " " + s + " ~ " + debug_symbol_printer.apply( e ) );
				sb.append( "\n" );
			}
		}
		return sb.toString( );
	}
}
