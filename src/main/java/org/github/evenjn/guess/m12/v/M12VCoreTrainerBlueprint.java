/**
 *
 * Copyright 2016 Marco Trevisan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.github.evenjn.guess.m12.v;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.github.evenjn.guess.markov.Markov;
import org.github.evenjn.yarn.Cursable;
import org.github.evenjn.yarn.Rook;
import org.github.evenjn.yarn.ProgressSpawner;

public class M12VCoreTrainerBlueprint {

	private Function<Rook, Consumer<String>> putter_core;

	private Cursable<String> reader_core;

	private BiFunction<Markov, ProgressSpawner, Boolean> quality_control;

	private Consumer<String> logger;
	
	private Function<Integer, Object> unveiler;

	public M12VCoreTrainerBlueprint unveiler( Function<Integer, Object> unveiler ) {
		this.unveiler = unveiler;
		return this;
	}

	public M12VCoreTrainerBlueprint qualityControl(
			BiFunction<Markov, ProgressSpawner, Boolean> quality_control ) {
		this.quality_control = quality_control;
		return this;
	}

	public M12VCoreTrainerBlueprint
			logger( Consumer<String> logger ) {
		this.logger = logger;
		return this;
	}

	public M12VCoreTrainerBlueprint
			serializeModel( Function<Rook, Consumer<String>> putter_core ) {
		this.putter_core = putter_core;
		return this;
	}

	public M12VCoreTrainerBlueprint
			deserializeModel( Cursable<String> reader_coalignments ) {
		this.reader_core = reader_coalignments;
		return this;
	}

	public M12VCoreTrainer create( ) {
		return new M12VCoreTrainer(
				logger,
				unveiler,
				putter_core,
				reader_core,
				quality_control );
	}

}
