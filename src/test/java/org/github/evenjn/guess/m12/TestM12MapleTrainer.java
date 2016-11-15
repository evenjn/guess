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
import org.github.evenjn.guess.benchmark.BenchmarkTrial;
import org.github.evenjn.guess.benchmark.TestUtils;
import org.github.evenjn.guess.benchmark.TupleEqualsEvaluator;
import org.github.evenjn.guess.m12.M12MapleTrainer;
import org.github.evenjn.guess.m12.core.M12CoreTrainerBlueprint;
import org.github.evenjn.yarn.Tuple;
import org.junit.Test;

public class TestM12MapleTrainer {

	/** EVALUATOR */
	private final static String evaluator_label = "equals";

	private final static TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>> evaluator =
			new TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>>( );

	/** TRAINER */
	private final static String trainer_label = "m12";

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainer( ) {
		
		M12CoreTrainerBlueprint m12TrainerBlueprint = new M12CoreTrainerBlueprint( )
				.seed( 1 )
				.states( 3 )
				.trainingTime( 3,  100 );
		
		M12MapleTrainer<Boolean, Boolean> trainer = new M12MapleTrainer<>(
				new TupleAlignmentGraphDataManager<Boolean, Boolean>( 0, 2 ),
				m12TrainerBlueprint.create( ));
		
		return trainer;
	}

	@Test
	public void testM12Identity( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.identity )
				.evaluator( evaluator, evaluator_label )
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
				.problem( TestUtils.reverse )
				.evaluator( evaluator, evaluator_label )
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
				.problem( TestUtils.constant_true )
				.evaluator( evaluator, evaluator_label )
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
				.problem( TestUtils.constant_true_false )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	/**
	 * m12 fails badly on zebra.
	 */
	@Test
	public void testM12Zebra( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer(), trainer_label )
				.problem( TestUtils.zebra )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.4 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	/**
	 * 
	 * This can be modeled perfectly using a 1:1 HMM with four states.
	 * 
	 * 1:0..2 HMM may fail because the initial delay can be simulated by emitting
	 * two symbols at the beginning, and implementing identity. For example:
	 * 
	 * gold   = x0110111
	 * input  =  01101110
	 * output = x01101110
	 */
	@Test
	public void testM12DelayByOne( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.delay_by_one )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.97 <= evaluator.one_minus_relative_distance( ) );
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
				.build( ).run( null );
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
				.build( ).run( null );
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
				.build( ).run( null );
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
				.problem( TestUtils.absorb_and_duplicate )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

}
