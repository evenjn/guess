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
package org.github.evenjn.align.alphabet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetGreedyBuilder<SymbolAbove, SymbolBelow>
		implements
		TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private Function<SymbolAbove, String> a_printer = null;

	private Function<SymbolBelow, String> b_printer = null;

	private int min_below;

	private int max_below;

	private boolean shrink_alphabet = false;

	private Function<Hook, Consumer<String>> logger;

	public TupleAlignmentAlphabetGreedyBuilder(boolean shrink_alphabet) {
		this.shrink_alphabet = shrink_alphabet;
	}

	@Override
	public void setMinMax( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
	}

	public void setPrinters(
			Function<Hook, Consumer<String>> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer ) {
		this.logger = logger;
		if ( a_printer != null ) {
			this.a_printer = a_printer;
		}
		if ( b_printer != null ) {
			this.b_printer = b_printer;
		}
	}

	private Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>
			shrinkAlphabet(
					ProgressSpawner progress_spawner,
					Consumer<String> logger,
					TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis,
					Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
					Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> initial_alphabet ) {

		HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );
		for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> s : initial_alphabet ) {
			alphabet.add( s );
		}

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::shrinkAlphabet" )
					.target( initial_alphabet.size( ) );

			for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> candidate : KnittingCursor
					.wrap( initial_alphabet.iterator( ) )
					.once( ) ) {
				alphabet.remove( candidate );
				int not_aligneable = TupleAlignmentAlphabetBuilderTools.computeCoverage(
						progress_spawner,
						logger,
						min_below, max_below,
						data, alphabet::contains,
						analysis.getTotal( ), analysis.getTotalAligneable( ) );
				spawn.step( 1 );

				if ( ( 1.0 * not_aligneable )
						/ ( 1.0 * analysis.getTotalAligneable( ) ) < threshold ) {
					/**
					 * removing this pair is harmless
					 */
					if ( a_printer != null && b_printer != null ) {
						StringBuilder sb = new StringBuilder( );
						sb.append( "Removed element: " );
						sb.append( a_printer.apply( candidate.above ) );
						sb.append( " >-> " );
						sb.append( TupleAlignmentAlphabetBuilderTools
								.tuple_printer( b_printer, candidate.below ) );
						sb.append( " Using " );
						sb.append( alphabet.size( ) );
						sb.append( " alphabet symbols." );
						logger.accept( sb.toString( ) );
					}
					continue;
				}
				alphabet.add( candidate );
			}
		}
		return alphabet;
	}

	private double threshold = 0.01;

	private Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>
			growAlphabetQuickly(
					ProgressSpawner progress_spawner,
					Consumer<String> open_logger,
					TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis,
					Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data ) {
		HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			int total_candidates = analysis.getTotalNumberOfCandidatePairs( );
			int batch_size = 0;

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::growAlphabetQuickly" )
					.target( analysis.getTotalAligneable( ) );
			int prev = analysis.getTotalAligneable( );

			for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> best_candidate : KnittingCursor
					.wrap( // fd_global.dataSorted( true )
							analysis.greedy( ) )
					// .map( x -> x.front( ) )
					.once( ) ) {
				alphabet.add( best_candidate );
				batch_size++;

				int not_aligneable = prev;
				if ( batch_size == 1 + ( total_candidates / 100 ) ) {
					not_aligneable = TupleAlignmentAlphabetBuilderTools.computeCoverage(
							spawn,
							open_logger,
							min_below, max_below,
							data, alphabet::contains,
							analysis.getTotal( ), analysis.getTotalAligneable( ) );
					batch_size = 0;
				}

				// not a good idea because certain frequent elements may
				// need a less-frequent element to become useful.
				// if (prev != total_aligneable && not_aligneable == prev ) {
				// alphabet.remove( best_candidate );
				// continue;
				// }

				spawn.step( prev - not_aligneable );
				prev = not_aligneable;
				if ( a_printer != null && b_printer != null ) {
					StringBuilder sb = new StringBuilder( );
					sb.append( "Added new element: " );
					sb.append( a_printer.apply( best_candidate.above ) );
					sb.append( " >-> " );
					sb.append( TupleAlignmentAlphabetBuilderTools
							.tuple_printer( b_printer, best_candidate.below ) );
					sb.append( "   Data coverage: " );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							analysis.getTotal( ) - not_aligneable, analysis.getTotal( ) ) );
					sb.append( " Using " );
					sb.append( alphabet.size( ) );
					sb.append( " (" );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							alphabet.size( ), total_candidates ) );
					sb.append( " of the alphabet)." );
					open_logger.accept(  sb.toString( ) );
				}
				if ( ( 1.0 * not_aligneable )
						/ ( 1.0 * analysis.getTotalAligneable( ) ) < threshold ) {
					break;
				}
			}
		}
		return alphabet;
	}

	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
				new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Consumer<String> open_logger = logger.apply( hook );
			TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis =
					new TupleAlignmentAlphabetAnalysis<>( min_below, max_below );
			analysis.computeCompleteAlphabet( progress_spawner, data );
			analysis.print( open_logger, a_printer, b_printer );

			Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
					growAlphabetQuickly( progress_spawner, open_logger, analysis, data );
			if ( shrink_alphabet ) {
				alphabet = shrinkAlphabet( progress_spawner, open_logger, analysis, data, alphabet );
			}
			for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> x : alphabet ) {
				result.add( x );
			}
		}
		return result;
	}

}
