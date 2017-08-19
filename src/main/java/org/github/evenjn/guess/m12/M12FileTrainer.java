package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.file.FileFool;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public interface M12FileTrainer<I, O> {

	void train(
			ProgressSpawner progress_spawner,
			FileFool training_cache_path,
			Cursable<Bi<Tuple<I>, Tuple<O>>> training_data );

	public Function<String, I> getDeserializerAbove( );

	public Function<String, O> getDeserializerBelow( );
}
