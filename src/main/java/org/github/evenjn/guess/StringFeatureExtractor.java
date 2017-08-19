/**
 *
 * Copyright 2017 Marco Trevisan
 * 
 * All rights reserved. 
 * 
 */
package org.github.evenjn.guess;

public interface StringFeatureExtractor<I> {

	public void extractFeatures( String[] result, I input );
	
	public int getNumberOfFeatures();
}
