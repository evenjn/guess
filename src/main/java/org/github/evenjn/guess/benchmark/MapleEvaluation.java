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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.Tael;
import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.knit.BiValue;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.numeric.NumericUtils;
import org.github.evenjn.numeric.NumericUtils.Summation;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.numeric.SixCharFormat;

public class MapleEvaluation<I, O> {

	private int total_io_pairs;

	private int total_distance;

	private int total_input_elements;

	private int total_gold_elements;

	private int total_longest_elements;

	private int total_guessed_elements;

	private Summation er_summation =
			NumericUtils.summation( 10000, NumericUtils::sumDoubles );

	private Summation input_accuracy_summation =
			NumericUtils.summation( 10000, NumericUtils::sumDoubles );

	private Summation gold_accuracy_summation =
			NumericUtils.summation( 10000, NumericUtils::sumDoubles );

	private Summation guessed_accuracy_summation =
			NumericUtils.summation( 10000, NumericUtils::sumDoubles );

	private Summation longest_accuracy_summation =
			NumericUtils.summation( 10000, NumericUtils::sumDoubles );

	private int positive;

	private Function<I, String> a_printer;

	private TupleAligner<I, O> aligner;

	public MapleEvaluation(
			TupleAligner<I, O> aligner,
			Function<I, String> a_printer,
			Function<O, String> b_printer) {
		this.aligner = aligner;
		this.a_printer = a_printer;
	}

	private FrequencyDistribution<Tuple<I>> errors_above_fd =
			new FrequencyDistribution<>( );

	private Map<Tuple<I>, Integer> errors_per_symbol_above = new HashMap<>( );

	private Map<Tuple<I>, Integer> occurrences_per_symbol_above =
			new HashMap<>( );

	public void record(
			Consumer<String> logger,
			Tuple<I> input,
			Tuple<O> gold,
			Tuple<O> guess ) {
		KnittingTuple<O> kguess = KnittingTuple.wrap( guess );
		KnittingTuple<O> kgold = KnittingTuple.wrap( gold );
		KnittingTuple<I> kinput = KnittingTuple.wrap( input );


		int distance = kgold.distance( kguess );
		total_distance += distance;
		int size_in = kinput.size( );
		int size_go = kgold.size( );
		int size_gu = kguess.size( );
		/**
		 * Taking the largest size guarantees that the relative distance is always
		 * in the interval [0,1]
		 */
		total_input_elements += size_in;
		total_gold_elements += size_go;
		total_guessed_elements += size_gu;
		total_longest_elements += ( size_go > size_gu ) ? size_go : size_gu;
		if ( distance == 0 ) {
			positive++;
		}
		total_io_pairs++;

		er_summation.add( ( 1.0 * distance ) );

		input_accuracy_summation.add( ( 1.0 * distance )
				/ ( 1.0 * size_in ) );
		gold_accuracy_summation.add( ( 1.0 * distance )
				/ ( 1.0 * size_go ) );
		guessed_accuracy_summation.add( ( 1.0 * distance )
				/ ( 1.0 * size_gu ) );
		longest_accuracy_summation.add( ( 1.0 * distance )
				/ ( 1.0 * ( ( size_go > size_gu ) ? size_go : size_gu ) ) );
		
		if ( aligner != null ) {

			Tuple<BiValue<Integer, Integer>> aligned_gold = aligner.align( kinput, kgold );

			Tuple<BiValue<Integer, Integer>> aligned_guess =
					aligner.align( kinput, kguess );

			Tuple<Tael<I, O>> tt_gold = Tael.tael( kinput, kgold, aligned_gold );
			Tuple<Tael<I, O>> tt_guess = Tael.tael( kinput, kguess, aligned_guess );
			/**
			 * we should analyze the diff of the sequence of alignment pairs. T/t A/ae
			 * X/ks I/i T/t A/a X/ks I/i
			 */

			for ( Bi<Tael<I, O>, Tael<I, O>> diff : KnittingTuple.wrap( tt_gold )
					.diff( tt_guess ) ) {
				Tael<I, O> front = diff.front( );
				Tael<I, O> back = diff.back( );

				if ( front == null ) {
					// this element contains information about what was incorrectly
					// guessed.
					// we will use it to print a confusion matrix.
					continue;
				}

				/*
				 * update the symbol above error rate
				 */
				Integer oc = occurrences_per_symbol_above.get( front.getAbove( ) );
				if ( oc == null ) {
					occurrences_per_symbol_above.put( front.getAbove( ), 0 );
					oc = 0;
				}
				occurrences_per_symbol_above.put( front.getAbove( ), oc + 1 );

				Integer er = errors_per_symbol_above.get( front.getAbove( ) );
				if ( er == null ) {
					errors_per_symbol_above.put( front.getAbove( ), 0 );
					er = 0;
				}

				if ( back == null ) {
					errors_per_symbol_above.put( front.getAbove( ), er + 1 );
					/*
					 * update the distribution of errors
					 */
					errors_above_fd.accept( front.getAbove( ) );
					continue;
				}

			}
		}

	}

