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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.KnittingTuple;

public class FrequencyDistribution<T> implements
		Consumer<T> {

	private int total = 0;

	private HashMap<T, Integer> map = new HashMap<>( );

	public FrequencyDistributionPlot<T> plot( ) {
		return new FrequencyDistributionPlot<T>( ).setData( map, total );
	}

	public String toString( ) {
		return plot( ).print( );
	}

	@Deprecated
	public String toString( Function<T, String> labelizer ) {
		return plot( ).setLabels( labelizer ).print( );
	}

	@Deprecated
	public String toString( boolean enhanced ) {
		return plot( ).displayFraction( enhanced ).print( );
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

	public KnittingCursor<FrequencyData<T>> data( ) {
		Function<Map.Entry<T, Integer>, FrequencyData<T>> mapper =
				new Function<Map.Entry<T, Integer>, FrequencyData<T>>( ) {

					@Override
					public FrequencyData<T> apply( Entry<T, Integer> t ) {
						FrequencyData<T> box = new FrequencyData<>( );
						box.first = t.getKey( );
						box.second = t.getValue( );
						return box;
					}
				};
		return KnittingCursor.wrap( map.entrySet( ) ).map( mapper );
	}
	
	public Optional<Integer> getFrequency( T object ) {
		return Optional.ofNullable( map.get( object ) );
	}

	public KnittingTuple<FrequencyData<T>> dataSorted( boolean descending ) {
		Vector<FrequencyData<T>> result = new Vector<>( );
		KnittingCursor<FrequencyData<T>> iterator = data( );
		for ( FrequencyData<T> data : iterator.once( ) ) {
			result.add( data );
		}

		if (descending) {
			Collections.sort( result, (o1, o2) -> Integer.compare( o2.second, o1.second ) );
		}
		else {
			Collections.sort( result, (o1, o2) -> Integer.compare( o1.second, o2.second ) );
		}
		return KnittingTuple.wrap( result );
	}

}
