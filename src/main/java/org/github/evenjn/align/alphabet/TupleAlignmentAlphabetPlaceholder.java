package org.github.evenjn.align.alphabet;

import java.util.function.Function;

import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.RookConsumer;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetPlaceholder<Above, Below>
		implements
		TupleAlignmentAlphabetBuilder<Above, Below> {

	@Override
	public TupleAlignmentAlphabet<Above, Below> build(
			Cursable<Bi<Tuple<Above>, Tuple<Below>>> data,
			ProgressSpawner progress_spawner ) {
		throw new IllegalStateException( "Operation not supported." );
	}

	@Override
	public void setPrinters( RookConsumer<String> logger,
			Function<Above, String> a_printer, Function<Below, String> b_printer ) {
		throw new IllegalStateException( "Operation not supported." );
	}

	@Override
	public void setMinMax( int min_above, int max_above, int min_below,
			int max_below ) {
		throw new IllegalStateException( "Operation not supported." );
	}

}
