package org.github.evenjn.guess.m12;

import java.util.function.Consumer;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabet;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.lang.ProgressSpawner;

public interface M12QualityChecker<I, O> {

		boolean check(
				Consumer<String> logger,
				TupleAlignmentAlphabet<I, O> alphabet,
				Markov core,
				ProgressSpawner spawn );
	}