	private String printTupleAbove( Tuple<I> above ) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "[" );
		for ( I a : KnittingTuple.wrap( above ).asIterable( ) ) {
			sb.append( " " ).append( a_printer.apply( a ) );
		}
		sb.append( " ]" );
		return sb.toString( );
	}

	public Iterable<Tuple<I>> aboveSortedByMostErrors( Consumer<String> out ) {
		LinkedList<Tuple<I>> sorted = new LinkedList<>( );
		for ( Tuple<I> sa : occurrences_per_symbol_above.keySet( ) ) {
			sorted.add( sa );
		}
		Comparator<Tuple<I>> comparator = new Comparator<Tuple<I>>( ) {

			@Override
			public int compare( Tuple<I> o1, Tuple<I> o2 ) {
				double er1 = errors_per_symbol_above.get( o1 );

				double er2 = errors_per_symbol_above.get( o2 );
				return Double.compare( er2, er1 );
			}
		};
		Collections.sort( sorted, comparator );
		return sorted;
	}

	private static final int limit_print = 10;

	public void printErrorsAboveFD( Consumer<String> out ) {
		String print = errors_above_fd.plot( )
				.setLabels( this::printTupleAbove )
				.setLimit( limit_print )
				.print( );
		out.accept( print );
	}

	public int getTotalDistance( ) {
		return total_distance;
	}

	public void print( Consumer<String> out ) {

		out.accept( "Total distance: " + total_distance );
		out.accept( "Total input elements: " + total_input_elements );
		out.accept( "Total gold elements: " + total_gold_elements );
		out.accept( "Total guessed elements: " + total_guessed_elements );
		out.accept( "Total longest elements: " + total_longest_elements );

		out.accept( "Average distance per pair: "
				+ SixCharFormat.nu( false )
						.apply( er_summation.getSum( ) / total_io_pairs ) );

		out.accept( "Average distance/input per pair: "
				+ SixCharFormat.nu( false )
						.apply( input_accuracy_summation.getSum( ) / total_io_pairs  )
				+ " " + PercentPrinter.printRatioAsPercent( 4, total_distance,
						total_input_elements ) );

		out.accept( "Average distance/gold per pair: "
				+ SixCharFormat.nu( false )
						.apply( gold_accuracy_summation.getSum( ) / total_io_pairs  )
				+ " " + PercentPrinter.printRatioAsPercent( 4, total_distance,
						total_gold_elements ) );

		out.accept( "Average distance/guessed per pair: "
				+ SixCharFormat.nu( false )
						.apply( guessed_accuracy_summation.getSum( ) / total_io_pairs  )
				+ " " + PercentPrinter.printRatioAsPercent( 4, total_distance,
						total_guessed_elements ) );

		out.accept( "Average distance/longest per pair: "
				+ SixCharFormat.nu( false )
						.apply( longest_accuracy_summation.getSum( ) / total_io_pairs  )
				+ " " + PercentPrinter.printRatioAsPercent( 4, total_distance,
						total_longest_elements ) );

		if ( a_printer != null ) {
			printErrorsAboveFD( out );
		}

	}
}
