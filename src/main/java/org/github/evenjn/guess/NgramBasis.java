/**
 *
 * Copyright 2016 Marco Trevisan
 * 
 * All rights reserved. 
 * 
 */
package org.github.evenjn.guess;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.lang.Tuple;

public class NgramBasis<T> implements
		Function<Tuple<T>, Tuple<NGram<T>>> {

	private Function<? super T, String> serializer;

	private Function<String, T> deserializer;

	private int window;

	private int pivot;

	private Function<? super T, String> printer;

	public NgramBasis(
			int window,
			int pivot,
			Function<? super T, String> encoder,
			Function<String, T> decoder,
			Function<? super T, String> printer) {
		this.window = window;
		this.pivot = pivot;
		this.printer = printer;
		this.serializer = encoder;
		this.deserializer = decoder;
	}

	public String print( NGram<T> t ) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "<" );
		for ( int i = 0; i < window; i++ ) {
			sb.append( " " );
			T t2 = t.arr.get( i );
			if ( t2 == null ) {
				sb.append( "_" );
			}
			else {
				sb.append( printer.apply( t2 ) );
			}
		}
		sb.append( " >" );
		return sb.toString( );
	}

	public NGram<T> deserialize( String s ) {
		String[] split = s.split( "-" );
		List<T> ser = new ArrayList<>( );
		for ( String it : split ) {
			if ( it.equals( "null" ) ) {
				ser.add( null );
			}
			else {
				ser.add( deserializer.apply( it ) );
			}
		}
		return new NGram<T>( ser, pivot );
	}

	public String serialize( NGram<T> g ) {
		StringBuilder sb = new StringBuilder( );
		boolean first = true;
		for ( T o : g.arr ) {
			if ( first ) {
				first = false;
			}
			else {
				sb.append( "-" );
			}
			if ( o != null ) {
				sb.append( serializer.apply( o ) );
			}
			else {
				sb.append( "null" );
			}
		}
		if ( !deserialize( sb.toString( ) ).equals( g ) ) {
			deserialize( sb.toString( ) );
			throw new IllegalStateException( "hmmmmmmm" );
		}
		return sb.toString( );
	}

	@Override
	public Tuple<NGram<T>> apply( Tuple<T> t ) {
		Vector<NGram<T>> result = new Vector<>( );
		for ( int i = 0; i < t.size( ); i++ ) {
			ArrayList<T> list = new ArrayList<>( );
			for ( int z = i - pivot; z < i; z++ ) {
				if ( z < 0 ) {
					list.add( null );
				}
				else {
					list.add( t.get( z ) );
				}
			}
			list.add( t.get( i ) );
			for ( int z = i + 1; z < i + ( window - pivot ); z++ ) {
				if ( z >= t.size( ) ) {
					list.add( null );
				}
				else {
					list.add( t.get( z ) );
				}
			}
			result.add( new NGram<T>( list, pivot ) );
		}
		return KnittingTuple.wrap( result );
	}

}
