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
package org.github.evenjn.align.graph;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.Tuple;

/**
 * This object acts as a preprocessor for systems that work on alingment graphs.
 * 
 * Its function is to transform a dataset of tuple pairs into a dataset of
 * alignment graphs.
 * 
 * It also provides information about the dataset such as the length of the
 * longest tuples and the maximum number of edges occurring in a graph.
 * 
 * 
 * There are four possible configurations that affect caching behaviour.
 * 
 * reader is null, writer is null
 * 
 * In this case, data is transformed and passed over. No caching occurs.
 * 
 * reader is null, writer is not null
 * 
 * In this case, data is transformed and cached. However, the cache created this
 * way is not used. Data is transformed and passed over.
 * 
 * reader is not null, writer is null
 * 
 * In this case, the incoming data is discarded, and loaded from cache instead.
 * Cache is only read, not written.
 * 
 * reader is not null, writer is not null
 * 
 * In this case, the data is transformed and cached. Then data is loaded from
 * cache and passed over.
 * 
 */
public class TupleAlignmentGraphDataManager<I, O> {

	private int record_max_length_above = 0;

	private int record_max_length_below = 0;

	private int record_max_number_of_edges = 0;

	public TupleAlignmentGraphDataManager(
			int min_below,
			int max_below,
			Function<Hook, Consumer<String>> putter_coalignment_graphs,
			Cursable<String> reader_coalignment_graphs ) {
		this.min_below = min_below;
		this.max_below = max_below;
		this.putter_coalignment_graphs = putter_coalignment_graphs;
		this.reader_coalignment_graphs = reader_coalignment_graphs;
	}

	private final int min_below;

	private final int max_below;

	private final Function<Hook, Consumer<String>> putter_coalignment_graphs;

	private final Cursable<String> reader_coalignment_graphs;

	private KnittingCursable<TupleAlignmentGraph> graphs;

	public KnittingCursable<TupleAlignmentGraph> getGraphs( ) {
		if ( graphs == null ) {
			throw new IllegalStateException( );
		}
		return graphs;
	}

	/**
	 * @return the maximum number of input symbols observed in the cached data.
	 */
	public int getMaxLenghtAbove( ) {
		return record_max_length_above;
	}

	/**
	 * @return the maximum number of output symbols observed in the cached data.
	 */
	public int getMaxLenghtBelow( ) {
		return record_max_length_below;
	}

	/**
	 * @return the maximum number of edges in a single tuple alignment graph
	 *         observed in the cached data.
	 */
	public int getMaxNumberOfEdges( ) {
		return record_max_number_of_edges;
	}

	public TupleAlignmentGraphDataManager<I, O> load(
			Cursable<Di<Tuple<I>, Tuple<O>>> data,
			BiFunction<I, Tuple<O>, Integer> pair_encoder,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<Di<Tuple<I>, Tuple<O>>> kc = KnittingCursable.wrap( data );
		graphs = prepareGraphs( kc, pair_encoder, progress_spawner );
		return this;
	}

	private void computeLimits( KnittingCursable<TupleAlignmentGraph> data ) {
		int record_max_length_above = 0;
		int record_max_length_below = 0;
		int record_max_number_of_edges = 0;
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			for ( TupleAlignmentGraph g : data.pull( hook ).once( ) ) {
				int la = g.la( );
				int lb = g.lb( );

				if ( record_max_length_above < la ) {
					record_max_length_above = la;
				}

				if ( record_max_length_below < lb ) {
					record_max_length_below = lb;
				}

				int current_number_of_edges = 0;
				Iterator<TupleAlignmentNode> iter = g.forward( );
				while ( iter.hasNext( ) ) {
					TupleAlignmentNode node = iter.next( );
					int no_ie = node.number_of_incoming_edges;
					current_number_of_edges = current_number_of_edges + no_ie;

				}
				if ( record_max_number_of_edges < current_number_of_edges ) {
					record_max_number_of_edges = current_number_of_edges;
				}
			}
		}
		this.record_max_length_above = record_max_length_above;
		this.record_max_length_below = record_max_length_below;
		this.record_max_number_of_edges = record_max_number_of_edges;
	}

	private
			KnittingCursable<TupleAlignmentGraph>
			prepareGraphs(
					KnittingCursable<Di<Tuple<I>, Tuple<O>>> data,
					BiFunction<I, Tuple<O>, Integer> pair_encoder,
					ProgressSpawner progress_spawner ) {

		KnittingCursable<TupleAlignmentGraph> graphs = null;

		if ( null != putter_coalignment_graphs
				|| null == reader_coalignment_graphs ) {
			/*
			 * re-compute the coalignment graphs.
			 * 
			 * This is a lazy iterator, so the graphs are computed on demand.
			 */

			SkipMap<Di<Tuple<I>, Tuple<O>>, TupleAlignmentGraph> skipMap =
					new SkipMap<Di<Tuple<I>, Tuple<O>>, TupleAlignmentGraph>( ) {

						@Override
						public TupleAlignmentGraph get(
								Di<Tuple<I>, Tuple<O>> x )
								throws SkipException {
							try {
								return TupleAlignmentGraphFactory.graph(
										pair_encoder,
										x.front( ),
										x.back( ),
										min_below,
										max_below );
							}
							catch ( NotAlignableException e ) {
								throw SkipException.neo;
							}
						}
					};

			graphs = data.skipmap( skipMap );
			computeLimits( graphs );
			if ( null != putter_coalignment_graphs ) {

				try ( AutoHook hook = new BasicAutoHook( ) ) {

					Progress spawn =
							ProgressManager.safeSpawn( hook, progress_spawner, "prepareGraphs" );
					KnittingCursable<TupleAlignmentGraph> graphs_to_write = graphs;

					spawn.info( "Computing dataset size." );
					spawn.target( data.size( ) );
					graphs_to_write = data
							.tap( x -> spawn.step( 1 ) )
							.skipmap( skipMap );

					StringBuilder header = new StringBuilder( );
					header.append( record_max_length_above );
					header.append( "," );
					header.append( record_max_length_below );
					header.append( "," );
					header.append( record_max_number_of_edges );

					KnittingCursor.on( header.toString( ) ).chain(
							graphs_to_write
									.pull( hook )
									.unfoldCursable(
											x -> new TupleAlignmentGraphSerializer( x ) ) )
							.consume( putter_coalignment_graphs );
				}
			}
		}

		if ( null != reader_coalignment_graphs ) {

			try ( AutoHook hook = new BasicAutoHook( ) ) {

				Pattern splitter = Pattern.compile( "," );

				String[] split = splitter.split(
						KnittingCursable.wrap( reader_coalignment_graphs ).head( 0, 1 )
								.one( hook ) );
				record_max_length_above = Integer.parseInt( split[0] );
				record_max_length_below = Integer.parseInt( split[1] );
				record_max_number_of_edges = Integer.parseInt( split[2] );
			}
			/*
			 * de-serialize them from the reader.
			 */
			graphs = KnittingCursable
					.wrap( reader_coalignment_graphs )
					.headless( 1 )
					.skipfold( ( ) -> new TupleAlignmentGraphDeserializer(
							record_max_length_above,
							record_max_length_below ) );
		}
		return graphs;
	}
}
