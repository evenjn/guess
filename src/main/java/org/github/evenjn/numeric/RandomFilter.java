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
package org.github.evenjn.numeric;

import java.util.Random;

import org.github.evenjn.yarn.SkipException;
import org.github.evenjn.yarn.SkipFold;
import org.github.evenjn.yarn.SkipFoldFactory;

public class RandomFilter<K> {

	private int id;

	private int outof;

	private Random random;
	
	private boolean block;

	private boolean is_fraction;
	
	private RandomFilter() {}
	
	/**
	 * 
	 * @param block when true,  
	 * @param id
	 * @param outof
	 * @param seed
	 */
	private RandomFilter(boolean block, int id, boolean is_fraction, int outof, long seed ) {
		this.id = id;
		this.is_fraction = is_fraction;
		this.outof = outof;
		this.random = new Random( seed );
		this.block = block;
	}
//	
//	/**
//	 * Blocks all but one out of N.
//	 * 
//	 */
//	public static <K> RandomFilter<K> pass(int id, int outof, long seed ) {
//		RandomFilter<K> randomFilter = new RandomFilter<K>( );
//		randomFilter.id = id;
//		randomFilter.outof = outof;
//		randomFilter.random = new Random( seed );
//		randomFilter.block = false;
//		return randomFilter;
//	}
//	
//	/**
//	 * Blocks one out of N.
//	 * 
//	 */
//	public static <K> RandomFilter<K> block(int id, int outof, long seed ) {
//		RandomFilter<K> randomFilter = new RandomFilter<K>( );
//		randomFilter.id = id;
//		randomFilter.outof = outof;
//		randomFilter.random = new Random( seed );
//		randomFilter.block = true;
//		return randomFilter;
//	}

	public K filter( K object )
			throws SkipException {
		// block is false. filter blocks 9 out of 10. throw when (random != id)
		// block is true. filter blocks 1 out of 10. throw when (random == id)
		if (is_fraction) {
			if ( block == ( random.nextInt( outof ) < id ) )
				throw SkipException.neo;
		}
		else {
			if ( block == ( random.nextInt( outof ) == id ) )
				throw SkipException.neo;
		}
		return object;
	}
	

	public static <K> SkipFoldFactory<K, K> block( int id,
			int outof, long seed ) {
		return new SkipFoldFactory<K, K>( ) {

			@Override
			public SkipFold<K, K> create() {
				return new RandomFilter<K>( true, id, false, outof, seed )::filter;
			}
		};
	}

	public static <K> SkipFoldFactory<K, K> pass( int id,
			int outof, long seed ) {
		return new SkipFoldFactory<K, K>( ) {

			@Override
			public SkipFold<K, K> create() {
				return new RandomFilter<K>( false, id, false, outof, seed )::filter;
			}
		};
	}
	public static <K> SkipFoldFactory<K, K> blockFraction( int id,
			int outof, long seed ) {
		return new SkipFoldFactory<K, K>( ) {

			@Override
			public SkipFold<K, K> create() {
				return new RandomFilter<K>( true, id, true, outof, seed )::filter;
			}
		};
	}

	public static <K> SkipFoldFactory<K, K> passFraction( int id,
			int outof, long seed ) {
		return new SkipFoldFactory<K, K>( ) {

			@Override
			public SkipFold<K, K> create() {
				return new RandomFilter<K>( false, id, true, outof, seed )::filter;
			}
		};
	}

}
