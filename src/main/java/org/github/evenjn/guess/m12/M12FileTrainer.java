package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.file.FileFool;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public interface M12FileTrainer<I, O> {

	void train(
			ProgressSpawner progress_spawner,
			FileFool training_cache_path,
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data );

	public Function<String, I> getDeserializerAbove( );

	public Function<String, O> getDeserializerBelow( );
}
