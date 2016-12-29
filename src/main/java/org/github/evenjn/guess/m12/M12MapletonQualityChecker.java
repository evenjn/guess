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
package org.github.evenjn.guess.m12;

import java.util.function.Consumer;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class M12MapletonQualityChecker<I, O> implements
		M12FileTrainer.QualityChecker<I, O> {

	private final MapleQualityChecker<I, O> mqc;

	public M12MapletonQualityChecker(
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data,
			Cursable<Bi<Tuple<I>, Tuple<O>>> test_data) {
		mqc = new MapleQualityChecker<>( training_data, test_data,
				( alphabet, core ) -> new M12Mapleton<I, O>( alphabet, core ) );
	}

	public boolean check(
			Consumer<String> logger,
			TupleAlignmentAlphabet<I, O> alphabet,
			Markov core,
			ProgressSpawner spawn ) {
		return mqc.check( logger, alphabet, core, spawn );
	}
}
