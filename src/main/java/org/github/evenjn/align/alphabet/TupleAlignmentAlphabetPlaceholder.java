package org.github.evenjn.align.alphabet;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public class TupleAlignmentAlphabetPlaceholder<Above, Below>
		implements
		TupleAlignmentAlphabetBuilder<Above, Below> {

	@Override
	public <K> TupleAlignmentAlphabet<Above, Below> build(
			Cursable<K> data,
			Function<K, Tuple<Above>> get_above,
			Function<K, Tuple<Below>> get_below,
			ProgressSpawner progress_spawner ) {
		throw new IllegalStateException( "Operation not supported." );
	}

	@Override
	public void setPrinters( Ring<Consumer<String>> logger,
			Function<Above, String> a_printer, Function<Below, String> b_printer ) {
		throw new IllegalStateException( "Operation not supported." );
	}

	@Override
	public void setMinMax( int min_above, int max_above, int min_below,
			int max_below ) {
		throw new IllegalStateException( "Operation not supported." );
	}

}
