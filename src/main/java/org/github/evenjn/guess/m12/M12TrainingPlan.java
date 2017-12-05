package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetBuilder;
import org.github.evenjn.lang.Bi;
import org.github.evenjn.lang.Kloneable;
import org.github.evenjn.lang.Tuple;
import org.github.evenjn.yarn.Cursable;

public abstract class M12TrainingPlan<I, P, O>
		extends M12Schema<I, P, O> {

	protected int min_below;

	protected int max_below;

	protected Function<P, String> a_printer;

	protected Function<O, String> b_printer;

	protected TupleAlignmentAlphabetBuilder<P, O> builder;

	protected M12QualityChecker<P, O> checker;

	protected Cursable<Bi<I, Tuple<O>>> training_data;

	public Object clone( )
			throws CloneNotSupportedException {
		return super.clone( );
	}

	public Function<P, String> getAbovePrinter( ) {
		return a_printer;
	}

	public Function<O, String> getBelowPrinter( ) {
		return b_printer;
	}

	public int getMaxBelow( ) {
		return max_below;
	}

	public int getMinBelow( ) {
		return min_below;
	}

	public M12QualityChecker<P, O> getQualityChecker( ) {
		return checker;
	}

	public Cursable<Bi<I, Tuple<O>>> getTrainingData( ) {
		return training_data;
	}

	public TupleAlignmentAlphabetBuilder<P, O>
			getTupleAlignmentAlphabetBuilder( ) {
		return builder;
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

	public M12TrainingPlan<I, P, O>
			setMinMaxBelow( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
		return this;
	}

	public M12TrainingPlan<I, P, O> setPrinters(
			Function<P, String> a_printer,
			Function<O, String> b_printer ) {
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		return this;
	}

	public M12TrainingPlan<I, P, O> setQualityChecker(
			M12QualityChecker<P, O> checker ) {
		this.checker = checker;
		return this;
	}

	public M12TrainingPlan<I, P, O>
			setTrainingData( Cursable<Bi<I, Tuple<O>>> training_data ) {
		this.training_data = training_data;
		return this;
	}

	public M12TrainingPlan<I, P, O> setTupleAlignmentAlphabetBuilder(
			TupleAlignmentAlphabetBuilder<P, O> builder ) {
		this.builder = builder;
		return this;
	}

}
