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

import java.util.function.Consumer;

import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.Tuple;

public class MapleEvaluation<I, O> {

	private TupleAligner<I, O> aligner;
	public MapleEvaluation(TupleAligner<I, O> aligner) {
		this.aligner = aligner;
	}
	/**
	 * move this to benchmark
	 */
	public void record(
			Consumer<String> logger,
			Tuple<I> input,
			Tuple<O> gold,
			Tuple<O> guess) {
		KnittingTuple<O> kguess = KnittingTuple.wrap( guess );
		KnittingTuple<O> kgold = KnittingTuple.wrap( gold );
		KnittingTuple<I> kinput = KnittingTuple.wrap( input );
		
		Tuple<Di<Integer, Integer>> aligned_gold = aligner.align( kinput, kgold );
		
		Tuple<Di<Integer, Integer>> aligned_guess = aligner.align( kinput, kguess );
		/**
		 * we should analyze the diff of the sequence of alignment pairs.
		 * T/t A/ae X/ks I/i
		 * T/t A/a X/ks I/i
		 */
		
		
		
		for (Bi<O, O> di : kgold.diff( kguess )) {
			if (di.first == null || di.second == null) {
				
			}
		}
	}
}
