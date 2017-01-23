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

import org.github.evenjn.yarn.Catcher;

/**
 * Suppressor acts as a proxy for a singleton
 * {@link org.github.evenjn.yarn.Catcher Catcher} for all the methods in this
 * package.
 *
 * @since 1.0
 */
public final class Suppressor {

	private static Catcher catcher;

	/**
	 * Sets a custom catcher.
	 * 
	 * @param custom_catcher
	 *          the catcher carrying out custom behavior.
	 * @since 1.0
	 */
	public static void setCustomCatcher( Catcher custom_catcher ) {
		if ( catcher != null ) {
			catcher.quit( new IllegalStateException(
					"A custom catcher may be set only once." ) );
		}
		catcher = custom_catcher;
	}

	static RuntimeException quit( Throwable throwable ) {
		if ( catcher == null ) {
			catcher = new Catcher( ) {
			};
		}
		return catcher.quit( throwable );
	}

	static void log( Throwable throwable ) {
		if ( catcher == null ) {
			catcher = new Catcher( ) {
			};
		}
		catcher.log( throwable );
	}

}
