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
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.yarn.Cursable;

/**
 * A blind guesser ignores all features but the target, and predicts always
 * the most frequent target value.
 */
public class BlindGuesserTrainer<I, O> implements
		Trainer<I, O> {

	@Override
	public Function<I, O> train(
			ProgressSpawner progress_spawner,
			Cursable<Bi<I, O>> data ) {
		FrequencyDistribution<O> fd = new FrequencyDistribution<>( );
		KnittingCursable.wrap( data ).map( d -> d.back( ) ).peek( fd ).roll( );
		return x -> fd.getMostFrequent( );
	}

}
