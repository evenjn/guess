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
package org.github.evenjn.guess.m12.core;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;

public class M12CoreTrainerBlueprint {

	private boolean check_consistency = false;

	private int number_of_states;

	private int period;

	private int epochs;

	private Function<Hook, Consumer<String>> putter_core;

	private Cursable<String> reader_core;

	private long seed;;

	public M12CoreTrainerBlueprint trainingTime( int period, int epochs ) {
		this.period = period;
		this.epochs = epochs;
		return this;
	}

	public M12CoreTrainerBlueprint checkConsistency( boolean check_consistency ) {
		this.check_consistency = check_consistency;
		return this;
	}

	public M12CoreTrainerBlueprint seed( long seed ) {
		this.seed = seed;
		return this;
	}

	public M12CoreTrainerBlueprint states( int number_of_states ) {
		this.number_of_states = number_of_states;
		return this;
	}

	public M12CoreTrainerBlueprint
			serializeModel( Function<Hook, Consumer<String>> putter_core ) {
		this.putter_core = putter_core;
		return this;
	}

	public M12CoreTrainerBlueprint
			deserializeModel( Cursable<String> reader_coalignments ) {
		this.reader_core = reader_coalignments;
		return this;
	}

	public M12CoreTrainer create( ) {
		return new M12CoreTrainer(
				check_consistency,
				number_of_states,
				period,
				epochs,
				putter_core,
				reader_core,
				seed );
	}

}
