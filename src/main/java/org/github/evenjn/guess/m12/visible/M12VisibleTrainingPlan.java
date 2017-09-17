package org.github.evenjn.guess.m12.visible;

import org.github.evenjn.guess.m12.M12TrainingPlan;
import org.github.evenjn.yarn.Kloneable;

public class M12VisibleTrainingPlan<I, P, O>
		extends M12TrainingPlan<I, P, O> {

	private int min_above;

	private int max_above;

	public int getMinAbove( ) {
		return min_above;
	}

	public int getMaxAbove( ) {
		return max_above;
	}

	public M12VisibleTrainingPlan<I, P, O>
			setMinMaxAbove( int min, int max ) {
		this.min_above = min;
		this.max_above = max;
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

	public Object clone( )
			throws CloneNotSupportedException {
		return super.clone( );
	}

}
