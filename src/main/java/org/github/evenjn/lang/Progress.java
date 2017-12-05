/**
 *
 * Copyright 2017 Marco Trevisan
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
package org.github.evenjn.lang;

/**
 * <p>
 * A {@code Progress} is an object that allows to track activity progress.
 * </p>
 * 
 * @since 1.0
 */
public interface Progress extends
		ProgressSpawner {

	/**
	 * <p>
	 * Reports that some progress has been made towards the goal.
	 * </p>
	 * 
	 * @param distance
	 *          If {@link Progress#step(int)} has never been invoked, the
	 *          distance covered so far. Otherwise, the difference between the
	 *          distance covered so far and the distance covered since the last
	 *          invocation of {@link Progress#step(int)}.
	 * @since 1.0
	 */
	void step( int distance );

	/**
	 * <p>
	 * Sets the distance to be covered to reach the goal.
	 * </p>
	 * 
	 * @param target
	 *          The distance to be covered to reach the goal.
	 * @return This {@code Progress}.
	 * @since 1.0
	 */
	Progress target( int target );

	/**
	 * <p>
	 * Sets the current status information.
	 * </p>
	 * 
	 * @param info
	 *          The current status information.
	 * @return This {@code Progress}.
	 * @since 1.0
	 */
	Progress info( String info );
}
