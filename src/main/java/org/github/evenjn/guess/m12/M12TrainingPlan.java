package org.github.evenjn.guess.m12;

import java.util.function.Function;

import org.github.evenjn.align.alphabet.TupleAlignmentAlphabetBuilder;
import org.github.evenjn.guess.TrainingData;
import org.github.evenjn.lang.Kloneable;
import org.github.evenjn.lang.Tuple;

public abstract class M12TrainingPlan<I, P, O>
		extends M12Schema<I, P, O> {

	protected int min_below;

	protected int max_below;

	protected Function<P, String> a_printer;

	protected Function<O, String> b_printer;

	protected TupleAlignmentAlphabetBuilder<P, O> builder;

	protected M12QualityChecker<P, O> checker;

	private TrainingData<?, Tuple<P>, Tuple<O>> training_data;

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

	public TrainingData<?, Tuple<P>, Tuple<O>> getTrainingData2( ) {
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

	public void setMinMaxBelow( int min, int max ) {
		this.min_below = min;
		this.max_below = max;
	}

	public void setPrinters(
			Function<P, String> a_printer,
			Function<O, String> b_printer ) {
		this.a_printer = a_printer;
		this.b_printer = b_printer;
	}

	public void setQualityChecker(
			M12QualityChecker<P, O> checker ) {
		this.checker = checker;
	}

	public void setTrainingData2( TrainingData<?, Tuple<P>, Tuple<O>> training_data ) {
		this.training_data = training_data;
	}

	public void setTupleAlignmentAlphabetBuilder(
			TupleAlignmentAlphabetBuilder<P, O> builder ) {
		this.builder = builder;
	}

}
