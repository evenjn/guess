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


public class ArrayVector {
	
	public static void subtract(double[] result, double[] a, double[] b) {
		int len = result.length;
		for (int i = 0; i < len; i++) {
			result[i] = a[i] - b[i];
		}
	}
	
	public static void clip(double[] result, double[] a, double treshold) {
		int len = result.length;
		for (int i = 0; i < len; i++) {
			if (Double.isNaN(a[i])) {
				result[i] = 0;
				continue;
			}
			if (a[i] > treshold) {
//				result[i] = treshold;
				result[i] = 1;
				continue;
			}
			if (a[i] < - treshold) {
//				result[i] = - treshold;
				result[i] = -1;
				continue;
			}
			result[i] = a[i];
		}
	}
	
	public static void sum(double[] result, double[] a, double[] b) {
		int len = result.length;
		for (int i = 0; i < len; i++) {
			result[i] = a[i] + b[i];
		}
	}
	
	public static double dot_product(double[] a, double[] b) {
		int len = a.length;
		double result = 0d;
		for (int i = 0; i < len; i++) {
			result = result + a[i] * b[i]; 
		}
		return result;
	}
	
	public static void scalar_product(double[] result, double scalar, double[] vector) {
		int len = result.length;
		for (int i = 0; i < len; i++) {
			result[i] = vector[i] * scalar; 
		}
	}

	public static void erase( double[] vector ) {
		int len = vector.length;
		for (int i = 0; i < len; i++) {
			vector[i] = 0.0; 
		}
	}
	
	public static String print(double[] vector) {
		StringBuilder sb = new StringBuilder( );
		int len = vector.length;
		String separator = "";
		for (int i = 0; i < len; i++) {
			sb.append(separator).append( vector[i] );
			separator = " ";
		}
		return sb.toString( );
	}
}
