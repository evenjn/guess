package org.github.evenjn.align.alphabet;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.Bi;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Hook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetSimpleBuilder<SymbolAbove, SymbolBelow>
		implements
		TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private int min_below;

	private int max_below;

	public TupleAlignmentAlphabetSimpleBuilder( ) {
	}

	@Override
	public void setMinMax( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
	}
	
	public void setPrinters(
			Function<Hook, Consumer<String>> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer ) {
		
	}

	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		KnittingCursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> kd =
				KnittingCursable.wrap( data );
		try ( AutoHook hook = new BasicAutoHook( ) ) {

			TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
					new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetSimpleBuilder::build" );

			spawn.info( "Computing dataset size." );
			spawn.target( kd.size( ) );
			spawn.info( "Collecting alphabet elements." );
			for ( Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : kd.pull( hook )
					.once( ) ) {

				spawn.step( 1 );

				try {
					Iterable<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> localAlphabet =
							TupleAlignmentAlphabetBuilderTools.localAlphabet(
									min_below, max_below, datum.front( ), datum.back( ) );
					for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pp : localAlphabet ) {
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
