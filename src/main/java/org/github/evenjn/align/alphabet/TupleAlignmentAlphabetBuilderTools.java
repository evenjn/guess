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
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

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

	public static <SymbolAbove, SymbolBelow> int computeMinMaxAligneable(
			ProgressSpawner progress_spawner,
			Consumer<String> logger,
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer,
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			int total ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilderTools::computeMinMaxCoverage" )
					.target( total );
			int not_aligneable = 0;
			for ( Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).pull( hook ).once( ) ) {
				spawn.step( 1 );
				try {
					TupleAlignmentAlphabetBuilderTools.attemptToAlingn(
							min_above,
							max_above,
							min_below,
							max_below,
							datum.front( ),
							datum.back( ),
							x -> true );
				}
				catch ( NotAlignableException e ) {
					if ( logger != null ) {
						StringBuilder sb = new StringBuilder( );
						sb.append( "Not aligneable using min = " ).append( min_below )
								.append( " max = " )
								.append( max_below ).append( ":" );
						for ( SymbolAbove a : KnittingTuple.wrap( datum.front( ) )
								.asIterable( ) ) {
							sb.append( " " ).append( a_printer.apply( a ) );
						}
						sb.append( " ----" );
						for ( SymbolBelow b : KnittingTuple.wrap( datum.back( ) )
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

	public static <SymbolAbove, SymbolBelow> int computeCoverage(
			ProgressSpawner progress_spawner,
			Consumer<String> logger,
			int min_above,
			int max_above,
			int min_below,
			int max_below,
			Cursable<Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			Predicate<Tael<SymbolAbove, SymbolBelow>> filter,
			int total,
			int min_max_total ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilderTools::computeCoverage" )
					.target( total );
			int not_aligneable = 0;
			for ( Bi<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).pull( hook ).once( ) ) {
				spawn.step( 1 );
				try {
					TupleAlignmentAlphabetBuilderTools.attemptToAlingn(
							min_above,
							max_above,
							min_below,
							max_below,
							datum.front( ),
							datum.back( ),
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
								new Tael<>( );
						pair.above = KnittingTuple.wrap( suba );
						pair.below = KnittingTuple.wrap( subb );
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
								new Tael<>( );
						pair.above = KnittingTuple.wrap( suba );
						pair.below = KnittingTuple.wrap( subb );
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
