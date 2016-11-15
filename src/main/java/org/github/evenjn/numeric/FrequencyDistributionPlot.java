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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.github.evenjn.knit.Bi;

public class FrequencyDistributionPlot<K> {

	private Function<K, String> labels = x -> x.toString( );

	private Comparator<K> comparator = null;

	private boolean display_fraction = false;

	private Function<Double, String> numeral_printer = new SixCharFormat( false );

	private Map<K, Integer> the_map;

	private int total;

	private int topN;

	public FrequencyDistributionPlot<K> setNumeralPrinter(
			Function<Double, String> numeral_printer ) {
		this.numeral_printer = numeral_printer;
		return this;
	}

	public FrequencyDistributionPlot<K> setLabels( Function<K, String> labels ) {
		this.labels = labels;
		return this;
	}

	public FrequencyDistributionPlot<K> setComparator( Comparator<K> comparator ) {
		this.comparator = comparator;
		return this;
	}

	public FrequencyDistributionPlot<K> displayFraction( boolean display ) {
		this.display_fraction = display;
		return this;
	}

	public FrequencyDistributionPlot<K> setData( Map<K, Integer> the_map,
			int total ) {
		this.the_map = the_map;
		this.total = total;
		return this;
	}
	
	public FrequencyDistributionPlot<K> setLimit( int topN ) {
		this.topN = topN;
		return this;
	}

	private Iterable<Bi<K, Integer>> data(
			Map<K, Integer> the_map,
			Comparator<K> comparator ) {
		ArrayList<Bi<K, Integer>> data = new ArrayList<>( );
		for ( Entry<K, Integer> d : the_map.entrySet( ) ) {
			data.add( Bi.nu( d.getKey( ), d.getValue( ) ) );
		}
		Collections.sort( data, new Comparator<Bi<K, Integer>>( ) {

			@Override
			public int compare( Bi<K, Integer> o1, Bi<K, Integer> o2 ) {
				if ( comparator != null ) {
					return comparator.compare( o1.first, o2.first );
				}
				return o2.second.compareTo( o1.second );
			}
		} );
		return data;
	}
	
	public String print( ) {

		double denominator = total;
		StringBuilder sb = new StringBuilder( );
		int printed = 0;
		for ( Bi<K, Integer> d : data( the_map, comparator ) ) {
			if (topN > 0 && printed > topN) {
				break;
			}
			int len = 0;
			double n = d.second;
			if ( display_fraction ) {
				sb.append( numeral_printer.apply( n ) ).append( " " );
				String percent = numeral_printer.apply( n / denominator );
				sb.append( percent ).append( "%" );
				len = percent.length( ) + 1;
			}
			else {
				String num = "" + d.second;
				sb.append( num ).append( " " );
				len = num.length( ) + 1;
			}
			while ( len < 10 ) {
				sb.append( " " );
				len++;
			}
			int black = (int) Math.floor( 40 * ( n / denominator ) );
			int white = 39 - black;

			while ( len < 10 + white ) {
				sb.append( " " );
				len++;
			}
			sb.append( "*" );
			while ( len < 10 + 40 ) {
				sb.append( "-" );
				len++;
			}
			sb.append( "| " );
			sb.append( labels.apply( d.first ) );
			sb.append( "\n" );
			printed++;
		}
		return sb.toString( );
	}

}
