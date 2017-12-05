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

public class BenchmarkDatum<I, O> {

	public I front( ) {
		return getInput( );
	}

	public O back( ) {
		return getGold( );
	}

	public I observed;

	public O bad_teacher;

	public O good_teacher;

	public BenchmarkDatum() {

	}

	public BenchmarkDatum(I observed, O good_teacher, O bad_teacher) {
		this.observed = observed;
		this.good_teacher = good_teacher;
		this.bad_teacher = bad_teacher;
	}

	public I getInput( ) {
		return observed;
	}

	public O getGold( ) {
		return good_teacher;
	}

	public O getGoodTeacherGold( ) {
		return good_teacher;
	}

	public O getBadTeacherGold( ) {
		return bad_teacher;
	}
}
