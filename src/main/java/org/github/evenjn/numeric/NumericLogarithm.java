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

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.PastTheEndException;

public class NumericLogarithm {


	/**
	 * See Tobias P.Mann, 2006: Numerically Stable Hidden Markov Model
	 * Implementation
	 * 
	 */
	public static double elnproduct( double ... factors ) {
		double product = 0d;
		for ( double factor : factors ) {
			if ( factor <= smallLogValue )
				return smallLogValue;
			product += factor;
		}
		return product;
	}

	public static double elnsum2( double lna, double lnb ) {
		return elnsum( KnittingCursable.on( lna, lnb ) );
	}

	public static double elndivision( double lna, double lnb ) {
		if ( lnb <= smallLogValue ) {
			throw new IllegalArgumentException( "Division by zero." );
		}
		if ( lna <= smallLogValue ) {
			return smallLogValue;
		}
		return lna - lnb;
	}

	static public double eln( double d ) {
		if ( d == 0 ) {
			return NumericLogarithm.smallLogValue;
		}
		return Math.log( d );
	}

	static public double eexp( double d ) {
		if ( d <= NumericLogarithm.smallLogValue ) {
			return 0;
		}
		return Math.exp( d );
	}

	public static final Double smallLogValue = -1E300d;

	public static final Double oneLogValue = 0d;


	/**
	 * 
	 * The logarithm of the sum of the numbers obtained by raising e to the power
	 * of the input values.
	 * 
	 */
	public static double elnsum( Cursable<Double> values ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Cursor<Double> pull = values.pull( hook );
			Double max = null;
			for ( ;; ) {
				try {
					Double v = pull.next( );
					if ( max == null || max < v ) {
						max = v;
					}
				}
				catch ( PastTheEndException e ) {
					break;
				}
			}
			if ( max == null || max < smallLogValue )
				return smallLogValue;

			pull = values.pull( hook );
			double sum = 0d;
			for ( ;; ) {
				try {
					Double v = pull.next( );
					double dif = v - max;
					if ( dif <= smallLogValue )
						continue;
					double raised = Math.exp( dif );
					sum += raised;
				}
				catch ( PastTheEndException e ) {
					break;
				}
			}
			double result = max + eln( sum );
			if ( result < smallLogValue )
				result = smallLogValue;
			return result;

		}
	}

	public static double elnsum( double max, double[] values, int len ) {
		if ( max < smallLogValue ) {
			return smallLogValue;
		}
		double sum = 0d;
		double dif;
		for ( int i = 0; i < len; i++ ) {
			dif = values[i] - max;
			if ( dif <= smallLogValue ) {
				continue;
			}
			sum += Math.exp( dif );
		}
		double result = max + eln( sum );
		if ( result < smallLogValue ) {
			return smallLogValue;
		}
		return result;
	}


}
