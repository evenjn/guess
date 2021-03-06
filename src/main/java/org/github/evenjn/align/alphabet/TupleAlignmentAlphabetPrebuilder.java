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

import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.Tael;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;
	
public class TupleAlignmentAlphabetPrebuilder<Above, Below>
		implements
		TupleAlignmentAlphabetBuilder<Above, Below> {

	TupleAlignmentAlphabet<Above, Below> result =
			new TupleAlignmentAlphabet<Above, Below>( );
	
	public <K> TupleAlignmentAlphabetPrebuilder(
			Iterable<K> data,
			Function<K, Tuple<Above>> get_above,
			Function<K, Tuple<Below>> get_below ) {
		for (K d : data ) {
			Tael<Above, Below> pair
			= new Tael<>( KnittingTuple.wrap( KnittingTuple.wrap( get_above.apply( d ))
					.asKnittingCursor( ).collect( new Vector<>( ) ) ).asTupleValue( ),
					KnittingTuple.wrap( KnittingTuple.wrap( get_below.apply( d ) )
							.asKnittingCursor( ).collect( new Vector<>( ) ) ).asTupleValue( ));
			result.add( pair );
		}
	}

	@Override
	public void setMinMax( 
			int min_above, int max_above,
			int min_below, int max_below ) {
	}

	@Override
	public void setPrinters(
			Ring<Consumer<String>> logger,
			Function<Above, String> a_printer,
			Function<Below, String> b_printer ) {
	}

	@Override
	public <K> TupleAlignmentAlphabet<Above, Below> build(
			Cursable<K> data,
			Function<K, Tuple<Above>> get_above,
			Function<K, Tuple<Below>> get_below,
			ProgressSpawner progress_spawner ) {
			return result;
	}
}
