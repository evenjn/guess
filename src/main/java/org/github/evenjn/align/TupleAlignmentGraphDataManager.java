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
package org.github.evenjn.align;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetBuilder;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetDeserializer;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetPair;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetSerializer;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentGraphDeserializer;
import org.github.evenjn.align.graph.TupleAlignmentGraphFactory;
import org.github.evenjn.align.graph.TupleAlignmentGraphSerializer;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipMap;
import org.github.evenjn.yarn.Tuple;

/*
 * This object acts as a preprocessor for systems that work on alingment graphs.
 * 
 * Its function is to transform a dataset of tuple pairs into a dataset of
 *  alignment graphs.
 *  
 * It also provides information about the dataset such as the length of the
 *  longest tuples and the maximum number of edges occurring in a graph.
 * 
 * 
 * 
 */
public class TupleAlignmentGraphDataManager<I, O> {

	private int record_max_length_above = 0;

	private int record_max_length_below = 0;

	private int record_max_number_of_edges = 0;
	
	private boolean enable_cache;

	private boolean refresh_cache;

	public TupleAlignmentGraphDataManager(
			int min_below,
			int max_below) {
		this.min_below = min_below;
		this.max_below = max_below;
		this.putter_coalignment_alphabet = null;
		this.reader_coalignment_alphabet = null;
		this.putter_coalignment_graphs = null;
		this.reader_coalignment_graphs = null;
		this.a_serializer = null;
		this.b_serializer = null;
		this.a_deserializer = null;
		this.b_deserializer = null;
		this.a_printer = null;
		this.b_printer = null;
		this.enable_cache = false;
		this.refresh_cache = false;
	}

	public TupleAlignmentGraphDataManager(
			int min_below,
			int max_below,
			Function<Hook, Consumer<String>> putter_coalignment_alphabet,
			Cursable<String> reader_coalignment_alphabet,
			Function<Hook, Consumer<String>> putter_coalignment_graphs,
			Cursable<String> reader_coalignment_graphs,
			Function<I, String> a_serializer,
			Function<O, String> b_serializer,
			Function<String, I> a_deserializer,
			Function<String, O> b_deserializer,
			Function<I, String> a_printer,
			Function<O, String> b_printer,
			boolean refresh_cache) {
		this.min_below = min_below;
		this.max_below = max_below;
		this.putter_coalignment_alphabet = putter_coalignment_alphabet;
		this.reader_coalignment_alphabet = reader_coalignment_alphabet;
		this.putter_coalignment_graphs = putter_coalignment_graphs;
		this.reader_coalignment_graphs = reader_coalignment_graphs;
		this.a_serializer = a_serializer;
		this.b_serializer = b_serializer;
		this.a_deserializer = a_deserializer;
		this.b_deserializer = b_deserializer;
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		this.enable_cache = true;
		this.refresh_cache = refresh_cache;
		if ( putter_coalignment_alphabet == null )
			throw new IllegalArgumentException( );
		if ( reader_coalignment_alphabet == null )
			throw new IllegalArgumentException( );
		if ( putter_coalignment_graphs == null )
			throw new IllegalArgumentException( );
		if ( reader_coalignment_graphs == null )
			throw new IllegalArgumentException( );
		if ( a_serializer == null )
			throw new IllegalArgumentException( );
		if ( b_serializer == null )
			throw new IllegalArgumentException( );
		if ( a_deserializer == null )
			throw new IllegalArgumentException( );
		if ( b_deserializer == null )
			throw new IllegalArgumentException( );
	}

	private final int min_below;

	private final int max_below;

	private final Function<Hook, Consumer<String>> putter_coalignment_alphabet;

	private final Cursable<String> reader_coalignment_alphabet;

	private final Function<Hook, Consumer<String>> putter_coalignment_graphs;

	private final Cursable<String> reader_coalignment_graphs;

	private final Function<I, String> a_printer;

	private final Function<O, String> b_printer;

	private final Function<I, String> a_serializer;

	private final Function<O, String> b_serializer;

	private final Function<String, I> a_deserializer;

	private final Function<String, O> b_deserializer;

	private KnittingCursable<TupleAlignmentGraph> graphs;

	private TupleAlignmentAlphabet<I, O> alphabet;

