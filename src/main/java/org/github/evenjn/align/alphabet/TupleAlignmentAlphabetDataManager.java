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

import java.util.function.Function;

import org.github.evenjn.knit.BasicAutoRook;
import org.github.evenjn.knit.BiTray;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.yarn.AutoRook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.RookConsumer;
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
public class TupleAlignmentAlphabetDataManager<I, O> {

	private TupleAlignmentAlphabetBuilder<I, O> builder;

	/**
	 * There are four possible configurations that affect caching behaviour.
	 * 
	 * reader is null, writer is null
	 * 
	 * In this case, data is transformed and passed over. No caching occurs.
	 * 
	 * reader is null, writer is not null
	 * 
	 * In this case, data is transformed and cached. However, the cache created
	 * this way is not used. Data is transformed and passed over.
	 * 
	 * reader is not null, writer is null
	 * 
	 * In this case, the incoming data is discarded, and loaded from cache
	 * instead. Cache is only read, not written.
	 * 
	 * reader is not null, writer is not null
	 * 
	 * In this case, the data is transformed and cached. Then data is loaded from
	 * cache and passed over.
	 * 
	 */
	public TupleAlignmentAlphabetDataManager(
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			TupleAlignmentAlphabetBuilder<I, O> builder,
			RookConsumer<String> writer,
			Cursable<String> reader,
			Function<I, String> a_serializer,
			Function<O, String> b_serializer,
			Function<String, I> a_deserializer,
			Function<String, O> b_deserializer,
			Function<I, String> a_printer,
			Function<O, String> b_printer,
			RookConsumer<String> logger) {
		this.min_above = min_above;
		this.max_above = max_above;
		this.min_below = min_below;
		this.max_below = max_below;
		this.builder = builder != null ? builder
				: new TupleAlignmentAlphabetGreedyBuilder<I, O>( false );
		this.writer = writer;
		this.reader = reader;
		this.a_serializer = a_serializer;
		this.b_serializer = b_serializer;
		this.a_deserializer = a_deserializer;
		this.b_deserializer = b_deserializer;
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		this.logger = logger;
	}

	private final int min_above;

	private final int max_above;

	private final int min_below;

	private final int max_below;

	private final RookConsumer<String> writer;

	private final RookConsumer<String> logger;

	private final Cursable<String> reader;

	private final Function<I, String> a_printer;

	private final Function<O, String> b_printer;

	private final Function<I, String> a_serializer;

	private final Function<O, String> b_serializer;

	private final Function<String, I> a_deserializer;

	private final Function<String, O> b_deserializer;

	private TupleAlignmentAlphabet<I, O> alphabet;

	public TupleAlignmentAlphabet<I, O> getAlphabet( ) {
		if ( alphabet == null ) {
			throw new IllegalStateException( );
		}
		return alphabet;
	}

	public TupleAlignmentAlphabetDataManager<I, O> load(
			Cursable<Bi<Tuple<I>, Tuple<O>>> data,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<Bi<Tuple<I>, Tuple<O>>> kc = KnittingCursable.wrap( data );
		alphabet = prepareAlphabet( kc, progress_spawner );
		return this;
	}

	private TupleAlignmentAlphabet<I, O> prepareAlphabet(
			KnittingCursable<Bi<Tuple<I>, Tuple<O>>> data,
			ProgressSpawner progress_spawner ) {

		TupleAlignmentAlphabet<I, O> coalignment_alphabet = null;

		if ( null != writer || null == reader ) {
			/*
			 * re-compute the coalignment alphabet.
			 */
			KnittingCursable<Bi<Tuple<I>, Tuple<O>>> map =
					data
							.map( x -> ( BiTray.nu( x.front( ), x.back( ) ) ) );

			try ( AutoRook rook = new BasicAutoRook( ) ) {
				Progress spawn =
						SafeProgressSpawner.safeSpawn( rook, progress_spawner, "prepareAlphabet" );

				spawn.info( "Computing dataset size." );
				int size = data.count( );
				spawn.target( null != writer ? 2 * size : size );
				spawn.info( "Working out alphabet" );

				builder.setPrinters( logger, a_printer, b_printer );
				builder.setMinMax( min_above, max_above, min_below, max_below );
				coalignment_alphabet = builder.build( map, spawn );

				/*
				 * serialize the coalignment alphabet, and pour it into the putter.
				 */
				if ( null != writer ) {
					spawn.info( "Serializing alignment graphs" );
					TupleAlignmentAlphabetSerializer<I, O> serializer =
							new TupleAlignmentAlphabetSerializer<>(
									coalignment_alphabet,
									a_serializer,
									b_serializer );
					KnittingCursable.wrap( serializer )
							.peek( x -> spawn.step( 1 ) )
							.consume(
									writer );
				}
			}
		}
		if ( null != reader ) {
			/*
			 * Otherwise, de-serialize it from the reader.
			 */

			try ( AutoRook rook = new BasicAutoRook( ) ) {
				/**
				 * This is interesting, because the output of the serializer is not
				 * volatile, but how can we communicate that?
				 */
				coalignment_alphabet = KnittingCursable
						.wrap( reader ).pull( rook )
						.purlOptional( new TupleAlignmentAlphabetDeserializer<>(
								a_deserializer,
								b_deserializer ) )
						.one( );
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

}
