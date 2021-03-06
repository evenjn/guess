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

import org.github.evenjn.guess.benchmark.generator.MapleAbsorbData;
import org.github.evenjn.guess.benchmark.generator.MapleAbsorbDuplicateData;
import org.github.evenjn.guess.benchmark.generator.MapleConstantData;
import org.github.evenjn.guess.benchmark.generator.MapleDelayByOneData;
import org.github.evenjn.guess.benchmark.generator.MapleDuplicateData;
import org.github.evenjn.guess.benchmark.generator.MapleIdentityData;
import org.github.evenjn.guess.benchmark.generator.MapleLycantropeData;
import org.github.evenjn.guess.benchmark.generator.MapleReverseData;
import org.github.evenjn.guess.benchmark.generator.MapleZebraData;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public class Benchmark {

	private static <X, Y>
			BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>>
			benchmarkProblem(
					String label,
					Cursable<BenchmarkDatum<Tuple<Boolean>, Tuple<Boolean>>> factory ) {
		return BenchmarkProblem.nu( KnittingCursable
				.wrap( factory ).head( 0, 100000 ) )
				.label( label )
				.inputPrinter( boolean_tuple_printer )
				.outputPrinter( x -> boolean_tuple_printer.apply( x ) )
				.build( );
	}

	public final static KnittingTuple<Boolean> boolean_alphabet =
			KnittingTuple.on( true, false );

	private final static Function<Tuple<Boolean>, String> boolean_tuple_printer =
			new Function<Tuple<Boolean>, String>( ) {

				@Override
				public String apply( Tuple<Boolean> t ) {
					StringBuilder sb = new StringBuilder( );
					for ( Boolean b : KnittingTuple.wrap( t ).asIterable( ) ) {
						sb.append( b ? "1" : "0" );
					}
					return sb.toString( );
				}
			};

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> identity =
			Benchmark.benchmarkProblem( "Identity",
					x -> new MapleIdentityData( ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> reverse =
			Benchmark.benchmarkProblem( "Reverse",
					x -> new MapleReverseData( ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> constant_true =
			Benchmark.benchmarkProblem( "Constant True",
					x -> new MapleConstantData( true, true ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> constant_true_false =
			Benchmark.benchmarkProblem( "Constant True-False",
					x -> new MapleConstantData( true, false ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> zebra =
			Benchmark.benchmarkProblem( "Zebra",
					x -> new MapleZebraData( ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> delay_by_one =
			Benchmark.benchmarkProblem( "Delay by One",
					x -> new MapleDelayByOneData( ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> lycantrope2 =
			Benchmark.benchmarkProblem( "Lycantrope Day 2",
					x -> new MapleLycantropeData( 2 ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> lycantrope3 =
			Benchmark.benchmarkProblem( "Lycantrope Day 3",
					x -> new MapleLycantropeData( 3 ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> absorb =
			Benchmark.benchmarkProblem( "Absorb",
					x -> new MapleAbsorbData( ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> duplicate =
			Benchmark.benchmarkProblem( "Duplicate",
					x -> new MapleDuplicateData( ) );

	public final static BenchmarkProblem<Tuple<Boolean>, Tuple<Boolean>> absorb_and_duplicate =
			Benchmark.benchmarkProblem( "Absorb and Duplicate",
					x -> new MapleAbsorbDuplicateData( ) );
}
