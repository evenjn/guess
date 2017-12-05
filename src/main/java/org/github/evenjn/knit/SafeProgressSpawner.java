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
package org.github.evenjn.knit;

import org.github.evenjn.lang.Progress;
import org.github.evenjn.lang.ProgressSpawner;
import org.github.evenjn.lang.Rook;

/**
 * Provides a method to create stub {@link org.github.evenjn.yarn.Progress
 * Progress} objects that can be used whenever a
 * {@link org.github.evenjn.yarn.ProgressSpawner ProgressSpawner} is not
 * available.
 * 
 * @since 1.0
 */
public class SafeProgressSpawner {

	public static Progress safeSpawn( Rook rook, ProgressSpawner master,
			String name ) {
		if ( master == null ) {
			return DummyProgress.singleton;
		}
		Progress spawn = master.spawn( rook, name );
		return spawn;
	}

}

class DummyProgress implements
		Progress {

	final static DummyProgress singleton = new DummyProgress( );

	private DummyProgress() {
	}

	@Override
	public void step( int distance ) {
	}

	@Override
	public Progress target( int target ) {
		return this;
	}

	@Override
	public Progress info( String info ) {
		return this;
	}

	@Override
	public Progress spawn( Rook rook, String name ) {
		return this;
	}

}
