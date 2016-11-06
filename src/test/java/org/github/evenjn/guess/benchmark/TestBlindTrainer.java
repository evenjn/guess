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

public class TestBlindTrainer {

	/** EVALUATOR */
	private final static String evaluator_label = "equals";

	private final static TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>> evaluator =
			new TupleEqualsEvaluator<Boolean, Tuple<Boolean>, Tuple<Boolean>>( );

	/** TRAINER */
	private final static String trainer_label = "blind";

	private final static Trainer<Tuple<Boolean>, Tuple<Boolean>> trainer( ) {
		BlindMapleTrainer<Boolean, Boolean> trainer = new BlindMapleTrainer<>(
				TestUtils.boolean_alphabet.asCursable( ), TestUtils.boolean_alphabet.asCursable( ) );
		return trainer;
	}

	@Test
	public void testBlindIdentity( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.identity )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindReverse( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.reverse )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindConstantTrue( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.constant_true )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 1.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindConstantTrueFalse( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.constant_true_false )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindZebra( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.zebra )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindDelayByOne( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.delay_by_one )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindLycantropeDay2( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.lycantrope2 )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindLycantropeDay3( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.lycantrope3 )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindAbsorb( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.absorb )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindDuplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.duplicate )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

	@Test
	public void testBlindAbsorbAndDuplicate( ) {
		/** RUN! */
		BenchmarkTrial
				.builder( trainer( ), trainer_label )
				.problem( TestUtils.absorb_and_duplicate )
				.evaluator( evaluator, evaluator_label )
				.progressFactory( x -> null ).build( ).run( );
		/** CHECK */
		org.junit.Assert
				.assertTrue( 0.0 <= evaluator.one_minus_relative_distance( ) );
		org.junit.Assert
				.assertTrue( 1.0 >= evaluator.one_minus_relative_distance( ) );
	}

}
