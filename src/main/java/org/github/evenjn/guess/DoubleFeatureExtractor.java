/**
 *
 * Copyright 2016 Marco Trevisan
 * 
 * All rights reserved. 
 * 
 */
package org.github.evenjn.guess;

public interface DoubleFeatureExtractor<I> {

	/**
	 * result[0] must always be 1.0.
	 * 
	 * @param result
	 * @param input
	 */
	public void extractFeatures( double[] result, I input );
	
	public int getNumberOfFeatures();
}
