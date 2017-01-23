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
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.Tael;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.numeric.FrequencyData;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetAnalysis<SymbolAbove, SymbolBelow> {

	public TupleAlignmentAlphabetAnalysis(
			int min_above, int max_above,
			int min_below, int max_below) {
		this.min_above = min_above;
		this.max_above = max_above;
		this.min_below = min_below;
		this.max_below = max_below;
	}

	private int min_above;

	private int max_above;

	private int min_below;

	private int max_below;

	/**
	 * Frequency distribution of symbols above
	 */
	private FrequencyDistribution<Tuple<SymbolAbove>> //
	fd_all_symbols_above = new FrequencyDistribution<>( );

	/**
	 * Frequency distribution of all pairs [symbol_above, tuple_of_symbol_below]
	 */
	private FrequencyDistribution<Tael<SymbolAbove, SymbolBelow>> //
	fd_all_pairs = new FrequencyDistribution<>( );

	/**
	 * maps each symbol_above to a frequency distribution of tuples of symbols
	 * below
	 */
	private HashMap<Tuple<SymbolAbove>, FrequencyDistribution<Tuple<SymbolBelow>>> //
	fd_tuplebelow_by_symbolabove = new HashMap<>( );

	/**
	 * for each J, there is a map at position J in this vector. Each such map
	 * sends each symbol_above to the frequency distribution of all
	 * [tuple_of_symbol_below] with size J.
	 */
	private Vector<HashMap<Tuple<SymbolAbove>, FrequencyDistribution<Tuple<SymbolBelow>>>> //
	fd_tuplebelow_by_symbolabove_and_size = new Vector<>( );

	/**
	 * 
	 * @return SymbolsAbove Sorted By Descending Frequency
	 */
	public KnittingTuple<Tuple<SymbolAbove>>
			getSymbolsAbove( ) {
		KnittingTuple<FrequencyData<Tuple<SymbolAbove>>> dataSorted =
				fd_all_symbols_above.dataSorted( true );
		return dataSorted.map( x -> x.front( ) );
	}

	/**
	 * 
	 * @return Pairs Sorted By Descending Frequency
	 */
	public KnittingTuple<Tael<SymbolAbove, SymbolBelow>>
			getPairs( ) {
		KnittingTuple<FrequencyData<Tael<SymbolAbove, SymbolBelow>>> dataSorted =
				fd_all_pairs.dataSorted( true );
		return dataSorted.map( x -> x.front( ) );
	}
	/**
	 * 
	 * @return tuples of SymbolsBelow for the given SymbolAbove Sorted By
	 *         Descending Frequency
	 */
	public KnittingTuple<Tuple<SymbolBelow>>
			getSymbolsBelow( Tuple<SymbolAbove> above ) {
		KnittingTuple<FrequencyData<Tuple<SymbolBelow>>> dataSorted =
				fd_tuplebelow_by_symbolabove.get( above ).dataSorted( true );
		return dataSorted.map( x -> x.front( ) );
	}

	/**
	 * 
	 * @return tuples of SymbolsBelow for the given SymbolAbove Sorted By
	 *         Descending Frequency
	 */
	public KnittingTuple<Tuple<SymbolBelow>>
			getSymbolsBelow( Tuple<SymbolAbove> above, int size ) {
		if (fd_tuplebelow_by_symbolabove_and_size.size( ) <= size) {
			return KnittingTuple.empty( );
		}

		FrequencyDistribution<Tuple<SymbolBelow>> frequencyDistribution =
				fd_tuplebelow_by_symbolabove_and_size.get( size ).get( above );

		if (frequencyDistribution == null) {
			return KnittingTuple.empty( );
		}
		KnittingTuple<FrequencyData<Tuple<SymbolBelow>>> dataSorted =
				frequencyDistribution.dataSorted( true );
		
		return dataSorted.map( x -> x.front( ) );
	}

	public void record( Tuple<SymbolAbove> above, Tuple<SymbolBelow> below ) {
		
		fd_all_symbols_above.accept( above );

		FrequencyDistribution<Tuple<SymbolBelow>> fd_tuplebelow_for_this_symbol =
				fd_tuplebelow_by_symbolabove.get( above );

		if ( fd_tuplebelow_for_this_symbol == null ) {
			fd_tuplebelow_for_this_symbol = new FrequencyDistribution<>( );
			fd_tuplebelow_by_symbolabove.put( above, fd_tuplebelow_for_this_symbol );
		}
		fd_tuplebelow_for_this_symbol.accept( below );
		

		int size = below.size( );
		for ( int i = fd_tuplebelow_by_symbolabove_and_size.size( ); i <= size; i++ ) {
			fd_tuplebelow_by_symbolabove_and_size.add( new HashMap<>( ) );
		}

		FrequencyDistribution<Tuple<SymbolBelow>> frequencyDistribution =
				fd_tuplebelow_by_symbolabove_and_size.get( size ).get( above );
		if ( frequencyDistribution == null ) {
			frequencyDistribution = new FrequencyDistribution<>( );
			fd_tuplebelow_by_symbolabove_and_size.get( size ).put( above,
					frequencyDistribution );
		}
		frequencyDistribution.accept( below );
	}

	private static final String decorator_line =
			">---------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "---------<";

	public void print( Consumer<String> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer) {

		if ( a_printer != null && b_printer != null ) {
			logger.accept( "" );
			logger.accept( " Beginning of TupleAlignmentAlphabetAnalysis report" );
			logger.accept( decorator_line );
			logger.accept( " The total number of distinct a/b pairs is: " +  complete_alphabet.size( ) );
			logger.accept( " The total number of tuple pairs is: " + total );
			logger.accept( " The number of aligneable tuple pairs is: " + total_aligneable );
			logger.accept( " The number of non aligneable tuple pairs is: " + ( total - total_aligneable ) );
			logger.accept( decorator_line );
			logger.accept( "" );
			logger.accept( " Distribution of a/b pairs" );
			logger.accept( fd_all_pairs.plot( ).setLabels(
					x -> TupleAlignmentAlphabetBuilderTools.tuple_printer( a_printer,
							x.above ) + " >-> "
							+ TupleAlignmentAlphabetBuilderTools.tuple_printer( b_printer,
									x.below ) )
					.print( ) );
			logger.accept( decorator_line );
			logger.accept( "" );
			logger.accept( " Distribution of symbols above" );
			logger.accept( fd_all_symbols_above.plot( )

					.setLabels( x -> TupleAlignmentAlphabetBuilderTools
							.tuple_printer( a_printer, x ) )
					.print( ) );
			logger.accept( decorator_line );
			KnittingTuple<FrequencyData<Tuple<SymbolAbove>>> dataSorted =
					fd_all_symbols_above.dataSorted( true );
			for ( int i = 0; i < dataSorted.size( ); i++ ) {
				FrequencyData<Tuple<SymbolAbove>> local_fd = dataSorted.get( i );
				logger.accept( "" );
				logger.accept( " Distribution of symbols below "
				+ TupleAlignmentAlphabetBuilderTools
				.tuple_printer( a_printer, local_fd.front( ) )
				+ "" );

				logger.accept(
						fd_tuplebelow_by_symbolabove.get( local_fd.front( ) )
								.plot( )
								.setLimit( 10 )
								.setLabels( x -> TupleAlignmentAlphabetBuilderTools
										.tuple_printer( b_printer, x ) )
								.print( ) );
				
				logger.accept( "" );
				logger.accept( " Distribution of symbols below "
						+ TupleAlignmentAlphabetBuilderTools
						.tuple_printer( a_printer, local_fd.front( ) )
						+ " (for a given size of the tuple below)" );
				logger.accept( decorator_line );
				for ( int j = 0; j < fd_tuplebelow_by_symbolabove_and_size.size( ); j++ ) {
					HashMap<Tuple<SymbolAbove>, FrequencyDistribution<Tuple<SymbolBelow>>> hashMap =
							fd_tuplebelow_by_symbolabove_and_size.get( j );
					if ( !hashMap.containsKey( local_fd.front( ) ) ) {
						continue;
					}
					logger.accept(
							hashMap.get( local_fd.front( ) )
									.plot( )
									.setLimit( 10 )
									.setLabels( x -> TupleAlignmentAlphabetBuilderTools
											.tuple_printer( b_printer, x ) )
									.print( ) );
				}
			}
			logger.accept( " End of TupleAlignmentAlphabetAnalysis report" );
			logger.accept( "" );
		}
	}

	public HashSet<Tael<SymbolAbove, SymbolBelow>> complete_alphabet =
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
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data ) {
		if ( total != 0 ) {
			throw new IllegalStateException( "this can be compued only once" );
		}

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			Progress spawn = SafeProgressSpawner.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetAnalysis::computeCompleteAlphabet" );
			int not_aligneable = 0;
			for ( Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).pull( hook ).once( ) ) {
				total++;
				spawn.step( 1 );
				KnittingTuple<SymbolAbove> ka = KnittingTuple.wrap( datum.front( ) );
				KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( datum.back( ) );

				try {

					Iterable<Tael<SymbolAbove, SymbolBelow>> localAlphabet =
							TupleAlignmentAlphabetBuilderTools.localAlphabet(
									min_above, max_above,
									min_below, max_below, ka, kb );

					for ( Tael<SymbolAbove, SymbolBelow> pp : localAlphabet ) {

						record( pp.above, pp.below );
						fd_all_pairs.accept( pp );
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
	

}
