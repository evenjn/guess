package org.github.evenjn.guess.m12;

import java.nio.file.Path;

import org.github.evenjn.align.TupleAligner;
import org.github.evenjn.guess.Libra;
import org.github.evenjn.guess.m12.aligner.M12AlignerFileDeserializer;
import org.github.evenjn.guess.m12.libra.M12LibraFileDeserializer;
import org.github.evenjn.guess.m12.maple.M12ClassicMapleFileDeserializer;
import org.github.evenjn.guess.m12.maple.M12PreciseMapleFileDeserializer;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Maple;
import org.github.evenjn.yarn.Tuple;

public class M12<I, O> {

	private Path path;

	private M12Schema<I, O> schema;

	M12(Path path, M12Schema<I, O> schema) {
		this.schema = schema.klone( schema );
		this.path = path;
	}

	public Libra<Bi<Tuple<I>, Tuple<O>>> asLibra( ) {
		return M12LibraFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
	}

	public Maple<I, O> asMapleClassic( ) {
		return M12ClassicMapleFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
	}

	public Maple<I, O> asMaplePrecise( ) {
		return M12PreciseMapleFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
	}

	public TupleAligner<I, O> asTupleAligner( ) {
		return M12AlignerFileDeserializer.deserialize(
				null,
				schema.getAboveDecoder( ),
				schema.getBelowDecoder( ),
				path );
	}
}
