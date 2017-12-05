package org.github.evenjn.guess;

import java.util.function.Function;

import org.github.evenjn.yarn.Cursable;

public interface TrainingData<K, I, O> {

	Cursable<K> getData( );

	Function<K, I> getInput( );

	Function<K, O> getOutput( );

	static <K, I, O> TrainingData<K, I, O> basic( Cursable<K> data,
			Function<K, I> input, Function<K, O> output ) {
		return new TrainingData<K, I, O>( ) {

			@Override
			public Cursable<K> getData( ) {
				return data;
			}

			@Override
			public Function<K, I> getInput( ) {
				return input;
			}

			@Override
			public Function<K, O> getOutput( ) {
				return output;
			}
		};
	}
}
