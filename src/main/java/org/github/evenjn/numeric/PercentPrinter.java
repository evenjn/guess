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

public class PercentPrinter implements
		Function<Float, String> {

	private final DecimalFormat form;

	public PercentPrinter(
			int digits) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "0" );
		if ( digits > 0 )
			sb.append( "." );
		for ( int i = 0; i < digits; i++ ) {
			sb.append( "#" );
		}
		sb.append( "%" );
		form = new DecimalFormat( sb.toString( ) );
	}

	@Override
	public String apply(
			Float object ) {
		if ( object == null )
			throw new IllegalArgumentException( );
		return form.format( object );
	}

	public static String printRatioAsPercent( int digits, long fraction,
			long total ) {
		double ratio = fraction;
		double tot = total;
		ratio = ratio / tot;
		return new PercentPrinter( digits ).form.format( ratio );
	}
}
