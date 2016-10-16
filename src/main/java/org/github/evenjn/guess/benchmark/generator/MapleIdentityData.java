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
package org.github.evenjn.guess.benchmark.generator;

import java.util.Random;
import java.util.Vector;

import org.github.evenjn.guess.benchmark.BenchmarkDatum;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

/**
 * This implements a stream of data where the input tuple is transformed into an
 * identical output tuple, symbol by symbol, with some random noise.
 *
 */
public class MapleIdentityData implements
		Itterator<BenchmarkDatum<Tuple<Boolean>, Tuple<Boolean>>> {

	final long seed = 1848174;

	private BenchmarkDatum<Tuple<Boolean>, Tuple<Boolean>> result =
			new BenchmarkDatum<>( );

	private Random root = new Random( seed );

	private Random input_size = new Random( seed % 158 );

	private Random noise = new Random( seed % 159 );

	protected void doRewind( ) {
		root = new Random( seed );
		input_size = new Random( seed % 158 );
		noise = new Random( seed % 159 );
	}

	@Override
	public BenchmarkDatum<Tuple<Boolean>, Tuple<Boolean>>
			next( )
					throws PastTheEndException {

		int size = 20 + input_size.nextInt( 10 );

		Vector<Boolean> input = new Vector<>( size );
		for ( int i = 0; i < size; i++ ) {
			input.add( root.nextBoolean( ) );
		}

		result.observed = KnittingTuple.wrap( input );

		Vector<Boolean> good_output = new Vector<>( size );
		for ( int i = 0; i < size; i++ ) {
			good_output.add( input.get( i ) );
		}

		result.good_teacher = KnittingTuple.wrap( good_output );

		Vector<Boolean> bad_output = new Vector<>( size );
		for ( int i = 0; i < size; i++ ) {
			int this_noise = noise.nextInt( 10 );
			if ( this_noise < 2 ) {
				bad_output.add( true );
				continue;
			}
			if ( this_noise >= 8 ) {
				bad_output.add( false );
				continue;
			}
			bad_output.add( good_output.get( i ) );
		}
		result.bad_teacher = KnittingTuple.wrap( bad_output );
		return result;
	}

}
