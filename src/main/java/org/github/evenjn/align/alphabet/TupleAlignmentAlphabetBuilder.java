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

import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public interface TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	<K> TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<K> data,
			Function<K, Tuple<SymbolAbove>> get_above,
			Function<K, Tuple<SymbolBelow>> get_below,
			ProgressSpawner progress_spawner );

	void setPrinters(
			Ring<Consumer<String>> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer );

	void setMinMax( int min_above, int max_above, int min_below, int max_below );
}
