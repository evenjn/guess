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

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.PastTheEndException;

public class MarkovSerializer implements
		Cursor<String> {

	private Markov core;

	private int step = 4;

	private int state_s = 0;

	private int state_d = 0;

	private int symbol = 0;

	public MarkovSerializer(Markov m12) {
		core = m12;
	}

	@Override
	public String next( )
			throws PastTheEndException {
		if ( step == 4 ) {
			step = 3;
			return "" + core.number_of_states + " " + core.number_of_symbols;
		}
		if ( step == 3 ) {
			/* initial */
			if ( state_s < core.number_of_states ) {
				return Double.toString( core.initial_table[state_s++] );
			}
			step = 2;
			state_s = 0;
			return "---" + step + "---";
		}
		if ( step == 2 ) {
			/* transition */
			while ( state_s < core.number_of_states ) {
				if ( state_d < core.number_of_states ) {
					return Double.toString( core.transition_table[state_s][state_d++] );
				}
				state_d = 0;
				state_s++;
			}
			step = 1;
			state_s = 0;
			return "---" + step + "---";
		}
		if ( step == 1 ) {
			/* emission */
			while ( symbol < core.number_of_symbols ) {
				if ( state_s < core.number_of_states ) {
					return Double.toString( core.emission_table[state_s++][symbol] );
				}
				state_s = 0;
				symbol++;
			}
			step = 0;
			return "---" + step + "---";
		}
		throw PastTheEndException.neo;
	}

}
