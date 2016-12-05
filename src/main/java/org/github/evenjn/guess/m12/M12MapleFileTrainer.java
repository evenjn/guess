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
package org.github.evenjn.guess.m12;

import java.nio.file.Path;

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12MapleFileTrainer<I, O> {

	private M12FileTrainer<I, O> m12_file_trainer;

	public M12MapleFileTrainer(M12FileTrainerBlueprint<I, O> blueprint) {
		this.m12_file_trainer = blueprint.create( );
	}

	public M12Maple<I, O> train(
			ProgressSpawner progress_spawner,
			Path training_cache_path,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress progress = ProgressManager
					.safeSpawn( hook, progress_spawner, "M12MapleFileTrainer::train" );
			m12_file_trainer.train( progress, training_cache_path, data );
			M12Maple<I, O> maple = M12MapleFileDeserializer.deserialize(
					progress_spawner,
					m12_file_trainer.getDeserializerAbove( ),
					m12_file_trainer.getDeserializerBelow( ),
					training_cache_path );
			return maple;
		}
	}
}
