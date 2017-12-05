package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.file.FileFool;
import org.github.evenjn.guess.TrainingData;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;

public interface M12FileTrainer<I, O> {

	<K> void train(
			ProgressSpawner progress_spawner,
			FileFool training_cache_path,
			TrainingData<K, Tuple<I>, Tuple<O>> training_data );

	public Function<String, I> getDeserializerAbove( );

	public Function<String, O> getDeserializerBelow( );
}
