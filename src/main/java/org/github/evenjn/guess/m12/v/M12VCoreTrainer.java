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
package org.github.evenjn.guess.m12.v;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.guess.markov.MarkovChecker;
import org.github.evenjn.guess.markov.MarkovDeserializer;
import org.github.evenjn.guess.markov.MarkovSerializer;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12VCoreTrainer {

	private Function<Hook, Consumer<String>> putter_core;

	private Cursable<String> reader_core;

	private BiFunction<Markov, ProgressSpawner, Boolean> quality_control;

	private Consumer<String> logger;

	private Function<Integer, Object> unveiler;

	public M12VCoreTrainer(
			Consumer<String> logger,
			Function<Integer, Object> unveiler,
			Function<Hook, Consumer<String>> putter_core,
			Cursable<String> reader_core,
			BiFunction<Markov, ProgressSpawner, Boolean> quality_control ) {
		this.logger = logger;
		this.unveiler = unveiler;
		this.putter_core = putter_core;
		this.reader_core = reader_core;
		this.quality_control = quality_control;
	}

	public Markov load(
			int number_of_symbols,
			int record_max_number_of_edges,
			int record_max_length_above,
			int record_max_length_below,
			KnittingCursable<TupleAlignmentGraph> graphs,
			ProgressSpawner progress_spawner ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( hook, progress_spawner,
					"M12VCoreTrainer::prepareCore" );

			Markov core = null;

			if ( reader_core == null ) {
				/*
				 * When the trainer was built without a core reader, initialize a new
				 * random core.
				 */
				spawn.info( "Creating M12 core using visible data." );

				core = new M12VCoreBuilder().build( logger, unveiler, graphs, progress_spawner );

				MarkovChecker.check( core );

			}
			else {

				/*
				 * Otherwise, de-serialize it from the reader.
				 */

				spawn.info( "Decoding M12 core." );
				try ( AutoHook hook2 = new BasicAutoHook( ) ) {
					core = KnittingCursable
							.wrap( reader_core )
							.pull( hook2 )
							.purlOptional( new MarkovDeserializer( ) )
							.one( );

					MarkovChecker.check( core );
				}
			}

			spawn.info( "Quality control." );
			
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
					
			local_core_inspector.apply( core, spawn );
			if ( putter_core != null ) {
				KnittingCursor.wrap( new MarkovSerializer( core ) )
						.consume( putter_core );
			}

			MarkovChecker.check( core );

			return core;
		}
	}
}
