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

import org.github.evenjn.yarn.Di;

public class FrequencyData<K> implements
		Di<K, Integer> {

	K first;

	Integer second;

	public K getElement( ) {
		return first;
	}

	public Integer getFrequency( ) {
		return second;
	}

	public boolean equals( Object o ) {
		if ( o != null && o instanceof FrequencyData ) {
			FrequencyData<?> fd = (FrequencyData<?>) o;
			return first.equals( fd.first );
		}
		return false;
	}

	public int hashCode( ) {
		return first.hashCode( );
	}

	public String toString( ) {
		return first.toString( ) + " " + second;
	}

	@Override
	public K front( ) {
		return first;
	}

	@Override
	public Integer back( ) {
		return second;
	}
}
