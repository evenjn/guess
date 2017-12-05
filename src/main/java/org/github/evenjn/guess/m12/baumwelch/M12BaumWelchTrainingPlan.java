package org.github.evenjn.guess.m12.baumwelch;

import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetBuilder;
import org.github.evenjn.guess.m12.M12QualityChecker;
import org.github.evenjn.guess.m12.M12Schema;
import org.github.evenjn.guess.m12.M12TrainingPlan;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.Kloneable;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

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

	public M12Schema<I, P, O> setAboveCoDec(
			Function<P, String> encoder,
			Function<String, P> decoder ) {
		super.setAboveCoDec( encoder, decoder );
		return this;
	}

	public M12Schema<I, P, O> setBelowCoDec(
			Function<O, String> encoder,
			Function<String, O> decoder ) {
		super.setBelowCoDec( encoder, decoder );
		return this;
	}

	public M12TrainingPlan<I, P, O>
			setMinMaxBelow( int min, int max ) {
		super.setMinMaxBelow( min, max );
		return this;
	}

	public M12BaumWelchTrainingPlan<I, P, O>
			setNumberOfStates( int number_of_states ) {
		this.number_of_states = number_of_states;
		return this;
	}

	public M12TrainingPlan<I, P, O> setPrinters(
			Function<P, String> a_printer,
			Function<O, String> b_printer ) {
		super.setPrinters( a_printer, b_printer );
		return this;
	}

	public M12Schema<I, P, O> setProjector( Function<I, Tuple<P>> projector ) {
		super.setProjector( projector );
		return this;
	}

	public M12TrainingPlan<I, P, O> setQualityChecker(
			M12QualityChecker<P, O> checker ) {
		super.setQualityChecker( checker );
		return this;
	}

	public M12BaumWelchTrainingPlan<I, P, O> setSeed( long seed ) {
		this.seed = seed;
		return this;
	}

	public M12TrainingPlan<I, P, O>
			setTrainingData( Cursable<Bi<I, Tuple<O>>> training_data ) {
		super.setTrainingData( training_data );
		return this;
	}

	public M12BaumWelchTrainingPlan<I, P, O> setTrainingTime( int grace_period,
			int epochs ) {
		this.grace_period = grace_period;
		this.epochs = epochs;
		return this;
	}

	public M12TrainingPlan<I, P, O> setTupleAlignmentAlphabetBuilder(
			TupleAlignmentAlphabetBuilder<P, O> builder ) {
		super.setTupleAlignmentAlphabetBuilder( builder );
		return this;
	}
}
