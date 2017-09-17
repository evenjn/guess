package org.github.evenjn.guess.m12;

import java.nio.file.Path;

import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.guess.Libra;
import org.github.evenjn.guess.m12.aligner.M12Aligner;
import org.github.evenjn.guess.m12.aligner.M12AlignerFileDeserializer;
import org.github.evenjn.guess.m12.libra.M12Libra;
import org.github.evenjn.guess.m12.libra.M12LibraFileDeserializer;
import org.github.evenjn.guess.m12.maple.M12ClassicMaple;
import org.github.evenjn.guess.m12.maple.M12ClassicMapleFileDeserializer;
import org.github.evenjn.guess.m12.maple.M12PreciseMaple;
import org.github.evenjn.guess.m12.maple.M12PreciseMapleFileDeserializer;
import org.github.evenjn.knit.BiTray;
import org.github.evenjn.knit.BiValue;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Maple;
import org.github.evenjn.yarn.Tuple;

public class M12<I, P, O> {

	private Path path;

	private M12Schema<I, P, O> schema;

	M12(Path path, M12Schema<I, P, O> schema) {
		this.schema = schema.klone( schema );
		this.path = path;
	}

	public Libra<Bi<Tuple<I>, Tuple<O>>> asLibra( ) {
		M12Libra<P, O> local = M12LibraFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new Libra<Bi<Tuple<I>, Tuple<O>>>( ) {

			@Override
			public double weigh( Bi<Tuple<I>, Tuple<O>> t ) {
				return local.weigh(
						BiTray.nu( schema.getProjector( ).apply( t.front( ) ), t.back( ) ) );
			}
		};
	}

	public Maple<I, O> asMapleClassic( ) {
		M12ClassicMaple<P, O> local = M12ClassicMapleFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new Maple<I, O>( ) {

			@Override
			public Tuple<O> apply( Tuple<I> t ) {
				return local.apply( schema.getProjector( ).apply( t ) );
			}
		};
	}

	public Maple<I, O> asMaplePrecise( ) {
		M12PreciseMaple<P, O> local = M12PreciseMapleFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new Maple<I, O>( ) {

			@Override
			public Tuple<O> apply( Tuple<I> t ) {
				return local.apply( schema.getProjector( ).apply( t ) );
			}
		};
	}

	public TupleAligner<I, O> asTupleAligner( ) {
		M12Aligner<P, O> local = M12AlignerFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
		return new TupleAligner<I, O>( ) {

			@Override
			public Tuple<BiValue<Integer, Integer>> align( Tuple<I> a, Tuple<O> b ) {
				return local.align( schema.getProjector( ).apply( a ), b );
			}

		};
	}
}
