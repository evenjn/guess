/**
 *
 * Copyright 2016 Marco Trevisan
 * 
 * All rights reserved. 
 * 
 */
package org.github.evenjn.guess;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.yarn.Tuple;

public class NGram<T> implements Tuple<T> {

	final Vector<T> arr;

	public T pivot( ) {
		return arr.get( pivot );
	}
	
	public NGram<T> head( int skip, int length) {
		return new NGram<>( KnittingTuple.wrap( arr ).head(skip, length), pivot + skip );
	}

	public NGram<T> tail( int skip, int length) {
		return new NGram<>( KnittingTuple.wrap( arr ).tail(skip, length), pivot - skip );
	}

	public NGram(Tuple<T> elements, int pivot) {
		this.pivot = pivot;
		boolean found_non_null_element = false;
		arr = new Vector<T>( elements.size( ) );
		for ( int i = 0; i < elements.size( ); i++ ) {
			T o = elements.get( i );
			if ( o == null ) {
				if ( found_non_null_element ) {
					holes_after++;
				}
				else {
					holes_before++;
				}
			}
			else {
				found_non_null_element = true;
			}
			arr.add( o );
		}
		if ( !found_non_null_element ) {
			throw new IllegalArgumentException( );
		}
	}
	
	public NGram(List<? extends T> elements, int pivot) {
		this.pivot = pivot;
		boolean found_non_null_element = false;
		arr = new Vector<T>( elements.size( ) );
		for ( T o : elements ) {
			if ( o == null ) {
				if ( found_non_null_element ) {
					holes_after++;
				}
				else {
					holes_before++;
				}
			}
			else {
				found_non_null_element = true;
			}
			arr.add( o );
		}
		if ( !found_non_null_element ) {
			throw new IllegalArgumentException( );
		}
	}

	private int holes_before;

	int pivot;

	private int holes_after;

	public boolean equals( Object other ) {
		if ( other == this ) {
			return true;
		}
		if ( !( other instanceof NGram ) ) {
			return false;
		}
		NGram<?> o = (NGram<?>) other;

		if ( o.holes_before != holes_before ) {
			return false;
		}
		if ( o.holes_after != holes_after ) {
			return false;
		}
		if ( o.pivot != pivot ) {
			return false;
		}

		Iterator<T> iterator = arr.iterator( );
		Iterator<?> o_iterator = o.arr.iterator( );

		while ( iterator.hasNext( ) && o_iterator.hasNext( ) ) {
			Object o1 = iterator.next( );
			Object o2 = o_iterator.next( );
			if ( !( o1 == null ? o2 == null : o1.equals( o2 ) ) )
				return false;
		}
		return !( iterator.hasNext( ) || o_iterator.hasNext( ) );
	}

	public String print( ) {
		StringBuilder sb = new StringBuilder( );
		for ( T t : arr ) {
			if ( t == null ) {
				sb.append( "_" );
			}
			else if ( t instanceof Integer ) {
				sb.append( Character.toChars( (Integer) t ) );
			}
		}
		return sb.toString( );
	}

	public int hashCode( ) {
		Iterator<T> iterator = arr.iterator( );
		int hashCode = 1;
		while ( iterator.hasNext( ) ) {
			Object e = iterator.next( );
			hashCode = 31 * hashCode + ( e == null ? 0 : e.hashCode( ) );
		}
		hashCode = 31 * hashCode + holes_before;
		hashCode = 31 * hashCode + holes_after;
		hashCode = 31 * hashCode + pivot;
		return hashCode;
	}

	@Override
	public T get( int index ) {
		return arr.get( index );
	}

	@Override
	public int size( ) {
		return arr.size( );
	}

//	private int distance( NGram<T> o ) {
//		if ( o.holes_before != holes_before ) {
//			return 999;
//		}
//		if ( o.holes_after != holes_after ) {
//			return 999;
//		}
//		if (arr.size( ) != o.arr.size( )) {
//			return 99999;
//		}
//		if ( arr.get( pivot ).equals( o.arr.get( o.pivot ) ) ) {
//			boolean prev_equals = true;
//			if ( pivot > 0 && arr.get( pivot - 1 ) != null
//					&& o.arr.get( pivot - 1 ) != null
//					&& !arr.get( pivot - 1 ).equals( o.arr.get( o.pivot - 1 ) ) ) {
//				prev_equals = false;
//			}
//			boolean next_equals = true;
//			if ( pivot + 1 < arr.size( ) && arr.get( pivot + 1 ) != null
//					&& o.arr.get( pivot + 1 ) != null
//					&& !arr.get( pivot + 1 ).equals( o.arr.get( o.pivot + 1 ) ) ) {
//				next_equals = false;
//			}
//			if ( next_equals && prev_equals ) {
//				return wrap( arr ).distance( wrap( o.arr ) );
//			}
//			if ( next_equals || prev_equals ) {
//				return 100 + wrap( arr ).distance( wrap( o.arr ) );
//			}
//			return 1000 + wrap( arr ).distance( wrap( o.arr ) );
//		}
//		return 9999;
//	}
//
//	private KnittingTuple<T> wrap( Vector<T> arr2 ) {
//		return KnittingTuple.wrap( arr2 );
//	}
}
