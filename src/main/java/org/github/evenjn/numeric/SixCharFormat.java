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

import java.text.DecimalFormat;
import java.util.function.Function;

/**
 * 
 * <pre>
 *     0  e 0
 * + 1.2  e-100  +100`12
 * + 1.23 e-100  +100`123
 * + 1.2  e-10   +10`12
 * + 1.23 e-10   +1`123
 * + 1.2  e-1    +1`12
 * + 1.23 e-1    +1`123
 * + 1.2  e 0    +1.2
 * + 1.23 e 0    +1.23
 * + 1.2  e 1    +1'12
 * + 1.23 e 1    +1'123
 * + 1.2  e 10   +10'12
 * + 1.23 e 10   +10'123
 * + 1.2  e 100  +100'12
 * + 1.23 e 100  +100'123
 * </pre>
 * 
 * 
 * 
 * <pre>
 *     0  e 0    0
 * + 1.2  e-100  +zz100
 * + 1.23 e-100  +zz100
 * + 1.2  e-10   +k.12
 * + 1.23 e-10   +k.123
 * + 1.2  e-1    +0.12
 * + 1.23 e-1    +0.123
 * + 1.2  e 0    +1.2
 * + 1.23 e 0    +1.23
 * + 1.2  e 1    +12
 * + 1.23 e 1    +12.3
 * + 1.2  e 10   +K.12
 * + 1.23 e 10   +K.123
 * + 1.2  e 100  +ZZ100
 * + 1.23 e 100  +ZZ100
 * </pre>
 * 
 */
public class SixCharFormat implements
		Function<Double, String> {

	private final static DecimalFormat expo = new DecimalFormat( "0.###E0" );

	private final static DecimalFormat e_zero = new DecimalFormat( "#.###" );

	private final static DecimalFormat e_pos_1 = new DecimalFormat( "00.##" );

	private final static DecimalFormat e_neg_1 = new DecimalFormat( "0.0##" );

	private final static DecimalFormat e_pos_2 = new DecimalFormat( "000.#" );

	private final static DecimalFormat e_pos_3 = new DecimalFormat( "0000" );

	private final static DecimalFormat e_pos_4 = new DecimalFormat( "00000" );

	private boolean print_signum;

	public SixCharFormat(boolean print_signum) {
		this.print_signum = print_signum;
		
	}
	
	private String format( Double t ) {
		// System.out.println("8CharNP input: " + Double.toString( t ));
		if ( t == 0.0 )
			return " 0    ";

		String prefix = "+";
		if ( t < 0 ) {
			prefix = "-";
			t = 0d - t;
		}
		if (!print_signum) {
			prefix = "";
		}
		String printed = expo.format( t );
		// System.out.println( "expo version = " + printed );
		int indexOf = printed.indexOf( 'E' );
		String mantissa = null;
		if ( indexOf > 1 ) {
			// System.out.println("indexOf = " + indexOf);
			mantissa = printed.substring( 0, 1 ) + printed.substring( 2, indexOf );
		}
		else {
			mantissa = printed.substring( 0, 1 );
		}
		// System.out.println("mantissa = " + mantissa);
		if ( mantissa.length( ) > 3 )
			mantissa = mantissa.substring( 0, 3 );

		String exponent = printed.substring( indexOf + 1 );
		int e = Integer.parseInt( exponent );
		if ( e == 0 ) {
			// System.out.println("using format e_zero");
			return prefix + e_zero.format( t );
		}
		if ( e == 1 ) {
			// System.out.println("using format e_pos_1");
			return prefix + e_pos_1.format( t );
		}
		if ( e == -1 ) {
			// System.out.println("using format e_neg_1");
			return prefix + e_neg_1.format( t );
		}
		if ( e == 2 ) {
			// System.out.println("using format e_pos_2");
			return prefix + e_pos_2.format( t );
		}
		if ( e == 3 ) {
			// System.out.println("using format e_pos_3");
			return prefix + e_pos_3.format( t );
		}
		if ( e == 4 ) {
			// System.out.println("using format e_pos_3");
			return prefix + e_pos_4.format( t );
		}

		String result = prefix + getExponentRankLetter( e, mantissa );
		// System.err.println(result);
		return result;
		// return expo.format( t );

	}

	@Override
	public String apply( Double t ) {
		String s;
		if (!Double.isFinite( t )) s = t.toString( );
		else s = format( t );
		while (s.length( ) < 6) { 
			s = s + " ";
		}
		return s;
	}

	public static String getExponentRankLetter( int exponent, String mantissa ) {

		// when exponent is zero, result is ''. when exponent is 3 -> C
		// 4d 5e 6f 7g 8h 9i 10j
		// 11k 12l 13m 14n 15o
		// 15p 16q 18r 19s 20t
		// 21u 22v 23w 24x 25y
		// rest is z
		if ( exponent > 26 ) {
			if ( exponent < 100 )
				return "ZZ0" + exponent;
		  return "ZZ" + exponent;
		}
		if ( exponent < -26 ) {
			if ( exponent > -100 )
				return "zz0" + ( -1 * exponent );
		  return "zz" + ( -1 * exponent );
		}
		if ( exponent < 0 ) {
			exponent = -1 * exponent;
			return "" + String.valueOf( Character.toChars( exponent + 96 ) ) + "'" + mantissa;
		}
		if ( exponent > 0 ) {
			return "" + String.valueOf( Character.toChars( exponent + 64 ) ) + "'" + mantissa;
		}
		throw new IllegalArgumentException( );
	}

}
