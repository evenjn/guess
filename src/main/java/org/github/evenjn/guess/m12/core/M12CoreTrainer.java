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
package org.github.evenjn.guess.m12.core;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12CoreTrainer {

	private boolean check_consistency = false;
	
	private int number_of_states;

	private int period;

	private int epochs;

	private Function<Hook, Consumer<String>> putter_core;

	private Cursable<String> reader_core;

	private long seed;
	
	public M12CoreTrainer(
			boolean check_consistency,
			int number_of_states,
			int period,
			int epochs,
			Function<Hook, Consumer<String>> putter_core,
			Cursable<String> reader_core,
			long seed ) {
		this.check_consistency = check_consistency;
		this.number_of_states = number_of_states;
		this.period = period;
		this.epochs = epochs;
		this.putter_core = putter_core;
		this.reader_core = reader_core;
		this.seed = seed;
	}

	public M12Core load(
			int number_of_symbols,
			int record_max_number_of_edges,
			int record_max_length_above,
			int record_max_length_below,
			KnittingCursable<TupleAlignmentGraph> graphs,
			ProgressSpawner progress ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = ProgressManager.safeSpawn( hook, progress, "M12CoreTrainer::prepareCore" );

			M12Core core = null;

			if ( reader_core == null ) {
				/*
				 * When the trainer was built without a core reader, initialize a new
				 * random core.
				 */
				spawn.info( "Creating random M12 core." );

				core = M12CoreRandomBuilder
						.nu( )
						.states( number_of_states )
						.symbols( number_of_symbols )
						.seed( seed  )
						.build( );

				if ( check_consistency ) {
					M12CoreChecker.check( core );
				}

			} else {

				/*
				 * Otherwise, de-serialize it from the reader.
				 */
				
				try ( AutoHook hook2 = new BasicAutoHook( ) ) {
					core = KnittingCursable
							.wrap( reader_core )
							.pull( hook2 )
							.skipfold( new M12CoreDeserializer( ) )
							.one( );

					if ( check_consistency ) {
						M12CoreChecker.check( core );
					}
				}
			}

			spawn.info( "Creating baumwelch data structures." );
			M12BaumWelch baum_welch = new M12BaumWelch(
					core,
					record_max_number_of_edges,
					record_max_length_above,
					record_max_length_below );

			spawn.info( "Training." );
			baum_welch.BaumWelch( graphs, period, epochs, spawn );

			if ( putter_core != null ) {
				KnittingCursor.wrap( new M12CoreSerializer( core ) )
				.consume(putter_core );
			}

			if ( check_consistency ) {
				M12CoreChecker.check( core );
			}

			return core;
		}
	}
}
