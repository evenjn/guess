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
package org.github.evenjn.guess.benchmark;

import java.util.Random;
import java.util.Vector;
import java.util.function.Function;

import org.github.evenjn.guess.Trainer;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class NoiseMapleTrainer<I, O> implements
		Trainer<Tuple<I>, Tuple<O>> {

	private final int below_size;

	private final Vector<O> below = new Vector<>( );

	public NoiseMapleTrainer(
			Cursable<I> above_alphabet,
			Cursable<O> below_alphabet) {
		KnittingCursable.wrap( below_alphabet ).collect( below );
		below_size = below.size( );
	}

	@Override
	public Function<Tuple<I>, Tuple<O>> train(
			ProgressSpawner progress,
			Cursable<Di<Tuple<I>, Tuple<O>>> data ) {
		final Random r = new Random( 1l );
		return new Function<Tuple<I>, Tuple<O>>( ) {

			@Override
			public Tuple<O> apply(
					Tuple<I> t ) {
				Vector<O> v = new Vector<>( );
				for ( int i = 0; i < t.size( ); i++ ) {
					int index = r.nextInt( below_size );
					v.add( below.get( index ) );
				}
				return KnittingTuple.wrap( v );
			}
		};
	}

}
