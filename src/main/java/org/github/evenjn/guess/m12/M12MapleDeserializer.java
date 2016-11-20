package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetDeserializer;
import org.github.evenjn.guess.m12.core.M12Core;
import org.github.evenjn.guess.m12.core.M12CoreChecker;
import org.github.evenjn.guess.m12.core.M12CoreDeserializer;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12MapleDeserializer {

	public static <I, O> M12Maple<I, O> deserialize(
			ProgressSpawner progress_spawner,
			Function<String, I> a_deserializer,
			Function<String, O> b_deserializer,
			Cursable<String> reader_alphabet,
			Cursable<String> reader_core,
			boolean check_consistency ) {

		TupleAlignmentAlphabet<I, O> alphabet;
		M12Core core;
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			/**
			 * This is interesting, because the output of the serializer is not
			 * volatile, but how can we communicate that?
			 */
			alphabet = KnittingCursable
					.wrap( reader_alphabet ).pull( hook )
					.skipfold( new TupleAlignmentAlphabetDeserializer<>(
							a_deserializer,
							b_deserializer ) )
					.one( );
		}
		try ( AutoHook hook2 = new BasicAutoHook( ) ) {
			core = KnittingCursable
					.wrap( reader_core )
					.pull( hook2 )
					.skipfold( new M12CoreDeserializer( ) )
					.one( );

			if ( check_consistency ) {
				M12CoreChecker.check( core );
			}
		}
		return new M12Maple<>( alphabet, core, progress_spawner );
	}
}
