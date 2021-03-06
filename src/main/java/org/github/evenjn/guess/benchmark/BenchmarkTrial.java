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
import org.github.evenjn.guess.TrainingData;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Progress;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;

public class BenchmarkTrial<I, O> {

	private Trainer<I, O> trainer;

	private BenchmarkProblem<I, O> problem;

	private Evaluator<I, O> evaluator;

	private String trainer_label;

	private String evaluator_label;

	public BenchmarkHandicap handicap = new BenchmarkHandicap( );

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

		public Builder<I, O> handicap( BenchmarkHandicap handicap ) {
			built.handicap = handicap;
			return this;
		}
	}

	private BenchmarkTrial() {
	}

	public Evaluator<I, O> run( Progress progress ) {
		System.out.println( "\n\n\n" );
		System.out.println( "Problem: " + problem.label( ) );
		System.out.println( "Trainer: " + trainer_label );
		System.out.println( "Evaluator: " + evaluator_label );
		int local_limit = handicap.size_of_traning_data;
		try ( BasicRook rook = new BasicRook( ) ) {

			KnittingCursable<BenchmarkDatum<I, O>> training_data =
					KnittingCursable.wrap( problem.data( ) ).head( 0, local_limit );

			Function<I, O> guesser = null;

			guesser = trainer.train(
					progress,

					new TrainingData<BenchmarkDatum<I, O>, I, O>( ) {

						@Override
						public Cursable<BenchmarkDatum<I, O>> getData( ) {
							return training_data;
						}

						@Override
						public Function<BenchmarkDatum<I, O>, I> getInput( ) {
							return x -> x.getInput( );
						}

						@Override
						public Function<BenchmarkDatum<I, O>, O> getOutput( ) {
							return ( x -> handicap.use_noise
									? x.getBadTeacherGold( ) : x.getGoodTeacherGold( ) );
						}

					} );
			evaluator.reset( );
			evaluator.evaluate(
					guesser,
					training_data,
					x -> x.getInput( ),
					x -> x.getGoodTeacherGold( ) );
			System.out.println( evaluator.printEvaluation( ) );

			StringBuilder sb = new StringBuilder( );

			BenchmarkDatum<I, O> next = null;
			O predicted = null;
			Cursor<BenchmarkDatum<I, O>> search = training_data.pull( rook );
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
			if ( handicap.use_noise ) {
				sb.append( "\nSample noisy training output: " );
				sb.append( problem.outputPrinter( ).apply( next.bad_teacher ) );
			}
			sb.append( "\nSample gold output    : " );
			sb.append( problem.outputPrinter( ).apply( next.good_teacher ) );
			sb.append( "\nPredicted             : " );
			sb.append( problem.outputPrinter( ).apply( predicted ) );
			System.out.println( sb.toString( ) );
		}
		return evaluator;
	}
}
