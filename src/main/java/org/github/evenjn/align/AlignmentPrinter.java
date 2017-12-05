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

import java.util.LinkedList;
import java.util.function.Function;

import org.github.evenjn.knit.BiValue;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Tuple;

public class AlignmentPrinter<I, O> {

	private final Function<I, String> i_printer;

	private final Function<O, String> o_printer;

	private int tab_size;

	public Iterable<String> printHorizontal(
			Tuple<I> above,
			Tuple<O> below,
			Tuple<BiValue<Integer, Integer>> alignment) {
		StringBuilder sb_above = new StringBuilder( );
		StringBuilder sb_below = new StringBuilder( );
		KnittingTuple<I> ka = KnittingTuple.wrap( above );
		KnittingTuple<O> kb = KnittingTuple.wrap( below );
		KnittingTuple<BiValue<Integer, Integer>> kl = KnittingTuple.wrap( alignment );
		int a_start = 0;
		int b_start = 0;
		for ( BiValue<Integer, Integer> ad : kl.asIterable( ) ) {
//			System.err.println( "AD: " + ad.a_length + " " + ad.b_length );
			String separator = "";
			int la = 0;
			for ( int i = 0; i < ad.front( ); i++ ) {
				String s = i_printer.apply( ka.get( a_start + i ) );
				sb_above.append( separator ).append( s );
				la = la + separator.length( ) + s.length( );
				separator = " ";
			}
			int lb = 0;
			separator = "";
			for ( int i = 0; i < ad.back( ); i++ ) {
				String s = o_printer.apply( kb.get( b_start + i ) );
				sb_below.append( separator ).append( s );
				lb = lb + separator.length( ) + s.length( );
				separator = " ";
			}
			
			int max = la < lb ? lb : la;
			max = max + 1;
			max = max < tab_size ? tab_size : max;
			
			
			for (int i = la; i < max; i++) { 
				sb_above.append( " " );
			}
			for (int i = lb; i < max; i++) { 
				sb_below.append( " " );
			}
			
			a_start = a_start + ad.front( );
			b_start = b_start + ad.back( );
			
		}
		LinkedList<String> result = new LinkedList<>( );
		result.add( sb_above.toString( ) );
		result.add( sb_below.toString( ) );
		return result;
	}

	public AlignmentPrinter(
			Function<I, String> i_printer,
			Function<O, String> o_printer,
			int tab_size ) {
		this.i_printer = i_printer;
		this.o_printer = o_printer;
		this.tab_size = tab_size;
	}
	
	public Iterable<String> printVertical(
			Tuple<I> above,
			Tuple<O> below,
			Tuple<BiValue<Integer, Integer>> alignment) {
		LinkedList<String> result = new LinkedList<>( );
		StringBuilder sb = new StringBuilder( );
		KnittingTuple<I> ka = KnittingTuple.wrap( above );
		KnittingTuple<O> kb = KnittingTuple.wrap( below );
		KnittingTuple<BiValue<Integer, Integer>> kl = KnittingTuple.wrap( alignment );
		int a_start = 0;
		int b_start = 0;
		for ( BiValue<Integer, Integer> ad : kl.asIterable( ) ) {
			String separator = "";
			int la = 0;
			for ( int i = 0; i < ad.front( ); i++ ) {
				String s = i_printer.apply( ka.get( a_start + i ) );
				sb.append( separator ).append( s );
				la = la + separator.length( ) + s.length( );
				separator = " ";
			}
			int max = la < tab_size ? tab_size : la;
			
			for (int i = la; i < max; i++) { 
				sb.append( " " );
			}
			separator = "";
			for ( int i = 0; i < ad.back( ); i++ ) {
				sb.append( separator ).append( o_printer.apply( kb.get( b_start + i ) ) );
				separator = " ";
			}
			result.add( sb.toString( ) );
			sb = new StringBuilder( );
			a_start = a_start + ad.front( );
			b_start = b_start + ad.back( );
		}
		return result;
	}
}
