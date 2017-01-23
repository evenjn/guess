package org.github.evenjn.guess.m12.v;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.align.graph.TupleAlignmentGraph;
import org.github.evenjn.align.graph.TupleAlignmentNode;
import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.knit.BasicAutoHook;
import org.github.evenjn.knit.KnittingCursable;
import org.github.evenjn.knit.SafeProgressSpawner;
import org.github.evenjn.numeric.FrequencyDistribution;
import org.github.evenjn.numeric.NumericLogarithm;
import org.github.evenjn.yarn.AutoHook;
import org.github.evenjn.yarn.Progress;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12VCoreBuilder {

	private FrequencyDistribution<Object> initial =
			new FrequencyDistribution<>( );
	
	private HashMap<Object, FrequencyDistribution<Object>> transitions =
			new HashMap<>( );
	
	private HashMap<Object, FrequencyDistribution<Integer>> emissions =
			new HashMap<>( );
	
	private HashSet<Integer> symbols= new HashSet<>();
	
	private HashSet<Object> states = new HashSet<>();

	/*
	 * {@code unveiler} is a function that associates each encoded Tael with a token
	 * identifying the corresponding symbol below.
	 * 
	 * For each token, the builder creates a state with emission
	 * distribution non-null for all the Tael associated to that token, and null
	 * for every other Tael. 
	 */
	public Markov build(
			Consumer<String> logger,
			Function<Integer, Object> unveiler,
			KnittingCursable<TupleAlignmentGraph> observed_cursable,
			ProgressSpawner progress_spawner ) {

		try ( AutoHook hook = new BasicAutoHook( ) ) {
			Progress spawn = SafeProgressSpawner.safeSpawn( hook, progress_spawner,
					"M12BaumWelch::BaumWelch" );
			int data_size = observed_cursable.size( );
			if ( logger != null ) {
				logger.accept( "Building a Markov model using visible states." );
			}
			spawn.target( data_size );
			
			for ( TupleAlignmentGraph observed : observed_cursable.pull( hook ) .once( )) {

				Iterator<TupleAlignmentNode> it = observed.forward( );
				while ( it.hasNext( ) ) {
					TupleAlignmentNode cell = it.next( );
					final int edges = cell.number_of_incoming_edges;
					for ( int edge = 0; edge < edges; edge++ ) {
						final int x = cell.incoming_edges[edge][0];
						final int y = cell.incoming_edges[edge][1];
						final int encoded = cell.incoming_edges[edge][2];
						Object key = unveiler.apply( encoded );
						
						FrequencyDistribution<Object> fdtest = transitions.get( key );
						if ( fdtest == null ) {
							fdtest = new FrequencyDistribution<>( );
							transitions.put( key, fdtest );
						}
						
						states.add( key );
						symbols.add( encoded );
						if (x == 0 && y == 0) {
							initial.accept( key );
						}
						else {
							// wait, when the Taels are 1:N it's easy to establish a
							// bijection between tuplealignment nodes and symbols below
							// but this would not work when Taels are M:N! How to do?
							
							// maybe it's as simple as this:
							
							TupleAlignmentNode source = observed.get( x, y );
							final int source_edges = source.number_of_incoming_edges;
							int current_encoded = 0;
							for ( int source_edge = 0; source_edge < source_edges; source_edge++ ) {
								final int source_encoded = source.incoming_edges[source_edge][2];
								if (source_edge == 0 || current_encoded != source_encoded ) {
									current_encoded = source_encoded;
									Object source_key = unveiler.apply( current_encoded );
									FrequencyDistribution<Object> fd = transitions.get( source_key );
									if ( fd == null ) {
										fd = new FrequencyDistribution<>( );
										transitions.put( source_key, fd );
									}
									fd.accept( key );
								}
							}
							
						}
						FrequencyDistribution<Integer> fd = emissions.get( key );
						if ( fd == null ) {
							fd = new FrequencyDistribution<>( );
							emissions.put( key, fd );
						}
						fd.accept( encoded );
					}
				}
				spawn.step( 1 );
			}

			Markov result = new Markov( states.size( ), symbols.size( ) );
			HashMap<Object, Integer> state2id = new HashMap<>( );
			HashMap<Integer, Integer> symbol2id = new HashMap<>( );
			{
				int state_id = 0;
				for ( Object state : states ) {
					state2id.put( state, state_id++ );
				}
				int symbol_id = 0;
				for ( Integer emission : symbols ) {
					symbol2id.put( emission, symbol_id++ );
				}
			}
			
			int total_initial = 0;

			for ( Object state : states ) {
				total_initial += initial.getFrequency( state ).orElse( 0 );
			}
			
			for ( Object state : states ) {
				Integer id = state2id.get( state );
				Integer local = initial.getFrequency( state ).orElse( 0 );
				double val = NumericLogarithm.eln( ( 1.0 * local ) / ( 1.0 * total_initial ) );
				result.initial_table[id] = val;
			}

			for ( Object source_state : states ) {
				int total_from_source = 0;
				Integer source_id = state2id.get( source_state );
				for ( Object destination_state : states ) {
					total_from_source += transitions.get( source_state )
							.getFrequency( destination_state ).orElse( 0 );
				}

				for ( Object destination_state : states ) {
					Integer destination_id = state2id.get( destination_state );
					Integer local = transitions.get( source_state )
							.getFrequency( destination_state ).orElse( 0 );
					double val = NumericLogarithm.eln( ( 1.0 * local ) / ( 1.0 * total_from_source ) );
					result.transition_table[source_id][destination_id] = val;
				}
			}

			for ( Object state : states ) {
				int total_emissions_from_state = 0;
				Integer state_id = state2id.get( state );
				for ( Integer symbol : symbols ) {
					total_emissions_from_state += emissions.get( state )
							.getFrequency( symbol ).orElse( 0 );
				}
				for ( Integer symbol : symbols ) {
					Integer symbol_id = symbol2id.get( symbol );
					Integer local = emissions.get( state )
							.getFrequency( symbol ).orElse( 0 );
					double val = NumericLogarithm.eln( ( 1.0 * local ) / ( 1.0 * total_emissions_from_state ) );
					result.emission_table[state_id][symbol_id] = val;
				}
			}
			
			return result;
		}
	}
}
