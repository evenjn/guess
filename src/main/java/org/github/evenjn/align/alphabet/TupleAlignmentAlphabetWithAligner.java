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

import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetWithAligner<SymbolAbove, SymbolBelow>
implements
TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private int min_below;

	private int max_below;

	private TupleAligner<SymbolAbove, SymbolBelow> aligner;

	private Function<Hook, Consumer<String>> logger;

	private Function<SymbolAbove, String> a_printer;

	private Function<SymbolBelow, String> b_printer;

	public TupleAlignmentAlphabetWithAligner(
			TupleAligner<SymbolAbove, SymbolBelow> aligner) {
		this.aligner = aligner;
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
				this.a_printer = a_printer;
				this.b_printer = b_printer;
	}

	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> kd =
				KnittingCursable.wrap( data );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Consumer<String> open_logger = null;
			if (logger != null) {
				open_logger = logger.apply( hook );
			}
			TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
					new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetSimpleBuilder::build" );

			spawn.info( "Computing dataset size." );
		  spawn.target( kd.size( ) );
			spawn.info( "Collecting alphabet elements." );
			for ( Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : kd.pull( hook )
					.once( ) ) {

				spawn.step( 1 );

				try {
					Iterable<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> localAlphabet =
							TupleAlignmentAlphabetBuilderTools.localAlphabetWithAligner(
									min_below, max_below, datum.front( ), datum.back( ), aligner );
					for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pp : localAlphabet ) {
						result.add( pp );
					}
				}
				catch ( NotAlignableException e ) {
					if ( open_logger != null ) {
						open_logger.accept( "Not aligneable: " +
								TupleAlignmentAlphabetBuilderTools.tuple_printer( b_printer,
										datum.back( ) )
								+ " "
								+ TupleAlignmentAlphabetBuilderTools.tuple_printer( a_printer,
										datum.front( ) ) );
					}
					// simply ignore them.
				}
			}
			return result;
		}
		
	}
}
