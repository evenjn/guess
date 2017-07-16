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

import java.util.function.Function;

import org.github.evenjn.guess.Trainer;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Progress;

public class BenchmarkTrial<I, O> {

	private Trainer<I, O> trainer;

	private BenchmarkProblem<I, O> problem;

	private Evaluator<I, O> evaluator;

	private String trainer_label;

	private String evaluator_label;

	public static <I, O> Builder<I, O> builder( Trainer<I, O> trainer,
			String label ) {
		return new Builder<I, O>( trainer, label );
	}

	public static class Builder<I, O> {

		BenchmarkTrial<I, O> built = new BenchmarkTrial<>( );

		private Builder(Trainer<I, O> trainer, String label) {
			built.trainer = trainer;
			built.trainer_label = label;
		}

		public Builder<I, O> setTrainerLabel( String label ) {
			return this;
		}

		public Builder<I, O> problem(
				BenchmarkProblem<I, O> problem ) {
			built.problem = problem;
			return this;
		}

		public Builder<I, O> evaluator(
				Evaluator<I, O> evaluator,
				String label ) {
			built.evaluator = evaluator;
			built.evaluator_label = label;
			return this;
		}

		public BenchmarkTrial<?, ?> build( ) {
			return built;
		}
	}

	private BenchmarkTrial() {
	}

	public Evaluator<I, O> run( int limit, Progress progress ) {
		System.out.println( "\n\n\n" );
		System.out.println( "Problem: " + problem.label( ) );
		System.out.println( "Trainer: " + trainer_label );
		System.out.println( "Evaluator: " + evaluator_label );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			KnittingCursable<BenchmarkDatum<I, O>> training_data =
					KnittingCursable.wrap( problem.data( ) ).head( 0, limit );
			Function<I, O> guesser = trainer.train(
					progress,
					training_data.map( x -> x.asBadTeacherWouldTell( ) )
					);
			evaluator.reset( );
			evaluator.evaluate( guesser, training_data.map( x->x.asGoodTeacherWouldTell( ) ) );
			System.out.println( evaluator.printEvaluation( ) );

			StringBuilder sb = new StringBuilder( );
			
			
			BenchmarkDatum<I, O> next = null;
			O predicted = null;
			Cursor<BenchmarkDatum<I, O>> search = training_data.pull( hook );
			try {
				for ( ;; ) {
					next = search.next( );
					predicted = guesser.apply( next.observed );
					break;
				}
			}
			catch ( EndOfCursorException e ) {
			}
			

			sb.append( "\nSample input          : " );
			sb.append( problem.inputPrinter( ).apply( next.observed ) );
			sb.append( "\nSample training output: " );
			sb.append( problem.outputPrinter( ).apply( next.bad_teacher ) );
			sb.append( "\nSample gold output    : " );
			sb.append( problem.outputPrinter( ).apply( next.good_teacher ) );
			sb.append( "\nPredicted             : " );
			sb.append( problem.outputPrinter( ).apply( predicted ) );
			System.out.println( sb.toString( ) );
		}
		return evaluator;
	}
}
