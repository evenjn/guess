package org.github.evenjn.align.alphabet;

import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.Tael;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Ring;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public class TupleAlignmentAlphabetBuilderAscendantWrapper<SymbolAbove, SymbolBelow> implements TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow>{

	private TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> wrapped;
	private Function<Tuple<SymbolAbove>, Iterable<Tuple<SymbolAbove>>> ascendants_provider;

	private TupleAlignmentAlphabetBuilderAscendantWrapper(
			TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> wrapped,
			Function<Tuple<SymbolAbove>, Iterable<Tuple<SymbolAbove>>> ascendants_provider) {
		this.wrapped = wrapped;
		this.ascendants_provider = ascendants_provider;
	}

	public static <SymbolAbove, SymbolBelow>
			TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow>
			wrap( TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> builder,
					Function<Tuple<SymbolAbove>, Iterable<Tuple<SymbolAbove>>> demux ) {
		return new TupleAlignmentAlphabetBuilderAscendantWrapper<SymbolAbove, SymbolBelow>(
				builder, demux );

	}
	
	@Override
	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build = wrapped.build( data, progress_spawner );
		
		TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result = new TupleAlignmentAlphabet<>( );
		for (int z = 0; z < build.size( ); z++) {
			Tael<SymbolAbove, SymbolBelow> pp =
					build.get( z );
			Iterable<Tuple<SymbolAbove>> ascendants = ascendants_provider.apply( pp.getAbove( ) );
			for (Tuple<SymbolAbove> ascendant : ascendants) {
				Tael<SymbolAbove, SymbolBelow> nu =
						new Tael<>( KnittingTuple.wrap( ascendant ).asTupleValue( ), pp.getBelow( ) );
				result.add( nu );
			}
		}
		return result;
	}

	@Override
	public void setPrinters( Ring<Consumer<String>> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer ) {
		wrapped.setPrinters( logger, a_printer, b_printer );
	}

	@Override
	public void setMinMax( int min_above, int max_above, int min_below,
			int max_below ) {
		wrapped.setMinMax( min_above, max_above, min_below, max_below );
	}

}
