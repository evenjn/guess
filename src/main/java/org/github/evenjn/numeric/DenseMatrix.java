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

import java.util.Iterator;
import java.util.function.BiFunction;

import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.yarn.Cursor;

public class DenseMatrix<T extends Number> implements
		Matrix<T> {

	private final T[][] matrix;

	private final int sizex;

	private final int sizey;

	private final BiFunction<T, T, T> sum;

	private T fill;

	@SuppressWarnings("unchecked")
	public DenseMatrix(int sizex, int sizey, BiFunction<T, T, T> sum, T fill) {
		this.sizex = sizex;
		this.sizey = sizey;
		this.sum = sum;
		this.fill = fill;
		matrix = (T[][]) new Number[sizex][sizey];
	}

	@SuppressWarnings("unchecked")
	public DenseMatrix(int sizex, int sizey, BiFunction<T, T, T> sum, T fill,
			Iterator<T> values) {
		this.sizex = sizex;
		this.sizey = sizey;
		this.sum = sum;
		this.fill = fill;
		matrix = (T[][]) new Number[sizex][sizey];
		set( values );
	}

	public void map( Integer row, Integer col, T val ) {
		matrix[row][col] = val;
	}

	public void add( Integer row, Integer col, T val ) {
		T t = matrix[row][col];
		if ( t == null )
			t = fill;
		matrix[row][col] = sum.apply( val, t );
	}

	public T apply( Integer row, Integer col ) {
		T t = matrix[row][col];
		return t == null ? fill : t;
	}

	public Iterable<T> walk( ) {
		return ( ) -> cursor( );
	}

	public Iterator<T> cursor( ) {

		return new Iterator<T>( ) {

			private int i = 0;

			@Override
			public T next( ) {
				if ( i == sizey * sizex ) {
					throw new IllegalStateException( );
				}
				i++;
				int j = i - 1;
				T t = matrix[j / sizey][j % sizey];
				return t == null ? fill : t;
			}

			@Override
			public boolean hasNext( ) {
				if ( i == sizey * sizex )
					return false;
				return true;
			}
		};
	}

	@Override
	public void set( Iterator<T> values ) {
		int i = 0;
		while ( values.hasNext( ) ) {
			T value = values.next( );
			matrix[i / sizey][i % sizey] = value;
			i++;
		}

	}

	public String toString( ) {
		Cursor<String> rows =
				KnittingCursor.wrap( NumericUtils.range( sizex ).pull( ) ).map(
						x -> "row " + x );
		Cursor<String> cols =
				KnittingCursor.wrap( NumericUtils.range( sizey ).pull( ) ).map(
						x -> "column " + x );

		String print =
				MatrixPrinter.printWithIterator(
						rows,
						cols,
						KnittingCursor.wrap( cursor( ) ).map( x -> x.doubleValue( ) ),
						" " );
		return print;
	}
}
