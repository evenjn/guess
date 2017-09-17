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
package org.github.evenjn.guess.m12.maple;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.benchmark.MapleQualityChecker;
import org.github.evenjn.guess.m12.M12QualityChecker;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12PreciseMapleQualityChecker<I, O> implements
		M12QualityChecker<I, O> {

	private final MapleQualityChecker<I, O> mqc;

	public M12PreciseMapleQualityChecker(
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data,
			Cursable<Bi<Tuple<I>, Tuple<O>>> test_data,
			TupleAligner<I, O> aligner,
			Function<I, String> a_printer,
			Function<O, String> b_printer) {
		mqc = new MapleQualityChecker<>( training_data, test_data, aligner,
				a_printer, b_printer );
	}

	public M12PreciseMapleQualityChecker(
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data,
			Cursable<Bi<Tuple<I>, Tuple<O>>> test_data) {
		mqc = new MapleQualityChecker<>( training_data, test_data );
	}

	public boolean check(
			Consumer<String> logger,
			TupleAlignmentAlphabet<I, O> alphabet,
			Markov core,
			ProgressSpawner spawn ) {
		M12PreciseMaple<I, O> maple = new M12PreciseMaple<I, O>( alphabet, core );
		return mqc.check( logger, alphabet, maple, spawn );
	}
}
