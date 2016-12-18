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

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;

public class TupleAlignmentGraphDataManagerBlueprint<I, O> {
	
	private int min_above = 1;

	private int max_above = 1;

	private int min_below = 1;

	private int max_below = 1;

	private Function<Hook, Consumer<String>> putter_coalignment_graphs;

	private Cursable<String> reader_coalignment_graphs;

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			setMinMaxBelow( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			setMinMaxAbove( int min, int max ) {
		this.min_above = min;
		this.max_above = max;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			deserializeTupleAlignmentGraphs(
					Cursable<String> reader_coalignments ) {
		this.reader_coalignment_graphs = reader_coalignments;
		return this;
	}

	public TupleAlignmentGraphDataManagerBlueprint<I, O>
			serializeTupleAlignmentGraphs(
					Function<Hook, Consumer<String>> putter_coalignments ) {
		this.putter_coalignment_graphs = putter_coalignments;
		return this;
	}

	public TupleAlignmentGraphDataManager<I, O> create( ) {
		return new TupleAlignmentGraphDataManager<>(
				min_above,
				max_above,
				min_below,
				max_below,
				putter_coalignment_graphs,
				reader_coalignment_graphs );
	}
}
