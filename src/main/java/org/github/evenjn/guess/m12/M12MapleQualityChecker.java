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
import org.github.evenjn.guess.m12.core.M12Core;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.numeric.NumericUtils;
import org.github.evenjn.numeric.NumericUtils.Summation;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.numeric.SixCharFormat;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Tuple;

public class M12MapleQualityChecker<I, O> implements
		M12FileTrainer.QualityChecker<I, O> {

	private Double previous_training = null;
	private Double previous_test = null;

	private int epoch = 0;

	private Cursable<Bi<Tuple<I>, Tuple<O>>> training_data;

	private Cursable<Bi<Tuple<I>, Tuple<O>>> test_data;

	public M12MapleQualityChecker(
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data,
			Cursable<Bi<Tuple<I>, Tuple<O>>> test_data) {
		this.training_data = training_data;
		this.test_data = test_data;
	}
	
	private double do_check(
			M12Maple<I, O> m12Maple,
			Consumer<String> logger,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data,
			Double previous) {
		try ( AutoHook hook2 = new BasicAutoHook( ) ) {
			int total = 0;
			int zero_length = 0;
			Summation summation =
					NumericUtils.summation( 10000, NumericUtils::sumDoubles );

			for ( Bi<Tuple<I>, Tuple<O>> g : KnittingCursable
					.wrap( data ).pull( hook2 ).once( ) ) {
					Tuple<O> apply = m12Maple.apply( g.front( ) );
					if ( g.back( ).size( ) == 0) {
						zero_length++;
						continue;
					}
					int distance = KnittingTuple.wrap( apply ).distance( g.back( ) );
					double errors_per_target_symbol = (1.0 * distance) / (1.0 * g.back( ).size( ) );
					summation.add( errors_per_target_symbol );
					total = total + 1;
			}

			double current_average_error_per_target_symbol = summation.getSum( ) / ( 1.0 * total );
			if ( previous != null ) {
				double change =
						current_average_error_per_target_symbol / previous;
				logger.accept( "  Average distance/targetlength: "
						+ SixCharFormat.nu( false )
								.apply( current_average_error_per_target_symbol ) );
				logger.accept( "  new/old: "
						+ SixCharFormat.nu( false ).apply( change ) );
			}
			else {
				logger.accept(
						"  Zero-length: " + zero_length + " out of " + total
						+ " (" + PercentPrinter.printRatioAsPercent( 4, zero_length, total ) + ")" );
				
				logger.accept( "  Average distance/targetlength: "
						+ SixCharFormat.nu( false ).apply( current_average_error_per_target_symbol ) );
			}

			return current_average_error_per_target_symbol;
		}
	}

	private static final String decorator_line =
			">---------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "---------<";

	public boolean check(
			Consumer<String> logger,
			TupleAlignmentAlphabet<I, O> alphabet,
			M12Core core  ) {
		M12Maple<I, O> m12Maple =
				new M12Maple<I, O>( alphabet, core, false, null );
		logger.accept( decorator_line );
		if ( training_data != null ) {
			logger.accept( "Maple check on training data");
			previous_training =
					do_check( m12Maple, logger, training_data, previous_training );
		}
		logger.accept( decorator_line );
		if ( test_data != null ) {
			logger.accept( "Maple check on test data" );
			previous_test = do_check( m12Maple, logger, test_data, previous_test );
		}
		logger.accept( decorator_line );
		epoch++;
		return false;
	}
}
