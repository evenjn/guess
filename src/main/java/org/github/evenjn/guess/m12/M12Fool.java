package org.github.evenjn.guess.m12;

import java.nio.file.Path;

import org.github.evenjn.file.FileFool;
import org.github.evenjn.guess.m12.baumwelch.M12BWFileTrainer;
import org.github.evenjn.guess.m12.baumwelch.M12BaumWelchTrainingPlan;
import org.github.evenjn.guess.m12.visible.M12VFileTrainer;
import org.github.evenjn.guess.m12.visible.M12VisibleTrainingPlan;
import org.github.evenjn.lang.ProgressSpawner;

public class M12Fool {

	private FileFool filefool;

	private M12Fool(Path base) {
		filefool = FileFool.rw( base );
	}

	public static M12Fool nu( Path base ) {
		return new M12Fool( base );
	}

	public <I, P, O> M12<I, P, O> open( Path path, M12Schema<I, P, O> schema ) {
		if ( !exists( path ) ) {
			throw new IllegalArgumentException(
					"Expected a path that identifies an existing directory." );
		}
		return new M12<>( filefool.normalizedAbsolute( path ), schema );
	}

	public boolean exists( Path path ) {
		return filefool.exists( path );
	}

	public void delete( Path path ) {
		filefool.delete( path );
	}

	public <I, P, O> Path create( Path path, ProgressSpawner progress_spawner,
			M12TrainingPlan<I, P, O> plan ) {
		if ( plan instanceof M12VisibleTrainingPlan<?, ?, ?> ) {
			return createVisible( path, progress_spawner,
					(M12VisibleTrainingPlan<?, ?, ?>) plan );
		}
		if ( plan instanceof M12BaumWelchTrainingPlan<?, ?, ?> ) {
			return createBaumwelch( path, progress_spawner,
					(M12BaumWelchTrainingPlan<?, ?, ?>) plan );
		}
		throw new IllegalArgumentException( );
	}

	public <I, P, O> M12VisibleTrainingPlan<I, P, O> moldVisible( ) {
		return new M12VisibleTrainingPlan<>( );
	}

	public <I, P, O> M12BaumWelchTrainingPlan<I, P, O> moldBaumWelch( ) {
		return new M12BaumWelchTrainingPlan<>( );
	}

	public <I, P, O> M12Schema<I, P, O> moldSchema( ) {
		return new M12Schema<>( );
	}

	private <I, P, O> Path createVisible( Path path,
			ProgressSpawner progress_spawner,
			M12VisibleTrainingPlan<I, P, O> plan ) {
		Path destination = filefool.create( filefool.mold( path ).asDirectory( ) );
		FileFool rw = FileFool.rw( filefool.normalizedAbsolute( destination ) );
		M12VFileTrainer<P, O> trainer = new M12VFileTrainer<>(
				plan.getMinAbove( ),
				plan.getMaxAbove( ),
				plan.getMinBelow( ),
				plan.getMaxBelow( ),
				plan.getTupleAlignmentAlphabetBuilder( ),
				plan.getQualityChecker( ),
				plan.getAbovePrinter( ),
				plan.getBelowPrinter( ),
				plan.getAboveEncoder( ),
				plan.getBelowEncoder( ),
				plan.getAboveDecoder( ),
				plan.getBelowDecoder( ) );

		trainer.train(
				progress_spawner,
				rw,
				plan.getTrainingData2( )
				);
		return path;
	}

	private <I, P, O> Path createBaumwelch( Path path,
			ProgressSpawner progress_spawner,
			M12BaumWelchTrainingPlan<I, P, O> plan ) {
		Path destination = filefool.create( filefool.mold( path ).asDirectory( ) );
		FileFool rw = FileFool.rw( filefool.normalizedAbsolute( destination ) );
		M12BWFileTrainer<P, O> trainer = new M12BWFileTrainer<>(
				plan.getMinBelow( ),
				plan.getMaxBelow( ),
				plan.getTupleAlignmentAlphabetBuilder( ),
				plan.getQualityChecker( ),
				plan.getAbovePrinter( ),
				plan.getBelowPrinter( ),
				plan.getAboveEncoder( ),
				plan.getBelowEncoder( ),
				plan.getAboveDecoder( ),
				plan.getBelowDecoder( ),
				plan.getGracePeriod( ),
				plan.getEpochs( ),
				plan.getSeed( ),
				plan.getNumberOfStates( ) );

		trainer.train(
				progress_spawner,
				rw,
				plan.getTrainingData2( )
				);
		return path;
	}
}
