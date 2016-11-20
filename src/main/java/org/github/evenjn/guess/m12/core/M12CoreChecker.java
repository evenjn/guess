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

import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.numeric.SixCharFormat;

public class M12CoreChecker {

	private final static SixCharFormat format = new SixCharFormat( true );

	private final static double threshold_max = 1.0001;

	private final static double threshold_min = 0.9999;

	private final M12Core m12;

	private M12CoreChecker(M12Core m12) {
		this.m12 = m12;
	}

	public static void check( M12Core m12 ) {
		M12CoreChecker m12Checker = new M12CoreChecker( m12 );
		m12Checker.checkConsistencyInitial( );
		m12Checker.checkConsistencyTransitions( );
		m12Checker.checkConsistencyEmissions( );
	}

	private void checkConsistencyInitial( ) {

		int size = m12.number_of_states;
		double[] values = new double[size];
		double max = m12.initial_table[0];
		for ( int s = 0; s < m12.number_of_states; s++ ) {
			double v = m12.initial_table[s];
			if ( max < v ) {
				max = v;
			}
			values[s] = v;
		}

		double sum = NumericLogarithm.elnsum( max, values, size );

		if ( NumericLogarithm.eexp( sum ) > threshold_max
				|| NumericLogarithm.eexp( sum ) < threshold_min ) {
			StringBuilder sb = new StringBuilder( );
			sb.append( "Initial distribution sums to " );
			sb.append( sum );
			sb.append( " ( raised: " );
			sb.append( format.apply( NumericLogarithm.eexp( sum ) ) );
			sb.append( " ) ( " );
			sb.append( NumericLogarithm.eexp( sum ) );
			sb.append( " )" );
			throw new IllegalStateException( sb.toString( ) );
		}
	}

	private void checkConsistencyTransitions( ) {
		int size = m12.number_of_states;
		double[] values = new double[size];

		for ( int z = 0; z < m12.number_of_states; z++ ) {
			double[] z_transition = m12.transition_table[z];

			double max = z_transition[0];
			for ( int s = 0; s < m12.number_of_states; s++ ) {
				double v = z_transition[s];
				if ( max < v ) {
					max = v;
				}
				values[s] = v;
			}

			double sum = NumericLogarithm.elnsum( max, values, size );

			if ( NumericLogarithm.eexp( sum ) > threshold_max
					|| NumericLogarithm.eexp( sum ) < threshold_min ) {
				StringBuilder sb = new StringBuilder( );
				sb.append( "Transition distribution sums to " );
				sb.append( sum );
				sb.append( " ( raised: " );
				sb.append( format.apply( NumericLogarithm.eexp( sum ) ) );
				sb.append( " ) ( " );
				sb.append( NumericLogarithm.eexp( sum ) );
				sb.append( " )" );
				throw new IllegalStateException( sb.toString( ) );
			}
		}
	}

	private void checkConsistencyEmissions( ) {

		int size = m12.number_of_symbols;
		double[] values = new double[size];

		for ( int z = 0; z < m12.number_of_states; z++ ) {
			double[] z_emission = m12.emission_table[z];

			double max = z_emission[0];
			for ( int y = 0; y < m12.number_of_symbols; y++ ) {
				double v = z_emission[y];
				if ( max < v ) {
					max = v;
				}
				values[y] = v;
			}

			double sum = NumericLogarithm.elnsum( max, values, size );

			if ( NumericLogarithm.eexp( sum ) > threshold_max
					|| NumericLogarithm.eexp( sum ) < threshold_min ) {
				StringBuilder sb = new StringBuilder( );
				sb.append( "Emission distribution sums to " );
				sb.append( sum );
				sb.append( " ( raised: " );
				sb.append( format.apply( NumericLogarithm.eexp( sum ) ) );
				sb.append( " ) ( " );
				sb.append( NumericLogarithm.eexp( sum ) );
				sb.append( " )" );
				throw new IllegalStateException( sb.toString( ) );
			}
		}
	}
}
