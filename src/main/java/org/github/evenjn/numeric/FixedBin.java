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

/**
 * Carries the legacy histogram density estimation method.
 */
public class FixedBin {

	private double size;

	public FixedBin(double size) {
		this.size = size;
	}

	public double bin( double d ) {
		return Math.floor( d / size ) * size;
	}

	public static void main( String[] args ) {
		FixedBin fb = new FixedBin( 0.2 );

		System.err.println( fb.bin( 0.22 ) );
		System.err.println( fb.bin( 0.24 ) );
		System.err.println( fb.bin( 0.27 ) );
		System.err.println( fb.bin( 0.31 ) );
		System.err.println( fb.bin( 0.33 ) );
		System.err.println( fb.bin( 0.35 ) );
		System.err.println( fb.bin( 0.37 ) );
		System.err.println( fb.bin( 0.39 ) );
		System.err.println( fb.bin( 0.40 ) );
		System.err.println( fb.bin( 0.41 ) );

		System.err.println( fb.bin( -0.22 ) );
		System.err.println( fb.bin( -0.24 ) );
		System.err.println( fb.bin( -0.27 ) );
		System.err.println( fb.bin( -0.31 ) );
		System.err.println( fb.bin( -0.33 ) );
		System.err.println( fb.bin( -0.35 ) );
		System.err.println( fb.bin( -0.37 ) );
		System.err.println( fb.bin( -0.39 ) );
		System.err.println( fb.bin( -0.40 ) );
		System.err.println( fb.bin( -0.41 ) );
	}
}
