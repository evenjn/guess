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
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
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
public class TupleAlignmentGraphDataManager<Above, Below> {

	private int record_max_length_front = 0;

	private int record_max_length_back = 0;

	private int record_max_number_of_edges = 0;

	public TupleAlignmentGraphDataManager(
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			Function<Hook, Consumer<String>> putter_coalignment_graphs,
			Cursable<String> reader_coalignment_graphs ) {
		this.min_above = min_above;
		this.max_above = max_above;
		this.min_below = min_below;
		this.max_below = max_below;
		this.putter_coalignment_graphs = putter_coalignment_graphs;
		this.reader_coalignment_graphs = reader_coalignment_graphs;
	}

	private final int min_above;

	private final int max_above;

	private final int min_below;

	private final int max_below;

	private final Function<Hook, Consumer<String>> putter_coalignment_graphs;

	private final Cursable<String> reader_coalignment_graphs;

	private KnittingCursable<TupleAlignmentGraph> exposed_graphs;

	public KnittingCursable<TupleAlignmentGraph> getGraphs( ) {
		if ( exposed_graphs == null ) {
			throw new IllegalStateException( );
		}
		return exposed_graphs;
	}

	/**
	 * @return the length of the longest tuple "at the front" observed in the data.
	 */
	public int getMaxLenghtFront( ) {
		return record_max_length_front;
	}

	/**
	 * @return the length of the longest tuple "at the back" observed in the data.
	 */
	public int getMaxLenghtBack( ) {
		return record_max_length_back;
	}

	/**
	 * @return the maximum number of edges in a single tuple alignment graph
	 *         observed in the cached data.
	 */
	public int getMaxNumberOfEdges( ) {
		return record_max_number_of_edges;
	}

	public TupleAlignmentGraphDataManager<Above, Below> load(
			Cursable<Bi<Tuple<Above>, Tuple<Below>>> data,
			BiFunction<Tuple<Above>, Tuple<Below>, Integer> pair_encoder,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<Bi<Tuple<Above>, Tuple<Below>>> kc = KnittingCursable.wrap( data );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn =
					SafeProgressSpawner.safeSpawn( hook, progress_spawner,
							"prepareGraphs" );
			exposed_graphs = prepareGraphs( kc, pair_encoder, spawn );
		}
		return this;
	}

	private boolean limits_are_computed = false;
	
	private void computeLimits( Progress progress, KnittingCursable<TupleAlignmentGraph> data ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			for ( TupleAlignmentGraph g : data.pull( hook ).once( ) ) {
				int la = g.la( );
				int lb = g.lb( );

				if ( record_max_length_front < la ) {
					record_max_length_front = la;
				}

				if ( record_max_length_back < lb ) {
					record_max_length_back = lb;
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
		limits_are_computed = true;
	}

	
	private
			KnittingCursable<TupleAlignmentGraph>
			prepareGraphs(
					KnittingCursable<Bi<Tuple<Above>, Tuple<Below>>> data,
					BiFunction<Tuple<Above>, Tuple<Below>, Integer> pair_encoder,
					Progress progress ) {
		SkipMap<Bi<Tuple<Above>, Tuple<Below>>, TupleAlignmentGraph> skipMap =
				new SkipMap<Bi<Tuple<Above>, Tuple<Below>>, TupleAlignmentGraph>( ) {

					@Override
					public TupleAlignmentGraph get(
							Bi<Tuple<Above>, Tuple<Below>> x )
							throws SkipException {
						try {
							return TupleAlignmentGraphFactory.graph(
									pair_encoder,
									x.front( ),
									x.back( ),
									min_above,
									max_above,
									min_below,
									max_below );
						}
						catch ( NotAlignableException e ) {
							throw SkipException.neo;
						}
					}
				};
				
		if ( null != putter_coalignment_graphs
				|| null == reader_coalignment_graphs ) {
			/*
			 * re-compute the coalignment graphs.
			 * 
			 * This is a lazy iterator, so the graphs are computed on demand.
			 */

			if ( null != putter_coalignment_graphs ) {

				progress.info( "Computing dataset size before computing limits." );
				int progress_target = 0;
				try ( AutoHook hook2 = new BasicAutoHook( ) ) {
					Progress spawn = progress.spawn( hook2, "computing dataset size" );
					progress_target = data.tap( x -> spawn.step( 1 ) ).size( );
				}
				progress.target( 2 * progress_target );
				
				progress.info( "Computing limits." );
				computeLimits( progress,
						data.tap( x -> progress.step( 1 ) ).skipmap( skipMap ) );
				
				progress.info( "Caching graphs." );
				KnittingCursable<TupleAlignmentGraph> graphs_to_write = data
						.tap( x -> progress.step( 1 ) )
						.skipmap( skipMap );

				StringBuilder header = new StringBuilder( );
				header.append( record_max_length_front );
				header.append( "," );
				header.append( record_max_length_back );
				header.append( "," );
				header.append( record_max_number_of_edges );
				try ( AutoHook hook = new BasicAutoHook( ) ) {
					KnittingCursor.on( header.toString( ) ).chain(
							graphs_to_write
									.pull( hook )
									.unfoldCursable(
											x -> new TupleAlignmentGraphSerializer( x ) ) )
							.consume( putter_coalignment_graphs );
				}
				limits_are_computed = true;
			}
		}

		if ( null != reader_coalignment_graphs ) {

			try ( AutoHook hook = new BasicAutoHook( ) ) {

				Pattern splitter = Pattern.compile( "," );

				String[] split = splitter.split(
						KnittingCursable.wrap( reader_coalignment_graphs ).head( 0, 1 )
								.one( hook ) );
				record_max_length_front = Integer.parseInt( split[0] );
				record_max_length_back = Integer.parseInt( split[1] );
				record_max_number_of_edges = Integer.parseInt( split[2] );
				limits_are_computed = true;
			}
			/*
			 * de-serialize them from the reader.
			 */
			return KnittingCursable
					.wrap( reader_coalignment_graphs )
					.headless( 1 )
					.skipfold( ( ) -> new TupleAlignmentGraphDeserializer(
							record_max_length_front,
							record_max_length_back ) );
		}
		else {
			if ( !limits_are_computed ) {
				progress.info(
						"Computing dataset size before computing limits." );
				int progress_target = 0;
				try ( AutoHook hook2 = new BasicAutoHook( ) ) {
					Progress spawn = progress.spawn( hook2, "computing dataset size" );
					progress_target = data.tap( x -> spawn.step( 1 ) ).size( );
				}
				progress.target( progress_target );
				progress.info( "Computing limits." );
				computeLimits( progress,
						data.tap( x -> progress.step( 1 ) ).skipmap( skipMap ) );
			}
			return data.skipmap( skipMap );
		}
	}
}
