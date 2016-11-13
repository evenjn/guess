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
package org.github.evenjn.numeric;

import java.util.function.BiFunction;

public class DenseCubix<T extends Number> implements
		Cubix<T> {

	private final T[][][] cubix;

	private final BiFunction<T, T, T> sum;

	private T fill;

	@SuppressWarnings("unchecked")
	public DenseCubix(int sizex, int sizey, int sizez, BiFunction<T, T, T> sum,
			T fill) {
		this.sum = sum;
		this.fill = fill;
		cubix = (T[][][]) new Number[sizex][sizey][sizez];
	}

	public void set( int x, int y, int z, T val ) {
		cubix[x][y][z] = val;
	}

	public void add( int x, int y, int z, T val ) {
		T t = cubix[x][y][z];
		if ( t == null )
			t = fill;
		cubix[x][y][z] = sum.apply( val, t );
	}

	public T get( int x, int y, int z ) {
		T t = cubix[x][y][z];
		return t == null ? fill : t;
	}
}
