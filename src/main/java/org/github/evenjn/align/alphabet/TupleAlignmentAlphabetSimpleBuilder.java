package org.github.evenjn.align.alphabet;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.Tael;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetSimpleBuilder<Above, Below>
		implements
		TupleAlignmentAlphabetBuilder<Above, Below> {

	private int min_above;

	private int max_above;

	private int min_below;

	private int max_below;

	public TupleAlignmentAlphabetSimpleBuilder( ) {
	}

	@Override
	public void setMinMax( 
			int min_above, int max_above,
			int min_below, int max_below ) {
		this.min_above = min_above;
		this.max_above = max_above;
		this.min_below = min_below;
		this.max_below = max_below;
	}

	@Override
	public void setPrinters(
			Function<Hook, Consumer<String>> logger,
			Function<Above, String> a_printer,
			Function<Below, String> b_printer ) {
		
	}

	@Override
	public TupleAlignmentAlphabet<Above, Below> build(
			Cursable<Bi<Tuple<Above>, Tuple<Below>>> data,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<Bi<Tuple<Above>, Tuple<Below>>> kd =
				KnittingCursable.wrap( data );
		try ( AutoHook hook = new BasicAutoHook( ) ) {

			TupleAlignmentAlphabet<Above, Below> result =
					new TupleAlignmentAlphabet<Above, Below>( );
			Progress spawn = SafeProgressSpawner.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetSimpleBuilder::build" );

			spawn.info( "Computing dataset size." );
			spawn.target( kd.size( ) );
			spawn.info( "Collecting alphabet elements." );
			for ( Bi<Tuple<Above>, Tuple<Below>> datum : kd.pull( hook )
					.once( ) ) {

				spawn.step( 1 );

				try {
					Iterable<Tael<Above, Below>> localAlphabet =
							TupleAlignmentAlphabetBuilderTools.localAlphabet(
									min_above, max_above,
									min_below, max_below,
									datum.front( ), datum.back( ) );
					for ( Tael<Above, Below> pp : localAlphabet ) {
						result.add( pp );
					}
				}
				catch ( NotAlignableException e ) {
					// simply ignore them.
				}
			}
			return result;
		}
	}
}
