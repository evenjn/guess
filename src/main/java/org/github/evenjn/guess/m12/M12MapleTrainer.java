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

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetDataManager;
import org.github.evenjn.align.graph.TupleAlignmentGraphDataManager;
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

	private M12CoreTrainer core_trainer;

	private TupleAlignmentGraphDataManager<I, O> graph_manager;

	private TupleAlignmentAlphabetDataManager<I, O> alphabet_manager;

	public M12MapleTrainer(
			TupleAlignmentAlphabetDataManager<I, O> alphabet_manager,
			TupleAlignmentGraphDataManager<I, O> graph_manager,
			M12CoreTrainer trainer) {
		this.alphabet_manager = alphabet_manager;
		this.core_trainer = trainer;
		this.graph_manager = graph_manager;
	}

	@Override
	public M12Maple<I, O> train(
			ProgressSpawner progress_spawner,
			Cursable<Di<Tuple<I>, Tuple<O>>> data ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress progress = ProgressManager
					.safeSpawn( hook, progress_spawner, "M12MapleTrainer::train" );

			progress.info( "Caching alingment data." );

			alphabet_manager.load( data, progress );

			graph_manager.load( data, alphabet_manager.getAlphabet( ).asEncoder( ),
					progress );

			progress.info( "Training." );

			M12Core core = core_trainer.load(
					alphabet_manager.getAlphabet( ).size( ),
					graph_manager.getMaxNumberOfEdges( ),
					graph_manager.getMaxLenghtAbove( ),
					graph_manager.getMaxLenghtBelow( ),
					graph_manager.getGraphs( ),
					progress );

			progress.info( "Finalizing machine." );

			M12Maple<I, O> maple = new M12Maple<I, O>(
					alphabet_manager.getAlphabet( ),
					core,
					progress );

			return maple;
		}
	}

}
