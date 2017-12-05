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

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.AlignmentElement;
import org.github.evenjn.align.Tael;
import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Progress;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public class TupleAlignmentAlphabetWithAligner<SymbolAbove, SymbolBelow>
		implements
		TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private TupleAligner<SymbolAbove, SymbolBelow> aligner;

	private Ring<Consumer<String>> logger;

	private Function<SymbolAbove, String> a_printer;

	private Function<SymbolBelow, String> b_printer;

	public TupleAlignmentAlphabetWithAligner(
			TupleAligner<SymbolAbove, SymbolBelow> aligner) {
		this.aligner = aligner;
	}

	@Override
	public void setMinMax(
			int min_above, int max_above,
			int min_below, int max_below ) {
	}

	public void setPrinters(
			Ring<Consumer<String>> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer ) {
		this.logger = logger;
		this.a_printer = a_printer;
		this.b_printer = b_printer;
	}

	public <K> TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<K> data,
			Function<K, Tuple<SymbolAbove>> get_above,
			Function<K, Tuple<SymbolBelow>> get_below,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<K> kd =
				KnittingCursable.wrap( data );
		try ( BasicRook rook = new BasicRook() ) {
			Consumer<String> open_logger = null;
			if ( logger != null ) {
				open_logger = logger.get( rook );
			}
			TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
					new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner,
					"TupleAlignmentAlphabetWithAligner::build" );

			spawn.info( "Computing dataset size." );
			spawn.target( kd.count( ) );
			spawn.info( "Collecting alphabet elements." );
			for ( K datum : kd.pull( rook )
					.once( ) ) {

				spawn.step( 1 );

				Tuple<AlignmentElement<Integer, Integer>> alignment =
						aligner.align( get_above.apply( datum ), get_below.apply( datum ));
				KnittingTuple<Tael<SymbolAbove, SymbolBelow>> localAlphabet =
						KnittingTuple.wrap(
								Tael.tael(
										get_above.apply( datum ), get_below.apply( datum ), alignment ) );
				for ( Tael<SymbolAbove, SymbolBelow> pp : localAlphabet
						.asIterable( ) ) {
					result.add( pp );
				}
				if ( alignment.size( ) < 2 && open_logger != null ) {

					open_logger.accept( "Possible degenerate alignment: " +
							TupleAlignmentAlphabetBuilderTools.tuple_printer( b_printer,
									get_below.apply( datum ) )
							+ " "
							+ TupleAlignmentAlphabetBuilderTools.tuple_printer( a_printer,
									get_above.apply( datum ) ) );
				}
			}
			return result;
		}

	}
}
