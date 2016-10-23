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

import java.util.function.Function;

import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Itterable;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;

public class NumericUtils {

	public static int raiseInt( int base, int exponent ) {
		int result = 1;
		for ( int i = 0; i < exponent; i++ ) {
			result = result * base;
		}
		return result;
	}


	public static <I> void argmax( Function<I, Double> function, Cursor<I> set,
			Bi<I, Double> bi ) {
		double max = 0d;
		I result = null;
		for ( I input : KnittingCursor.wrap( set ).once( ) ) {
			if ( result == null ) {
				result = input;
				max = function.apply( input );
				continue;
			}
			double tmp = function.apply( input );
			if ( tmp > max ) {
				max = tmp;
				result = input;
			}
		}
		bi.first = result;
		bi.second = max;
	}

	public static <I> I argmax( Iterable<I> set, Function<I, Double> function ) {
		boolean found = false;
		double max = 0d;
		I result = null;
		for ( I input : set ) {
			double tmp = function.apply( input );
			if ( !found || tmp > max ) {
				found = true;
				result = input;
				max = tmp;
			}
		}
		return result;
	}

	public static <I> I argmax( Cursor<I> set, Function<I, Double> function ) {
		boolean found = false;
		double max = 0d;
		I result = null;
		for ( I input : KnittingCursor.wrap( set ).once( ) ) {
			double tmp = function.apply( input );
			if ( !found || tmp > max ) {
				found = true;
				result = input;
				max = tmp;
			}
		}
		return result;
	}

	public static <I> Double sum( Function<I, Double> function, Cursor<I> set ) {
		double result = 0d;
		for ( I input : KnittingCursor.wrap( set ).once( ) ) {
			result += function.apply( input );
		}
		return result;
	}

	public static <I> Double sum( Cursor<I> set, Function<I, Double> function ) {
		double result = 0d;
		for ( I input : KnittingCursor.wrap( set ).once( ) ) {
			result += function.apply( input );
		}
		return result;
	}

	public static <I extends Comparable<I>> I max( Cursor<I> set ) {
		I result = null;
		for ( I input : KnittingCursor.wrap( set ).once( ) ) {
			if ( result == null || result.compareTo( input ) < 0 ) {
				result = input;
			}
		}
		return result;
	}

	public static <I extends Comparable<I>> I min( Cursor<I> set ) {
		I result = null;
		for ( I input : KnittingCursor.wrap( set ).once( ) ) {
			if ( result == null || result.compareTo( input ) > 0 ) {
				result = input;
			}
		}
		return result;
	}


	public static <I> Double sum( Iterable<I> set, Function<I, Double> function ) {
		double result = 0d;
		for ( I input : set ) {
			result += function.apply( input );
		}
		return result;
	}

	public static <I> Double product( Iterable<I> set,
			Function<I, Double> function ) {
		double result = 1d;
		for ( I input : set ) {
			result *= function.apply( input );
		}
		return result;
	}
	
	/**
	 * Returns all pairs [ 0 0 ] [ 0 1 ] [ 0 2 ] .. [ 0 ( max - 1 ) ] [ 1 0 ] [ 1
	 * 1 ] [ 1 2 ] .. [ 1 ( max - 1 ) ] [ ( max - 1 ) 0 ] [ ( max - 1 ) 1 ] [ (
	 * max - 1 ) 2 ] .. [ ( max - 1 ) ( max - 1 ) ]
	 * 
	 * 
	 */
	public static Itterable<Bi<Integer, Integer>> birange( final int max ) {
		return new Itterable<Bi<Integer, Integer>>( ) {

			@Override
			public Itterator<Bi<Integer, Integer>> pull( ) {
				final Bi<Integer, Integer> bi = new Bi<Integer, Integer>( );
				bi.first = 0;
				bi.second = -1;
				return new Itterator<Bi<Integer, Integer>>( ) {

					@Override
					public Bi<Integer, Integer> next( )
							throws PastTheEndException {
						if ( bi.second + 1 == max ) {
							if ( bi.first + 1 == max )
								throw PastTheEndException.neo;
							bi.first = bi.first + 1;
							bi.second = 0;
						}
						else {
							bi.second = bi.second + 1;
						}
						// System.err.println(bi.first + " " + bi.second);
						return bi;
					}

				};

			}
		};

	}
  

	/*
	 * Range generators
	 */

	public static Itterable<Integer> range( final int to ) {
		return range( 0, to );
	}

	public static Itterable<Integer> range( final int from, final int to ) {
		return new Itterable<Integer>( ) {

			@Override
			public Itterator<Integer> pull( ) {
				return new Itterator<Integer>( ) {

					private int current = from;

					@Override
					public Integer next( )
							throws PastTheEndException {
						if ( current >= to )
							throw PastTheEndException.neo;
						return current++;
					}
				};
			}
		};
	}

	public static Itterable<Long> range( final long to ) {
		return range( 0, to );
	}

	public static Itterable<Long> range( final long from, final long to ) {
		return new Itterable<Long>( ) {

			@Override
			public Itterator<Long> pull( ) {
				return new Itterator<Long>( ) {

					private long current = from;

					@Override
					public Long next( )
							throws PastTheEndException {
						if ( current >= to )
							throw PastTheEndException.neo;
						return current++;
					}
				};
			}
		};
	}
}