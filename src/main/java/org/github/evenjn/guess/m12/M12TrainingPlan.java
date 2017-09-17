package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetBuilder;
import org.github.evenjn.yarn.Bi;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Kloneable;
import org.github.evenjn.yarn.Tuple;

public abstract class M12TrainingPlan<I, P, O>
		extends M12Schema<I, P, O> {

	private int min_below;

	private int max_below;

	public int getMinBelow( ) {
		return min_below;
	}

	public int getMaxBelow( ) {
		return max_below;
	}

	public M12TrainingPlan<I, P, O>
			setMinMaxBelow( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
		return this;
	}

	private Function<P, String> a_printer;

	private Function<O, String> b_printer;

	private TupleAlignmentAlphabetBuilder<P, O> builder;

	private M12QualityChecker<P, O> checker;

	private Cursable<Bi<I, Tuple<O>>> training_data;

	public Cursable<Bi<I, Tuple<O>>> getTrainingData( ) {
		return training_data;
	}

	public M12TrainingPlan<I, P, O>
			setTrainingData( Cursable<Bi<I, Tuple<O>>> training_data ) {
		this.training_data = training_data;
		return this;
	}

	public M12TrainingPlan<I, P, O> setPrinters(
			Function<P, String> a_printer,
			Function<O, String> b_printer ) {
		this.a_printer = a_printer;
		this.b_printer = b_printer;
		return this;
	}

	public Function<P, String> getAbovePrinter( ) {
		return a_printer;
	}

	public Function<O, String> getBelowPrinter( ) {
		return b_printer;
	}

	public M12TrainingPlan<I, P, O> setTupleAlignmentAlphabetBuilder(
			TupleAlignmentAlphabetBuilder<P, O> builder ) {
		this.builder = builder;
		return this;
	}

	public TupleAlignmentAlphabetBuilder<P, O>
			getTupleAlignmentAlphabetBuilder( ) {
		return builder;
	}

	public M12TrainingPlan<I, P, O> setQualityChecker(
			M12QualityChecker<P, O> checker ) {
		this.checker = checker;
		return this;
	}

	public M12QualityChecker<P, O> getQualityChecker( ) {
		return checker;
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
