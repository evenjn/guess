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

import java.util.function.Function;

import org.github.evenjn.guess.Trainer;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.yarn.Cursable;

public class BlindMapleTrainer<I, O> implements
		Trainer<Tuple<I>, Tuple<O>> {

	public BlindMapleTrainer(
			Cursable<? extends I> above_alphabet,
			Cursable<? extends O> below_alphabet) {
	}

	@Override
	public Function<Tuple<I>, Tuple<O>> train(
			ProgressSpawner progress_spawner,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data ) {
		FrequencyDistribution<O> fd = new FrequencyDistribution<>( );
		KnittingCursable.wrap( data )
		.flatmapCursable( d -> KnittingTuple.wrap( d.back( ) ).asKnittingCursable( ) )
		.consume( h -> fd );
		O mostFrequent = fd.getMostFrequent( );
						

		return x -> new Tuple<O>( ) {

			@Override
			public O get( int index ) {
				x.get( index );
				return mostFrequent;
			}

			@Override
			public int size( ) {
				return x.size( );
			}
		};
	}

}
