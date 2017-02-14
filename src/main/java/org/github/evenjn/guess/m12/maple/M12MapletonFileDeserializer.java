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
package org.github.evenjn.guess.m12.maple;

import java.nio.file.Path;
import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetDeserializer;
import org.github.evenjn.file.FileFool;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.guess.markov.MarkovChecker;
import org.github.evenjn.guess.markov.MarkovDeserializer;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.plaintext.PlainText;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12MapletonFileDeserializer {

	public static <I, O> M12Mapleton<I, O> deserialize(
			ProgressSpawner progress_spawner,
			Function<String, I> a_deserializer,
			Function<String, O> b_deserializer,
			Path training_cache_path ) {

		TupleAlignmentAlphabet<I, O> alphabet;
		Markov core;
		Path m12core_stable_file = training_cache_path.resolve( "./m12_core.stable.txt" );
		Path alphabet_stable_file = training_cache_path.resolve( "./ta_alphabet.stable.txt" );
		FileFool ff = FileFool.nu( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			/**
			 * This is interesting, because the output of the serializer is not
			 * volatile, but how can we communicate that?
			 */
			alphabet = KnittingCursable
					.wrap( h -> PlainText.reader( )
							.build( ).get( h, ff.open( alphabet_stable_file ).read( h ) ) )
					.pull( hook )
					.purlOptional( new TupleAlignmentAlphabetDeserializer<>(
							a_deserializer,
							b_deserializer ) )
					.one( );
		}
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			core = KnittingCursable
					.wrap( h -> PlainText.reader( )
							.build( ).get( h, ff.open( m12core_stable_file ).read( h ) ) )
					.pull( hook )
					.purlOptional( new MarkovDeserializer( ) )
					.one( );

			MarkovChecker.check( core );
		}
		return new M12Mapleton<>( alphabet, core );
	}
}
