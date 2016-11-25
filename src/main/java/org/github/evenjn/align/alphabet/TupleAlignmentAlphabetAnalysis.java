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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.numeric.FrequencyData;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> {

	private int min_below;

	private int max_below;

	public TupleAlignmentAlphabetAnalysis(int min_below, int max_below) {
		this.min_below = min_below;
		this.max_below = max_below;
	}


	private FrequencyDistribution<SymbolAbove> fd_base =
			new FrequencyDistribution<>( );

	private FrequencyDistribution<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> fd_global =
			new FrequencyDistribution<>( );

	private HashMap<SymbolAbove, Vector<FrequencyDistribution<Tuple<SymbolBelow>>>> fds =
			new HashMap<>( );

	public void record(
			SymbolAbove above, Tuple<SymbolBelow> below ) {
		Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector =
				fds.get( above );

		if ( vector == null ) {
			vector = new Vector<>( );
			fds.put( above, vector );
		}
		int size = below.size( );
		for ( int i = 0; i < size; i++ ) {
			if ( vector.size( ) < i + 1 ) {
				vector.add( new FrequencyDistribution<Tuple<SymbolBelow>>( ) );
			}
		}
		if ( size > 0 ) {
			vector.get( size - 1 ).accept( below );
		}
		fd_base.accept( above );
	}

	private static final String decorator_line =
			"----------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "----------";

	public void print( Consumer<String> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer) {

		if ( a_printer != null && b_printer != null ) {
			logger.accept( decorator_line );
			logger.accept( fd_global.plot( ).setLabels(
					x -> a_printer.apply( x.above ) + " >-> "
							+ TupleAlignmentAlphabetBuilderTools.tuple_printer( b_printer,
									x.below ) )
					.print( ) );
			logger.accept( decorator_line );
			logger.accept( fd_base.plot( ).setLabels( a_printer ).print( ) );
			logger.accept( decorator_line );
			KnittingTuple<FrequencyData<SymbolAbove>> dataSorted =
					fd_base.dataSorted( true );
			for ( int i = 0; i < dataSorted.size( ); i++ ) {
				FrequencyData<SymbolAbove> local_fd = dataSorted.get( i );
				logger.accept( a_printer.apply( local_fd.front( ) ) );
				logger.accept( decorator_line );
				Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector =
						fds.get( local_fd.front( ) );
				for ( int j = 0; j < vector.size( ); j++ ) {

					logger.accept(
							vector.get( j )
									.plot( )
									.setLimit( 10 )
									.setLabels( x -> TupleAlignmentAlphabetBuilderTools
											.tuple_printer( b_printer, x ) )
									.print( ) );
				}
			}
		}
	}

	public HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> complete_alphabet =
			new HashSet<>( );

	private int total;

	private int total_aligneable;

	public int getTotalNumberOfCandidatePairs() {
		return complete_alphabet.size( );
	}
	
	public int getTotalAligneable( ) {
		return total_aligneable;
	}

	public int getTotal( ) {
		return total;
	}

	public void computeCompleteAlphabet(
			ProgressSpawner progress_spawner,
			Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data ) {
		if ( total != 0 ) {
			throw new IllegalStateException( "this can be compued only once" );
		}

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetAnalysis::computeCompleteAlphabet" );
			int not_aligneable = 0;
			for ( Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).pull( hook ).once( ) ) {
				total++;
				spawn.step( 1 );
				KnittingTuple<SymbolAbove> ka = KnittingTuple.wrap( datum.front( ) );
				KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( datum.back( ) );

				try {

					Iterable<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> localAlphabet =
							TupleAlignmentAlphabetBuilderTools.localAlphabet(
									min_below, max_below, ka, kb );

					for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pp : localAlphabet ) {

						record( pp.above, pp.below );
						fd_global.accept( pp );
						complete_alphabet.add( pp );
					}
				}
				catch ( NotAlignableException e ) {
					not_aligneable++;
					// simply ignore them.
				}
			}
			total_aligneable = total - not_aligneable;
		}
	}
	
	public Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> basicOneToOne( ) {
		HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> result =
				new HashSet<>( );
		for (SymbolAbove above : fd_base.data( ).map( x->x.front( ) ).once( )) {
			Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector = fds.get( above );
			TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair = new TupleAlignmentAlphabetPair<>( );
			pair.above = above;
			if (vector != null && vector.size( ) > 0) {
				pair.below = KnittingTuple.wrap(vector.get( 0 ).getMostFrequent( ));
			}
			else {
				pair.below = KnittingTuple.empty( );
			}
			result.add( pair );
		}
		return result;
	}

	public Cursor<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>
			greedy( ) {
		KnittingTuple<SymbolAbove> symbols =
				fd_base.dataSorted( true ).map( x -> x.front( ) );
		return new Cursor<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>( ) {

			int symbol = 0;

			int size = 0;

			int rank = 0;

			boolean none_in_this_rank = true;

			@Override
			public TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> next( )
					throws PastTheEndException {

				TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> result = null;
				for ( ; result == null; ) {

					SymbolAbove symbolAbove = symbols.get( symbol );
					if ( size == 0 ) {
						result = new TupleAlignmentAlphabetPair<>( );
						result.above = symbolAbove;
						result.below = KnittingTuple.empty( );
					}
					else {
						Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector =
								fds.get( symbolAbove );
						if ( vector.size( ) + 1 > size ) {
							FrequencyDistribution<Tuple<SymbolBelow>> frequencyDistribution =
									vector.get( size - 1 );
							if ( frequencyDistribution.dataSorted( true ).size( ) > rank ) {
								result = new TupleAlignmentAlphabetPair<>( );
								result.above = symbolAbove;
								result.below = KnittingTuple.wrap(
										frequencyDistribution.dataSorted( true ).get( rank )
												.front( ) );
								none_in_this_rank = false;
							}
						}
					}
					if ( symbol + 1 == symbols.size( ) ) {
						symbol = 0;
						if ( size == max_below ) {
							size = 1;
							rank++;
							if ( none_in_this_rank ) {
								throw PastTheEndException.neo;
							}
							none_in_this_rank = true;
						}
						else {
							size++;
						}
					}
					else {
						symbol++;
					}
				}
				return result;
			}
		};
	}

}
