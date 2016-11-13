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
package org.github.evenjn.guess.m12.core;

public class M12Core {

	public int number_of_states;

	public int number_of_symbols;

	public final double[] initial_table;

	public final double[][] transition_table;

	public final double[][] emission_table;

	public M12Core(int number_of_states, int number_of_symbols) {
		this.number_of_states = number_of_states;
		this.number_of_symbols = number_of_symbols;
		initial_table = new double[number_of_states];
		transition_table = new double[number_of_states][number_of_states];
		emission_table = new double[number_of_states][number_of_symbols];
	}

}