	public TupleAlignmentAlphabet<I, O> getAlphabet( ) {
		if ( alphabet == null ) {
			throw new IllegalStateException( );
		}
		return alphabet;
	}

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
			ProgressSpawner progress ) {
		KnittingCursable<Di<Tuple<I>, Tuple<O>>> kc = KnittingCursable.wrap( data );
		alphabet = prepareAlphabet( kc, progress );
		graphs = prepareGraphs( kc, progress );
		return this;
	}

	private
			TupleAlignmentAlphabet<I, O>
			prepareAlphabet(
					KnittingCursable<Di<Tuple<I>, Tuple<O>>> data,
					ProgressSpawner progress ) {

		TupleAlignmentAlphabet<I, O> coalignment_alphabet = null;

		if ( !enable_cache || refresh_cache ) {
			/*
			 * re-compute the coalignment alphabet.
			 */
			KnittingCursable<Di<Tuple<I>, Tuple<O>>> map =
					data
							.map( x -> ( new Bi<Tuple<I>, Tuple<O>>( )
									.set( x.front( ), x.back( ) ) ) );

			try ( AutoHook hook = new BasicAutoHook( ) ) {
				Progress spawn = ProgressManager.safeSpawn( hook, progress, "prepareAlphabet" );

				spawn.info( "Computing dataset size." );
				int size = data.size( );
				spawn.target( enable_cache ? 2 * size : size );
				spawn.info( "Working out alphabet" );

				coalignment_alphabet =
						createAlphabet( map.pull( hook ), min_below, max_below, spawn );

				/*
				 * serialize the coalignment alphabet, and pour it into the putter.
				 */
				if ( enable_cache ) {
					spawn.info( "Serializing alignment graphs" );
					TupleAlignmentAlphabetSerializer<I, O> serializer =
							new TupleAlignmentAlphabetSerializer<>(
									coalignment_alphabet,
									a_serializer,
									b_serializer );
					KnittingCursable.wrap( serializer )
							.tap( x -> spawn.step( 1 ) )
							.consume(
									putter_coalignment_alphabet );
				}
			}
		}
		if ( enable_cache ) {
			/*
			 * Otherwise, de-serialize it from the reader.
			 */

			try ( AutoHook hook = new BasicAutoHook( ) ) {
				/**
				 * This is interesting, because the output of the serializer is not
				 * volatile, but how can we communicate that?
				 */
				coalignment_alphabet = KnittingCursable
						.wrap( reader_coalignment_alphabet ).pull( hook )
						.skipfold( new TupleAlignmentAlphabetDeserializer<>(
								a_deserializer,
								b_deserializer ) ).one( );
			}
			// int count = 0;
			// for (TupleAlignmentPair<I, O> i : coalignment_alphabet) {
			// StringBuilder sb = new StringBuilder( );
			// sb.append( count++ ).append( " ").append( i.print( ) );
			// System.out.println(sb.toString( ));
			// }
		}

		return coalignment_alphabet;
	}

	private void computeLimits(
			KnittingCursable<TupleAlignmentGraph> data,
			ProgressSpawner progress) {
		int record_max_length_above = 0;
		int record_max_length_below = 0;
		int record_max_number_of_edges = 0;
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			for (TupleAlignmentGraph g : data.pull( hook ).once( )) {	
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
				while( iter.hasNext( )) {
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
					ProgressSpawner progress ) {

		KnittingCursable<TupleAlignmentGraph> graphs = null;

		if ( !enable_cache || refresh_cache ) {
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
										( a, b ) -> alphabet.encode( a, b ),
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
			computeLimits( graphs, progress );
			if ( enable_cache ) {

				try ( AutoHook hook = new BasicAutoHook( ) ) {

					Progress spawn =
							ProgressManager.safeSpawn( hook, progress, "prepareGraphs");
					KnittingCursable<TupleAlignmentGraph> graphs_to_write = graphs;

					spawn.info( "Computing dataset size." );
					spawn.target( data.size( ) );
					graphs_to_write = data
							.tap( x -> spawn.step(1 ) )
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

		if ( enable_cache ) {

			try ( AutoHook hook = new BasicAutoHook( ) ) {

				Pattern splitter = Pattern.compile( "," );

				String[] split = splitter.split(
						KnittingCursable.wrap( reader_coalignment_graphs ).head( 0, 1 ).one( hook ) );
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

	private FrequencyDistribution<I> fd_sa = null;

	private FrequencyDistribution<O> fd_sb = null;

	private FrequencyDistribution<TupleAlignmentAlphabetPair<I, O>> fd_pair =
			null;

	private void computeStats( ) {
		fd_sa = new FrequencyDistribution<>( );
		fd_sb = new FrequencyDistribution<>( );
		fd_pair = new FrequencyDistribution<>( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			for ( TupleAlignmentGraph g : graphs.pull( hook ).once( ) ) {
				Iterator<TupleAlignmentNode> forward = g.forward( );
				while ( forward.hasNext( ) ) {
					TupleAlignmentNode node = forward.next( );
					for ( int i = 0; i < node.number_of_incoming_edges; i++ ) {
						int encoded = node.incoming_edges[i][2];
						TupleAlignmentAlphabetPair<I, O> pair =
								alphabet.get( encoded );
						fd_pair.accept( pair );
					}
				}
			}
			for ( TupleAlignmentGraph g : graphs.pull( hook ).once( ) ) {
				TupleAlignmentNode node = g.get( g.la( ), g.lb( ) );
				for ( ;; ) {

					int x = node.incoming_edges[0][0];
					int y = node.incoming_edges[0][1];
					int encoded = node.incoming_edges[0][2];

					TupleAlignmentAlphabetPair<I, O> pair =
							alphabet.get( encoded );
					fd_sa.accept( pair.above );
					for ( O b : pair.below.asIterable( ) ) {
						fd_sb.accept( b );
					}
					if ( x == 0 && y == 0 ) {
						break;
					}
					node = g.get( x, y );
				}
			}
		}
	}

	public FrequencyDistribution<I> getIFD( ) {
		if ( fd_sa == null ) {
			computeStats( );
		}
		return fd_sa;
	}

	public FrequencyDistribution<O> getOFD( ) {
		if ( fd_sb == null ) {
			computeStats( );
		}
		return fd_sb;
	}

	public FrequencyDistribution<TupleAlignmentAlphabetPair<I, O>>
			getPairFD( ) {
		if ( fd_pair == null ) {
			computeStats( );
		}
		return fd_pair;
	}

	private TupleAlignmentAlphabet<I, O>
			createAlphabet(
					KnittingCursor<Di<Tuple<I>, Tuple<O>>> data,
					int min_below,
					int max_below,
					Progress progress ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			TupleAlignmentAlphabetBuilder<I, O> builder =
					new TupleAlignmentAlphabetBuilder<>( );
			builder.setPrinters( a_printer, b_printer );
			builder.setMinMax(min_below, max_below);
			return builder.build( data , progress );
		}
	}
}
