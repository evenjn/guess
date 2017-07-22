package org.github.evenjn.guess.benchmark;

public class BenchmarkHandicap {

	public BenchmarkHandicap(boolean use_noise, int size_of_traning_data) {
		this.use_noise = use_noise;
		this.size_of_traning_data = size_of_traning_data;
	}

	/**
	 * Default values are no noise, 1000 instances of training data, test on
	 * training data.
	 */
	public BenchmarkHandicap() {
		this.use_noise = false;
		this.size_of_traning_data = 1000;
		this.test_on_training_data = true;
	}

	public BenchmarkHandicap
			setTestOnTrainingData( boolean test_on_training_data ) {
		this.test_on_training_data = test_on_training_data;
		return this;
	}

	public BenchmarkHandicap setTrainOnNoisyData( boolean train_on_noisy_data ) {
		this.use_noise = train_on_noisy_data;
		return this;
	}

	public BenchmarkHandicap setLimitTrainingData( int limit ) {
		this.size_of_traning_data = limit;
		return this;
	}

	public boolean test_on_training_data = false;

	public boolean use_noise = false;

	public int size_of_traning_data = 1000;
}
