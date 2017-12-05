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
package org.github.evenjn.guess.m12.baumwelch;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.guess.markov.MarkovChecker;
import org.github.evenjn.guess.markov.MarkovDeserializer;
import org.github.evenjn.guess.markov.MarkovRandomBuilder;
import org.github.evenjn.guess.markov.MarkovSerializer;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Progress;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.yarn.Cursable;

public class M12BWCoreTrainer {

	private int number_of_states;

	private int grace_period;

	private int epochs;

	private Ring<Consumer<String>> putter_core;

	private Cursable<String> reader_core;

	private long seed;

	private BiFunction<Markov, ProgressSpawner, Boolean> quality_control;

	private Consumer<String> logger;

	public M12BWCoreTrainer(
			int number_of_states,
			int period,
			int epochs,
			Consumer<String> logger,
			Ring<Consumer<String>> putter_core,
			Cursable<String> reader_core,
			BiFunction<Markov, ProgressSpawner, Boolean> quality_control,
			long seed) {
		this.number_of_states = number_of_states;
		this.grace_period = period;
		this.epochs = epochs;
		this.logger = logger;
		this.putter_core = putter_core;
		this.reader_core = reader_core;
		this.quality_control = quality_control;
		this.seed = seed;
	}

	public Markov load(
			int number_of_symbols,
			int record_max_number_of_edges,
			int record_max_length_above,
			int record_max_length_below,
			KnittingCursable<TupleAlignmentGraph> graphs,
			ProgressSpawner progress_spawner ) {

		try ( BasicRook rook = new BasicRook() ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner,
					"M12CoreTrainer::prepareCore" );

			Markov core = null;

			if ( reader_core == null ) {
				/*
				 * When the trainer was built without a core reader, initialize a new
				 * random core.
				 */
				spawn.info( "Creating random M12 core." );

				core = MarkovRandomBuilder
						.nu( )
						.states( number_of_states )
						.symbols( number_of_symbols )
						.seed( seed )
						.build( );

				MarkovChecker.check( core );

			}
			else {

				/*
				 * Otherwise, de-serialize it from the reader.
				 */

				try ( BasicRook rook2 = new BasicRook( ) ) {
					core = KnittingCursable
							.wrap( reader_core )
							.pull( rook2 )
							.purlOptional( new MarkovDeserializer( ) )
							.one( );

					MarkovChecker.check( core );
				}
			}

			spawn.info( "Creating baumwelch data structures." );
			
			BiFunction<Markov, ProgressSpawner, Boolean> local_core_inspector =
					new BiFunction<Markov, ProgressSpawner, Boolean>( ) {
						@Override
						public Boolean apply( Markov t, ProgressSpawner spawn ) {
							KnittingCursor.wrap( new MarkovSerializer( t ) )
									.consume( putter_core );
							MarkovChecker.check( t );
							if ( quality_control != null ) {
								return quality_control.apply( t, spawn );
							}
							return false;
						}

					};
			M12BaumWelch baum_welch = new M12BaumWelch(
					core,
					local_core_inspector,
					record_max_number_of_edges,
					record_max_length_above,
					record_max_length_below );
			
			spawn.info( "Training." );
			baum_welch.BaumWelch( logger, graphs, grace_period, epochs, spawn );

			if ( putter_core != null ) {
				KnittingCursor.wrap( new MarkovSerializer( core ) )
						.consume( putter_core );
			}

			MarkovChecker.check( core );

			return core;
		}
	}
}
