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

import org.github.evenjn.align.Tael;
import org.github.evenjn.knit.BasicAutoRook;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.knit.TupleValue;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.yarn.AutoRook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.EndOfCursorException;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.RookConsumer;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetGreedyBuilder<SymbolAbove, SymbolBelow>
		implements
		TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private Function<SymbolAbove, String> a_printer = null;

	private Function<SymbolBelow, String> b_printer = null;

	private int min_above;

	private int max_above;

	private int min_below;

	private int max_below;

	private boolean shrink_alphabet = false;

	private RookConsumer<String> logger;

	public TupleAlignmentAlphabetGreedyBuilder(boolean shrink_alphabet) {
		this.shrink_alphabet = shrink_alphabet;
	}

	@Override
	public void setMinMax(
			int min_above, int max_above,
			int min_below, int max_below ) {
		this.min_above = min_above;
		this.max_above = max_above;
		this.min_below = min_below;
		this.max_below = max_below;
	}

	public void setPrinters(
			RookConsumer<String> logger,
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

	private Set<Tael<SymbolAbove, SymbolBelow>>
			shrinkAlphabet(
					ProgressSpawner progress_spawner,
					Consumer<String> logger,
					TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis,
					Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
					Set<Tael<SymbolAbove, SymbolBelow>> initial_alphabet ) {

		HashSet<Tael<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );
		for ( Tael<SymbolAbove, SymbolBelow> s : initial_alphabet ) {
			alphabet.add( s );
		}

		if ( logger != null ) {
			logger.accept( "" );
			logger.accept( " Shrinking the alphabet" );
		}
		try ( AutoRook rook = new BasicAutoRook( ) ) {

			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::shrinkAlphabet" )
					.target( initial_alphabet.size( ) );

			
			for ( Tael<SymbolAbove, SymbolBelow> candidate :
				analysis.getPairs( ).reverse( ).asIterable( ) ) {
				if (! alphabet.contains( candidate ) ) {
					continue;
				}
				alphabet.remove( candidate );
				int not_aligneable = TupleAlignmentAlphabetBuilderTools.computeCoverage(
						progress_spawner,
						null,
						min_above, max_above,
						min_below, max_below,
						data, alphabet::contains,
						analysis.getTotal( ), analysis.getTotalAligneable( ) );
				spawn.step( 1 );

				boolean remove_this = false;
				if ( ( 1.0 * not_aligneable )
						/ ( 1.0 * analysis.getTotalAligneable( ) ) < threshold ) {
					/**
					 * removing this pair is harmless
					 */
					remove_this = true;
					
				}
					if ( a_printer != null && b_printer != null ) {
						int total_candidates = analysis.getTotalNumberOfCandidatePairs( );
						StringBuilder sb = new StringBuilder( );
						if (remove_this)
						sb.append( "Removed element: " );
						else
							sb.append( "Will not remove element: " );
						sb.append( TupleAlignmentAlphabetBuilderTools
								.tuple_printer( a_printer, candidate.getAbove( ) ) );
						sb.append( " >-> " );
						sb.append( TupleAlignmentAlphabetBuilderTools
								.tuple_printer( b_printer, candidate.getBelow( ) ) );
						sb.append( "   Data coverage: " );
						sb.append( PercentPrinter.printRatioAsPercent( 4,
								analysis.getTotal( ) - not_aligneable, analysis.getTotal( ) ) );
						sb.append( " Using " );
						sb.append( alphabet.size( ) );
						sb.append( " a/b pairs (" );
						sb.append( PercentPrinter.printRatioAsPercent( 4,
								alphabet.size( ), total_candidates ) );
						sb.append( ")." );
						logger.accept( sb.toString( ) );
					}
				if ( remove_this ) {
					continue;
				}
				alphabet.add( candidate );
			}
		}
		return alphabet;
	}

	private double threshold = 0.01;

	private Set<Tael<SymbolAbove, SymbolBelow>>
			growAlphabetQuickly(
					ProgressSpawner progress_spawner,
					Consumer<String> open_logger,
					TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis,
					Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data ) {
		HashSet<Tael<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );

		if ( open_logger != null ) {
			open_logger.accept( "" );
			open_logger.accept(
					" Growing alphabet greedily, picking from the following list:" );
			for ( Tael<SymbolAbove, SymbolBelow> best_candidate : KnittingCursor
					.wrap( // fd_global.dataSorted( true )
							greedy( analysis ) )
					// .map( x -> x.front( ) )
					.head( 0, 42 )
					.once( ) ) {
				StringBuilder sb = new StringBuilder( );
				sb.append( TupleAlignmentAlphabetBuilderTools
						.tuple_printer( a_printer, best_candidate.getAbove( ) ) );
				sb.append( " >-> " );
				sb.append( TupleAlignmentAlphabetBuilderTools
						.tuple_printer( b_printer, best_candidate.getBelow( ) ) );
				open_logger.accept( sb.toString( ) );

			}
			if ( analysis.getTotalNumberOfCandidatePairs( ) > 42 ) {
				open_logger.accept( " ... and many more ("
						+ analysis.complete_alphabet.size( ) + " in total)!" );
			}
		}

		try ( AutoRook rook = new BasicAutoRook( ) ) {
			int total_candidates = analysis.getTotalNumberOfCandidatePairs( );
			int batch_size = 0;

			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::growAlphabetQuickly" )
					.target( analysis.getTotalAligneable( ) );
			int prev = analysis.getTotalAligneable( );

			for ( Tael<SymbolAbove, SymbolBelow> best_candidate : KnittingCursor
					.wrap( // fd_global.dataSorted( true )
							greedy( analysis ) )
					// .map( x -> x.front( ) )
					.once( ) ) {
				alphabet.add( best_candidate );
				batch_size++;

				int not_aligneable = prev;
				if ( batch_size == 1 + ( total_candidates / 100 ) ) {
					not_aligneable = TupleAlignmentAlphabetBuilderTools.computeCoverage(
							spawn,
							null,
							min_above, max_above,
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
					sb.append( TupleAlignmentAlphabetBuilderTools
							.tuple_printer( a_printer, best_candidate.getAbove( ) ) );
					sb.append( " >-> " );
					sb.append( TupleAlignmentAlphabetBuilderTools
							.tuple_printer( b_printer, best_candidate.getBelow( ) ) );
					sb.append( "   Data coverage: " );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							analysis.getTotal( ) - not_aligneable, analysis.getTotal( ) ) );
					sb.append( " Using " );
					sb.append( alphabet.size( ) );
					sb.append( " a/b pairs (" );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							alphabet.size( ), total_candidates ) );
					sb.append( ")." );
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
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
				new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
		try ( AutoRook rook = new BasicAutoRook( ) ) {
			Consumer<String> open_logger = logger.get( rook );
			TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis =
					new TupleAlignmentAlphabetAnalysis<>( 
							min_above, max_above, min_below, max_below );
			analysis.computeCompleteAlphabet( progress_spawner, data );
			analysis.print( open_logger, a_printer, b_printer );

			Set<Tael<SymbolAbove, SymbolBelow>> alphabet =
					growAlphabetQuickly( progress_spawner, open_logger, analysis, data );
			if ( shrink_alphabet ) {
				alphabet = shrinkAlphabet( progress_spawner, open_logger, analysis, data, alphabet );
			}
			for ( Tael<SymbolAbove, SymbolBelow> x : alphabet ) {
				result.add( x );
			}
		}
		return result;
	}

	public Cursor<Tael<SymbolAbove, SymbolBelow>> greedy(
			TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> analysis ) {
		int total_symbols = analysis.getTotalNumberOfCandidatePairs( );
		KnittingTuple<TupleValue<SymbolAbove>> symbols = analysis.getSymbolsAbove( );
		return new Cursor<Tael<SymbolAbove, SymbolBelow>>( ) {

			int symbol = 0;

			int size = min_below;

			int rank = 0;
			
			int retrieved = 0;
			
			@Override
			public Tael<SymbolAbove, SymbolBelow> next( )
					throws EndOfCursorException {
				if (retrieved >= total_symbols) {
					throw EndOfCursorException.neo();
				}
				Tael<SymbolAbove, SymbolBelow> result = null;
				for ( ; result == null; ) {
					
					if ( size > max_below ) {
						rank++;
						size = min_below;
					}

					TupleValue<SymbolAbove> symbolAbove = symbols.get( symbol );
					KnittingTuple<TupleValue<SymbolBelow>> symbolsBelow =
							analysis.getSymbolsBelow( symbolAbove, size );
					if ( symbolsBelow.size( ) > rank ) {
						if (symbolAbove.size( ) != 1) {
							throw new IllegalStateException( );
						}
						result = new Tael<>( KnittingTuple.wrap( symbolAbove ).asTupleValue( ),
								KnittingTuple.wrap( symbolsBelow.get( rank ) ).asTupleValue( ) );
					}
					if ( symbol + 1 == symbols.size( ) ) {
						symbol = 0;
						size++;
					}
					else {
						symbol++;
					}
				}
				retrieved++;
				return result;
			}
		};
	}
}
