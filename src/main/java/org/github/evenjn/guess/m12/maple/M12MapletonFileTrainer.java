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
import org.github.evenjn.guess.m12.M12FileTrainer;
import org.github.evenjn.guess.m12.M12QualityChecker;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12MapletonFileTrainer<I, O> {

	private M12FileTrainer<I, O> file_trainer;

	public M12MapletonFileTrainer(Supplier<? extends M12FileTrainer<I, O>> blueprint) {
		this.file_trainer = blueprint.get( );
	}
	
	@Deprecated
	public M12Mapleton<I, O> train(
			ProgressSpawner progress_spawner,
			FileFool filefool,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data ) {
		return train( progress_spawner, filefool, data, null );
	}

	public M12Mapleton<I, O> train(
			ProgressSpawner progress_spawner,
			FileFool filefool,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data,
			M12QualityChecker<I, O> checker ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress progress = SafeProgressSpawner
					.safeSpawn( hook, progress_spawner, "M12MapletonFileTrainer::train" );
			file_trainer.train( progress, filefool, data, checker );
			M12Mapleton<I, O> maple = M12MapletonFileDeserializer.deserialize(
					progress_spawner,
					file_trainer.getDeserializerAbove( ),
					file_trainer.getDeserializerBelow( ),
					filefool.getRoot( ) );
			return maple;
		}
	}
}
