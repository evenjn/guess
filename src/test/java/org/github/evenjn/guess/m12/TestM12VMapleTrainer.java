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

import java.nio.file.Paths;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetGreedyBuilder;
import org.github.evenjn.file.FileFool;
import org.github.evenjn.file.FileFoolWriter;
import org.github.evenjn.guess.Trainer;
import org.github.evenjn.guess.benchmark.BenchmarkTrial;
import org.github.evenjn.guess.benchmark.BenchmarkHandicap;
import org.github.evenjn.guess.benchmark.Benchmark;
import org.github.evenjn.guess.benchmark.TupleEqualsEvaluator;
import org.github.evenjn.guess.m12.maple.M12MapleFileTrainer;
import org.github.evenjn.guess.m12.v.M12VFileTrainerBlueprint;
import org.github.evenjn.yarn.Tuple;
import org.junit.Test;

public class TestM12VMapleTrainer {

	{
		FileFoolWriter w = FileFool.w( Paths.get( "." ).toAbsolutePath( ).resolve( "target" ) );
		w.create( w.mold( Paths.get( "training_cache" ) ).asDirectory( ).eraseIfExists( ) );
		training_cache_path = FileFool.rw(
				Paths.get( "." ).toAbsolutePath( ).resolve( "target" )
						.resolve( "training_cache" ) );
	}
	
	private static FileFool training_cache_path;

	private static void removeModelFiles( ) {
		training_cache_path.delete( training_cache_path.getRoot( ) );
	}

	private final static int limit = 20;

	/** EVALUATOR */
	private final static String evaluator_label = "equals";

	private final static TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>> evaluator =
			new TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>>( );

	/** TRAINER */
	private final static String trainer_label = "m12 V";

	private static final M12VFileTrainerBlueprint<Boolean, Boolean> blueprint( ) {
		return new M12VFileTrainerBlueprint<Boolean, Boolean>( )
				.setMinMaxBelow( 0, 2 )
				.setMinMaxAbove( 1, 1 )
				.setInputCoDec( x -> x ? "1" : "0", x -> x.startsWith( "1" ) )
				.setOutputCoDec( x -> x ? "1" : "0", x -> x.startsWith( "1" ) )
				.setTupleAlignmentAlphabetBuilder(
						new TupleAlignmentAlphabetGreedyBuilder<Boolean, Boolean>( true ) )
				.setPrinter(
						x -> x ? "1" : "0",
						x -> x ? "1" : "0" );
	}

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainer( ) {
		removeModelFiles( );
		M12VFileTrainerBlueprint<Boolean, Boolean> blueprint = blueprint( );
		M12MapleFileTrainer<Boolean, Boolean> trainer =
				new M12MapleFileTrainer<>( blueprint, Object::equals );
		return ( p, d ) -> trainer.train( p, training_cache_path, d, null );
	}

	@Test
	public void testM12Identity( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( Benchmark.identity )
				.evaluator( evaluator, evaluator_label )
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
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
				.handicap( new BenchmarkHandicap( true, limit ) )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

}
