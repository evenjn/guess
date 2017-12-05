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
package org.github.evenjn.guess.benchmark;

import java.util.HashMap;
import java.util.function.Function;

import org.github.evenjn.guess.Trainer;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.TupleValue;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public class CheatingMapleTrainer<I, O> implements
		Trainer<Tuple<I>, Tuple<O>> {

	public CheatingMapleTrainer(Tuple<O> last) {
		this.last = last;
	}

	private final Tuple<O> last;

	@Override
	public Function<Tuple<I>, Tuple<O>> train(
			ProgressSpawner progress_spawner,
			Cursable<Bi<Tuple<I>, Tuple<O>>> data ) {
		HashMap<TupleValue<I>, TupleValue<O>> cheat_sheet = new HashMap<>( );

		try ( BasicRook rook = new BasicRook() ) {
			for ( Bi<Tuple<I>, Tuple<O>> td : KnittingCursable.wrap( data ).pull( rook ).once( ) ) {
				cheat_sheet.put( KnittingTuple.wrap(td.front( )).asTupleValue(), KnittingTuple.wrap(td.back( )).asTupleValue() );
			}
		}
		return new Function<Tuple<I>, Tuple<O>>( ) {

			@Override
			public Tuple<O> apply(
					Tuple<I> t ) {
				Tuple<O> o = cheat_sheet.get( KnittingTuple.wrap( t ).asTupleValue( ) );
				if ( o == null )
					return last;
				return o;
			}
		};
	}

}
