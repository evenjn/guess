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
package org.github.evenjn.guess.m12.bw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetBuilder;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetDataManager;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetDataManagerBlueprint;
import org.github.evenjn.align.graph.TupleAlignmentGraphDataManager;
import org.github.evenjn.align.graph.TupleAlignmentGraphDataManagerBlueprint;
import org.github.evenjn.file.FileFool;
import org.github.evenjn.guess.m12.M12FileTrainer;
import org.github.evenjn.guess.m12.M12QualityChecker;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.plaintext.PlainText;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12BWFileTrainer<I, O> implements M12FileTrainer<I, O> {
	
	private final Function<String, I> a_deserializer;

	private final Function<String, O> b_deserializer;

	private Function<I, String> a_printer;

	private Function<O, String> b_printer;

	public Function<String, I> getDeserializerAbove( ) {
		return a_deserializer;
	}

	public Function<String, O> getDeserializerBelow( ) {
		return b_deserializer;
	}

	public M12BWFileTrainer(
			int min_below,
			int max_below,
			TupleAlignmentAlphabetBuilder<I, O> builder,
			Function<I, String> a_printer,
			Function<O, String> b_printer,
			Function<I, String> a_serializer,
			Function<O, String> b_serializer,
			Function<String, I> a_deserializer,
			Function<String, O> b_deserializer,
			int period,
			int epochs,
			long seed,
			int number_of_states) {
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		this.a_deserializer = a_deserializer;
		this.b_deserializer = b_deserializer;
		taadmb.setMinMaxBelow( min_below, max_below )
				.setInputCoDec( a_serializer, a_deserializer )
				.setOutputCoDec( b_serializer, b_deserializer )
				.setAlphabetBuilder( builder );
		tagdmb.setMinMaxBelow( min_below, max_below );
		m12ctb.trainingTime( period, epochs );
		m12ctb.states( number_of_states );
	}

	private final TupleAlignmentAlphabetDataManagerBlueprint<I, O> taadmb =
			new TupleAlignmentAlphabetDataManagerBlueprint<I, O>( );

	private final TupleAlignmentGraphDataManagerBlueprint<I, O> tagdmb =
			new TupleAlignmentGraphDataManagerBlueprint<I, O>( );

	private final M12BWCoreTrainerBlueprint m12ctb =
			new M12BWCoreTrainerBlueprint( );

	public void train(
			ProgressSpawner progress_spawner,
			FileFool training_cache_path,
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data,
			M12QualityChecker<I, O> checker ) {
		FileFool ff = training_cache_path;
		/**
		 * Override any custom or previous setting.
		 */
		taadmb.deserializeTupleAlignmentAlphabet( null );
		taadmb.serializeTupleAlignmentAlphabet( null );
		tagdmb.deserializeTupleAlignmentGraphs( null );
		tagdmb.serializeTupleAlignmentGraphs( null );
		m12ctb.deserializeModel( null );
		m12ctb.serializeModel( null );

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress progress = SafeProgressSpawner
					.safeSpawn( hook, progress_spawner, "M12FileTrainer::train" );

			/**
			 * The whole point of the FileTrainer is to handle serialization and
			 * deserialization of intermediate and final data structures, to resume
			 * the process if possible.
			 */
			Path alphabet_stable_file =
					ff.getRoot( ).resolve( "ta_alphabet.stable.txt" );
			Path alphabet_working_file =
					ff.getRoot( ).resolve( "ta_alphabet.working.txt" );
			Path alphabet_log_file =
					ff.getRoot( ).resolve( "ta_alphabet.log.txt" );

			if ( ff.exists( alphabet_working_file ) ) {
				ff.delete( alphabet_working_file );
			}
			if ( ff.exists( alphabet_log_file ) ) {
				ff.delete( alphabet_log_file );
			}
			if ( ff.exists( alphabet_stable_file ) ) {
				System.out
						.println( "Using alphabet cached in " + alphabet_stable_file );
				taadmb.deserializeTupleAlignmentAlphabet( h -> PlainText.reader( )
						.build( ).get( h, ff.open( alphabet_stable_file ).read( h ) ) );
			}
			else {
				ff.create( ff.mold( alphabet_working_file ) );
				ff.create( ff.mold( alphabet_log_file ) );
				taadmb
						.setPrinter( h -> PlainText.writer( ).setForcedFlush( true ).build( )
								.get( h, ff.open( alphabet_log_file ).write( h ) ), a_printer,
								b_printer )
						.serializeTupleAlignmentAlphabet( h -> PlainText.writer( ).build( )
								.get( h, ff.open( alphabet_working_file ).write( h ) ) );
				taadmb.deserializeTupleAlignmentAlphabet( h -> PlainText.reader( )
						.build( ).get( h, ff.open( alphabet_working_file ).read( h ) ) );
			}

			Path graphs_stable_file =
					ff.getRoot( ).resolve( "ta_graphs.stable.txt" );
			Path graphs_working_file =
					ff.getRoot( ).resolve( "ta_graphs.working.txt" );

			if ( ff.exists( graphs_working_file ) ) {
				ff.delete( graphs_working_file );
			}
			if ( ff.exists( graphs_stable_file ) ) {
				System.out.println( "Using graphs cached in " + graphs_stable_file );
				tagdmb.deserializeTupleAlignmentGraphs( h -> PlainText.reader( )
						.build( ).get( h, ff.open( graphs_stable_file ).read( h ) ) );
			}
			else {
				ff.create( ff.mold( graphs_working_file ) );
				tagdmb.serializeTupleAlignmentGraphs( h -> PlainText.writer( ).build( )
						.get( h, ff.open( graphs_working_file ).write( h ) ) );
				tagdmb.deserializeTupleAlignmentGraphs( h -> PlainText.reader( )
						.build( ).get( h, ff.open( graphs_working_file ).read( h ) ) );
			}

			Path m12core_initial_file =
					ff.getRoot( ).resolve( "./m12_core.initial.txt" );
			Path m12core_stable_file =
					ff.getRoot( ).resolve( "./m12_core.stable.txt" );
			Path m12core_working_file =
					ff.getRoot( ).resolve( "./m12_core.working.txt" );
			Path m12core_log_file =
					ff.getRoot( ).resolve( "./m12_core.log.txt" );

			if ( ff.exists( m12core_working_file ) ) {
				ff.delete( m12core_working_file );
			}

			boolean skip_m12_training = false;
			if ( ff.exists( m12core_stable_file ) ) {
				System.out.println(
						"Using stable M12 core cached in " + m12core_stable_file );
				System.out.println( "No M12 training will be carried out." );
				skip_m12_training = true;
			}
			else {
				if ( ff.exists( m12core_initial_file ) ) {
					System.out.println(
							"Using initial M12 core cached in " + m12core_initial_file );
					System.out.println( "Training will improve upon that core." );
					Files.copy( m12core_initial_file, m12core_working_file );
					m12ctb.deserializeModel( h -> PlainText.reader( )
							.build( ).get( h, ff.open( m12core_working_file ).read( h ) ) );
				}
				else {
					ff.create( ff.mold( m12core_working_file ) );
				}
				m12ctb.serializeModel( h -> PlainText.writer( ).build( )
						.get( h, ff.open( m12core_working_file ).write( h ) ) );
			}

			/*
			 * heavy computation starts here
			 */

			progress.info( "Loading tuple alignment alphabet." );
			TupleAlignmentAlphabetDataManager<I, O> taadm = taadmb.create( );
			taadm.load( training_data, progress );

			if ( !ff.exists( alphabet_stable_file ) ) {
				Files.copy( alphabet_working_file, alphabet_stable_file );
			}

			progress.info( "Loading tuple alignment graphs." );
			TupleAlignmentGraphDataManager<I, O> tagdm = tagdmb.create( );
			tagdm.load( training_data, taadm.getAlphabet( ).asEncoder( ), progress );

			if ( !ff.exists( graphs_stable_file ) ) {
				Files.copy( graphs_working_file, graphs_stable_file );
			}

			if ( !skip_m12_training ) {

				/*
				 * setup quality control
				 */
				final Consumer<String> training_logger = PlainText
						.writer( ).setForcedFlush( true )
						.build( )
						.get( hook, ff.open( m12core_log_file ).write( hook ) );
				
				if (checker != null) {
					m12ctb.qualityControl( (core, spawn) -> checker.check(
							training_logger,
							taadm.getAlphabet( ),
							core,
							spawn ) );
				}
				
				m12ctb.logger( training_logger );

				progress.info( "Training M12 core." );
				M12BWCoreTrainer m12ct = m12ctb.create( );

				m12ct.load(
						taadm.getAlphabet( ).size( ),
						tagdm.getMaxNumberOfEdges( ),
						tagdm.getMaxLenghtFront( ),
						tagdm.getMaxLenghtBack( ),
						tagdm.getGraphs( ),
						progress );

				Files.copy( m12core_working_file, m12core_stable_file );
			}

			if ( ff.exists( alphabet_working_file ) ) {
				ff.delete( alphabet_working_file );
			}

			if ( ff.exists( graphs_working_file ) ) {
				ff.delete( graphs_working_file );
			}

			if ( ff.exists( m12core_working_file ) ) {
				ff.delete( m12core_working_file );
			}
		}
		catch ( IOException t ) {
			throw new RuntimeException( t );
		}
	}
}
