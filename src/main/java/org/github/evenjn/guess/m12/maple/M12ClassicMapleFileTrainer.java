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
package org.github.evenjn.guess.m12.maple;

import java.util.function.Supplier;

import org.github.evenjn.file.FileFool;
import org.github.evenjn.guess.TrainingData;
import org.github.evenjn.guess.m12.M12FileTrainer;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Progress;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;

public class M12ClassicMapleFileTrainer<I, O> {

	private M12FileTrainer<I, O> file_trainer;

	public M12ClassicMapleFileTrainer(
			Supplier<? extends M12FileTrainer<I, O>> blueprint) {
		this.file_trainer = blueprint.get( );
	}

	public <K> M12ClassicMaple<I, O> train(
			ProgressSpawner progress_spawner,
			FileFool filefool,
			TrainingData<K, Tuple<I>, Tuple<O>> training_data ) {
		try ( BasicRook rook = new BasicRook( ) ) {
			Progress progress = SafeProgressSpawner
					.safeSpawn( rook, progress_spawner, "M12MapleFileTrainer::train" );
			file_trainer.train( progress, filefool, training_data );
			M12ClassicMaple<I, O> maple = M12ClassicMapleFileDeserializer.deserialize(
					progress_spawner,
					file_trainer.getDeserializerAbove( ),
					file_trainer.getDeserializerBelow( ),
					filefool.getRoot( ) );
			return maple;
		}
	}
}
