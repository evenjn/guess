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
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Progress;

public class CheatingMapleTrainer<I, O> implements
		Trainer<I, O> {

	public CheatingMapleTrainer(O last) {
		this.last = last;
	}

	private final O last;

	@Override
	public Function<I, O> train(
			Progress progress,
			Cursable<Di<I, O>> data ) {
		HashMap<I, O> cheat_sheet = new HashMap<>( );

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			for ( Di<I, O> td : KnittingCursable.wrap( data ).pull( hook ).once( ) ) {
				cheat_sheet.put( td.front( ), td.back( ) );
			}
		}
		return new Function<I, O>( ) {

			@Override
			public O apply(
					I t ) {
				O o = cheat_sheet.get( t );
				if ( o == null )
					return last;
				return o;
			}
		};
	}

}
