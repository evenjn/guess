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

import java.util.function.Function;

import org.github.evenjn.guess.TrainingDatum;

public class BenchmarkDatum<I, O> implements
		TrainingDatum<I, O> {

	public static <K, I, O> TrainingDatum<I, O> wrap( K k,
			Function<K, I> observer, Function<K, O> teacher ) {
		I input = observer.apply( k );
		O gold = teacher.apply( k );
		return new TrainingDatum<I, O>( ) {

			@Override
			public I getInput( ) {
				return input;
			}

			@Override
			public O getGold( ) {
				return gold;
			}
		};
	}

	public I observed;

	public O bad_teacher;

	public O good_teacher;

	public BenchmarkDatum() {

	}

	public TrainingDatum<I, O> asBadTeacherWouldTell( ) {
		return new TrainingDatum<I, O>( ) {

			@Override
			public I getInput( ) {
				return observed;
			}

			@Override
			public O getGold( ) {
				return bad_teacher;
			}

		};
	}

	public BenchmarkDatum(I observed, O good_teacher, O bad_teacher) {
		this.observed = observed;
		this.good_teacher = good_teacher;
		this.bad_teacher = bad_teacher;
	}

	@Override
	public I getInput( ) {
		return observed;
	}

	@Override
	public O getGold( ) {
		return good_teacher;
	}

}
