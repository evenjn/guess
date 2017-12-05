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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.function.Function;

import org.github.evenjn.knit.BiTray;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

public class NumericUtils {

	public static double sumDoubles(Iterable<Double> numbers) {
		double r = 0;
		for (Double d: numbers) {
			r = r + d;
		}
		return r;
	}
	
	public static Summation summation(int buffer_size,  Function<Iterable<Double>, Double> sum) {
		return new Summation( buffer_size, sum);
	}
	
	public static class Summation {

		private final int buffer_size;
		private Function<Iterable<Double>, Double> sum;
		
		public Summation(int buffer_size, Function<Iterable<Double>, Double> sum) {
			this.sum = sum;
			layers.add( new LinkedList<>( ) );
			this.buffer_size = buffer_size;
		}

		private Vector<LinkedList<Double>> layers = new Vector<>( );
		
		public void add(double value) {
			makeSureThereIsFreeSpace( 0 );
			layers.get( 0 ).add( value );
		}
		
		public double getSum() {
			LinkedList<Double> values = new LinkedList<>( );
			for (LinkedList<Double> list : layers) {
				for (Double d : list) {
					values.add( d );
				}	
			}
			return this.sum.apply( values );
		}
		private void makeSureThereIsFreeSpace( int level ) {
			if (level + 1 > layers.size( )) {
				layers.add( new LinkedList<>( ));
				return;
			}
			LinkedList<Double> list = layers.get( level );
			if (list.size( ) < buffer_size) {
				return;
			}
			makeSureThereIsFreeSpace( level + 1 );
			double sum = this.sum.apply( list );
			layers.get( level + 1 ).add( sum );
			list.clear( );
		}
	}

	
	public static int raiseInt( int base, int exponent ) {
		int result = 1;
		for ( int i = 0; i < exponent; i++ ) {
			result = result * base;
		}
		return result;
	}


	public static <I> BiTray<I, Double> argmax( Function<I, Double> function, Cursor<I> set ) {
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
		return BiTray.nu( result, max );
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
	
	private static <T> Iterator<T> asIterator(Cursor<T> cursor) {
		return KnittingCursor.wrap(cursor).once().iterator();
	}
	
	/**
	 * Returns all pairs [ 0 0 ] [ 0 1 ] [ 0 2 ] .. [ 0 ( max - 1 ) ] [ 1 0 ] [ 1
	 * 1 ] [ 1 2 ] .. [ 1 ( max - 1 ) ] [ ( max - 1 ) 0 ] [ ( max - 1 ) 1 ] [ (
	 * max - 1 ) 2 ] .. [ ( max - 1 ) ( max - 1 ) ]
	 * 
	 * 
	 */
	public static Iterable<BiTray<Integer, Integer>> birange( final int max ) {
		return new Iterable<BiTray<Integer, Integer>>( ) {

			@Override
			public Iterator<BiTray<Integer, Integer>> iterator( ) {
				final BiTray<Integer, Integer> bi = BiTray.nu( 0, -1 );
				return asIterator( new Cursor<BiTray<Integer, Integer>>( ) {

					@Override
					public BiTray<Integer, Integer> next( )
							throws EndOfCursorException {
						int first = bi.front( );
						int second = bi.back( );
						if ( second + 1 == max ) {
							if ( first + 1 == max )
								throw EndOfCursorException.neo();
							first = first + 1;
							second = 0;
						}
						else {
							second = second + 1;
						}
						bi.set( first, second );
						return bi;
					}

				} );

			}
		};

	}
  

	/*
	 * Range generators
	 */

	public static Iterable<Integer> range( final int to ) {
		return range( 0, to );
	}

	public static Iterable<Integer> range( final int from, final int to ) {
		return new Iterable<Integer>( ) {

			@Override
			public Iterator<Integer> iterator( ) {
				return asIterator(new Cursor<Integer>( ) {

					private int current = from;

					@Override
					public Integer next( )
							throws EndOfCursorException {
						if ( current >= to )
							throw EndOfCursorException.neo();
						return current++;
					}
				} );
			}
		};
	}

	public static Iterable<Long> range( final long to ) {
		return range( 0, to );
	}

	public static Iterable<Long> range( final long from, final long to ) {
		return new Iterable<Long>( ) {

			@Override
			public Iterator<Long> iterator( ) {
				return asIterator( new Cursor<Long>( ) {

					private long current = from;

					@Override
					public Long next( )
							throws EndOfCursorException {
						if ( current >= to )
							throw EndOfCursorException.neo();
						return current++;
					}
				} );
			}
		};
	}
}
