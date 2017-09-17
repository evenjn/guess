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
import java.nio.file.Paths;
import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetGreedyBuilder;
import org.github.evenjn.file.FileFool;
import org.github.evenjn.file.FileFoolWriter;
import org.github.evenjn.guess.Trainer;
import org.github.evenjn.guess.benchmark.Benchmark;
import org.github.evenjn.guess.benchmark.BenchmarkHandicap;
import org.github.evenjn.guess.benchmark.BenchmarkTrial;
import org.github.evenjn.guess.benchmark.TupleEqualsEvaluator;
import org.github.evenjn.guess.m12.visible.M12VisibleTrainingPlan;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;
import org.junit.Test;

public class TestM12VisibleClassicMapleTrainer {

	
	{
		Path target = Paths.get( "." ).toAbsolutePath( ).resolve( "target" );
		FileFoolWriter w = FileFool.w( target );

		Path training_cache =
				w.create( w.mold( Paths.get( "training_cache" ) ).asDirectory( )
						.eraseIfExists( ) );

		training_cache_path = w.normalizedAbsolute( training_cache );
	}

	private static Path training_cache_path;

	
	/** HANDICAP */
	private final static BenchmarkHandicap handicap =
			new BenchmarkHandicap( true, 20 );

	/** EVALUATOR */
	private final static String evaluator_label = "equals";

	private final static TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>> evaluator =
			new TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>>( );

	/** TRAINER */
	private final static String trainer_label = "Maple: M12 visible classic viterbi";
	
	private final static M12VisibleTrainingPlan<Boolean, Boolean, Boolean> getTrainingPlan(
			Cursable<Bi<Tuple<Boolean>, Tuple<Boolean>>> data ) {

		M12VisibleTrainingPlan<Boolean, Boolean, Boolean> plan = new M12VisibleTrainingPlan<>( );

		plan 
		.setMinMaxAbove( 1, 1 )
		.setMinMaxBelow( 0, 2 )
		.setTupleAlignmentAlphabetBuilder(
				new TupleAlignmentAlphabetGreedyBuilder<Boolean, Boolean>( true ) )
		.setQualityChecker( null )
		.setPrinters(
				x -> x ? "1" : "0",
				x -> x ? "1" : "0" )
				.setTrainingData( data )
				.setAboveCoDec( x -> x ? "1" : "0", x -> x.startsWith( "1" ) )
				.setBelowCoDec( x -> x ? "1" : "0", x -> x.startsWith( "1" ) )
				.setProjector( x->x );
		return plan;
	}

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainer( ) {
		M12Fool fool = M12Fool.nu( training_cache_path );
		Path test_crf_path = Paths.get( "test_m12" );
		fool.delete( test_crf_path );
		
		return new Trainer<Tuple<Boolean>, Tuple<Boolean>>( ) {
			
			@Override
			public Function<Tuple<Boolean>, Tuple<Boolean>> train(
					ProgressSpawner progress_spawner,
					Cursable<Bi<Tuple<Boolean>, Tuple<Boolean>>> data ) {
				M12VisibleTrainingPlan<Boolean, Boolean, Boolean> plan = getTrainingPlan( data );
				Path created = fool.create( test_crf_path, progress_spawner, plan );
				M12<Boolean, Boolean, Boolean> open = fool.open( created, plan );
				return open.asMapleClassic( );
			}
		};
	}

	@Test
	public void testM12Identity( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.identity )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12Reverse( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.reverse )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12ConstantTrue( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.constant_true )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12ConstantTrueFalse( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.constant_true_false )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	/**
	 * This can be modeled perfectly using a 1:1 HMM with four states.
	 */
	@Test
	public void testM12Zebra( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.zebra )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.01 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	/**
	 * 
	 * This can be modeled perfectly using a 1:1 HMM with four states.
	 * 
	 * 
	 * gold = x0110111 input = 01101110 output = x01101110
	 */
	@Test
	public void testM12DelayByOne( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.delay_by_one )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.01 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12LycantropeDay2( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.lycantrope2 )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.01 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12LycantropeDay3( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.lycantrope3 )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.01 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12Absorb( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.absorb )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.01 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12Duplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.duplicate )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12AbsorbAndDuplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.absorb_and_duplicate )
				.evaluator( evaluator, evaluator_label )
				.handicap( handicap )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

}
