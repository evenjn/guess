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

import java.util.Optional;

import org.github.evenjn.yarn.OptionalPurl;

public class MarkovDeserializer implements
		OptionalPurl<String, Markov> {

	private int step = 4;

	private Markov core;

	private int symbol = 0;

	private int state_s = 0;

	private int state_d = 0;

	@Override
	public Optional<Markov> end( ) {
		if ( core == null ) {
			throw new IllegalArgumentException( );
		}
		return Optional.of( core );
	}

	@Override
	public Optional<Markov> next( String object ) {
		try {
			if ( step == 4 ) {
				step = 3;
				int indexOf = object.indexOf( ' ' );
				core = new Markov(
						Integer.parseInt( object.substring( 0, indexOf ) ),
						Integer.parseInt( object.substring( indexOf + 1 ) ) );
				return Optional.empty( );
			}
			if ( step == 3 ) {
				/* initial */
				if ( state_s < core.number_of_states ) {
					double val = Double.parseDouble( object );
					core.initial_table[state_s++] = val;
					return Optional.empty( );
				}
				step = 2;
				state_s = 0;
				if ( !object.equals( "---" + step + "---" ) ) {
					throw new IllegalArgumentException( );
				}
				return Optional.empty( );
			}
			if ( step == 2 ) {
				/* transition */
				while ( state_s < core.number_of_states ) {
					if ( state_d < core.number_of_states ) {
						double val = Double.parseDouble( object );
						core.transition_table[state_s][state_d++] = val;
						return Optional.empty( );
					}
					state_d = 0;
					state_s++;
				}
				step = 1;
				state_s = 0;
				if ( !object.equals( "---" + step + "---" ) ) {
					throw new IllegalArgumentException( );
				}
				return Optional.empty( );
			}
			if ( step == 1 ) {
				/* emission */
				while ( symbol < core.number_of_symbols ) {
					if ( state_s < core.number_of_states ) {
						double val = Double.parseDouble( object );
						core.emission_table[state_s++][symbol] = val;
						return Optional.empty( );
					}
					state_s = 0;
					symbol++;
				}
				step = 0;
				if ( !object.equals( "---" + step + "---" ) ) {
					throw new IllegalArgumentException( );
				}
				return Optional.empty( );
			}
		}
		catch ( IndexOutOfBoundsException | NumberFormatException t ) {
			throw new IllegalArgumentException( object, t );
		}
		return Optional.empty( );
	}
}
