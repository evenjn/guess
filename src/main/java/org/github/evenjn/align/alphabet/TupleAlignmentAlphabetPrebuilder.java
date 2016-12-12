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

import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;
	
public class TupleAlignmentAlphabetPrebuilder<Above, Below>
		implements
		TupleAlignmentAlphabetBuilder<Above, Below> {

	TupleAlignmentAlphabet<Above, Below> result =
			new TupleAlignmentAlphabet<Above, Below>( );
	
	public TupleAlignmentAlphabetPrebuilder(
			Iterable<Bi<Above, Tuple<Below>>> data ) {
		for (Bi<Above, Tuple<Below>> d : data) {
			TupleAlignmentAlphabetPair<Above, Below> pair
			= new TupleAlignmentAlphabetPair<>( );
			pair.above = d.first;
			pair.below = KnittingTuple.wrap( KnittingTuple.wrap( d.second )
					.asCursor( ).collect( new Vector<>( ) ) );
			result.add( pair );
		}
	}

	@Override
	public void setMinMax( int min, int max ) {
	}

	@Override
	public void setPrinters(
			Function<Hook, Consumer<String>> logger,
			Function<Above, String> a_printer,
			Function<Below, String> b_printer ) {
	}

	@Override
	public TupleAlignmentAlphabet<Above, Below> build(
			Cursable<Bi<Tuple<Above>, Tuple<Below>>> data,
			ProgressSpawner progress_spawner ) {
			return result;
	}
}
