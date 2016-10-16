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
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.yarn.Bi;

public class FrequencyDistribution<T> implements
		Consumer<T> {

	private int total = 0;

	private HashMap<T, Integer> map = new HashMap<>( );

	public Iterable<Bi<T, Integer>> data( ) {
		ArrayList<Bi<T, Integer>> data = new ArrayList<>( );
		for ( Entry<T, Integer> d : map.entrySet( ) ) {
			data.add( Bi.nu( d.getKey( ), d.getValue( ) ) );
		}
		Collections.sort( data, new Comparator<Bi<T, Integer>>( ) {

			@Override
			public int compare( Bi<T, Integer> o1, Bi<T, Integer> o2 ) {
				return o1.second.compareTo( o2.second );
			}
		} );
		return data;
	}

	public String toString( ) {
		return toString( x -> x.toString( ) );
	}

	public String toString( Function<T, String> labelizer ) {
		SixCharFormat fun = new SixCharFormat( false );
		double denominator = total;
		StringBuilder sb = new StringBuilder( );
		for ( Bi<T, Integer> d : data( ) ) {
			int len = 0;
			double n = d.second;
			sb.append( fun.apply( n ) ).append( " " );
			String percent = fun.apply( n / denominator );
			sb.append( percent ).append( "%" );
			len = percent.length( ) + 1;
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
			sb.append( labelizer.apply( d.first ) );
			sb.append( "\n" );
		}
		return sb.toString( );
	}

	public int getTotal( ) {
		return total;
	}

	private T mostfrequent = null;

	public T getMostFrequent( ) {
		if ( mostfrequent != null )
			return mostfrequent;
		int max = 0;
		for ( Entry<T, Integer> pair : map.entrySet( ) ) {
			int curr = pair.getValue( );
			if ( curr >= max ) {
				max = curr;
				mostfrequent = pair.getKey( );
			}
		}
		return mostfrequent;
	}

	@Override
	public void accept( T t ) {
		Integer integer = map.get( t );
		if ( integer == null ) {
			integer = 0;
		}
		total++;
		map.put( t, integer + 1 );
	}
}
