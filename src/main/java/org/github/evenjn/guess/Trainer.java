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
package org.github.evenjn.guess;

import java.util.function.Function;

import org.github.evenjn.lang.ProgressSpawner;

/**
 * A Trainer is a system that produces a discriminant function.
 */
public interface Trainer<I, O> {

	<K> Function<I, O> train(
			ProgressSpawner progress_spawner,
			TrainingData<K, I, O> data );
}
