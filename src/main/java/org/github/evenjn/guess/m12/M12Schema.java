package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.yarn.Kloneable;
import org.github.evenjn.yarn.Tuple;

public class M12Schema<I, P, O> implements
		Kloneable,
		Cloneable {

	protected Function<I, Tuple<P>> projector;

	protected Function<P, String> a_serializer;

	protected Function<O, String> b_serializer;

	protected Function<String, P> a_deserializer;

	protected Function<String, O> b_deserializer;

	public Object clone( )
			throws CloneNotSupportedException {
		return super.clone( );
	}

	public Function<String, P> getAboveDecoder( ) {
		return a_deserializer;
	}

	public Function<P, String> getAboveEncoder( ) {
		return a_serializer;
	}

	public Function<String, O> getBelowDecoder( ) {
		return b_deserializer;
	}

	public Function<O, String> getBelowEncoder( ) {
		return b_serializer;
	}

	public Function<I, Tuple<P>> getProjector( ) {
		return projector;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K extends Kloneable> K klone( K kloneable )
			throws IllegalArgumentException {
		if ( this != kloneable ) {
			throw new IllegalArgumentException( );
		}
		try {
			return (K) clone( );
		}
		catch ( CloneNotSupportedException e ) {
			throw new RuntimeException( e );
		}
	}

	public M12Schema<I, P, O> setAboveCoDec(
			Function<P, String> encoder,
			Function<String, P> decoder ) {
		this.a_serializer = encoder;
		this.a_deserializer = decoder;
		return this;
	}

	public M12Schema<I, P, O> setBelowCoDec(
			Function<O, String> encoder,
			Function<String, O> decoder ) {
		this.b_serializer = encoder;
		this.b_deserializer = decoder;
		return this;
	}

	public M12Schema<I, P, O> setProjector( Function<I, Tuple<P>> projector ) {
		this.projector = projector;
		return this;
	}
}
