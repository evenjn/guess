package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.yarn.Kloneable;

public class M12Schema<I, O> implements
		Kloneable,
		Cloneable {


	private Function<I, String> a_serializer;

	private Function<O, String> b_serializer;

	private Function<String, I> a_deserializer;

	private Function<String, O> b_deserializer;

	public Function<I, String> getAboveEncoder( ) {
		return a_serializer;
	}

	public Function<String, I> getAboveDecoder( ) {
		return a_deserializer;
	}

	public Function<O, String> getBelowEncoder( ) {
		return b_serializer;
	}

	public Function<String, O> getBelowDecoder( ) {
		return b_deserializer;
	}

	public M12Schema<I, O> setBelowCoDec(
			Function<O, String> encoder,
			Function<String, O> decoder ) {
		this.b_serializer = encoder;
		this.b_deserializer = decoder;
		return this;
	}

	public M12Schema<I, O> setAboveCoDec(
			Function<I, String> encoder,
			Function<String, I> decoder ) {
		this.a_serializer = encoder;
		this.a_deserializer = decoder;
		return this;
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

	protected Object clone( )
			throws CloneNotSupportedException {
		return super.clone( );
	}
}
