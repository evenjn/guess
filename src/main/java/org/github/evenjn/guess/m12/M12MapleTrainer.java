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

import org.github.evenjn.align.TupleAlignmentGraphDataManager;
import org.github.evenjn.guess.Trainer;
import org.github.evenjn.guess.m12.core.M12Core;
import org.github.evenjn.guess.m12.core.M12CoreTrainer;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12MapleTrainer<I, O>
		implements
		Trainer<Tuple<I>, Tuple<O>> {

	private M12CoreTrainer trainer;

	private TupleAlignmentGraphDataManager<I, O> datamanager;

	public M12MapleTrainer(
			TupleAlignmentGraphDataManager<I, O> datamanager,
			M12CoreTrainer trainer) {
		this.trainer = trainer;
		this.datamanager = datamanager;
	}

	@Override
	public M12Maple<I, O> train(
			ProgressSpawner progress,
			Cursable<Di<Tuple<I>, Tuple<O>>> data ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn =
					ProgressManager.safeSpawn( hook, progress, "M12MapleTrainer::train" );

			spawn.info( "Caching alingment data." );

			datamanager.load( data, spawn );

			spawn.info( "Training." );

			M12Core core = trainer.load(
					datamanager.getAlphabet( ).size( ),
					datamanager.getMaxNumberOfEdges( ),
					datamanager.getMaxLenghtAbove( ),
					datamanager.getMaxLenghtBelow( ),
					datamanager.getGraphs( ),
					spawn );

			spawn.info( "Finalizing machine." );

			M12Maple<I, O> maple = new M12Maple<I, O>(
					datamanager.getAlphabet( ),
					core,
					spawn );

			return maple;
		}
	}

}
