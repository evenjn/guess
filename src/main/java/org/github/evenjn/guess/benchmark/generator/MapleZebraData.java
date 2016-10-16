package org.github.evenjn.guess.benchmark.generator;

import java.util.Random;
import java.util.Vector;

import org.github.evenjn.guess.benchmark.BenchmarkDatum;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Itterator;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Tuple;

/**
 * This implements a stream of data where the input tuple is transformed into a
 * tuple of alternating true and false, and the first element is always equal to
 * the first element of the input.
 *
 */
public class MapleZebraData implements
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

		int size = 4 + input_size.nextInt( 12 );

		Vector<Boolean> input = new Vector<>( size );
		boolean first = root.nextBoolean( );
		for ( int i = 0; i < size; i++ ) {
			if ( i == 0 ) {
				input.add( first );
			}
			else {
				input.add( root.nextBoolean( ) );
			}
		}

		result.observed = KnittingTuple.wrap( input );

		Vector<Boolean> good_output = new Vector<>( size );
		for ( int i = 0; i < size; i++ ) {
			good_output.add( first == ( i % 2 == 0 ) );
		}

		result.good_teacher = KnittingTuple.wrap( good_output );

		Vector<Boolean> bad_output = new Vector<>( size );
		for ( int i = 0; i < size; i++ ) {
			int this_noise = noise.nextInt( 100 );
			if ( this_noise < 2 ) {
				bad_output.add( true );
				continue;
			}
			if ( this_noise >= 98 ) {
				bad_output.add( false );
				continue;
			}
			bad_output.add( good_output.get( i ) );
		}
		result.bad_teacher = KnittingTuple.wrap( bad_output );
		return result;
	}

}
