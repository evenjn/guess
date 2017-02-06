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
import java.util.Random;
import java.util.function.Function;

import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipFold;

public class NumericRandom {

	public static <K> SkipFold<K, K> filterPartition(
			int partition_id,
			int number_of_partitions,
			boolean match_means_skip,
			long seed ) {
		Random random = new Random( seed );
		return new SkipFold<K, K>( ) {

			@Override
			public K next( K input )
					throws SkipException {
				if ( match_means_skip == ( random
						.nextInt( number_of_partitions ) == partition_id ) ) {
					throw SkipException.neo;
				}
				return input;
			}

			@Override
			public K end( )
					throws SkipException {
				throw SkipException.neo;
			}

		};
	}

	public static <K> SkipFold<K, K> filterBlockFraction(
			int permille,
			long seed ) {
		Random random = new Random( seed );
		return new SkipFold<K, K>( ) {

			@Override
			public K next( K input )
					throws SkipException {
				if ( random.nextInt( 1000 ) < permille ) {
					throw SkipException.neo;
				}
				return input;
			}

			@Override
			public K end( )
					throws SkipException {
				throw SkipException.neo;
			}

		};
	}

	public static <K> SkipFold<K, K> filterPassFraction(
			int permille,
			long seed ) {
		Random random = new Random( seed );
		return new SkipFold<K, K>( ) {

			@Override
			public K next( K input )
					throws SkipException {
				if ( random.nextInt( 1000 ) >= permille ) {
					throw SkipException.neo;
				}
				return input;
			}

			@Override
			public K end( )
					throws SkipException {
				throw SkipException.neo;
			}

		};
	}

	public static RandomProbabilityMassGenerator generator( long seed ) {
		return new RandomProbabilityMassGenerator( seed );
	}

	public static Function<Integer, Double> randomProbabilityMass(
			long seed, int n ) {
		final Random r = new Random( seed );
		double[] values = new double[n];
		double total = 0d;
		for ( int i = 0; i < n; i++ ) {
			double d = 0.01 + r.nextDouble( );
			total += d;
			values[i] = d;
		}
		for ( int i = 0; i < n; i++ ) {
			values[i] = values[i] / total;
		}
		return x -> values[x];
	}

	public static class RandomProbabilityMassGenerator implements
			Cursor<Double> {

		private Random r;

		private double total;

		public RandomProbabilityMassGenerator(long seed) {
			r = new Random( seed );
		}

		public Double next( ) {
			double d = 0.01 + r.nextDouble( );
			total += d;
			return d;
		}

		public Double totalSoFar( ) {
			return total;
		}

		public void resetTotal( ) {
			total = 0;
		}
	}

	public static Iterable<Integer> randomRobin( long seed, int top ) {
		Iterable<Integer> result = new Iterable<Integer>( ) {

			public Iterator<Integer> iterator( ) {
				final Random r = new Random( seed );
				return new Iterator<Integer>( ) {

					@Override
					public Integer next( ) {
						return r.nextInt( top );
					}

					@Override
					public boolean hasNext( ) {
						return true;
					}
				};
			}
		};
		return result;
	}
}
