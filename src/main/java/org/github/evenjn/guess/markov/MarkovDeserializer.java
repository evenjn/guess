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

import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipFold;

public class MarkovDeserializer implements
		SkipFold<String, Markov> {

	private int step = 4;

	private Markov core;

	private int symbol = 0;

	private int state_s = 0;

	private int state_d = 0;

	@Override
	public Markov end( )
			throws SkipException {
		if ( core == null ) {
			throw new IllegalArgumentException( );
		}
		return core;
	}

	@Override
	public Markov next( String object )
			throws SkipException {
		try {
			if ( step == 4 ) {
				step = 3;
				int indexOf = object.indexOf( ' ' );
				core = new Markov(
						Integer.parseInt( object.substring( 0, indexOf ) ),
						Integer.parseInt( object.substring( indexOf + 1 ) ) );
				throw SkipException.neo;
			}
			if ( step == 3 ) {
				/* initial */
				if ( state_s < core.number_of_states ) {
					double val = Double.parseDouble( object );
					core.initial_table[state_s++] = val;
					throw SkipException.neo;
				}
				step = 2;
				state_s = 0;
				if ( !object.equals( "---" + step + "---" ) ) {
					throw new IllegalArgumentException( );
				}
				throw SkipException.neo;
			}
			if ( step == 2 ) {
				/* transition */
				while ( state_s < core.number_of_states ) {
					if ( state_d < core.number_of_states ) {
						double val = Double.parseDouble( object );
						core.transition_table[state_s][state_d++] = val;
						throw SkipException.neo;
					}
					state_d = 0;
					state_s++;
				}
				step = 1;
				state_s = 0;
				if ( !object.equals( "---" + step + "---" ) ) {
					throw new IllegalArgumentException( );
				}
				throw SkipException.neo;
			}
			if ( step == 1 ) {
				/* emission */
				while ( symbol < core.number_of_symbols ) {
					if ( state_s < core.number_of_states ) {
						double val = Double.parseDouble( object );
						core.emission_table[state_s++][symbol] = val;
						throw SkipException.neo;
					}
					state_s = 0;
					symbol++;
				}
				step = 0;
				if ( !object.equals( "---" + step + "---" ) ) {
					throw new IllegalArgumentException( );
				}
				throw SkipException.neo;
			}
		}
		catch ( IndexOutOfBoundsException | NumberFormatException t ) {
			throw new IllegalArgumentException( object, t );
		}
		throw SkipException.neo;
	}
}
