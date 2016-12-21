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

import java.util.function.Consumer;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.numeric.NumericUtils;
import org.github.evenjn.numeric.NumericUtils.Summation;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.numeric.SixCharFormat;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Tuple;

public class M12LibraQualityChecker<I, O> implements
		M12FileTrainer.QualityChecker<I, O> {

	private Double previous_training = null;

	private Double previous_test = null;

	private int epoch = 0;

	private Cursable<Bi<Tuple<I>, Tuple<O>>> training_data;

	private Cursable<Bi<Tuple<I>, Tuple<O>>> test_data;

	public M12LibraQualityChecker(
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data,
			Cursable<Bi<Tuple<I>, Tuple<O>>> test_data) {
		this.training_data = training_data;
		this.test_data = test_data;
	}

	private double do_check(
			M12Libra<I, O> m12Libra,
			Consumer<String> logger,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data,
			Double previous ) {
		int total = 0;
		int not_aligneable = 0;
		Summation summation = NumericUtils.summation( 10000,
				x -> NumericLogarithm.elnsum( KnittingCursable.wrap( x ) ) );
		try ( AutoHook hook2 = new BasicAutoHook( ) ) {

			for ( Bi<Tuple<I>, Tuple<O>> g : KnittingCursable
					.wrap( data ).pull( hook2 ).once( ) ) {
				try {
					double p = m12Libra.weigh( g );
					summation.add( p );
					total = total + 1;
				}
				catch ( NotAlignableException e ) {
					not_aligneable++;
				}
			}

			double current_probability =
					NumericLogarithm.eexp( summation.getSum( ) ) / ( 1.0 * total );
			if ( previous != null ) {
				double change =
						current_probability / previous;
				logger.accept( "  Average probability: "
						+ SixCharFormat.nu( false ).apply( current_probability ) );
				logger.accept( "  new/old: "
						+ SixCharFormat.nu( false ).apply( change ) );
			}
			else {
				logger
						.accept( "  Not aligneable: " + not_aligneable + " out of " + total
								+ " (" + PercentPrinter.printRatioAsPercent( 4, not_aligneable,
										total ) + ")" );
				logger.accept( "  Average probability: "
						+ SixCharFormat.nu( false ).apply( current_probability ) );
			}
			return current_probability;

		}
	}
	
	private static final String decorator_line =
			">---------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "---------<";

	public boolean check(
			Consumer<String> logger,
			TupleAlignmentAlphabet<I, O> alphabet,
			Markov core ) {
		M12Libra<I, O> m12Libra =
				new M12Libra<I, O>( alphabet, core );

		logger.accept( decorator_line );
		if ( training_data != null ) {
			logger.accept( "Libra check on training data" );
			previous_training =
					do_check( m12Libra, logger, training_data, previous_training );
		}
		logger.accept( decorator_line );
		if ( test_data != null ) {
			logger.accept( "Libra check on test data" );
			previous_test = do_check( m12Libra, logger, test_data, previous_test );
		}
		logger.accept( decorator_line );
		epoch++;
		return false;
	}
}
