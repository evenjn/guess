/**
 *
 * Copyright 2016 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.align.alphabet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.github.evenjn.align.graph.NotAlignableException;
import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentGraphFactory;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.KnittingCursor;
import org.github.evenjn.knit.KnittingTuple;
import org.github.evenjn.knit.ProgressManager;
import org.github.evenjn.numeric.FrequencyData;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.numeric.PercentPrinter;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Cursor;
import org.github.evenjn.yarn.Di;
import org.github.evenjn.yarn.PastTheEndException;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;
import org.github.evenjn.yarn.Tuple;

public class TupleAlignmentAlphabetBuilder<SymbolAbove, SymbolBelow> {

	private Function<SymbolAbove, String> a_printer = null;

	private Function<SymbolBelow, String> b_printer = null;

	private int min_below;

	private int max_below;

	private boolean shrink_alphabet = false;

	public TupleAlignmentAlphabetBuilder(int min_below, int max_below,
			boolean shrink_alphabet) {
		this.min_below = min_below;
		this.max_below = max_below;
		this.shrink_alphabet = shrink_alphabet;
	}

	public void setPrinters(
			Function<SymbolAbove, String> a_printer,
			Function<SymbolBelow, String> b_printer ) {
		if ( a_printer != null ) {
			this.a_printer = a_printer;
		}
		if ( b_printer != null ) {
			this.b_printer = b_printer;
		}
	}

	private FrequencyDistribution<SymbolAbove> fd_base =
			new FrequencyDistribution<>( );

	private FrequencyDistribution<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> fd_global =
			new FrequencyDistribution<>( );

	private HashMap<SymbolAbove, Vector<FrequencyDistribution<Tuple<SymbolBelow>>>> fds =
			new HashMap<>( );

	public void record(
			SymbolAbove above, Tuple<SymbolBelow> below ) {
		Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector =
				fds.get( above );

		if ( vector == null ) {
			vector = new Vector<>( );
			fds.put( above, vector );
		}
		int size = below.size( );
		for ( int i = 0; i < size; i++ ) {
			if ( vector.size( ) < i + 1 ) {
				vector.add( new FrequencyDistribution<Tuple<SymbolBelow>>( ) );
			}
		}
		if ( size > 0 ) {
			vector.get( size - 1 ).accept( below );
		}
		fd_base.accept( above );
	}

	private static final String decorator_line =
			"----------" + "----------" + "----------" + "----------"
					+ "----------" + "----------" + "----------" + "----------";

	private String tuple_printer( Tuple<SymbolBelow> tuple ) {
		StringBuilder sb = new StringBuilder( );
		sb.append( "[" );
		for ( int i = 0; i < tuple.size( ); i++ ) {
			sb.append( " " ).append( b_printer.apply( tuple.get( i ) ) );
		}
		sb.append( " ]" );
		return sb.toString( );
	}

	public void print( ) {

		if ( a_printer != null && b_printer != null ) {
			System.out.println( decorator_line );
			System.out.println( fd_global.plot( ).setLabels(
					x -> a_printer.apply( x.above ) + " >-> " + tuple_printer( x.below ) )
					.print( ) );
			System.out.println( decorator_line );
			System.out.println( fd_base.plot( ).setLabels( a_printer ).print( ) );
			System.out.println( decorator_line );
			KnittingTuple<FrequencyData<SymbolAbove>> dataSorted =
					fd_base.dataSorted( true );
			for ( int i = 0; i < dataSorted.size( ); i++ ) {
				FrequencyData<SymbolAbove> local_fd = dataSorted.get( i );
				System.out.println( a_printer.apply( local_fd.front( ) ) );
				System.out.println( decorator_line );
				Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector =
						fds.get( local_fd.front( ) );
				for ( int j = 0; j < vector.size( ); j++ ) {

					System.out.println(
							vector.get( j )
									.plot( )
									.setLimit( 10 )
									.setLabels( this::tuple_printer )
									.print( ) );
				}
			}
		}
	}

	private HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> complete_alphabet =
			new HashSet<>( );

	private int total;

	private int total_aligneable;

	public void computeCompleteAlphabet(
			Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		if ( total != 0 ) {
			throw new IllegalStateException( "this can be compued only once" );
		}

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::computeCompleteAlphabet" );
			int not_aligneable = 0;
			for ( Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).pull( hook ).once( ) ) {
				total++;
				spawn.step( 1 );
				KnittingTuple<SymbolAbove> ka = KnittingTuple.wrap( datum.front( ) );
				KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( datum.back( ) );

				try {
					/**
					 * We invoke the graph factory passing a special encoder. This encoder
					 * appends the requested pair to a buffer vector and returns the size
					 * of the buffer.
					 */
					final Vector<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> buffer =
							new Vector<>( );
					BiFunction<SymbolAbove, Tuple<SymbolBelow>, Integer> pair_encoder =
							new BiFunction<SymbolAbove, Tuple<SymbolBelow>, Integer>( ) {

								@Override
								public Integer apply( SymbolAbove suba,
										Tuple<SymbolBelow> subb ) {

									TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair =
											new TupleAlignmentAlphabetPair<>( );
									pair.above = suba;
									pair.below = KnittingTuple.wrap( subb );
									buffer.add( pair );
									return buffer.size( );
								}
							};
					TupleAlignmentGraph graph =
							TupleAlignmentGraphFactory.graph( pair_encoder, ka, kb,
									min_below, max_below );

					/**
					 * When the graph is complete, we iterate through all the edges and
					 * record all the pairs that have survived.
					 */
					Iterator<TupleAlignmentNode> forward = graph.forward( );
					while ( forward.hasNext( ) ) {
						TupleAlignmentNode node = forward.next( );
						for ( int ie = 0; ie < node.number_of_incoming_edges; ie++ ) {
							int index = node.incoming_edges[ie][2];
							TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair =
									buffer.get( index - 1 );
							record( pair.above, pair.below );
							fd_global.accept( pair );
							complete_alphabet.add( pair );
						}
					}
				}
				catch ( NotAlignableException e ) {
					not_aligneable++;
					// simply ignore them.
				}
			}
			total_aligneable = total - not_aligneable;
			System.out.println(
					PercentPrinter.printRatioAsPercent( 4, total_aligneable, total ) );

		}
	}

	public Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>
			shrinkAlphabet(
					Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> initial_alphabet,
					Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
					ProgressSpawner progress_spawner ) {

		HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );
		for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> s : initial_alphabet ) {
			alphabet.add( s );
		}

		try ( AutoHook hook = new BasicAutoHook( ) ) {

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::shrinkAlphabet" )
					.target( initial_alphabet.size( ) );

			for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> candidate : KnittingCursor
					.wrap( initial_alphabet.iterator( ) )
					.once( ) ) {
				alphabet.remove( candidate );
				int not_aligneable =
						computeCoverage( data, progress_spawner, alphabet::contains, total );
				spawn.step( 1 );

				if ( ( 1.0 * not_aligneable )
						/ ( 1.0 * total_aligneable ) < threshold ) {
					/**
					 * removing this pair is harmless
					 */
					if ( a_printer != null && b_printer != null ) {
						StringBuilder sb = new StringBuilder( );
						sb.append( "Removed element: " );
						sb.append( a_printer.apply( candidate.above ) );
						sb.append( " >-> " );
						sb.append( tuple_printer( candidate.below ) );
						sb.append( " Using " );
						sb.append( alphabet.size( ) );
						sb.append( " alphabet symbols." );
						System.out.println( sb.toString( ) );
					}
					continue;
				}
				alphabet.add( candidate );
			}
		}
		return alphabet;
	}

	private double threshold = 0.01;

	private Cursor<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>
			greedy( ) {
		KnittingTuple<SymbolAbove> symbols =
				fd_base.dataSorted( true ).map( x -> x.front( ) );
		return new Cursor<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>( ) {

			int symbol = 0;

			int size = 0;

			int rank = 0;

			boolean none_in_this_rank = true;

			@Override
			public TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> next( )
					throws PastTheEndException {

				TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> result = null;
				for ( ; result == null; ) {

					SymbolAbove symbolAbove = symbols.get( symbol );
					if ( size == 0 ) {
						result = new TupleAlignmentAlphabetPair<>( );
						result.above = symbolAbove;
						result.below = KnittingTuple.empty( );
					}
					else {
						Vector<FrequencyDistribution<Tuple<SymbolBelow>>> vector =
								fds.get( symbolAbove );
						if ( vector.size( ) + 1 > size ) {
							FrequencyDistribution<Tuple<SymbolBelow>> frequencyDistribution =
									vector.get( size - 1 );
							if ( frequencyDistribution.dataSorted( true ).size( ) > rank ) {
								result = new TupleAlignmentAlphabetPair<>( );
								result.above = symbolAbove;
								result.below = KnittingTuple.wrap(
										frequencyDistribution.dataSorted( true ).get( rank )
												.front( ) );
								none_in_this_rank = false;
							}
						}
					}
					if ( symbol + 1 == symbols.size( ) ) {
						symbol = 0;
						if ( size == max_below ) {
							size = 1;
							rank++;
							if ( none_in_this_rank ) {
								throw PastTheEndException.neo;
							}
							none_in_this_rank = true;
						}
						else {
							size++;
						}
					}
					else {
						symbol++;
					}
				}
				return result;
			}
		};
	}

	public Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>>
			growAlphabetQuickly(
					Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
					ProgressSpawner progress_spawner ) {
		HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			int total_candidates = KnittingCursor.wrap( fd_global.data( ) ).size( );
			int batch_size = 0;

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::growAlphabetQuickly" )
					.target( total_aligneable );
			int prev = total_aligneable;

			for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> best_candidate : KnittingCursor
					.wrap( // fd_global.dataSorted( true )
							greedy( ) )
					// .map( x -> x.front( ) )
					.once( ) ) {
				alphabet.add( best_candidate );
				batch_size++;

				int not_aligneable = prev;
				if ( batch_size == 1 + ( total_candidates / 100 ) ) {
					not_aligneable =
							computeCoverage( data, spawn, alphabet::contains, total );
					batch_size = 0;
				}

				// not a good idea because certain frequent elements may
				// need a less-frequent element to become useful.
				// if (prev != total_aligneable && not_aligneable == prev ) {
				// alphabet.remove( best_candidate );
				// continue;
				// }

				spawn.step( prev - not_aligneable );
				prev = not_aligneable;
				if ( a_printer != null && b_printer != null ) {
					StringBuilder sb = new StringBuilder( );
					sb.append( "Added new element: " );
					sb.append( a_printer.apply( best_candidate.above ) );
					sb.append( " >-> " );
					sb.append( tuple_printer( best_candidate.below ) );
					spawn.step( total_aligneable - not_aligneable );
					sb.append( "   Data coverage: " );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							total - not_aligneable, total ) );
					sb.append( " Using " );
					sb.append( alphabet.size( ) );
					sb.append( " (" );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							alphabet.size( ), total_candidates ) );
					sb.append( " of the alphabet)." );
					System.out.println( sb.toString( ) );
				}
				if ( ( 1.0 * not_aligneable )
						/ ( 1.0 * total_aligneable ) < threshold ) {
					break;
				}
			}
		}
		return alphabet;
	}

	public Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> growAlphabet(
			Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		HashSet<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
				new HashSet<>( );

		int total_candidates = KnittingCursor.wrap( fd_global.data( ) ).size( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {

			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::growAlphabet" )
					.target( total_aligneable );
			int not_aligneable = total_aligneable;
			int limit = 1;
			while ( not_aligneable > 0 ) {
				TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> best_candidate =
						null;

				try ( AutoHook hook2 = new BasicAutoHook( ) ) {

					Progress spawn_bc = ProgressManager.safeSpawn( hook2, progress_spawner,
							"TupleAlignmentAlphabetBuilder::findBestCandidate" )
							.target( limit );
					for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> candidate : KnittingCursor
							.wrap( fd_global.dataSorted( true ) ).head( 0, limit )
							.map( x -> x.front( ) )
							.once( ) ) {
						spawn_bc.step( 1 );
						if ( alphabet.contains( candidate ) ) {
							continue;
						}
						alphabet.add( candidate );
						int tmp =
								computeCoverage( data, spawn_bc, alphabet::contains, total );
						alphabet.remove( candidate );
						if ( best_candidate == null || tmp < not_aligneable ) {
							not_aligneable = tmp;
							best_candidate = candidate;
						}
					}

					if ( best_candidate == null ) {
						limit++;
						continue;
					}
					if ( not_aligneable == total_aligneable ) {
						limit++;
					}
					else {
						if ( limit < total_candidates ) {
							/**
							 * consider how much we cover at the moment ( between 0 and 1)
							 */
							double current_coverage =
									1.0 - ( 1.0 * not_aligneable ) / ( 1.0 * total_aligneable );
							/**
							 * consider how many candidates we can add
							 */
							int new_limit =
									(int) Math.floor( total_candidates * current_coverage );
							if ( new_limit < limit ) {
								new_limit = limit + 1;
							}
							limit = new_limit;
						}
					}
				}
				alphabet.add( best_candidate );
				if ( a_printer != null && b_printer != null ) {
					StringBuilder sb = new StringBuilder( );
					sb.append( "Added new element: " );
					sb.append( a_printer.apply( best_candidate.above ) );
					sb.append( " >-> " );
					sb.append( tuple_printer( best_candidate.below ) );
					spawn.step( total_aligneable - not_aligneable );
					sb.append( " : " );
					sb.append( PercentPrinter.printRatioAsPercent( 4,
							total - not_aligneable, total ) );
					System.out.println( sb.toString( ) );
				}
			}
		}
		return alphabet;
	}

	public int computeCoverage(
			Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner,
			Predicate<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> filter,
			int total ) {
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = ProgressManager.safeSpawn( hook, progress_spawner,
					"TupleAlignmentAlphabetBuilder::computeCoverage" ).target( total );
			int not_aligneable = 0;
			for ( Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>> datum : KnittingCursable
					.wrap( data ).pull( hook ).once( ) ) {
				spawn.step( 1 );
				KnittingTuple<SymbolAbove> ka = KnittingTuple.wrap( datum.front( ) );
				KnittingTuple<SymbolBelow> kb = KnittingTuple.wrap( datum.back( ) );

				try {
					/**
					 * We invoke the graph factory passing a special encoder. This encoder
					 * appends the requested pair to a buffer vector and returns the size
					 * of the buffer.
					 */
					BiFunction<SymbolAbove, Tuple<SymbolBelow>, Integer> pair_encoder =
							new BiFunction<SymbolAbove, Tuple<SymbolBelow>, Integer>( ) {

								@Override
								public Integer apply( SymbolAbove suba,
										Tuple<SymbolBelow> subb ) {

									TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> pair =
											new TupleAlignmentAlphabetPair<>( );
									pair.above = suba;
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
							ka,
							kb,
							min_below, max_below );
				}
				catch ( NotAlignableException e ) {
					not_aligneable++;
					// simply ignore them.
				}
			}
			return not_aligneable;
		}
	}

	public TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> build(
			Cursable<Di<Tuple<SymbolAbove>, Tuple<SymbolBelow>>> data,
			ProgressSpawner progress_spawner ) {
		TupleAlignmentAlphabet<SymbolAbove, SymbolBelow> result =
				new TupleAlignmentAlphabet<SymbolAbove, SymbolBelow>( );
		try ( AutoHook hook = new BasicAutoHook( ) ) {
			computeCompleteAlphabet( data, progress_spawner );
			print( );
			Set<TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow>> alphabet =
					growAlphabetQuickly( data, progress_spawner );
			if ( shrink_alphabet ) {
				alphabet = shrinkAlphabet( alphabet, data, progress_spawner );
			}
			for ( TupleAlignmentAlphabetPair<SymbolAbove, SymbolBelow> x : alphabet ) {
				result.add( x );
			}
		}
		return result;
	}
}
