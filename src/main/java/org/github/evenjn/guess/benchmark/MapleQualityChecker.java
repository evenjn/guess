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

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.TrainingData;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Progress;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.numeric.SixCharFormat;

public class MapleQualityChecker<I, O> {

	private int previous_training = -1;
	private int previous_test = -1;

	private TrainingData<?, Tuple<I>, Tuple<O>> training_data;
	
	private int training_data_size = 0;

	private TrainingData<?, Tuple<I>, Tuple<O>> test_data;

	private int test_data_size = 0;
	private TupleAligner<I, O> aligner;
	private Function<I, String> a_printer;
	private Function<O, String> b_printer;

	public MapleQualityChecker(
			TrainingData<?, Tuple<I>, Tuple<O>> training_data,
			TrainingData<?, Tuple<I>, Tuple<O>> test_data ) {
		this.training_data = training_data;
		this.aligner = null;
		this.a_printer = null;
		this.b_printer = null;
		this.training_data_size = KnittingCursable.wrap( training_data.getData( ) ).count( );
		this.test_data = test_data;
		this.test_data_size = KnittingCursable.wrap( test_data.getData( ) ).count( );
	}

	public MapleQualityChecker(
			TrainingData<?, Tuple<I>, Tuple<O>> training_data,
			TrainingData<?, Tuple<I>, Tuple<O>> test_data,
			TupleAligner<I, O> aligner,
			Function<I, String> a_printer,
			Function<O, String> b_printer) {
		this.training_data = training_data;
		this.aligner = aligner;
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		this.training_data_size = KnittingCursable.wrap( training_data.getData( ) ).count( );
		this.test_data = test_data;
		this.test_data_size = KnittingCursable.wrap( test_data.getData( ) ).count( );
	}
	
	
	private <K> int do_check(
		  Function<Tuple<I>, Tuple<O>> maple,
			Consumer<String> logger,
			TrainingData<K, Tuple<I>, Tuple<O>> data,
			int previous,
			ProgressSpawner spawn,
			int target) {
		
		MapleEvaluation<I, O> evaluation = new MapleEvaluation<>( aligner, a_printer, b_printer );
		try ( BasicRook rook2 = new BasicRook( ) ) {
			Progress spawn2 = spawn.spawn( rook2, "check" ).target( target );
			
			for ( K g : KnittingCursable
					.wrap( data.getData( ) ).pull( rook2 ).peek( x->spawn2.step(1) ).once( ) ) {

				Tuple<O> guess = maple.apply( data.getInput( ).apply( g ) );
				evaluation.record( logger, data.getInput( ).apply( g ), data.getOutput( ).apply( g ), guess );
				
			}

			int total_distance = evaluation.getTotalDistance( );
			if ( previous != -1 ) {
				double change =
						(1.0 * total_distance) / (1.0 * previous);
				logger.accept( "  new/old: "
						+ SixCharFormat.nu( false ).apply( change ) );
			}
			
			evaluation.print( logger );
			return total_distance;
		}
	}

	private static final String decorator_line =
			">---------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "---------<";

	public boolean check(
			Consumer<String> logger,
			TupleAlignmentAlphabet<I, O> alphabet,
			Function<Tuple<I>, Tuple<O>> maple,
			ProgressSpawner spawn   ) {
		
		logger.accept( decorator_line );
		if ( test_data != null ) {
			logger.accept( "Maple check on test data" );
			previous_test = do_check( maple, logger, test_data, previous_test, spawn, test_data_size );
		}
		logger.accept( decorator_line );
		if ( training_data != null ) {
			logger.accept( "Maple check on training data");
			previous_training =
					do_check( maple, logger, training_data, previous_training, spawn, training_data_size );
		}
		logger.accept( decorator_line );
		return false;
	}
}
