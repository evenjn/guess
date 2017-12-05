package org.github.evenjn.guess.m12;

import java.nio.file.Path;
import java.util.function.Function;

import org.github.evenjn.align.AlignmentElement;
import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.guess.DiscriminativeModel;
import org.github.evenjn.guess.m12.aligner.M12Aligner;
import org.github.evenjn.guess.m12.aligner.M12AlignerFileDeserializer;
import org.github.evenjn.guess.m12.libra.M12Libra;
import org.github.evenjn.guess.m12.libra.M12LibraFileDeserializer;
import org.github.evenjn.guess.m12.maple.M12ClassicMaple;
import org.github.evenjn.guess.m12.maple.M12ClassicMapleFileDeserializer;
import org.github.evenjn.guess.m12.maple.M12PreciseMaple;
import org.github.evenjn.guess.m12.maple.M12PreciseMapleFileDeserializer;
import org.github.evenjn.lang.Tuple;

public class M12<I, P, O> {

	private Path path;

	private M12Schema<I, P, O> schema;

	M12(Path path, M12Schema<I, P, O> schema) {
		this.schema = schema.klone( schema );
		this.path = path;
	}

	public DiscriminativeModel<I, Tuple<O>> asLibra( ) {
		M12Libra<P, O> local = M12LibraFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new DiscriminativeModel<I, Tuple<O>>( ) {

			@Override
			public double weigh( I input, Tuple<O> output ) {
				return local.weigh( schema.getProjector( ).apply( input ), output );
			}
		};
	}

	public Function<I, Tuple<O>> asMapleClassic( ) {
		M12ClassicMaple<P, O> local = M12ClassicMapleFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new Function<I, Tuple<O>>( ) {

			@Override
			public Tuple<O> apply( I input ) {
				return local.apply( schema.getProjector( ).apply( input ) );
			}
		};
	}

	public Function<I, Tuple<O>> asMaplePrecise( ) {
		M12PreciseMaple<P, O> local = M12PreciseMapleFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new Function<I, Tuple<O>>( ) {

			@Override
			public Tuple<O> apply( I input ) {
				return local.apply( schema.getProjector( ).apply( input ) );
			}
		};
	}

	public TupleAligner<P, O> asTupleAligner( ) {
		M12Aligner<P, O> local = M12AlignerFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new TupleAligner<P, O>( ) {

			@Override
			public Tuple<AlignmentElement<Integer, Integer>> align( Tuple<P> a, Tuple<O> b ) {
				return local.align( a, b );
			}
		};
	}
}
