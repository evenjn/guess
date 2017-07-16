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
package org.github.evenjn.guess.benchmark;

import org.github.evenjn.guess.Trainer;
import org.github.evenjn.yarn.Tuple;
import org.junit.Test;

public class TestNoiseTrainer {

	/** EVALUATOR */
	private final static String evaluator_label = "equals";

	private final static TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>> evaluator =
			new TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>>( );

	private final static int limit = 20;

	/** TRAINER */
	private final static String trainer_label = "noise";

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainer( ) {
		NoiseMapleTrainer<Boolean, Boolean> trainer = new NoiseMapleTrainer<>(
				TestUtils.boolean_alphabet.asKnittingCursable( ),
				TestUtils.boolean_alphabet.asKnittingCursable( ) );
		return trainer;
	}

	@Test
	public void testNoiseIdentity( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.identity )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseReverse( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.reverse )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseConstantTrue( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.constant_true )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseConstantTrueFalse( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.constant_true_false )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseZebra( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.zebra )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseDelayByOne( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.delay_by_one )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseLycantropeDay2( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.lycantrope2 )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseLycantropeDay3( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.lycantrope3 )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseAbsorb( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.absorb )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseDuplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.duplicate )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testNoiseAbsorbAndDuplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.absorb_and_duplicate )
				.evaluator( evaluator, evaluator_label )
				.build( ).run( limit, null );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

}
