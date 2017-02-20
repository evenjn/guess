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
import org.github.evenjn.guess.Trainer;
import org.github.evenjn.guess.benchmark.BenchmarkTrial;
import org.github.evenjn.guess.benchmark.TestUtils;
import org.github.evenjn.guess.benchmark.TupleEqualsEvaluator;
import org.github.evenjn.guess.m12.bw.M12BWFileTrainerBlueprint;
import org.github.evenjn.guess.m12.maple.M12MapleFileTrainer;
import org.github.evenjn.yarn.Tuple;
import org.junit.Test;

public class TestM12BWMapleTrainer {

	private static final FileFool training_cache_path = FileFool.nu(
			Paths.get( ".", "target", "training_cache" ));

	private static void removeModelFiles( ) {
		training_cache_path.create( training_cache_path.mold( training_cache_path.getRoot( ) ).asDirectory( ).eraseIfExists( ) );
	}

	private final static int limit = 20;

	/** EVALUATOR */
	private final static String evaluator_label = "equals";

	private final static TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>> evaluator =
			new TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>>( );

	/** TRAINER */
	private final static String trainer_label = "m12 baum-welch maple";

	private static final M12BWFileTrainerBlueprint<Boolean, Boolean> blueprint( ) {
		return new M12BWFileTrainerBlueprint<Boolean, Boolean>( )
				.seed( 43 )
				.states( 3 )
				.trainingTime( 1, 26 )
				.setMinMaxBelow( 0, 2 )
				.setInputCoDec( x -> x ? "1" : "0", x -> x.startsWith( "1" ) )
				.setOutputCoDec( x -> x ? "1" : "0", x -> x.startsWith( "1" ) )
				.setBuilder( new TupleAlignmentAlphabetGreedyBuilder<Boolean, Boolean>( true ) )
				.setPrinter(
						x -> x ? "1" : "0",
						x -> x ? "1" : "0" );
	}

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainer( ) {
		removeModelFiles( );
		M12BWFileTrainerBlueprint<Boolean, Boolean> blueprint = blueprint( );
		M12MapleFileTrainer<Boolean, Boolean> trainer =
				new M12MapleFileTrainer<>( blueprint, Object::equals );
		return ( p, d ) -> trainer.train( p, training_cache_path, d, null );
	}

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>>
			trainerDelayByOne( ) {
		removeModelFiles( );
		M12BWFileTrainerBlueprint<Boolean, Boolean> blueprint = blueprint( )
				.states( 4 )
				.trainingTime( 1, 100 );
		M12MapleFileTrainer<Boolean, Boolean> trainer =
				new M12MapleFileTrainer<>( blueprint, Object::equals );
		return ( p, d ) -> trainer.train( p, training_cache_path, d, null );
	}

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainerZebra( ) {
		removeModelFiles( );
		M12BWFileTrainerBlueprint<Boolean, Boolean> blueprint = blueprint( )
				.trainingTime( 1, 50 )
				.states( 4 )
				;
		M12MapleFileTrainer<Boolean, Boolean> trainer =
				new M12MapleFileTrainer<>( blueprint, Object::equals );
		return ( p, d ) -> trainer.train( p, training_cache_path, d, null );
	}

	@Test
	public void testM12Identity( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.identity )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
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
				.problem( TestUtils.reverse )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
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
				.problem( TestUtils.constant_true )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
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
				.problem( TestUtils.constant_true_false )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
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
				.builder( trainerZebra( ), trainer_label )
				.problem( TestUtils.zebra )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
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
				.builder( trainerDelayByOne( ), trainer_label )
				.problem( TestUtils.delay_by_one )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12LycantropeDay2( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.lycantrope2 )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12LycantropeDay3( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.lycantrope3 )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12Absorb( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.absorb )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testM12Duplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.duplicate )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
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
				.problem( TestUtils.absorb_and_duplicate )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

}
