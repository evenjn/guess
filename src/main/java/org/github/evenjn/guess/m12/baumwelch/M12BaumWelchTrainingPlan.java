package org.github.evenjn.guess.m12.baumwelch;

import org.github.evenjn.guess.m12.M12TrainingPlan;
import org.github.evenjn.lang.Kloneable;

public class M12BaumWelchTrainingPlan<I, P, O>
		extends M12TrainingPlan<I, P, O> {

	private int grace_period;

	private int epochs;

	private long seed;

	private int number_of_states;

	public Object clone( )
			throws CloneNotSupportedException {
		return super.clone( );
	}

	public int getEpochs( ) {
		return epochs;
	}

	public int getGracePeriod( ) {
		return grace_period;
	}

	public int getNumberOfStates( ) {
		return number_of_states;
	}

	public long getSeed( ) {
		return seed;
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

	public void setNumberOfStates( int number_of_states ) {
		this.number_of_states = number_of_states;
	}

	public void setSeed( long seed ) {
		this.seed = seed;
	}

	public void setTrainingTime( int grace_period,
			int epochs ) {
		this.grace_period = grace_period;
		this.epochs = epochs;
	}

}
