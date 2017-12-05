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
package org.github.evenjn.align;

import org.github.evenjn.lang.Tuple;

/**
 * Given two sequences Alpha and Beta, returns a sequence of alignment data.
 * 
 */
public interface TupleAligner<A, B> {

	/**
	 * Returns an alignment.
	 * 
	 * @param a
	 *          a tuple
	 * @param b
	 *          another tuple
	 * @return a tuple of pairs. each pair contains the length of a subtuple above
	 *         and the length of the corresponding subtuple below.
	 */
	Tuple<AlignmentElement<Integer, Integer>> align( Tuple<A> a, Tuple<B> b );

}
