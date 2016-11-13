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

import java.util.Random;

import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.numeric.NumericRandom;
import org.github.evenjn.numeric.NumericRandom.RandomProbabilityMassGenerator;

public class M12CoreRandomBuilder {

	private int number_of_states;

	private int number_of_symbols;

	private long seed;
	
	public static M12CoreRandomBuilder nu( ) {
		return new M12CoreRandomBuilder( );
	}
	
	public M12CoreRandomBuilder states(
			int number_of_states ) {
		this.number_of_states = number_of_states;
		return this;
	}
	
	
	public M12CoreRandomBuilder symbols(
			int number_of_symbols ) {
		this.number_of_symbols = number_of_symbols;
		return this;
	}

	public M12CoreRandomBuilder seed(long seed) {
		this.seed = seed;
		return this;
	}


	public M12Core build() {
		if ( number_of_states == 0 ) {
			throw new IllegalStateException( "The number of states must be greater than zero." );
		}
		if ( number_of_symbols == 0 ) {
			throw new IllegalStateException( "The number of symbols must be greater than zero." );
		}
		M12Core m12 = new M12Core( number_of_states, number_of_symbols );

		Random r = new Random( seed );
		
		RandomProbabilityMassGenerator generator =
				NumericRandom.generator( r.nextLong( ) );
		double denominator;

		/*
		 * Initial
		 */

		generator = NumericRandom.generator( r.nextLong( ) );

		for ( int s = 0; s < number_of_states; s++ ) {
			m12.initial_table[s] = NumericLogarithm.eln( generator.next( ) );
		}

		denominator = NumericLogarithm.eln( generator.totalSoFar( ) );

		for ( int s = 0; s < number_of_states; s++ ) {
			m12.initial_table[s] =
					NumericLogarithm.elndivision( m12.initial_table[s], denominator );
		}

		/*
		 * Transition
		 */

		generator = NumericRandom.generator( r.nextLong( ) );

		for ( int s = 0; s < number_of_states; s++ ) {
			generator.resetTotal( );
			for ( int d = 0; d < number_of_states; d++ ) {
				m12.transition_table[s][d] = NumericLogarithm.eln( generator.next( ) );
			}
			denominator = NumericLogarithm.eln( generator.totalSoFar( ) );
			for ( int d = 0; d < number_of_states; d++ ) {
				m12.transition_table[s][d] =
						NumericLogarithm.elndivision( m12.transition_table[s][d],
								denominator );
			}
		}

		/*
		 * Emission
		 */

		generator = NumericRandom.generator( r.nextLong( ) );

		for ( int s = 0; s < number_of_states; s++ ) {
			generator.resetTotal( );
			for ( int e = 0; e < number_of_symbols; e++ ) {
				m12.emission_table[s][e] = NumericLogarithm.eln( generator.next( ) );
			}
			denominator = NumericLogarithm.eln( generator.totalSoFar( ) );
			for ( int e = 0; e < number_of_symbols; e++ ) {
				m12.emission_table[s][e] =
						NumericLogarithm.elndivision( m12.emission_table[s][e],
								denominator );
			}
		}

		return m12;
	}
}
