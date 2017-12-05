package org.github.evenjn.align.alphabet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.github.evenjn.align.Tael;
import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentGraphFactory;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.lang.BasicRook;
import org.github.evenjn.lang.Progress;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.yarn.Cursable;

public class TupleAlignmentAlphabetBuilderTools {

	public TupleAlignmentAlphabetBuilderTools() {
		
	}
	
	public static <SymbolBelow> String tuple_printer(
			Function<SymbolBelow, String> b_printer, Tuple<SymbolBelow> tuple ) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "[" );
		for ( int i = 0; i < tuple.size( ); i++ ) {
			sb.append( " " ).append( b_printer.apply( tuple.get( i ) ) );
		}
		sb.append( " ]" );
		return sb.toString( );
	}

	public static <K, SymbolAbove, SymbolBelow> int computeMinMaxAligneable(
			ProgressSpawner progress_spawner,
			Consumer<String> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer,
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			Cursable<K> data,
			Function<K, Tuple<SymbolAbove>> get_above,
			Function<K, Tuple<SymbolBelow>> get_below,
			int total ) {
		try ( BasicRook rook = new BasicRook() ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner,
					"TupleAlignmentAlphabetBuilderTools::computeMinMaxCoverage" )
					.target( total );
			int not_aligneable = 0;
			for ( K datum : KnittingCursable
					.wrap( data ).pull( rook ).once( ) ) {
				spawn.step( 1 );
				try {
					TupleAlignmentAlphabetBuilderTools.attemptToAlingn(
							min_above,
							max_above,
							min_below,
							max_below,
							get_above.apply( datum ),
							get_below.apply( datum ),
							x -> true );
				}
				catch ( NotAlignableException e ) {
					if ( logger != null ) {
						StringBuilder sb = new StringBuilder( );
						sb.append( "Not aligneable using min = " ).append( min_below )
								.append( " max = " )
								.append( max_below ).append( ":" );
						for ( SymbolAbove a : KnittingTuple.wrap( get_above.apply( datum ) )
								.asIterable( ) ) {
							sb.append( " " ).append( a_printer.apply( a ) );
						}
						sb.append( " ----" );
						for ( SymbolBelow b : KnittingTuple.wrap( get_below.apply( datum ) )
								.asIterable( ) ) {
							sb.append( " " ).append( b_printer.apply( b ) );
						}
						logger.accept( sb.toString( ) );
					}
					not_aligneable++;
				}
			}
			if ( logger != null ) {
				StringBuilder sb = new StringBuilder( );
				sb.append( "The number of not aligneable pairs using" )
						.append( " min = " )
						.append( min_below )
						.append( " max = " ).append( max_below )
						.append( " is " )
						.append( not_aligneable )
						.append( " out of " )
						.append( total )
						.append( " ( " ).append(
								PercentPrinter.printRatioAsPercent( 2, not_aligneable, total ) )
						.append( " )" );
				logger.accept( sb.toString( ) );
			}
			return total - not_aligneable;
		}
	}

	public static <K, SymbolAbove, SymbolBelow> int computeCoverage(
			ProgressSpawner progress_spawner,
			Consumer<String> logger,
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			Cursable<K> data,
			Function<K, Tuple<SymbolAbove>> get_above,
			Function<K, Tuple<SymbolBelow>> get_below,
			Predicate<Tael<SymbolAbove, SymbolBelow>> filter,
			int total,
			int min_max_total ) {
		try ( BasicRook rook = new BasicRook() ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( rook, progress_spawner,
					"TupleAlignmentAlphabetBuilderTools::computeCoverage" )
					.target( total );
			int not_aligneable = 0;
			for ( K datum : KnittingCursable
					.wrap( data ).pull( rook ).once( ) ) {
				spawn.step( 1 );
				try {
					TupleAlignmentAlphabetBuilderTools.attemptToAlingn(
							min_above,
							max_above,
							min_below,
							max_below,
							get_above.apply(datum),
							get_below.apply(datum),
							filter );
				}
				catch ( NotAlignableException e ) {
					not_aligneable++;
				}
			}
			if ( logger != null ) {
				StringBuilder sb = new StringBuilder( );
				sb.append( "The number of not aligneable pairs using the current alphabet is " )
						.append( not_aligneable )
						.append( " out of " )
						.append( min_max_total )
						.append( " pairs aligneable with a full alphabet." )
						.append( " ( " ).append(
								PercentPrinter.printRatioAsPercent( 2, not_aligneable, min_max_total ) )
						.append( " )" );
				logger.accept( sb.toString( ) );
			}
			return not_aligneable;
		}
	}

	public static <SymbolAbove, SymbolBelow> void attemptToAlingn(
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			Tuple<SymbolAbove> above,
			Tuple<SymbolBelow> below,
			Predicate<Tael<SymbolAbove, SymbolBelow>> filter )
			throws NotAlignableException {
		BiFunction<Tuple<SymbolAbove>, Tuple<SymbolBelow>, Integer> pair_encoder =
				new BiFunction<Tuple<SymbolAbove>, Tuple<SymbolBelow>, Integer>( ) {

					@Override
					public Integer apply( Tuple<SymbolAbove> suba,
							Tuple<SymbolBelow> subb ) {
						if (suba.size( ) != 1) {
							throw new IllegalArgumentException( );
						}

						Tael<SymbolAbove, SymbolBelow> pair =
								new Tael<>( KnittingTuple.wrap( suba ).asTupleValue( ) , KnittingTuple.wrap( subb ).asTupleValue( ));
						boolean test = filter.test( pair );
						if ( !test ) {
							return null;
						}
						return 1;
					}
				};
		TupleAlignmentGraphFactory.graph(
				pair_encoder,
				above,
				below,
				min_above,
				max_above,
				min_below,
				max_below );
	}
                  
	public static <SymbolAbove, SymbolBelow>
			Iterable<Tael<SymbolAbove, SymbolBelow>>
			localAlphabet(
					int min_above,
					int max_above,
					int min_below,
					int max_below,
					Tuple<SymbolAbove> above,
					Tuple<SymbolBelow> below )
					throws NotAlignableException {

		/**
		 * We invoke the graph factory passing a special encoder. This encoder
		 * appends the requested pair to a buffer vector and returns the size of the
		 * buffer.
		 */
		final Vector<Tael<SymbolAbove, SymbolBelow>> buffer =
				new Vector<>( );
		LinkedList<Tael<SymbolAbove, SymbolBelow>> result =
				new LinkedList<>( );
		BiFunction<Tuple<SymbolAbove>, Tuple<SymbolBelow>, Integer> pair_encoder =
				new BiFunction<Tuple<SymbolAbove>, Tuple<SymbolBelow>, Integer>( ) {

					@Override
					public Integer apply( Tuple<SymbolAbove> suba,
							Tuple<SymbolBelow> subb ) {
						if (suba.size( ) != 1) {
							throw new IllegalArgumentException( );
						}
						Tael<SymbolAbove, SymbolBelow> pair =
								new Tael<>( KnittingTuple.wrap( suba ).asTupleValue( ), KnittingTuple.wrap( subb ).asTupleValue( ) );
						buffer.add( pair );
						return buffer.size( );
					}
				};
		TupleAlignmentGraph graph =
				TupleAlignmentGraphFactory.graph(
						pair_encoder,
						above,
						below,
						min_above,
						max_above,
						min_below,
						max_below );

		Iterator<TupleAlignmentNode> forward = graph.forward( );
		while ( forward.hasNext( ) ) {
			TupleAlignmentNode node = forward.next( );
			for ( int ie = 0; ie < node.number_of_incoming_edges; ie++ ) {
				int index = node.incoming_edges[ie][2];
				Tael<SymbolAbove, SymbolBelow> pair =
						buffer.get( index - 1 );
				result.add( pair );
			}
		}
		return result;
	}
}
